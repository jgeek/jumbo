package com.jumbo.application.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    @Test
    void isOpen_WhenNowBetweenOpenAndClose_ReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.minusHours(1), now.plusHours(1));
        assertTrue(store.isOpen());
    }

    @Test
    void isOpen_WhenNowBeforeOpen_ReturnsFalse() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.plusHours(1), now.plusHours(2));
        assertFalse(store.isOpen());
    }

    @Test
    void isOpen_WhenNowAfterClose_ReturnsFalse() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.minusHours(2), now.minusHours(1));
        assertFalse(store.isOpen());
    }

    @Test
    void isOpen_OvernightHoursAndNowAfterOpenBeforeMidnight_ReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.minusHours(1), now.minusHours(2));
        assertTrue(store.isOpen());
    }

    @Test
    void isOpen_OvernightHoursAndNowBeforeCloseAfterMidnight_ReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.plusHours(2), now.plusHours(1));
        assertTrue(store.isOpen());
    }

    @Test
    void isOpen_OvernightHoursAndNowBetweenCloseAndOpen_ReturnsFalse() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.plusHours(1), now.minusHours(1));
        assertFalse(store.isOpen());
    }

    @Test
    void isOpen_WhenTimesMissingOrBlank_ReturnsFalse() {
        Store s1 = createStore(null, LocalTime.of(22, 0));
        Store s2 = createStore(LocalTime.of(8, 0), null);
        Store s3 = createStore(null, null);

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
        assertFalse(s3.isOpen());
    }

    @Test
    void isOpen_WithValidTimes_ParsesCorrectly() {
        LocalTime now = LocalTime.now();
        Store store = createStore(now.minusMinutes(30), now.plusMinutes(30));
        assertTrue(store.isOpen());
    }

    private static Store createStore(LocalTime open, LocalTime close) {
        Store store = new Store();
        store.setTodayOpen(open);
        store.setTodayClose(close);
        return store;
    }
}