package org.diploma.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.diploma.mapper.NewRequestMapper;
import org.diploma.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Controller
public class RouteController {
    private final ObjectMapper mapper = new ObjectMapper();
    private final NewRequestMapper requestMapper = new NewRequestMapper();

    @GetMapping("/")
    public String index() {
        return "map";
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GRAPHOPPER_URL = "http://localhost:8989/route";

    @PostMapping("/route")
    public ResponseEntity<String> getRoute(@RequestBody RouteRequest body) {
        String url = GRAPHOPPER_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AlternativeRoute rout = requestMapper.getAlternativeRoute(3, 2.0, 0.9);
        Geometry geometry = requestMapper.getGeometry("Polygon", body.polygons().get(0));
        NewFeature feature = requestMapper.getFeature("Feature", "blocked0", geometry);
        Areas areas = requestMapper.getAreas("FeatureCollection", List.of(feature));
        PriorityF priority = requestMapper.getPriority("in_blocked0", 0.0);
        InModel model = requestMapper.getInModel(List.of(priority), areas);
        CustomModel cstMd = requestMapper.createCustomModel(body.points(),
                "car", false, "alternative_route", rout, model);
        try {
            String jsonRequest = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cstMd);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
            System.out.println(entity);
            String response = restTemplate.postForObject(url, entity, String.class);

            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

//    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> getJson(@RequestBody String body) {
//        AlternativeRoute rout = requestMapper.getAlternativeRoute(3, 2.0, 0.9);
//        Geometry geometry = requestMapper.getGeometry("Polygon",
//                List.of(List.of(
//                        List.of(37.617, 55.751), List.of(37.618, 55.751),
//                        List.of(37.618, 55.752), List.of(37.617, 55.752),
//                        List.of(37.617, 55.751))));
//        NewFeature feature = requestMapper.getFeature("Feature", "blocked0", geometry);
//        Areas areas = requestMapper.getAreas("FeatureCollection", List.of(feature));
//        PriorityF priority = requestMapper.getPriority("in_blocked0", 0.0);
//        CustomModel cstMd = requestMapper.createCustomModel(List.of(List.of(37.615, 55.75), List.of(37.625, 55.76)),
//                "car", false, "alternative_route", rout, areas, List.of(priority));
//        try {
//            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cstMd);
//            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//    }

}