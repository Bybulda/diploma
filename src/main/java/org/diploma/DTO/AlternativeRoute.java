package org.diploma.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlternativeRoute {
    @JsonProperty("max_paths")
    private Integer maxPaths;
    @JsonProperty("max_weight_factor")
    private Double maxWeightFactor;
    @JsonProperty("max_share_factor")
    private Double maxShareFactor;
}
