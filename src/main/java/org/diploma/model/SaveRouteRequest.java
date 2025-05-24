package org.diploma.model;

import java.util.List;

public record SaveRouteRequest(
        String email, Double lat1, Double lat2, Double lon1, Double lon2,
        List<String> polygons) {
}
