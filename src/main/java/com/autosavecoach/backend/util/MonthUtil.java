package com.autosavecoach.backend.util;

import com.autosavecoach.backend.exception.InvalidMonthException;

import java.time.YearMonth;

public class MonthUtil {

    public static YearMonth parse(String value) {
        try {
            return YearMonth.parse(value);
        } catch (Exception e) {
            throw new InvalidMonthException(value);
        }
    }
}

