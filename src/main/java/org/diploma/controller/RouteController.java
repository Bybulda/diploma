package org.diploma.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.diploma.mapper.NewRequestMapper;
import org.diploma.DTO.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class RouteController {
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
        InModel model = null;
        AlternativeRoute rout = requestMapper.getAlternativeRoute(3, 2.0, 0.9);
        if(body.polygons() != null && !body.polygons().isEmpty()){
            List<NewFeature> newFeatureList = new ArrayList<>();
            List<PriorityF> newPriorityList = new ArrayList<>();
            int i = 0;
            for(List<List<List<Double>>> p : body.polygons()){
                Geometry pGeometry = requestMapper.getGeometry("Polygon", p);
                NewFeature feature = requestMapper.getFeature("Feature", "blocked" + i, pGeometry);
                newFeatureList.add(feature);
                newPriorityList.add(requestMapper.getPriority("in_blocked" + i, 0.0));
                i++;
            }
            Areas areas = requestMapper.getAreas("FeatureCollection", newFeatureList);
            model = requestMapper.getInModel(newPriorityList, areas);
        }

        CustomModel cstMd = requestMapper.createCustomModel(body.points(),
                "car", false, "alternative_route", rout, model);
        try {
            String jsonRequest = requestMapper.getJsonModel(cstMd);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
            System.out.println(entity);
            String response = restTemplate.postForObject(url, entity, String.class);

            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}