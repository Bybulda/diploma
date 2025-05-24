package org.diploma.model;

import java.time.LocalDateTime;

public record RouteResponse(long id, LocalDateTime timestamp,
                            double lat1, double lng1, double lat2, double lng2) {
}
