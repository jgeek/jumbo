package com.jumbo.service;

import com.jumbo.application.port.in.NearByRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NearByRequestTest {

    @Test
    void createsInstanceWithValidValues() {
        assertDoesNotThrow(() -> new NearByRequest(52.0, 4.0, 5, true));
        assertDoesNotThrow(() -> new NearByRequest(-45.123, 100.987, 1, false));
    }

    @Test
    void acceptsBoundaryValuesForLatitudeAndLongitude() {
        assertDoesNotThrow(() -> new NearByRequest(-90.0, -180.0, 1, false));
        assertDoesNotThrow(() -> new NearByRequest(90.0, 180.0, 1, true));
    }

    @Test
    void throwsConstraintViolationWhenLimitIsZero() {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> new NearByRequest(0.0, 0.0, 0, false));
    }

    @Test
    void throwsConstraintViolationWhenLimitIsNegative() {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> new NearByRequest(0.0, 0.0, -1, true));
    }

    @Test
    void throwsConstraintViolationWhenLatitudeTooLow() {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> new NearByRequest(-90.0001, 0.0, 1, false));
    }

    @Test
    void throwsConstraintViolationWhenLatitudeTooHigh() {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> new NearByRequest(90.0001, 0.0, 1, false));
    }

    @Test
    void throwsConstraintViolationWhenLongitudeTooLow() {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> new NearByRequest(0.0, -180.0001, 1, true));
    }

    @Test
    void throwsConstraintViolationWhenLongitudeTooHigh() {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> new NearByRequest(0.0, 180.0001, 1, true));
    }
}