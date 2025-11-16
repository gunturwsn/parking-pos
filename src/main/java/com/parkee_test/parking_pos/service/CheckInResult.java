package com.parkee_test.parking_pos.service;

import com.parkee_test.parking_pos.entity.Ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckInResult {

    private final boolean success;
    private final Ticket ticket;
    private final String error;

    public static CheckInResult ok(Ticket ticket) {
        return new CheckInResult(true, ticket, null);
    }

    public static CheckInResult error(String message) {
        return new CheckInResult(false, null, message);
    }

}
