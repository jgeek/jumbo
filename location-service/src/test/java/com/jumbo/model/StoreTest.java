package com.jumbo.model;

import com.jumbo.application.domain.model.Store;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Test
    void isOpen_WhenNowBetweenOpenAndClose_ReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore(format(now.minusHours(1)), format(now.plusHours(1)));
        assertTrue(store.isOpen());
    }

    @Test
    void isOpen_WhenNowBeforeOpen_ReturnsFalse() {
        LocalTime now = LocalTime.now();
        Store store = createStore(format(now.plusHours(1)), format(now.plusHours(2)));
        assertFalse(store.isOpen());
    }

    @Test
    void isOpen_WhenNowAfterClose_ReturnsFalse() {
        LocalTime now = LocalTime.now();
        Store store = createStore(format(now.minusHours(2)), format(now.minusHours(1)));
        assertFalse(store.isOpen());
    }

    @Test
    void isOpen_OvernightHoursAndNowAfterOpenBeforeMidnight_ReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore(format(now.minusHours(1)), format(now.minusHours(2)));
        assertTrue(store.isOpen());
    }

    @Test
    void isOpen_OvernightHoursAndNowBeforeCloseAfterMidnight_ReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore(format(now.plusHours(2)), format(now.plusHours(1)));
        assertTrue(store.isOpen());
    }

    @Test
    void isOpen_OvernightHoursAndNowBetweenCloseAndOpen_ReturnsFalse() {
        LocalTime now = LocalTime.now();
        Store store = createStore(format(now.plusHours(1)), format(now.minusHours(1)));
        assertFalse(store.isOpen());
    }

    @Test
    void isOpen_WhenTimesMissingOrBlank_ReturnsFalse() {
        Store s1 = createStore(null, "22:00");
        Store s2 = createStore("08:00", null);
        Store s3 = createStore(" ", "22:00");
        Store s4 = createStore("08:00", " ");

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
        assertFalse(s3.isOpen());
        assertFalse(s4.isOpen());
    }

    @Test
    void isOpen_WhenTimeFormatInvalid_ReturnsFalse() {
        Store s1 = createStore("invalid", "22:00");
        Store s2 = createStore("08:00", "bad");

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
    }

    @Test
    void isOpen_WhenTimeOutOfRange_ReturnsFalse() {
        Store s1 = createStore("25:00", "22:00");
        Store s2 = createStore("08:00", "60:00");

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
    }

    @Test
    void isOpen_WithWhitespaceAroundTimes_ParsesAndReturnsTrue() {
        LocalTime now = LocalTime.now();
        Store store = createStore("  " + format(now.minusMinutes(30)) + "  ", "  " + format(now.plusMinutes(30)) + " ");
        assertTrue(store.isOpen());
    }

    private static String format(LocalTime time) {
        return time.format(FMT);
    }

    private static Store createStore(String open, String close) {
        Store store = new Store();
        store.setTodayOpen(open);
        store.setTodayClose(close);
        return store;
    }
}