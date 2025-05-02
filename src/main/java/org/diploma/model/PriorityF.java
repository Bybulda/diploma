package org.diploma.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriorityF {
    @JsonProperty("if")
    private String condition;
    @JsonProperty("multiply_by")
    private Double multiplyBy;
}
