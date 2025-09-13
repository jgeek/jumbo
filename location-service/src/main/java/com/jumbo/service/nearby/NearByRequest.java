package com.jumbo.service.nearby;

public record NearByRequest(double latitude, double longitude, int limit, boolean onlyOpen) {
}
