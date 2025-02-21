package org.eclipse.digitaltwin.basyx.dashboard.controllers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.eclipse.digitaltwin.basyx.dashboard.dto.ElementDto;
import org.eclipse.digitaltwin.basyx.dashboard.dto.ElementOrderUpdateDto;
import org.eclipse.digitaltwin.basyx.dashboard.dto.GroupSummaryDto;
import org.eclipse.digitaltwin.basyx.dashboard.services.ElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Base64;
import java.util.List;


@RestController
@RequestMapping("/api/elements")
public class ElementController {

    private final ElementService elementService;
    private static final Logger logger = LoggerFactory.getLogger(ElementController.class);

    @Autowired
    public ElementController(ElementService elementService) {
        this.elementService = elementService;
    }

    @PostMapping("/addElement") //adds an element configured by the user to the dashboard-Notice :element id is generated automatically
    @Operation(summary = "Add Element",description = "Adds an element configured by the user to the dashboard. The element ID is generated automatically.")
    public ResponseEntity<ElementDto> addElement(@RequestBody ElementDto elementDto) {
        logger.info("Adding new element: {}", elementDto); //  this log statement to indicate that the method is invoked
        ElementDto createdElement = elementService.addElement(elementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdElement);
    }


    @GetMapping("/findElement/{id}")//Returns an element based off its elementId
    @Operation(summary = "Find Element by ID", description = "Returns an element based on its element ID.")
    public ResponseEntity<ElementDto> getElementById(@PathVariable String id) {
        return elementService.findElementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/deleteElement/{id}")//Deletes an element based on its elementId
    @Operation(summary = "Delete Element", description = "Deletes an element based on its element ID.")
    public ResponseEntity<Void> deleteElement(@PathVariable String id) {
        elementService.deleteElementById(id);
        return ResponseEntity.ok().build();
    }

// Group-related endpoints

    @GetMapping("/groups/summary")//Returns group names, IDs and number of groups
    @Operation(summary = "Get Groups Summary", description = "Returns a summary of all groups including group names, IDs, and the number of elements in each group.")
    public ResponseEntity<List<GroupSummaryDto>> getGroupsSummary() {
        List<GroupSummaryDto> groupsSummary = elementService.getGroupsSummary();
        return ResponseEntity.ok(groupsSummary);
    }


    @PutMapping("/updateElement/{id}")
    @Operation(summary = "Update Element", description = "Updates an existing element on the dashboard based on the element ID provided.")
    public ResponseEntity<ElementDto> updateElement(@PathVariable String id, @Valid @RequestBody ElementDto elementDto) {
        try {
            ElementDto updatedElement = elementService.updateElement(id, elementDto);
            return ResponseEntity.ok(updatedElement);
        } catch (EntityNotFoundException e) {
            logger.error("Error updating element: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }

    }
    @GetMapping("/findGroup/{groupId}")
    @Operation(summary = "Get Elements by Group ID(The Base64 encoded group ID). Make sure to provide the group ID in a Base64 encoded format",
            description = "Returns all elements of a group based on the decoded group ID provided.",
            responses = {
                    @ApiResponse(description = "Successful operation", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid Group ID format"),
                    @ApiResponse(responseCode = "404", description = "Group not found")
            })
    public ResponseEntity<?> getElementsByGroupId(
            @Parameter(description = "The Base64 encoded group ID. Make sure to provide the group ID in a Base64 encoded format.", required = true)
            @PathVariable String groupId) {

        try {
            String decodedGroupId = new String(Base64.getDecoder().decode(groupId));
            List<ElementDto> elements = elementService.getElementsByGroupId(decodedGroupId);

            if (elements.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(elements);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid Group ID format.");
        }
    }

    @PutMapping("/updateGroup/{groupId}/order")
    @Operation(summary = "Update Group by Group ID(The Base64 encoded group ID). Make sure to provide the group ID in a Base64 encoded format")
    public ResponseEntity<?> updateOrderWithinGroup(@PathVariable String groupId, @RequestBody List<ElementOrderUpdateDto> elementOrderUpdateDtos) {
        try {
            String decodedGroupId = new String(Base64.getDecoder().decode(groupId));
            elementService.updateOrderWithinGroup(decodedGroupId, elementOrderUpdateDtos);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating order within group: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteGroup/{groupId}")
    @Operation(summary = "Delete Elements by Group ID(The Base64 encoded group ID). Make sure to provide the group ID in a Base64 encoded format", description = "Deletes all elements within a specified group.")
    public ResponseEntity<Void> deleteElementsByGroupId(@PathVariable String groupId) {
        try {
            String decodedGroupId = new String(Base64.getDecoder().decode(groupId));
            elementService.deleteElementsByGroupId(decodedGroupId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting elements by group ID: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
