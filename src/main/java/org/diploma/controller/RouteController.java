package org.diploma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class RouteController {

    @GetMapping("/")
    public String index() {
        return "map";
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GRAPHOPPER_URL = "http://localhost:8989/route";

    @GetMapping("/route")
    public ResponseEntity<String> getRoute(
            @RequestParam("lat1") double lat1,
            @RequestParam("lon1") double lon1,
            @RequestParam("lat2") double lat2,
            @RequestParam("lon2") double lon2) {

        // Формируем запрос к GraphHopper
        String url = GRAPHOPPER_URL + "?point=" + lat1 + "," + lon1 +
                "&point=" + lat2 + "," + lon2 +
                "&points_encoded=false" +
                "&profile=car" +
                "&alternative_route.max_paths=3" +
                "&alternative_route.max_weight_factor=2" +
                "&alternative_route.max_share_factor=0.9"; // <-- ВАЖНО

        // Получаем ответ от GraphHopper
        String response = restTemplate.getForObject(url, String.class);

        return ResponseEntity.ok(response);
    }
}