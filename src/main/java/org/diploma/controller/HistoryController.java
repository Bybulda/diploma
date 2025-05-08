package org.diploma.controller;

import lombok.RequiredArgsConstructor;

import org.diploma.entity.BlockedArea;
import org.diploma.entity.Route;
import org.diploma.entity.User;
import org.diploma.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getRoutes(@RequestParam String email) {
        Optional<User> userOpt = routeService.findUserByEmail(email);
        return userOpt.map(user -> ResponseEntity.ok(routeService.getUserRoutes(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/blocked/{routeId}")
    public ResponseEntity<List<BlockedArea>> getBlocked(@PathVariable Long routeId) {
        Optional<Route> routeOpt = routeService.findRouteById(routeId);
        return routeOpt.map(route -> ResponseEntity.ok(routeService.getBlockedAreasForRoute(route)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public ResponseEntity<Route> saveRoute(
            @RequestParam String email,
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2,
            @RequestBody List<String> polygonsJson
    ) {
        Optional<User> userOpt = routeService.findUserByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().build();

        Route route = routeService.saveRoute(userOpt.get(), lat1, lon1, lat2, lon2);
        routeService.saveBlockedAreas(route, polygonsJson);
        return ResponseEntity.ok(route);
    }
}
