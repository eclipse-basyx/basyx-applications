package org.eclipse.digitaltwin.basyx.TestOrchestrator;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.utility.ComparisonResult;
import org.eclipse.digitaltwin.basyx.core.exceptions.RepositoryRegistryLinkException;
import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ResultSubmodelFactory {

    private static final String TEST_RESULTS_SUBMODEL_ID = "TestResults";
    private static final String UNSUCCESSFUL_RESULTS_SUBMODEL_ID = "UnsuccessfulTestResults";

    private static SubmodelRepository submodelRepository;

    public ResultSubmodelFactory(SubmodelRepository submodelRepository) {
        this.submodelRepository = submodelRepository;
        ensureSubmodelExists(TEST_RESULTS_SUBMODEL_ID);
        ensureSubmodelExists(UNSUCCESSFUL_RESULTS_SUBMODEL_ID);
    }

    private void ensureSubmodelExists(String submodelId) {
        try {
            submodelRepository.getSubmodel(submodelId);
            System.out.println(submodelId + " submodel already exists.");
        } catch (ElementDoesNotExistException e) {
            System.out.println(submodelId + " submodel not found. Creating new submodel...");

            try{
                submodelRepository.createSubmodel(createAndStoreSubmodel(submodelId));
            }
            catch (RepositoryRegistryLinkException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException apiEx) {
                    if (apiEx.getCode() == 409) {
                        System.out.println(submodelId + " already registered in registry. Skipping registration.");
                    } else {
                        throw ex; // not a duplicate, rethrow
                    }
                } else {
                    throw ex; // unexpected error
                }
            }
        }
    }

    public static Submodel createAndStoreSubmodel(String submodelId) {
        String descriptionText = submodelId.equals(TEST_RESULTS_SUBMODEL_ID) ? "Submodel containing test results" : "Submodel containing unsuccessful test results";
        List<LangStringTextType> description = List.of(
                new DefaultLangStringTextType.Builder().language("en-US").text(descriptionText).build()
        );

        return new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort(submodelId)
                .description(description)
                .kind(ModellingKind.INSTANCE)
                .submodelElements(new ArrayList<>())
                .build();
    }


    /**
     * Adds a new test result to the submodel as a SubmodelElementCollection.
     */
    public static void addResultToSubmodel(ComparisonResult comparisonResult, Submodel comparedSubmodel, Submodel schemaSubmodel) {
        String timestamp = Instant.now().toString().replace(":", "-").replace(".", "-");

        SubmodelElementCollection resultCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("Result_" + timestamp)
                .value(Arrays.asList(
                        createProperty("ComparedSubmodelId", comparedSubmodel.getId()),
                        createProperty("ComparedSubmodelIdShort", comparedSubmodel.getIdShort()),
                        createProperty("SemanticId", getSemanticId(comparedSubmodel)),
                        createProperty("Errors", String.join(", ", comparisonResult.getErrors())),
                        createProperty("Warnings", String.join(", ", comparisonResult.getWarnings())),
                        createProperty("Differences", String.join(", ", comparisonResult.getDifferences())),
                        createProperty("Infos", String.join(", ", comparisonResult.getInfos()))
                ))
                .build();

        try {
            submodelRepository.createSubmodelElement(TEST_RESULTS_SUBMODEL_ID, resultCollection);
            System.out.println("Added test result for submodel: " + comparedSubmodel.getIdShort());
        } catch (ElementDoesNotExistException e) {
            System.err.println("Failed to add result element: " + e.getMessage());
        }
    }

    /**
     * Adds a result indicating no matching schema was found.
     */
    public static void addNoMatchResultToSubmodel(Submodel comparedSubmodel) {
        String timestamp = Instant.now().toString().replace(":", "-").replace(".", "-");

        SubmodelElementCollection resultCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("Result_" + timestamp)
                .value(Arrays.asList(
                        createProperty("ComparedSubmodelId", comparedSubmodel.getId()),
                        createProperty("ComparedSubmodelIdShort", comparedSubmodel.getIdShort()),
                        createProperty("SemanticId", getSemanticId(comparedSubmodel)),
                        createProperty("Errors", "The submodel does not match any standardized submodel by IDTA."),
                        createProperty("Warnings", "Please check the Semantic ID of the submodel."),
                        createProperty("Differences", "No differences to show."),
                        createProperty("Infos", "No matching submodel found.")
                ))
                .build();

        try {
            submodelRepository.createSubmodelElement(TEST_RESULTS_SUBMODEL_ID, resultCollection);
            System.out.println("Added 'no match found' result for submodel: " + comparedSubmodel.getIdShort());
        } catch (ElementDoesNotExistException e) {
            System.err.println("Failed to add 'no match found' result: " + e.getMessage());
        }
    }

    public static void addUnsuccessfulResultToSubmodel(Submodel comparedSubmodel) {
        String timestamp = Instant.now().toString().replace(":", "-").replace(".", "-");

        SubmodelElementCollection resultCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("Result_" + timestamp)
                .value(Arrays.asList(
                        createProperty("ComparedSubmodelId", comparedSubmodel.getId()),
                        createProperty("ComparedSubmodelIdShort", comparedSubmodel.getIdShort()),
                        createProperty("SemanticId", "None"),
                        createProperty("Errors", "No Semantic ID found. No test was conducted."),
                        createProperty("Warnings", "Ensure that the submodel contains a valid Semantic ID."),
                        createProperty("Differences", "N/A"),
                        createProperty("Infos", "This submodel was skipped due to missing Semantic ID.")
                ))
                .build();

        try {
            submodelRepository.createSubmodelElement(UNSUCCESSFUL_RESULTS_SUBMODEL_ID, resultCollection);
            System.out.println("Added unsuccessful test result for submodel: " + comparedSubmodel.getIdShort());
        } catch (ElementDoesNotExistException e) {
            System.err.println("Failed to add unsuccessful test result: " + e.getMessage());
        }
    }

    private static Property createProperty(String idShort, String value) {
        return new DefaultProperty.Builder()
                .idShort(idShort)
                .value(value != null ? value : "None")
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private static String getSemanticId(Submodel submodel) {
        return submodel.getSemanticId() != null && !submodel.getSemanticId().getKeys().isEmpty()
                ? submodel.getSemanticId().getKeys().get(0).getValue()
                : "Unknown";
    }
}
