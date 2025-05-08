package org.diploma.service;

import lombok.RequiredArgsConstructor;
import org.diploma.entity.User;
import org.diploma.entity.Route;
import org.diploma.entity.BlockedArea;
import org.diploma.repository.UserRepository;
import org.diploma.repository.RouteRepository;
import org.diploma.repository.BlockedAreaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final BlockedAreaRepository blockedAreaRepository;

    public User registerUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(password);
        return userRepository.save(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Route saveRoute(User user, double lat1, double lon1, double lat2, double lon2) {
        Route route = new Route();
        route.setUser(user);
        route.setStartLat(lat1);
        route.setStartLng(lon1);
        route.setEndLat(lat2);
        route.setEndLng(lon2);
        return routeRepository.save(route);
    }

    public void saveBlockedAreas(Route route, List<String> polygonsJson) {
        for (String poly : polygonsJson) {
            BlockedArea area = new BlockedArea();
            area.setRoute(route);
            area.setPolygonCoordinatesJson(poly);
            blockedAreaRepository.save(area);
        }
    }

    public List<Route> getUserRoutes(User user) {
        return routeRepository.findByUser(user);
    }

    public List<BlockedArea> getBlockedAreasForRoute(Route route) {
        return blockedAreaRepository.findByRoute(route);
    }

    public Optional<Route> findRouteById(Long routeId) {
        return routeRepository.findByRouteId(routeId);
    }
}