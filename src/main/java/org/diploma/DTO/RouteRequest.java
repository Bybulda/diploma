package org.diploma.DTO;

import java.util.List;

public record RouteRequest(
        List<List<Double>> points,                         // [ [lng, lat], [lng, lat] ]
        List<List<List<List<Double>>>> polygons            // [[[ [lng, lat], ... ]], ... ]
) {}
