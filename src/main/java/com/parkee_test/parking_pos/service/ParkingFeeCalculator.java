package com.parkee_test.parking_pos.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class ParkingFeeCalculator {

    private static final int ONE_HOUR = 60;
    private static final int ONE_HOUR_FEE = 3000;

    public FeeResult calculateFee(LocalDateTime checkIn, LocalDateTime checkOut) {
        int fee;

        String error = validationCheckTime(checkIn, checkOut);

        if (error != null) {
            return FeeResult.error(error);
        }

        long minutes = Duration.between(checkIn, checkOut).toMinutes();

        int hours = Math.max(1, (int) Math.ceil((double) minutes / ONE_HOUR));
        fee = hours * ONE_HOUR_FEE;

        return FeeResult.ok(fee);
    }

    public String validationCheckTime(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            return "Check-in and check-out time cannot be null";
        }
        if (checkOut.isBefore(checkIn)) {
            return "Check-out time must be after check-in time";
        }

        return null;
    }

}
