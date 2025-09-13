package com.jumbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Data
public class Store {
    private String city;
    private String postalCode;
    private String street;
    private String street2;
    private String street3;
    private String addressName;
    private String uuid;
    private double longitude;
    private double latitude;
    private String complexNumber;
    private boolean showWarningMessage;
    private String todayOpen;
    private String todayClose;
    private String locationType;
    private boolean collectionPoint;
    private String sapStoreID;
    private transient double distance;

    @JsonIgnore
    public boolean isOpen() {
        try {
            String[] openParts = todayOpen.split(":");
            String[] closeParts = todayClose.split(":");
            int openHour = Integer.parseInt(openParts[0]);
            int openMin = Integer.parseInt(openParts[1]);
            int closeHour = Integer.parseInt(closeParts[0]);
            int closeMin = Integer.parseInt(closeParts[1]);

            LocalTime now = LocalTime.now();
            LocalTime openTime = LocalTime.of(openHour, openMin);
            LocalTime closeTime = LocalTime.of(closeHour, closeMin);

            return !now.isBefore(openTime) && !now.isAfter(closeTime);
        } catch (Exception e) {
            return true; // if parsing fails, assume open
        }
    }
}