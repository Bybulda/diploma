package org.diploma.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.diploma.entity.BlockedArea;
import org.diploma.entity.Route;
import org.diploma.entity.User;
import org.diploma.model.RouteResponse;
import org.diploma.model.SaveRouteRequest;
import org.diploma.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final RouteService routeService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestParam String email, @RequestParam String password) {
        if (routeService.findUserByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User newUser = routeService.registerUser(email, password);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam String email, @RequestParam String password) {
        if (routeService.findUserByEmailAndPassword(email, password).isPresent()) {
            return ResponseEntity.ok(routeService.findUserByEmail(email).get());
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> getRoutes(@RequestParam String email) {
        Optional<User> userOpt = routeService.findUserByEmail(email);
        if (userOpt.isPresent()) {
            List<Route> routes = routeService.getUserRoutes(userOpt.get());
            if (!routes.isEmpty()) {
                List<RouteResponse> routeResponses = new ArrayList<>();
                for (Route route : routes) {
                    RouteResponse response = new RouteResponse(route.getId(), route.getCreatedAt(),
                            route.getStartLat(), route.getStartLng(), route.getEndLat(), route.getEndLng());
                    routeResponses.add(response);
                }
                return ResponseEntity.ok(routeResponses);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/blocked/{routeId}")
    public ResponseEntity<List<BlockedArea>> getBlocked(@PathVariable Long routeId) {
        Optional<Route> routeOpt = routeService.findRouteById(routeId);
        return routeOpt.map(route -> ResponseEntity.ok(routeService.getBlockedAreasForRoute(route)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public ResponseEntity<RouteResponse> saveRoute(
            @RequestBody SaveRouteRequest saveRouteRequest
    ) {
        Optional<User> userOpt = routeService.findUserByEmail(saveRouteRequest.email());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().build();

        Route route = routeService.saveRoute(userOpt.get(),
                saveRouteRequest.lat1(), saveRouteRequest.lat2(), saveRouteRequest.lon1(), saveRouteRequest.lon2());
        routeService.saveBlockedAreas(route, saveRouteRequest.polygons());
        return ResponseEntity.ok(new RouteResponse(route.getId(), route.getCreatedAt(),
                route.getStartLat(), route.getStartLng(), route.getEndLat(), route.getEndLng()));
    }
}
