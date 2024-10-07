package org.eclipse.digitaltwin.basyx.dashboard.mapper;

import org.eclipse.digitaltwin.basyx.dashboard.dto.ElementDto;
import org.eclipse.digitaltwin.basyx.dashboard.models.Element;
import org.eclipse.digitaltwin.basyx.dashboard.models.Element.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ElementMapperTest {

    private ElementMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = Mappers.getMapper(ElementMapper.class);
    }

    @Test
    public void shouldMapElementToElementDto() {
        // Given an Element with a nested Group
        Element element = new Element();
        element.setElementId("123");
        element.setTitle("Test Element");
        element.setEndpoint("http://example.com/api");

        Group group = new Group();
        group.setGroupName("Test Group");
        group.setGroupId("group123");
        element.setGroup(group);

        // When the Element is mapped to an ElementDto
        ElementDto dto = mapper.toDto(element);

        // Then the fields should be correctly mapped
        assertNotNull(dto);
        assertEquals(element.getElementId(), dto.getElementId());
        assertEquals(element.getTitle(), dto.getTitle());
        assertEquals(element.getEndpoint(), dto.getEndpoint());
        assertEquals(element.getGroup().getGroupName(), dto.getGroup().getGroupName());
        assertEquals(element.getGroup().getGroupId(), dto.getGroup().getGroupId());
    }

    @Test
    public void shouldMapElementDtoToElement() {
        // Given an ElementDto with a nested Group
        ElementDto dto = new ElementDto();
        dto.setElementId("123");
        dto.setTitle("Test Element");
        dto.setEndpoint("http://example.com/api");

        Group group = new Group();
        group.setGroupName("Test Group");
        group.setGroupId("group123");
        dto.setGroup(group);

        // When the ElementDto is mapped to an Element
        Element element = mapper.toEntity(dto);

        // Then the fields should be correctly mapped
        assertNotNull(element);
        assertEquals(dto.getElementId(), element.getElementId());
        assertEquals(dto.getTitle(), element.getTitle());
        assertEquals(dto.getEndpoint(), element.getEndpoint());
        assertEquals(dto.getGroup().getGroupName(), element.getGroup().getGroupName());
        assertEquals(dto.getGroup().getGroupId(), element.getGroup().getGroupId());
    }
}
