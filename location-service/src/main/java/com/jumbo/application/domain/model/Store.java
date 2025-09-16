package com.jumbo.application.domain.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalTime;

@Data
@Schema(description = "Jumbo store information with location details")
public class Store {

    @Schema(description = "City where the store is located", example = "Amsterdam")
    private String city;

    @Schema(description = "Postal code of the store", example = "1012 AB")
    private String postalCode;

    @Schema(description = "Primary street address", example = "Damrak 123")
    private String street;

    @Schema(description = "Additional address line 2")
    private String street2;

    @Schema(description = "Additional address line 3")
    private String street3;

    @Schema(description = "Name/description of the address location")
    private String addressName;

    @Schema(description = "Unique identifier for the store")
    private String uuid;

    @Schema(description = "Longitude coordinate", example = "4.8952")
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180.0")
    private double longitude;

    @Schema(description = "Latitude coordinate", example = "52.3702")
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90.0")
    private double latitude;

    @Schema(description = "Complex number identifier")
    private String complexNumber;

    @Schema(description = "Whether to show warning message for this store")
    private boolean showWarningMessage;

    @Schema(description = "Opening time for today", example = "08:00")
    private LocalTime todayOpen;

    @Schema(description = "Closing time for today", example = "22:00")
    private LocalTime todayClose;

    @Schema(description = "Type of store location")
    private String locationType;

    @Schema(description = "Whether this location is a collection point")
    private boolean collectionPoint;

    @Schema(description = "SAP store identifier")
    private String sapStoreID;

    @Schema(description = "Distance from search coordinates in kilometers", example = "1.23")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private transient double distance;

    public Store opensAt(int hour, int minute) {
        validateTime(hour, minute);
        this.todayOpen = LocalTime.of(hour, minute);
        return this;
    }

    public Store closesAt(int hour, int minute) {
        validateTime(hour, minute);
        this.todayClose = LocalTime.of(hour, minute);
        return this;
    }

    private static void validateTime(int hour, int minute) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
    }

    @JsonIgnore
    public boolean isOpen() {
        return isOpen(LocalTime.now());
    }

    @JsonIgnore
    public boolean isOpen(LocalTime currentTime) {

        if (currentTime == null) {
            throw new IllegalArgumentException("currentTime cannot be null");
        }
        if (todayOpen == null || todayClose == null) {
            return false; // Cannot determine, assume closed
        }

        // Handle stores that close after midnight
        if (todayClose.isBefore(todayOpen)) {
           /*
            If `todayOpen = 22:00` and `todayClose = 03:00` (store open overnight), and `currentTime = 23:00`,
            then `!currentTime.isBefore(todayOpen)` is `true` (23:00 is not before 22:00),
            so the store is open.

            If `currentTime = 02:00`,
            then `!currentTime.isAfter(todayClose)` is `true` (02:00 is not after 03:00),
            so the store is open.

            If `currentTime = 10:00`,both conditions are `false`, so the store is closed.*/
            return !currentTime.isBefore(todayOpen) || !currentTime.isAfter(todayClose);
        } else {
            return !currentTime.isBefore(todayOpen) && !currentTime.isAfter(todayClose);
        }
    }
}