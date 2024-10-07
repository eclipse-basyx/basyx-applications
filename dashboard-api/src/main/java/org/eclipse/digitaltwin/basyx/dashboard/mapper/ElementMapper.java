package org.eclipse.digitaltwin.basyx.dashboard.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.basyx.dashboard.dto.ElementDto;
import org.eclipse.digitaltwin.basyx.dashboard.models.Element;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ElementMapper {

    @Mapping(source = "elementId", target = "elementId")
    @Mapping(source = "configObject", target = "configObject", qualifiedByName = "stringToJsonNode")
    ElementDto toDto(Element element);

    @Mapping(source = "elementId", target = "elementId")
    @Mapping(source = "configObject", target = "configObject", qualifiedByName = "jsonNodeToString")
    Element toEntity(ElementDto dto);

    @Named("stringToJsonNode")
    default JsonNode stringToJsonNode(String value) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(value);
    }

    @Named("jsonNodeToString")
    default String jsonNodeToString(JsonNode jsonNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        return jsonNode.toString();
    }
}
