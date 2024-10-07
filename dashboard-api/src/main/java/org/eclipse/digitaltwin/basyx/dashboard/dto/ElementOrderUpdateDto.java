package org.eclipse.digitaltwin.basyx.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementOrderUpdateDto {
    @JsonProperty("id")
    private String elementId;

    @JsonProperty("order")
    private Integer order;
}
