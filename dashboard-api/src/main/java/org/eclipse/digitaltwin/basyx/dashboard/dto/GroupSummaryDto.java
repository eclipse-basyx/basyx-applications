package org.eclipse.digitaltwin.basyx.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupSummaryDto {
    @NotBlank
    @JsonProperty("groupName")
    private String groupName;

    @NotBlank
    @JsonProperty("groupId")
    private String groupId;

    @Min(0)
    @JsonProperty("elementCount")
    private long numberOfElements;
}
