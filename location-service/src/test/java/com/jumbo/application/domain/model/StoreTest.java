package com.jumbo.application.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    @Test
    void isOpen_WhenNowBetweenOpenAndClose_ReturnsTrue() {
        // now = 14:00, open = 13:00, close = 15:00
        LocalTime now = LocalTime.of(14, 0);
        Store store = givenStore()
                .opens(13, 0)
                .closes(15, 0)
                .build();
        assertTrue(store.isOpen(now));
    }

    @Test
    void isOpen_WhenNowBeforeOpen_ReturnsFalse() {
        // now = 14:00, open = 15:00, close = 16:00
        LocalTime now = LocalTime.of(14, 0);
        Store store = givenStore()
                .opens(15, 0)
                .closes(16, 0)
                .build();
        assertFalse(store.isOpen(now));
    }

    @Test
    void isOpen_WhenNowAfterClose_ReturnsFalse() {
        // now = 14:00, open = 12:00, close = 13:00
        LocalTime now = LocalTime.of(14, 0);
        Store store = givenStore()
                .opens(12, 0)
                .closes(13, 0)
                .build();
        assertFalse(store.isOpen(now));
    }

    @Test
    void isOpen_OvernightHoursAndNowAfterOpenBeforeMidnight_ReturnsTrue() {
        // now = 23:00, open = 22:00, close = 02:00 (next day) - store open overnight
        LocalTime now = LocalTime.of(23, 0);
        Store store = givenStore()
                .opens(22, 0)
                .closes(2, 0)
                .build();
        assertTrue(store.isOpen(now));
    }

    @Test
    void isOpen_OvernightHoursAndNowBeforeCloseAfterMidnight_ReturnsTrue() {
        // now = 01:00, open = 22:00 (prev day), close = 03:00 - store open overnight
        LocalTime now = LocalTime.of(1, 0);
        Store store = givenStore()
                .opens(22, 0)
                .closes(3, 0)
                .build();
        assertTrue(store.isOpen(now));
    }

    @Test
    void isOpen_OvernightHoursAndNowBetweenCloseAndOpen_ReturnsFalse() {
        // now = 10:00, open = 22:00, close = 08:00 - overnight store closed between 08:00-22:00
        LocalTime now = LocalTime.of(10, 0);
        Store store = givenStore()
                .opens(22, 0)
                .closes(8, 0)
                .build();
        assertFalse(store.isOpen(now));
    }

    @Test
    void isOpen_WhenTimesMissingOrBlank_ReturnsFalse() {
        Store s1 = givenStore().opensAt(null).closes(22, 0).build();
        Store s2 = givenStore().opens(8, 0).closesAt(null).build();
        Store s3 = givenStore().opensAt(null).closesAt(null).build();

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
        assertFalse(s3.isOpen());
    }

    @Test
    void isOpen_WithValidTimes_ParsesCorrectly() {
        // now = 15:00, open = 14:30, close = 15:30
        LocalTime now = LocalTime.of(15, 0);
        Store store = givenStore()
                .opens(14, 30)
                .closes(15, 30)
                .build();
        assertTrue(store.isOpen(now));
    }

    private static StoreBuilder givenStore() {
        return new StoreBuilder();
    }

    private static class StoreBuilder {
        private LocalTime openTime;
        private LocalTime closeTime;

        public StoreBuilder opens(int hour, int minute) {
            this.openTime = LocalTime.of(hour, minute);
            return this;
        }

        public StoreBuilder opensAt(LocalTime time) {
            this.openTime = time;
            return this;
        }

        public StoreBuilder closes(int hour, int minute) {
            this.closeTime = LocalTime.of(hour, minute);
            return this;
        }

        public StoreBuilder closesAt(LocalTime time) {
            this.closeTime = time;
            return this;
        }

        public Store build() {
            Store store = new Store();
            store.setTodayOpen(openTime);
            store.setTodayClose(closeTime);
            return store;
        }
    }
}