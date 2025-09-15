package com.jumbo.application.domain.servcie;

public interface DistanceCalculator {
    double distanceInKm(double lat1, double lon1, double lat2, double lon2);
}
