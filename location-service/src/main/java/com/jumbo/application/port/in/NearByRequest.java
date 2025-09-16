package com.jumbo.application.port.in;

import static com.jumbo.common.validation.Validation.validate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;


public record NearByRequest(

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        double latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        double longitude,

        @Min(1)
        double maxRadiusKm,

        @Min(1) int limit,
        boolean onlyOpen
) {

    public NearByRequest(double latitude, double longitude, double maxRadiusKm, int limit, boolean onlyOpen) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxRadiusKm = maxRadiusKm;
        this.limit = limit;
        this.onlyOpen = onlyOpen;
        validate(this);
    }
}
