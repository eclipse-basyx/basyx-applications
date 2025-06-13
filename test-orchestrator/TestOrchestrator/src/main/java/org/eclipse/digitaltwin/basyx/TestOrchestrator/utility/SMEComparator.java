package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMEComparator {

    public static void checkMultiplicityOne(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap, ComparisonResult result) {
        String semanticId = getSemanticIdValue(schemaElement);

        List<SubmodelElement> matchingElements = findAllMatchingInputElements(schemaElement, inputElementMap);

        // Multiplicity check
        if (matchingElements.isEmpty()) {
            result.addError("Expected one element with semantic ID (One): " + semanticId + " but found none. || Correction: Add the element with Semantic ID: " + semanticId);
            result.markError(semanticId);
        } else if (matchingElements.size() > 1) {
            result.addError("Alert: More than one element found for semantic ID (One): " + semanticId + ". Only one element is accepted. || Correction: Only keep one element for Semantic ID: " + semanticId);
            result.markError(semanticId);
        }

        // Content validation continues regardless of warnings
        if (!matchingElements.isEmpty()) {
            validateElement(schemaElement, matchingElements.get(0), result);
        }

    }

    public static void checkMultiplicityOneToMany(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap, ComparisonResult result) {
        List<SubmodelElement> matchingElements = findAllMatchingInputElements(schemaElement, inputElementMap);
        String semanticId = getSemanticIdValue(schemaElement);


        if (matchingElements.isEmpty()) {
            result.addError("Expected at least one element with semantic ID (OneToMany): " + semanticId + " but found none. || Correction: Add at least one element with Semantic ID: " + semanticId);
            result.markError(semanticId);

        } else {
            for (SubmodelElement inputElement : matchingElements) {
                validateElement(schemaElement, inputElement, result);
            }
        }

    }

    public static void checkMultiplicityZeroToOne(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap, ComparisonResult result) {
        List<SubmodelElement> matchingElements = findAllMatchingInputElements(schemaElement, inputElementMap);
        String semanticId = getSemanticIdValue(schemaElement);



        // Multiplicity check
        if (matchingElements.size() > 1) {
            result.addError("Alert: More than one element found for semantic ID (ZeroToOne): " + getSemanticIdValue(schemaElement) + ". Only one element is accepted. || Correction: Remove the extra elements with Semantic ID: " + getSemanticIdValue(schemaElement));
            result.markError(semanticId);

        }

        // Content validation continues regardless of warnings
        if (!matchingElements.isEmpty()) {
            validateElement(schemaElement, matchingElements.get(0), result);
        }

    }

    public static void checkMultiplicityZeroToMany(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap, ComparisonResult result) {
        List<SubmodelElement> matchingElements = findAllMatchingInputElements(schemaElement, inputElementMap);

        // Content validation continues regardless of warnings
        for (SubmodelElement inputElement : matchingElements) {
            validateElement(schemaElement, inputElement, result);
        }
    }

    // New validation logic
    public static void validateElement(SubmodelElement schemaElement, SubmodelElement inputElement, ComparisonResult result) {
        String semanticId = getSemanticIdValue(schemaElement);

        boolean hasIssues = false;
        // Check for spaces in idShort and semanticId and log warnings
        checkForSpaces(inputElement, result);


        // Check if the element is marked as Required
        boolean isRequired = "Required".equals(getQualifierFlag(schemaElement));

        // If it's not required, log an info and skip validation
        if (!isRequired) {
            result.addInfo("Optional element found: " + inputElement.getIdShort());
            return;
        }

        // Check IDShort by trimming spaces
        String schemaIdShort = schemaElement.getIdShort().replaceAll("\\s", "");
        String inputIdShort = inputElement.getIdShort().replaceAll("\\s", "");

        if (!schemaIdShort.equals(inputIdShort)) {
            String difference = "IDShort mismatch for element with semantic ID: "
                    + getSemanticIdValue(schemaElement)
                    + ". Expected: " + schemaIdShort
                    + ", Found: " + inputIdShort
                    + " || Correction: Replace idShort" + inputIdShort + "with" + schemaIdShort;
            result.addDifference(difference);
            result.markError(semanticId);
            hasIssues = true;
        }

        // Check element type by comparing the interface types
        String expectedType = schemaElement.getClass().getInterfaces()[0].getSimpleName();
        String foundType = inputElement.getClass().getInterfaces()[0].getSimpleName();

        if (!expectedType.equals(foundType)) {
            String difference = "Element type mismatch for element with semantic ID: "
                    + getSemanticIdValue(schemaElement)
                    + ". Expected: " + expectedType
                    + ", Found: " + foundType
                    + " || Correction: Replace idShort" + foundType + "with" + expectedType;
            result.addDifference(difference);
            result.markError(semanticId);
            hasIssues = true;
        }

        // Check value for Property elements
        if (schemaElement instanceof Property && inputElement instanceof Property) {
            Property schemaProperty = (Property) schemaElement;
            Property inputProperty = (Property) inputElement;

            String schemaValue = schemaProperty.getValue().replaceAll("\\s", "");
            String inputValue = inputProperty.getValue().replaceAll("\\s", "");

            if (!schemaValue.equals(inputValue)) {
                String difference = "Value mismatch for element with semantic ID: "
                        + getSemanticIdValue(schemaElement)
                        + ". Expected: " + schemaValue
                        + ", Found: " + inputValue
                        + " || Correction: Replace idShort" + inputValue + "with" + schemaValue;
                result.addDifference(difference);
                result.markError(semanticId);
                hasIssues = true;
            }
        }
        // Only mark as correct if no issues found and not marked invalid earlier
        if (!hasIssues) {
            result.markCorrect(semanticId);
        }
    }

    // Helper method to check for spaces in both idShort and semanticId
    public static void checkForSpaces(SubmodelElement inputElement, ComparisonResult result) {
        // Check if the input IDShort contains spaces and log a warning

        if (inputElement.getIdShort().contains(" ")) {
            result.addWarning("IDShort contains spaces: " + inputElement.getIdShort() + " || Correction: Remove spaces from the IDShort");
        }

        // Check if the input Semantic ID contains spaces and log a warning
        String semanticIdValue = getSemanticIdValue(inputElement);
        if (semanticIdValue != null && semanticIdValue.contains(" ")) {
            result.addWarning("SemanticId contains spaces: " + semanticIdValue + " || Correction: Remove spaces from the Semantic ID");
        }
    }

    // Method to find all matching input elements based on semanticId
    private static List<SubmodelElement> findAllMatchingInputElements(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap) {
        String schemaSemanticId = getSemanticIdValue(schemaElement);
        return inputElementMap.getOrDefault(schemaSemanticId, List.of());
    }

    public static Map<String, List<SubmodelElement>> createSemanticIdMap(List<SubmodelElement> elements) {
        Map<String, List<SubmodelElement>> map = new HashMap<>();
        for (SubmodelElement element : elements) {
            String semanticId = getSemanticIdValue(element);
            if (semanticId != null) {
                map.computeIfAbsent(semanticId, k -> new ArrayList<>()).add(element);
            }
        }
        return map;
    }

    public static String getMultiplicityFromQualifier(SubmodelElement element) {
        if (element.getQualifiers() != null) {
            for (Qualifier qualifier : element.getQualifiers()) {
                if ("Multiplicity".equals(qualifier.getType())) {
                    return qualifier.getValue();
                }
            }
        }
        return null;
    }

    public static String getQualifierFlag(SubmodelElement element) {
        if (element.getQualifiers() != null) {
            for (Qualifier qualifier : element.getQualifiers()) {
                if ("Flag".equals(qualifier.getType())) {
                    return qualifier.getValue();
                }
            }
        }
        return "Not Allowed";  // Default to "Not Allowed" if no qualifier is found
    }

    // Updated method to check for spaces and ignore everything after '*'
    public static String getSemanticIdValue(SubmodelElement element) {
        if (element.getSemanticId() != null && !element.getSemanticId().getKeys().isEmpty()) {
            String value = element.getSemanticId().getKeys().get(0).getValue();

            // Ignore everything after '*'
            if (value.contains("*")) {
                value = value.split("\\*")[0];
            }
            return value.trim();  // Trimming of any leading or trailing spaces
        }
        return null;
    }
}
