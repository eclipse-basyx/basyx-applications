package org.eclipse.digitaltwin.basyx.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.digitaltwin.basyx.dashboard.models.Element.Group;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Jacksonized
public class ElementDto {
    @JsonProperty("id")
    private String elementId;

    @NotBlank
    private String title;

    @NotBlank
    private String endpoint;

    @NotNull
    @Valid
    private Group group;

    @NotNull
    private JsonNode configObject;

    @NotNull
    private Boolean visibility;

    @NotNull
    @Min(0)
    private Integer order;
}
