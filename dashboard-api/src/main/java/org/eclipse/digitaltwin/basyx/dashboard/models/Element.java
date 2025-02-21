package org.eclipse.digitaltwin.basyx.dashboard.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Document(collection = "elements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Jacksonized // Enhances compatibility with Jackson's deserialization
public class Element {
    @Id
    @JsonProperty("id")
    private String elementId;

    private String title;
    private String endpoint;

    private Group group;

    @JsonProperty("config")
    private String configObject;

    private boolean visibility;
    private int order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    public static class Group {
        @JsonProperty("groupName")
        private String groupName;

        @JsonProperty("groupId")
        private String groupId;
    }

    public JsonNode getConfigObjectAsJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(configObject);
    }

    public void setConfigObjectFromJson(JsonNode jsonNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.configObject = objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
