package com.jumbo.service;

import jakarta.validation.constraints.Min;

public record NearByRequest(double latitude, double longitude, @Min(1) int limit, boolean onlyOpen) {
}
