package com.parkee_test.parking_pos.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class ParkingFeeCalculatorTest {

    private final ParkingFeeCalculator parkingFeeCalculator = new ParkingFeeCalculator();

    @Test
    void checkInOrCheckOutIsNull() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = null;

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(null, feeResult.getFee());
        assertFalse(feeResult.isSuccess());
        assertNotNull(feeResult.getError());

    }

    @Test
    void checkOutBeforeCheckIn() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = LocalDateTime.of(2025, 11, 15, 8, 30);

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(null, feeResult.getFee());
        assertFalse(feeResult.isSuccess());
        assertNotNull(feeResult.getError());
        assertEquals("Check-out time must be after check-in time", feeResult.getError());
    }

    @Test
    void durationLessThanOneHourIsChargedAsOneHour() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = LocalDateTime.of(2025, 11, 15, 9, 45);

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(3000, feeResult.getFee());
    }

    @Test
    void exactlyOneHourIsChargedAsOneHour() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = LocalDateTime.of(2025, 11, 15, 10, 30);

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(3000, feeResult.getFee());
    }

    @Test
    void moreThanOneHourIsRoundedUp() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = LocalDateTime.of(2025, 11, 15, 10, 45);

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(6000, feeResult.getFee());
    }

    @Test
    void multipleHoursAreRoundedUp() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = LocalDateTime.of(2025, 11, 15, 12, 45);

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(12000, feeResult.getFee());
    }

    @Test
    void checkInAndCheckOutStillOneHour() {
        LocalDateTime checkin = LocalDateTime.of(2025, 11, 15, 9, 30);
        LocalDateTime checkOut = LocalDateTime.of(2025, 11, 15, 9, 30);

        FeeResult feeResult = parkingFeeCalculator.calculateFee(checkin, checkOut);

        assertEquals(3000, feeResult.getFee());
    }
}
