package com.jumbo.application.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    @Test
    void isOpen_WhenNowBetweenOpenAndClose_ReturnsTrue() {
        // now = 14:00, open = 13:00, close = 15:00
        LocalTime now = LocalTime.of(14, 0);
        Store store = new Store()
                .opensAt(13, 0)
                .closesAt(15, 0);
        assertTrue(store.isOpen(now));
    }

    @Test
    void isOpen_WhenNowBeforeOpen_ReturnsFalse() {
        // now = 14:00, open = 15:00, close = 16:00
        LocalTime now = LocalTime.of(14, 0);
        Store store = new Store()
                .opensAt(15, 0)
                .closesAt(16, 0);
        assertFalse(store.isOpen(now));
    }

    @Test
    void isOpen_WhenNowAfterClose_ReturnsFalse() {
        // now = 14:00, open = 12:00, close = 13:00
        LocalTime now = LocalTime.of(14, 0);
        Store store = new Store()
                .opensAt(12, 0)
                .closesAt(13, 0);
        assertFalse(store.isOpen(now));
    }

    @Test
    void isOpen_OvernightHoursAndNowAfterOpenBeforeMidnight_ReturnsTrue() {
        // now = 23:00, open = 22:00, close = 02:00 (next day) - store open overnight
        LocalTime now = LocalTime.of(23, 0);
        Store store = new Store()
                .opensAt(22, 0)
                .closesAt(2, 0);
        assertTrue(store.isOpen(now));
    }

    @Test
    void isOpen_OvernightHoursAndNowBeforeCloseAfterMidnight_ReturnsTrue() {
        // now = 01:00, open = 22:00 (prev day), close = 03:00 - store open overnight
        LocalTime now = LocalTime.of(1, 0);
        Store store = new Store()
                .opensAt(22, 0)
                .closesAt(3, 0);
        assertTrue(store.isOpen(now));
    }

    @Test
    void isOpen_OvernightHoursAndNowBetweenCloseAndOpen_ReturnsFalse() {
        // now = 10:00, open = 22:00, close = 08:00 - overnight store closed between 08:00-22:00
        LocalTime now = LocalTime.of(10, 0);
        Store store = new Store()
                .opensAt(22, 0)
                .closesAt(8, 0);
        assertFalse(store.isOpen(now));
    }

    @Test
    void isOpen_WhenTimesMissingOrBlank_ReturnsFalse() {

        Store s1 = new Store();
        s1.setTodayOpen(null);
        s1.closesAt(20, 0);

        Store s2 = new Store().opensAt(8, 0);
        s2.setTodayClose(null);

        Store s3 = new Store();
        s3.setTodayOpen(null);
        s3.setTodayClose(null);

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
        assertFalse(s3.isOpen());
    }

    @Test
    void isOpen_WithValidTimes_ParsesCorrectly() {
        // now = 15:00, open = 14:30, close = 15:30
        LocalTime now = LocalTime.of(15, 0);
        Store store = new Store()
                .opensAt(14, 30)
                .closesAt(15, 30);
        assertTrue(store.isOpen(now));
    }

    @Test
    void creation_fails_if_hour_is_out_if_range() {
        Store store = new Store();
        assertThrows(IllegalArgumentException.class, () -> store.opensAt(24, 0));
        assertThrows(IllegalArgumentException.class, () -> store.opensAt(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> store.opensAt(20, 60));
        assertThrows(IllegalArgumentException.class, () -> store.opensAt(20, -1));
    }
}