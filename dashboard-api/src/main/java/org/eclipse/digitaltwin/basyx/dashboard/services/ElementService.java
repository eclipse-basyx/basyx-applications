package org.eclipse.digitaltwin.basyx.dashboard.services;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.basyx.dashboard.dto.ElementDto;
import org.eclipse.digitaltwin.basyx.dashboard.dto.ElementOrderUpdateDto;
import org.eclipse.digitaltwin.basyx.dashboard.dto.GroupSummaryDto;
import org.eclipse.digitaltwin.basyx.dashboard.mapper.ElementMapper;
import org.eclipse.digitaltwin.basyx.dashboard.models.Element;
import org.eclipse.digitaltwin.basyx.dashboard.repositories.ElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ElementService {

    @Autowired
    private ElementRepository elementRepository;

    @Autowired
    private ElementMapper elementMapper;

    public ElementDto addElement(ElementDto elementDto) {
        // Convert DTO to entity
        Element element = elementMapper.toEntity(elementDto);

        // Fetch elements in the same group to determine the next order
        List<Element> groupElements = elementRepository.findByGroup_GroupId(element.getGroup().getGroupId());
        int nextOrder = groupElements.stream()
                .mapToInt(Element::getOrder)
                .max()
                .orElse(-1) + 1; // Start at 0 if there are no elements in the group

        // Set the order of the new element
        element.setOrder(nextOrder);

        // Save the new element
        Element savedElement = elementRepository.save(element);

        // Convert the saved entity back to DTO
        return elementMapper.toDto(savedElement);
    }



    public Optional<ElementDto> findElementById(String id) {
        return elementRepository.findById(id).map(elementMapper::toDto);
    }

      public void deleteElementById(String id) {
        elementRepository.deleteById(id);
    }


    public List<GroupSummaryDto> getGroupsSummary() {
        List<Element> allElements = elementRepository.findAll();

        // Collect group information and count elements
        Map<Element.Group, Long> groupCounts = allElements.stream()
                .collect(Collectors.groupingBy(Element::getGroup, Collectors.counting()));

        // Construct GroupSummaryDto objects without decoding group IDs
        return groupCounts.entrySet().stream()
                .map(entry -> {
                    Element.Group group = entry.getKey();
                    Long elementCount = entry.getValue();

                    return new GroupSummaryDto(group.getGroupName(), group.getGroupId(), elementCount);
                })
                .collect(Collectors.toList());
    }



    public List<ElementDto> getElementsByGroupId(String groupId) {
        return elementRepository.findByGroup_GroupId(groupId).stream()
                .map(elementMapper::toDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public void updateOrderWithinGroup(String groupId, List<ElementOrderUpdateDto> elementOrderUpdateDtos) {
        Map<String, Integer> idToOrderMap = elementOrderUpdateDtos.stream()
                .collect(Collectors.toMap(ElementOrderUpdateDto::getElementId, ElementOrderUpdateDto::getOrder));

        List<Element> elementsToUpdate = elementRepository.findByGroup_GroupId(groupId)
                .stream()
                .filter(element -> idToOrderMap.containsKey(element.getElementId()))
                .peek(element -> element.setOrder(idToOrderMap.get(element.getElementId())))
                .collect(Collectors.toList());

        elementRepository.saveAll(elementsToUpdate);
    }



    @Transactional
    public ElementDto updateElement(String id, ElementDto elementDto) {
        Element element = elementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Element not found with ID: " + id));

        element.setTitle(elementDto.getTitle());
        element.setEndpoint(elementDto.getEndpoint());
        element.setGroup(elementDto.getGroup());
        element.setConfigObject(String.valueOf(elementDto.getConfigObject())); // Assuming direct assignment is possible
        element.setVisibility(elementDto.getVisibility());
        element.setOrder(elementDto.getOrder());

        Element updatedElement = elementRepository.save(element);
        return elementMapper.toDto(updatedElement);
    }

    @Transactional
    public void deleteElementsByGroupId(String groupId) {
        // Find elements by groupId and delete them
        List<Element> elements = elementRepository.findByGroup_GroupId(groupId);
        elementRepository.deleteAll(elements);
    }
}
