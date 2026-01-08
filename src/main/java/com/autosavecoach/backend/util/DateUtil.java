package com.autosavecoach.backend.util;

import com.autosavecoach.backend.exception.InvalidDateException;

import java.time.LocalDate;

public class DateUtil {

    public static LocalDate parse(LocalDate date) {

        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException("Expense date cannot be in the future");
        }

        return date;
    }
}

