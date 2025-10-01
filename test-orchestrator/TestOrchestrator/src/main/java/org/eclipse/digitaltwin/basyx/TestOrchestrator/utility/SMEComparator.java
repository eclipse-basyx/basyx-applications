package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.config.AppConfig;

import java.util.*;


public class SMEComparator {

    public static void checkMultiplicityOne(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap, ComparisonResult result) {
        String semanticId = getSemanticIdValue(schemaElement);

        List<SubmodelElement> matchingElements = findAllMatchingInputElements(schemaElement, inputElementMap);

        if (matchingElements.isEmpty()) {
            result.addError("Expected one element with semantic ID (One): " + semanticId + " but found none. || Correction: Add the element with Semantic ID: " + semanticId);
            result.markError(semanticId);
        } else if (matchingElements.size() > 1) {
            result.addError("Alert: More than one element found for semantic ID (One): " + semanticId + ". Only one element is accepted. || Correction: Only keep one element for Semantic ID: " + semanticId);
            result.markError(semanticId);
        }

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

        if (matchingElements.size() > 1) {
            result.addError("Alert: More than one element found for semantic ID (ZeroToOne): " + getSemanticIdValue(schemaElement) + ". Only one element is accepted. || Correction: Remove the extra elements with Semantic ID: " + getSemanticIdValue(schemaElement));
            result.markError(semanticId);

        }

        if (!matchingElements.isEmpty()) {
            validateElement(schemaElement, matchingElements.get(0), result);
        }

    }

    public static void checkMultiplicityZeroToMany(SubmodelElement schemaElement, Map<String, List<SubmodelElement>> inputElementMap, ComparisonResult result) {
        List<SubmodelElement> matchingElements = findAllMatchingInputElements(schemaElement, inputElementMap);


        for (SubmodelElement inputElement : matchingElements) {
            validateElement(schemaElement, inputElement, result);
        }
    }

    public static void validateElement(SubmodelElement schemaElement, SubmodelElement inputElement, ComparisonResult result) {
        String semanticId = getSemanticIdValue(schemaElement);
        String inputSemanticId = getSemanticIdValue(inputElement);

        checkForSpaces(inputElement, result);

        String schemaIdShort = schemaElement.getIdShort().replaceAll("[^a-zA-Z]", "").toLowerCase();
        String inputIdShort = inputElement.getIdShort().replaceAll("[^a-zA-Z]", "").toLowerCase();


        if (!schemaIdShort.equals(inputIdShort)) {
            String difference = "IDShort mismatch for element with semantic ID: "
                    + getSemanticIdValue(schemaElement)
                    + ". Expected: " + schemaIdShort + ", Found: " + inputIdShort
                    + " || Correction: Replace idShort " + inputElement.getIdShort() + " with " + schemaElement.getIdShort();
            result.addDifference(difference);
            result.markError(semanticId);
        }



        if (schemaElement instanceof Property && inputElement instanceof Property) {
            Property schemaProperty = (Property) schemaElement;
            Property inputProperty = (Property) inputElement;


            String schemaValue = schemaProperty.getValue() != null ? schemaProperty.getValue().replaceAll("\\s", "") : "";
            String inputValue = inputProperty.getValue() != null ? inputProperty.getValue().replaceAll("\\s", "") : "";


            DataTypeDefXsd expectedvalueType = schemaProperty.getValueType();
            DataTypeDefXsd actualValueType = inputProperty.getValueType();
            if (expectedvalueType != null && actualValueType != null && !expectedvalueType.equals(actualValueType)) {
                result.addDifference("ValueType mismatch for semantic ID: " + semanticId + ". Expected: " + expectedvalueType + " but found value: " + actualValueType);
            }


            validateInputProperty(inputElement, result);


            if (inputProperty.getValue() != null && expectedvalueType != null) {
                try {
                    switch (expectedvalueType) {
                        case INT:
                        case INTEGER:
                            Integer.parseInt(inputValue);
                            break;
                        case FLOAT:
                        case DOUBLE:
                            Double.parseDouble(inputValue);
                            break;
                        case BOOLEAN:
                            String val = inputValue.trim().toLowerCase();
                            if (!val.equals("true") && !val.equals("false"))
                                throw new IllegalArgumentException("Invalid boolean: " + inputValue);
                            break;
                        case DATE:
                            if (!inputValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                throw new IllegalArgumentException("Invalid date format: " + inputValue);
                            }
                            break;
                        case STRING:
                        default:
                            break;
                    }
                } catch (Exception e) {
                    result.addInfo("ValueType mismatch: Value for semantic ID '" + semanticId +
                            "' was expected to be of type '" + expectedvalueType + "', but found: '" + inputValue + "'");
                }
            }


            if (EClassQueryService.isEClassEnabled() && inputSemanticId != null && inputSemanticId.contains("0173-1")) {

                try{
                    System.out.println("Calling ECLASS for semantic ID: " + inputSemanticId);
                    Map<String, Object> eclassData = EClassQueryService.fetchEClassData(inputSemanticId.replace("#", "%23"));
                    if (eclassData != null) {
                        Map<String, Object> preferredName = (Map<String, Object>) eclassData.get("preferredName");
                        if (preferredName != null && preferredName.containsKey("en-US")) {
                            String expectedRaw = (String) preferredName.get("en-US");
                            String actualRaw = inputProperty.getIdShort();

                            String expectedNormalized = expectedRaw.replaceAll("[\\s.,;:()\\[\\]{}\"'!@#$%^&*<>?/\\\\|+=~-]", "").toLowerCase();
                            String actualNormalized = actualRaw.replaceAll("\\s+", "").toLowerCase();

                            if (!actualNormalized.equals(expectedNormalized)) {
                                result.addDifference("Preferred name mismatch for semantic ID: " + inputSemanticId +
                                        ". Expected name containing: '" + expectedRaw + "', but found: '" + actualRaw + "'");
                                result.markError(inputSemanticId);
                            }
                        }

                        Map<String, Object> definition = (Map<String, Object>) eclassData.get("definition");
                        if (definition != null && definition.containsKey("en-US")) {
                            result.addInfo("EClass Definition for " + inputSemanticId + ": " + definition.get("en-US"));
                        }
                    }
                } catch (Exception ignored) {
                    System.out.println("EClass lookup skipped due to missing certificate");
                }

            }
        }

    }


