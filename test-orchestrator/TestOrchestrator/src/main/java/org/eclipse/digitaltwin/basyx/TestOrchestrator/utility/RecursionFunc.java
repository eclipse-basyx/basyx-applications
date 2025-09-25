package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;


import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import java.util.List;
import java.util.Map;

public class RecursionFunc {

    public static void compareSubmodelElements(List<SubmodelElement> schemaElements, List<SubmodelElement> inputElements, ComparisonResult result) {
        // Create maps for schema and input elements for easy lookup by semanticId
        Map<String, List<SubmodelElement>> schemaElementMap = SMEComparator.createSemanticIdMap(schemaElements);
        Map<String, List<SubmodelElement>> inputElementMap = SMEComparator.createSemanticIdMap(inputElements);

        // First, check for spaces in input elements and log warnings before proceeding with validation
        for (SubmodelElement inputElement : inputElements) {
            SMEComparator.checkForSpaces(inputElement, result); // Check for spaces only in input elements
        }

        // Proceed with normal validation after checking for spaces
        for (SubmodelElement schemaElement : schemaElements) {
            String semanticId = SMEComparator.getSemanticIdValue(schemaElement);
            String qualifier = SMEComparator.getQualifierFlag(schemaElement);

            // If the qualifier is "Allowed", skip strict validation but allow additional elements
            if ("Allowed".equals(qualifier)) {
                continue; // Skip validation for this element and move to the next
            }

            // Retrieve matching elements from the input map
            List<SubmodelElement> matchingInputElements = inputElementMap.get(semanticId);

            // Handle element multiplicity
            String multiplicity = SMEComparator.getMultiplicityFromQualifier(schemaElement);

            if ("One".equals(multiplicity)) {
                SMEComparator.checkMultiplicityOne(schemaElement, inputElementMap, result);
            } else if ("OneToMany".equals(multiplicity)) {
                SMEComparator.checkMultiplicityOneToMany(schemaElement, inputElementMap, result);
            } else if ("ZeroToOne".equals(multiplicity)) {
                SMEComparator.checkMultiplicityZeroToOne(schemaElement, inputElementMap, result);
            } else if ("ZeroToMany".equals(multiplicity)) {
                SMEComparator.checkMultiplicityZeroToMany(schemaElement, inputElementMap, result);
            }

            // Recursion for SubmodelElementCollection
            if (schemaElement instanceof SubmodelElementCollection && matchingInputElements != null && !matchingInputElements.isEmpty()) {
                for (SubmodelElement inputElement : matchingInputElements) {
                    if (inputElement instanceof SubmodelElementCollection) {
                        SubmodelElementCollection schemaCollection = (SubmodelElementCollection) schemaElement;
                        SubmodelElementCollection inputCollection = (SubmodelElementCollection) inputElement;
                        compareSubmodelElements(schemaCollection.getValue(), inputCollection.getValue(), result);
                    } else {
                        result.addError("Expected a collection with semantic ID: " + semanticId + " but found a non-collection element. || Correction: Add a SMC with Semantic ID: " + semanticId);
                        result.markError(semanticId);
                    }
                }
            } else if (matchingInputElements == null || matchingInputElements.isEmpty()) {
                // Optional elements should be marked as warnings, not errors
                result.addWarning("Optional element with semantic ID: " + semanticId + " not found but it's not mandatory. || Correction: Add the element with Semantic ID: " + semanticId);
                result.markWarning(semanticId);
            } else {result.markCorrect(semanticId);}
        }


        // Handling unexpected input elements that are not part of the schema
        for (String inputSemanticId : inputElementMap.keySet()) {
            if (!schemaElementMap.containsKey(inputSemanticId)) {
                // If the qualifier of the input allows extra elements, treat it as info
                SubmodelElement inputElement = inputElementMap.get(inputSemanticId).get(0);
                SMEComparator.validateInputProperty(inputElement, result);
                String inputQualifier = SMEComparator.getQualifierFlag(inputElement);
                if ("Allowed".equals(inputQualifier)) {
                    result.addInfo("Additional element with semantic ID: " + inputSemanticId + " allowed as per schema flexibility.");
                } else {
                    result.addError("Unexpected element with semantic ID: " + inputSemanticId + "|| Correction: Remove the element with Semantic ID: " + inputSemanticId);

                }
            }
        }
    }
}
