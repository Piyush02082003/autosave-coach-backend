# Autosave Coach — Current Status (as of repo state, main branch)

Snapshot generated from the actual code in `src/main/java/com/autosavecoach/backend`, cross-checked against the design doc. Use this to resume work without re-reading every file.

**Stack:** Spring Boot 3.2.1, Java, Spring Security + JWT (jjwt 0.11.5), Spring Data JPA, H2 database, Lombok.

---

## Domain status vs. design doc

| Domain | Design doc status | Actual code state |
| --- | --- | --- |
| User | ✅ Completed | Matches: signup, login (JWT), profile GET. |
| Expense | ✅ Completed | Matches, and then some — more read endpoints exist than the doc's original scope implied. |
| Budget | ✅ Completed | Matches: upsert-based create, get-by-id, list with month/category filters. |
| Budget Analytics | ⏳ In progress | 3 of 4 planned sub-features done (Summary, Calibration, Drift, Feasibility). **Root cause is not started.** |
| Analytics (aggregate/trends) | ❌ Not doing | No code — consistent with doc. |
| Simulation | ❌ Planned | No code — consistent with doc. |
| AI Decision Making | ❌ Planned | No code — consistent with doc. |

## Current Focus checklist — actual state

1. Budget Calibration — **done** (`GET /api/budgets/analytics/calibration`)
2. Behaviour Drift — **done** (`GET /api/budgets/analytics/drift`)
3. Budget failure root cause — **not started**, no controller/service/DTO exists for it
4. Future feasibility check — **done** (`GET /api/budgets/analytics/feasibility`)

So Phase 1 is 3/4 complete. The only remaining item to close out Phase 1 is **Budget Failure Root Cause**.

---

## API Inventory — what's actually live

### User (`/api/users`)
| Method | Endpoint | Status |
| --- | --- | --- |
| POST | /api/users | ✅ |
| POST | /api/users/login | ✅ |
| GET | /api/users | ✅ |

### Expense (`/api/expenses`)
All match the doc: `POST /`, `GET /`, `GET /{expenseId}`, `GET /total`, `GET /monthly`, `GET /category`, `GET /weekly`, `GET /range`, `GET /burn-rate`. All present in `ExpenseController`.

### Budget (`/api/budgets`)
| Method | Endpoint | Status |
| --- | --- | --- |
| POST | /api/budgets (upsert) | ✅ |
| GET | /api/budgets/{id} | ✅ |
| GET | /api/budgets?month=&category= | ✅ |

### Budget Analytics (`/api/budgets/analytics/*`)
**Note: the actual route shape differs from the design doc.** The doc describes `GET /api/budgets/compare/{user_id}&category&rangeDate`; the real implementation nests everything under `/api/budgets/analytics/*` and derives the user from the JWT (no `user_id` in the path — auth already scopes it). Treat the doc's endpoint spellings as stale; the table below is the source of truth.

| Method | Endpoint | Purpose | Status |
| --- | --- | --- | --- |
| GET | /api/budgets/analytics/summary?startMonth&endMonth&category | Budget vs. spend per month/category, with status (`NOT_STARTED` / `ON_TRACK` / `WARNING` / `LIMIT_REACHED` / `EXCEEDED`) | ✅ |
| GET | /api/budgets/analytics/calibration?month&category | Is the budget set correctly vs. historical average spend (`UNDERSET` / `OVERSET` / `WELL_CALIBRATED`) + recommended amount | ✅ |
| GET | /api/budgets/analytics/drift?month&category | Recent (1mo) vs. historical (prior 3mo avg) spend change (`NONE` / `MINOR` / `MAJOR`) | ✅ |
| GET | /api/budgets/analytics/feasibility | Can the user finish the month within budget, overall + per-category, using required-daily-spend vs. historical daily average (`SAFE` / `TIGHT` / `UNLIKELY` / `UNKNOWN`) | ✅ |
| — | Budget failure root cause | Why a budget failed (which categories/days drove it) | ❌ not started |

---

## Loose ends worth cleaning up before/while building Root Cause

- **Leftover debug `System.out.println` calls** in 5 files: `JwtAuthFilter`, `ExpenseService`, `BudgetAnalyticsController`, `ExpenseController`, `BudgetAnalyticsService`. Doc's "Phase 1" says "harden Budget Calibration" — this is part of that hardening.
- **No real tests.** `AutosaveCoachBackendApplicationTests` is still the unmodified Spring Boot scaffold (`contextLoads()` only). None of the analytics logic (calibration math, drift thresholds, feasibility thresholds) has test coverage, which is risky given the doc calls Budget Analytics "the core of the project."
- **H2 (in-memory/file) database** is the only configured datasource — fine for dev, but there's no separate prod datasource config yet if that matters for your timeline.
- **Progress Tracker table in the design doc is empty** — no dated entries, so there's no record of actual start/end dates per feature. Consider backfilling from `git log` (feasibility landed most recently: commit `1118abb`).

## Phase 2/3 (Simulation, AI, transaction sync via webhook/polling/manual refresh)

No code yet for any of this — matches the doc's "Planned" status. Nothing to reconcile.

---

## Suggested next step

Build **Budget Failure Root Cause** (`/api/budgets/analytics/root-cause` or similar) — it's the last gap in Phase 1 and the doc explicitly calls out Budget Analytics as the core of the project. Natural inputs: reuse the per-category spend maps already built in `BudgetAnalyticsService` (`sumExpensesByCategory`) plus daily expense breakdown to identify which category/week drove a budget over its limit, following the same pattern as `calDrift`/`calFeasibility`.
