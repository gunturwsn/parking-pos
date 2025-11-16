package com.parkee_test.parking_pos.service;

import com.parkee_test.parking_pos.entity.Ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfirmCheckOutResult {

    private final boolean success;
    private final Ticket ticket;
    private final String error;

    public final static ConfirmCheckOutResult ok(Ticket ticket) {
        return new ConfirmCheckOutResult(true, ticket, null);
    }

    public final static ConfirmCheckOutResult error(String message) {
        return new ConfirmCheckOutResult(false, null, message);
    }
}
