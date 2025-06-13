package org.eclipse.digitaltwin.basyx.TestOrchestrator;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.InvokableOperation;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.utility.Comparator;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.utility.ComparisonResult;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.utility.Deserializer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.io.File;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SubmodelFactory {

    private static OkHttpClient client = new OkHttpClient();

    public static Submodel creationSubmodel() {
        List<LangStringTextType> description = new ArrayList<>();
        description.add(new DefaultLangStringTextType.Builder().language("de-DE")
                .text("TestOrchestrator")
                .build());
        List<LangStringNameType> displayName = new ArrayList<>();
        displayName.add(new DefaultLangStringNameType.Builder().language("de-DE")
                .text("TestOrchestrator")
                .build());
        Operation creation = createTestOrchestrator();
        Operation creationLink = createTestOrchestratorLink();
        List<SubmodelElement> smeList = Arrays.asList(creation, creationLink);

        Submodel submodel = new DefaultSubmodel.Builder().category("TestCategory")
                .description(description)
                .displayName(displayName)
                .id("TestOrchestrator")
                .idShort("TestOrchestrator")
                .kind(ModellingKind.INSTANCE)
                .submodelElements(smeList)
                .build();

        return submodel;
    }

    public static Operation createTestOrchestrator() {
        System.out.println("Creating Invokable Operation for uploading in Test Orchestrator");

        return new InvokableOperation.Builder()
                .idShort("AASUpload")
                .inputVariables(Arrays.asList(createStringOperationVariable("aasFile"),
                        createStringOperationVariable("customAASFile")))
                .outputVariables(createStringOperationVariable("result"))
                .invokable(SubmodelFactory::creation)
                .build();
    }

    public static Operation createTestOrchestratorLink() {
        System.out.println("Creating Invokable Operation for giving API Link in Test Orchestrator");

        return new InvokableOperation.Builder()
                .idShort("AASLink")
                .inputVariables(Arrays.asList(createStringOperationVariable("inputAASLink"),
                        createStringOperationVariable("customAASLink")))
                .outputVariables(createStringOperationVariable("result"))
                .invokable(SubmodelFactory::creationLink)
                .build();
    }

    private static OperationVariable createOperationVariable(Property val) {
        return new DefaultOperationVariable.Builder().value(val).build();
    }

    private static DefaultOperationVariable createStringOperationVariable(String idShort) {
        return new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort(idShort).valueType(DataTypeDefXsd.STRING).build()).build();
    }

    public static OperationVariable[] creationLink(OperationVariable[] inputs) {
        Property userInputLink = (Property) inputs[0].getValue();
        Property customInputLink = (Property) inputs[1].getValue();

        String userProvidedUrl = String.valueOf(userInputLink.getValue());

        List<String> predefinedLinks = getPredefinedLinks(); // Predefined links from IDTA

        try {
            // Fetch user-provided submodel
            Submodel userSubmodel = fetchSubmodelFromUrl(userProvidedUrl);

            List<String> schemaLinks;

            // Determine whether to use predefined links or the custom input
            if (customInputLink == null || customInputLink.getValue() == null || customInputLink.getValue().isEmpty()) {
                System.out.println("Using predefined schema links for comparison.");
                schemaLinks = predefinedLinks;
            } else {
                System.out.println("Using custom schema link for comparison.");
                schemaLinks = List.of(String.valueOf(customInputLink.getValue()));
            }

            boolean matchFound = false;

            for (String schemaLink : schemaLinks) {
                System.out.println(schemaLink);
                Submodel schemaSubmodel = fetchSubmodelFromUrl(schemaLink);
                System.out.println(userSubmodel.getSemanticId());
                // Check if the semantic ID matches
                if (userSubmodel.getSemanticId().getKeys().get(0).getValue().equals(schemaSubmodel.getSemanticId().getKeys().get(0).getValue())) {
                    matchFound = true;
                    System.out.println("Matching submodel found: " + schemaSubmodel.getIdShort());

                    // Perform comparison
                    ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, userSubmodel);
                    ResultSubmodelFactory.addResultToSubmodel(comparisonResult, userSubmodel, schemaSubmodel);

                    // Log or set the result
                    System.out.println("Comparison Result: " + comparisonResult);
                    userInputLink.setValue("Comparison Result: " + comparisonResult);
                    userInputLink.setIdShort("Result");
                    break; // Exit loop once a match is found and compared
                }
            }
            if (!matchFound) {
                // If no match, fallback to local resource schemas
                System.out.println("Falling back to local schemas in resources...");
                List<String> localSchemas = getAllSchemaFilesFromResources();
                matchFound = compareSchemasFromFiles(localSchemas, userSubmodel);
            }

            if (!matchFound) {
                System.out.println("No matching schema file found for user-provided submodel.");
                ResultSubmodelFactory.addNoMatchResultToSubmodel(userSubmodel);
                userInputLink.setValue("No matching schema found for the provided submodel.");
            }

        } catch (IOException | DeserializationException |
                 org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException e) {
            userInputLink.setValue("Comparison failed: " + e.getMessage());
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new OperationVariable[]{new DefaultOperationVariable.Builder().value(userInputLink).build()};
    }

    private static List<String> getPredefinedLinks() {
        return Arrays.asList(
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQ29udGFjdEluZm9ybWF0aW9uLzEvMA==", // ContactInformation v.1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvU29mdHdhcmVOYW1lcGxhdGUvMS8w", // SoftwareNameplate v1.0
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvSGFuZG92ZXJEb2N1bWVudGF0aW9uLzEvMA==",  // HandoverDocumentation/1/0 v1.2
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9aVkVJL1RlY2huaWNhbERhdGEvU3VibW9kZWwvMS8y",  // TechnicalData/Submodel/1/2 v1.2
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvVGltZVNlcmllcy8xLzE=",  // TimeSeries/1/1 v1.1
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQ2FyYm9uRm9vdHByaW50LzAvOQ==",  // CarbonFootprint/0/9 v0.9
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvUmVsaWFiaWxpdHkvMS8w",  // Reliability/1/0 v1.0
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvRnVuY3Rpb25hbFNhZmV0eS8xLzA=",  // FunctionalSafety/1/0 v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvRGlnaXRhbE5hbWVwbGF0ZS8zLzA=",  // DigitalNameplate/3/0 v3.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvSGllcmFyY2hpY2FsU3RydWN0dXJlc0JvTS8xLzE=",  // HierarchicalStructures/1/1/Submodel v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvU2VydmljZVJlcXVlc3ROb3RpZmljYXRpb24vMS8w", // ServiceRequestNotification/1/0 v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQXNzZXRJbnRlcmZhY2VzRGVzY3JpcHRpb24=" // AssetInterfacesDescription/1/0/Submodel v1.0
        );
    }

    private static Submodel fetchSubmodelFromUrl(String url) throws IOException, DeserializationException, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch submodel: " + response.code());
            }
            String submodelJson = response.body().string();
            JsonDeserializer deserializer = new JsonDeserializer();
            return deserializer.read(submodelJson, DefaultSubmodel.class);
        }
    }
    private static boolean compareSchemasFromFiles(List<String> schemaFiles, Submodel userSubmodel) {
        try {
            for (String schemaFileContent : schemaFiles) {
                Environment schemaEnv = Deserializer.deserializejsonFile(schemaFileContent);
                for (Submodel schemaSubmodel : schemaEnv.getSubmodels()) {
                    String schemaSemanticId = schemaSubmodel.getSemanticId().getKeys().get(0).getValue();
                    String userSemanticId = userSubmodel.getSemanticId().getKeys().get(0).getValue();

                    if (userSemanticId.equals(schemaSemanticId)) {
                        System.out.println("Matching local schema found for semantic ID: " + userSemanticId);

                        // Compare submodels
                        ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, userSubmodel);
                        ResultSubmodelFactory.addResultToSubmodel(comparisonResult, userSubmodel, schemaSubmodel);

                        return true; // Match found
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during schema comparison from files: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // No match found
    }

    public static OperationVariable[] creation(OperationVariable[] inputs) {

        Property in = (Property) inputs[0].getValue();
        Property customInput = (Property) inputs[1].getValue();
        String jsonString = String.valueOf(in.getValue());

        // Create a list to store all comparison results
        List<ComparisonResult> allComparisonResults = new ArrayList<>();
        try {
            System.out.println("Deserializing Input JSON");
            Environment inputEnv = Deserializer.deserializejsonFile(jsonString);
            System.out.println("Completed Deserializing Input JSON");

            for (AssetAdministrationShell inputAAS : inputEnv.getAssetAdministrationShells()) {
                System.out.println("Processing AAS: " + inputAAS.getIdShort());

                // Filter submodels that belong to the current AAS
                List<Reference> submodelReferences = inputAAS.getSubmodels();
                List<Submodel> associatedSubmodels = inputEnv.getSubmodels().stream()
                        .filter(submodel -> submodelReferences.stream()
                                .anyMatch(ref -> ref.getKeys().get(0).getValue().equals(submodel.getId())))
                        .collect(Collectors.toList());

                System.out.println("Found " + associatedSubmodels.size() + " submodels for AAS: " + inputAAS.getIdShort());

            for (Submodel inputSubmodel : associatedSubmodels) {

                String inputSemanticId = inputSubmodel.getSemanticId().getKeys().get(0).getValue();
                String inputSubmodelIdShort = inputSubmodel.getIdShort();
                List<String> schemaFiles;

                if (customInput == null || customInput.getValue() == null || customInput.getValue().isEmpty()) {
                    // Handle the case where customInput is null or its value is null or empty
                    System.out.println("Using the standardized schema files from IDTA for Comparison");
                    schemaFiles = getAllSchemaFilesFromResources();
                } else {
                    // customInput is not null, and customInput.getValue() is neither null nor empty
                    System.out.println("Using custom schema file from customAASFile");
                    schemaFiles = List.of(String.valueOf(customInput.getValue()));
                }
                boolean matchFound = false;

                for (String schemaFileContent : schemaFiles) {

                    Environment schemaEnv = Deserializer.deserializejsonFile(schemaFileContent);

                    // Get the semantic ID of the schema's submodel
                    List<Submodel> schemaSubmodels = schemaEnv.getSubmodels();
                    for (Submodel schemaSubmodel : schemaSubmodels) {
                        String schemaSemanticId = schemaSubmodel.getSemanticId().getKeys().get(0).getValue();

                        // Step 4: If the semantic ID matches, compare the submodels
                        if (inputSemanticId.equals(schemaSemanticId)) {
                            matchFound = true;
                            System.out.println("Matching submodel found for semantic ID: " + inputSemanticId + " and ID short:" + inputSubmodelIdShort);
                            // Perform the comparison between the input submodel and schema submodel
                            ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, inputSubmodel);
                            allComparisonResults.add(comparisonResult); // Add the result to the list
                            ResultSubmodelFactory.addResultToSubmodel(comparisonResult, inputSubmodel, schemaSubmodel);
                            break;  // Exit loop if a match is found
                        }
                    }
                    if (matchFound) {
                        break;
                    }
                }
                if (!matchFound) {
                    System.out.println("No matching schema file found for semantic ID: " + inputSemanticId + " and ID Short: " + inputSubmodelIdShort);
                    ResultSubmodelFactory.addNoMatchResultToSubmodel(inputSubmodel);
                }
            }
        }
            System.out.println("All comparsion results: " + allComparisonResults);



        } catch (DeserializationException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        // Convert allComparisonResults to a single string representation
        StringBuilder resultBuilder = new StringBuilder();
        for (ComparisonResult result : allComparisonResults) {
            resultBuilder.append(result.toString()).append("\n");
        }
        String finalResultString = resultBuilder.toString();

        // Set the result value
        in.setValue(finalResultString);
        in.setIdShort("result");
        return new OperationVariable[]{createOperationVariable(in)};
    }

    private static List<String> getAllSchemaFilesFromResources() throws IOException, URISyntaxException {

        List<String> schemaFilesContent = new ArrayList<>();

        // Use PathMatchingResourcePatternResolver to get all resources inside the "schema" folder
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:schema/*.json");

        for (Resource resource : resources) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String schemaContent = reader.lines().collect(Collectors.joining("\n"));
                schemaFilesContent.add(schemaContent);
            } catch (IOException e) {
                System.err.println("Error reading schema file: " + resource.getFilename());
            }
        }
        return schemaFilesContent;
    }

    public static void processReceivedSubmodel(Submodel submodel) throws IOException, DeserializationException, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException, URISyntaxException {
        System.out.println("Processing Submodel: " + submodel.getIdShort());
        List<String> schemaLinks = getPredefinedLinks();

        boolean matchFound = false;
        for (String schemaLink : schemaLinks) {
            Submodel schemaSubmodel = fetchSubmodelFromUrl(schemaLink);
            // Check if the semantic ID matches
            if (submodel.getSemanticId().getKeys().get(0).getValue().equals(schemaSubmodel.getSemanticId().getKeys().get(0).getValue())) {
                matchFound = true;
                System.out.println("Matching submodel found: " + schemaSubmodel.getIdShort());

                // Perform comparison
                ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, submodel);
                ResultSubmodelFactory.addResultToSubmodel(comparisonResult, submodel, schemaSubmodel);

                break; // Exit loop once a match is found and compared
            }
        }
        if (!matchFound) {
            // If no match, fallback to local resource schemas
            System.out.println("Falling back to local schemas in resources...");
            List<String> localSchemas = getAllSchemaFilesFromResources();
            matchFound = compareSchemasFromFiles(localSchemas, submodel);
        }

        if (!matchFound) {
            System.out.println("Checking external folder for schemas...");
            String folderPath = System.getenv().getOrDefault("EXTERNAL_SCHEMA_PATH", "external-schemas/");
            List<String> externalSchemas = getAllSchemaFilesFromExternalFolder(folderPath);
            matchFound = compareSchemasFromFiles(externalSchemas, submodel);
        }


        if (!matchFound) {
            System.out.println("No matching schema file found for user-provided submodel.");
            ResultSubmodelFactory.addNoMatchResultToSubmodel(submodel);
        }
    }

    private static List<String> getAllSchemaFilesFromExternalFolder(String folderPath) {
        List<String> schemaFilesContent = new ArrayList<>();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("External schema folder not found: " + folderPath);
            return schemaFilesContent;
        }

        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                try {
                    String content = Files.readString(file.toPath());
                    schemaFilesContent.add(content);
                } catch (IOException e) {
                    System.err.println("Error reading external schema file: " + file.getName());
                }
            }
        }
        return schemaFilesContent;
    }

}
