package com.jumbo.application.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jumbo.common.validation.ValidTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

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
    @ValidTime
    private String todayOpen;

    @Schema(description = "Closing time for today", example = "22:00")
    @ValidTime
    private String todayClose;

    @Schema(description = "Type of store location")
    private String locationType;

    @Schema(description = "Whether this location is a collection point")
    private boolean collectionPoint;

    @Schema(description = "SAP store identifier")
    private String sapStoreID;

    @Schema(description = "Distance from search coordinates in kilometers", example = "1.23")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private transient double distance;

    @JsonIgnore
    public boolean isOpen() {
        if (todayOpen == null || todayClose == null ||
                todayOpen.trim().isEmpty() || todayClose.trim().isEmpty()) {
            return false; // Cannot determine, assume closed
        }

        try {
            LocalTime openTime = parseTime(todayOpen);
            LocalTime closeTime = parseTime(todayClose);
            LocalTime now = LocalTime.now();

            // Handle stores that close after midnight
            if (closeTime.isBefore(openTime)) {
                return !now.isBefore(openTime) || !now.isAfter(closeTime);
            } else {
                return !now.isBefore(openTime) && !now.isAfter(closeTime);
            }
        } catch (DateTimeParseException e) {
            return false; // If parsing fails, assume closed for safety
        }
    }

    private LocalTime parseTime(String timeStr) {
        String[] parts = timeStr.trim().split(":");
        if (parts.length != 2) {
            throw new DateTimeParseException("Invalid time format", timeStr, 0);
        }

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        return LocalTime.of(hour, minute);
    }
}