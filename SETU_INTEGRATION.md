# Setu Integration — Pulling Bank Transactions

Proposed approach for integrating [Setu](https://setu.co) APIs so Autosave Coach can pull a user's real bank transactions instead of relying solely on manually-entered `Expense` records. This is a design doc, not an implementation — no code has been written yet.

Status: **Proposed**. Corresponds to the "transaction sync via webhook/polling/manual refresh" line item under Phase 2/3 in `STATUS.md`.

---

## 1. Which Setu product fits

Setu offers several API products. For "pull a user's transactions with their consent," the relevant one is:

- **Account Aggregator (AA) — Setu Data APIs**: RBI-regulated consent framework. The user links their bank account through an AA app/web flow, grants a time-bound, purpose-bound consent, and we (the Financial Information User, "FIU") pull their Financial Information (FI) — bank statement transactions — for the consented window.

This is a better fit than scraping/screen-scraping or asking users to upload statements, because:
- It's the RBI-sanctioned, compliant way to pull bank data in India.
- Data comes back structured (Setu normalizes bank-specific formats into a common FI schema).
- Consent is explicit, scoped, and revocable by the user — which also reduces our liability around holding bank credentials (we never see them).

If instead the near-term goal is just "let the user's card/UPI spends show up automatically" with less compliance overhead, Setu also has UPI-adjacent and Bridge products, but AA is the one that actually gives transaction-level bank statement data. This doc assumes AA.

---

## 2. High-level flow

```
User                  Autosave Coach Backend            Setu AA (FIU flow)              Account Aggregator / Bank
 |                            |                                  |                                  |
 |--"Link my bank"----------->|                                  |                                  |
 |                            |--Create Consent Request--------->|                                  |
 |                            |<--consentHandle, redirect URL----|                                  |
 |<--redirect to AA web/app---|                                  |                                  |
 |---- approves consent in AA UI (out of our system) ----------->|-------------------------------->|
 |                            |<--Webhook: CONSENT_STATUS_UPDATE-|                                  |
 |                            |--Create FI Data Session---------->|                                  |
 |                            |                                  |--fetch statement------------------>|
 |                            |<--Webhook: FI_DATA_READY----------|                                  |
 |                            |--Fetch FI Data (session id)------>|                                  |
 |                            |<--Encrypted FI payload------------|                                  |
 |                            |--decrypt, parse, normalize        |                                  |
 |                            |--store as Transactions            |                                  |
 |<--transactions visible-----|                                  |                                  |
```

Key point: we never see bank credentials. We only ever talk to Setu's APIs and receive webhooks; the actual consent approval happens in the AA app/web UI, outside our system.

---

## 3. Where this fits in the current architecture

Current domain: `User` → `Expense` (manually entered, has `title`, `category`, `amount`, `date`). Budget analytics is built entirely on `Expense`.

Proposed approach: **don't bolt transactions onto `Expense` directly.** Bank transactions are a different shape (raw narration, no user-assigned category, need dedup, need a link back to the source account/consent). Introduce a parallel domain and reconcile into `Expense` via a mapping step:

```
com.autosavecoach.backend.integration.setu/
├── client/
│   ├── SetuAuthClient.java          # client-id/secret -> bearer token, token caching/refresh
│   ├── SetuConsentClient.java       # create/get/revoke consent requests
│   └── SetuFiDataClient.java        # create session, fetch FI data, decrypt payload
├── controller/
│   ├── BankLinkController.java      # POST /api/bank-links (start consent), GET /api/bank-links
│   └── SetuWebhookController.java   # POST /api/webhooks/setu (consent + data-ready notifications)
├── service/
│   ├── ConsentService.java          # orchestrates consent lifecycle, persists ConsentRequest
│   ├── FiDataSyncService.java       # pulls FI data, parses, writes SyncedTransaction rows
│   └── TransactionReconciliationService.java  # SyncedTransaction -> Expense (auto-categorize, dedupe)
├── model/
│   ├── BankLink.java                # user_id, fip_id (bank), consent_handle, status, linked_at
│   ├── ConsentRequest.java          # consent_id, consent_handle, status, purpose, expiry, raw artifact ref
│   └── SyncedTransaction.java       # raw txn: bank_ref_id, amount, narration, txn_date, type, mode, balance
├── dto/                             # Setu request/response DTOs (mirror existing dto/request, dto/response split)
└── config/
    └── SetuProperties.java          # @ConfigurationProperties for client id/secret/base URLs/env
```

This mirrors the existing package layout (`controller` / `service` / `model` / `repository` / `dto`) rather than inventing a new convention.

### New entities

| Entity | Purpose |
|---|---|
| `BankLink` | One row per user-linked bank account. Tracks which FIP (bank) and the current consent status. |
| `ConsentRequest` | Setu consent lifecycle state (`PENDING`, `ACTIVE`, `PAUSED`, `REVOKED`, `EXPIRED`). Store the consent handle/ID, not credentials. |
| `SyncedTransaction` | Raw transaction as returned by Setu, before categorization. Kept separate from `Expense` so re-syncs are idempotent (upsert on bank's transaction reference id) and raw data is auditable. |

`Expense` gains an optional nullable `source` field (`MANUAL` vs `SETU_SYNC`) and a nullable `syncedTransactionId` reference, so analytics code that already sums `Expense` rows keeps working unmodified — synced transactions just become `Expense` rows with `source = SETU_SYNC` after categorization.

---

## 4. Data mapping

Setu's FI data schema (per account) returns transactions roughly as:

| Setu FI field | Maps to |
|---|---|
| `transactionTimestamp` / `valueDate` | `Expense.date` |
| `amount` | `Expense.amount` |
| `narration` | `Expense.title` (raw bank description, e.g. `"UPI/SWIGGY/..."`) |
| `type` (CREDIT/DEBIT) | filter — only sync `DEBIT` into `Expense`; store `CREDIT` rows in `SyncedTransaction` only, useful later for income/savings-rate features but out of scope now |
| `mode` (UPI/NEFT/CARD/...) | stored on `SyncedTransaction`, not currently used by `Expense` |
| — (no category from bank) | `Expense.category` must be inferred — see below |

**Category inference is the real gap.** Setu gives no category. Options, roughly in order of effort:
1. Keyword/merchant-pattern matching on `narration` (cheap, works for common merchants — Swiggy/Zomato → FOOD, Uber/Ola → TRANSPORT, etc.) as a first pass.
2. Fall back to `OTHER` and let the user re-categorize in the UI, feeding corrections back into the keyword table.
3. (Later) small classifier if manual corrections accumulate enough signal.

Start with (1) + (2); this is enough to unblock budget analytics, which already tolerates `OTHER`.

---

## 5. Sync trigger strategy

Three ways to get fresh data after initial linking, matching what `STATUS.md` already lists as planned:

1. **Webhook-driven (primary)**: Setu notifies `FI_DATA_READY` after each periodic pull it does on our behalf (if using a recurring/periodic consent). We fetch and reconcile on receipt.
2. **Manual refresh**: user-triggered `POST /api/bank-links/{id}/sync`, calls `FiDataSyncService` synchronously (or kicks a background job) for on-demand pulls, useful right after linking and for "pull to refresh" UX.
3. **Scheduled polling (fallback)**: a daily `@Scheduled` job re-checks active `BankLink`s that haven't synced recently, in case a webhook was missed. Not the primary path — webhooks should be trusted first; polling is a safety net.

---

## 6. Security & compliance notes

- **Webhook signature verification is mandatory.** Setu signs webhook payloads; `SetuWebhookController` must verify the signature before trusting any payload, and the endpoint must be excluded from JWT auth (it's Setu calling us, not a logged-in user) but protected by signature check instead — mirror how `SecurityConfig` already permits `/api/webhooks/**` as a distinct rule from the JWT filter chain.
- **We never store bank credentials.** Only consent handles/IDs and the FI data itself.
- **Encrypt FI payload at rest** if raw `SyncedTransaction` data is retained long-term — bank statement data is sensitive; at minimum rely on Postgres-level encryption/managed disk encryption, revisit column-level encryption if compliance requires it later.
- **Respect consent expiry and revocation.** When Setu reports a consent as `REVOKED`/`EXPIRED` (via webhook or on next call), stop syncing that `BankLink` and surface it to the user rather than silently failing.
- **Secrets via `.env`**, consistent with existing `spring-dotenv` usage (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` pattern in `application.properties`): add `SETU_CLIENT_ID`, `SETU_CLIENT_SECRET`, `SETU_PRODUCT_INSTANCE_ID`, `SETU_BASE_URL` (sandbox vs prod), `SETU_WEBHOOK_SECRET`.

---

## 7. Environments

Setu provides a sandbox with test FIPs (mock banks) that return canned transaction data — use this for all development, no real bank linking needed until go-live. Config should be environment-switched via the existing `.env` pattern (`SETU_BASE_URL` pointing at sandbox vs production, separate client id/secret per environment).

---

## 8. Phased rollout

| Phase | Scope |
|---|---|
| 1 | `SetuAuthClient` (token handling) + `ConsentService` against sandbox; `BankLinkController` to start a consent and land the redirect URL in the API response. No data pull yet — just prove the consent handshake + webhook receipt works end-to-end. |
| 2 | `FiDataSyncService`: fetch + decrypt + persist `SyncedTransaction` on `FI_DATA_READY`. Manual refresh endpoint. |
| 3 | `TransactionReconciliationService`: keyword-based categorization, dedupe, write into `Expense` with `source = SETU_SYNC`. Verify existing Budget Analytics endpoints work unmodified against synced data. |
| 4 | Scheduled polling fallback, consent expiry/revocation handling in UI, re-categorization feedback loop. |

Phase 1–2 can be built and tested entirely against Setu's sandbox before any production credentials are needed.

---

## 9. Open questions (need a decision before implementation)

1. **Recurring vs one-time consent?** Recurring (Setu periodically pulls and pushes `FI_DATA_READY`) fits "autosave coach" better than one-time, but has a longer consent-approval UX and needs the webhook path built first.
2. **How far back to pull on initial link?** Affects budget analytics that already look at historical months (calibration/drift use prior-month averages) — recommend requesting the max allowed window (typically up to 10 years per AA spec, though FIPs vary) as a one-time backfill, then recurring pulls going forward.
3. **Multiple linked accounts per user?** `BankLink` model above already supports many-per-user; confirm whether budget analytics should aggregate across accounts or let the user scope by account.
