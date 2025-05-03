package org.diploma.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomModel {
    private List<List<Double>> points;
    private String profile;
    @JsonProperty("points_encoded")
    private Boolean pointsEncoded;
    private String algorithm;
    @JsonProperty("alternative_route")
    private AlternativeRoute alternativeRoute;
    @JsonProperty("custom_model")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private InModel customModel;


}
