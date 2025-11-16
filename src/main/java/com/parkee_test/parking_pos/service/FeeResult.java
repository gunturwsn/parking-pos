package com.parkee_test.parking_pos.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeeResult {

    private final boolean success;
    private final Integer fee;
    private final String error;

    public static FeeResult ok(int fee) {
        return new FeeResult(true, fee, null);
    }

    public static FeeResult error(String message) {
        return new FeeResult(false, null, message);
    }
}