    public static void validateInputProperty(SubmodelElement inputElement, ComparisonResult result){


        if (!(inputElement instanceof Property)) {
            return;
        }

        Property inputProperty = (Property) inputElement;
        String inputSemanticId = getSemanticIdValue(inputElement);
        String encodedSemanticId = Base64.getEncoder().encodeToString(inputSemanticId.getBytes());

        try {


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(AppConfig.getStaticSubmodelApiBaseUrl()+"/concept-descriptions/"+encodedSemanticId)
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseJson = response.body().string();
                    JsonDeserializer deserializer = new JsonDeserializer();
                    ConceptDescription cd = deserializer.read(responseJson, DefaultConceptDescription.class);

                    String unit = null;
                    String dataType = null;
                    String preferredName_idShort = inputProperty.getIdShort();

                    for (EmbeddedDataSpecification eds : cd.getEmbeddedDataSpecifications()) {
                        if (eds.getDataSpecificationContent() instanceof DataSpecificationIec61360) {
                            DataSpecificationIec61360 spec = (DataSpecificationIec61360) eds.getDataSpecificationContent();
                            unit = spec.getUnit();
                            dataType = spec.getDataType() != null ? spec.getDataType().name() : null;

                            if (spec.getPreferredName() != null && !spec.getPreferredName().isEmpty()) {
                                preferredName_idShort = spec.getPreferredName().get(0).getText();
                            }
                        }
                    }


                    if (unit != null && !unit.isBlank()) {
                        String prompt = String.format("Is '%s' a valid unit for the property '%s'?", unit, preferredName_idShort);
                        result.addLLMPrompt(prompt);
                    }

                } else {
                    System.err.println("Failed with HTTP code: " + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkForSpaces(SubmodelElement inputElement, ComparisonResult result) {

        if (inputElement.getIdShort().contains(" ")) {
            result.addWarning("IDShort contains spaces: " + inputElement.getIdShort() + " || Correction: Remove spaces from the IDShort");
        }

        String semanticIdValue = getSemanticIdValue(inputElement);
        if (semanticIdValue != null && semanticIdValue.contains(" ")) {
            result.addWarning("SemanticId contains spaces: " + semanticIdValue + " || Correction: Remove spaces from the Semantic ID");
        }
    }

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
                String type = qualifier.getType();
                if ("Multiplicity".equalsIgnoreCase(type) ||
                        "Cardinality".equalsIgnoreCase(type) ||
                        "SMT/Cardinality".equalsIgnoreCase(type) ||
                        "SMT/SMT/Cardinality".equalsIgnoreCase(type)) {
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
        return "Not Allowed";
    }


    public static String getSemanticIdValue(SubmodelElement element) {
        if (element.getSemanticId() != null && !element.getSemanticId().getKeys().isEmpty()) {
            String value = element.getSemanticId().getKeys().get(0).getValue();


            if (value.contains("*")) {
                value = value.split("\\*")[0];
            }
            return value.trim();
        }
        return null;
    }
}
