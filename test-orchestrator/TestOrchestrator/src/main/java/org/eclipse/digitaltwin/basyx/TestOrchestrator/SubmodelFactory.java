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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SubmodelFactory {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build();


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
                .idShort("AASJSON")
                .inputVariables(Arrays.asList(createStringOperationVariable("aasJSONInput"),
                        createStringOperationVariable("customAASJSONInput")))
                .outputVariables(createStringOperationVariable("Result"))
                .invokable(SubmodelFactory::creation)
                .build();
    }

    public static Operation createTestOrchestratorLink() {
        System.out.println("Creating Invokable Operation for giving API Link in Test Orchestrator");

        return new InvokableOperation.Builder()
                .idShort("AASLink")
                .inputVariables(Arrays.asList(createStringOperationVariable("inputAAS/SMLink"),
                        createStringOperationVariable("customAAS/SMLink")))
                .outputVariables(createStringOperationVariable("Result"))
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

        try {

            Request request = new Request.Builder().url(userProvidedUrl).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to fetch AAS or Submodel: " + response.code());
                }

                String responseJson = response.body().string();
                JsonDeserializer deserializer = new JsonDeserializer();

                try {

                    AssetAdministrationShell aas = deserializer.read(responseJson, DefaultAssetAdministrationShell.class);
                    String baseUrl = userProvidedUrl.split("/shells/")[0];
                    List<Reference> submodelRefs = aas.getSubmodels();

                    if (submodelRefs == null || submodelRefs.isEmpty()) {
                        userInputLink.setValue("No submodels found in the provided AAS.");
                        return new OperationVariable[]{new DefaultOperationVariable.Builder().value(userInputLink).build()};
                    }

                    for (Reference ref : submodelRefs) {
                        String submodelId = ref.getKeys().get(0).getValue();
                        String encodedId = java.util.Base64.getEncoder().encodeToString(submodelId.getBytes(StandardCharsets.UTF_8));


                        String submodelUrl = baseUrl + "/submodels/" + encodedId;
                        System.out.println("The requested url: " + submodelUrl);
                        Submodel submodel = fetchSubmodelFromUrl(submodelUrl);
                        compareWithSchemas(submodel, customInputLink);
                    }

                    userInputLink.setValue("Comparison completed for all submodels in AAS.");
                } catch (Exception e) {

                    try {
                        Submodel userSubmodel = deserializer.read(responseJson, DefaultSubmodel.class);
                        compareWithSchemas(userSubmodel, customInputLink);
                        userInputLink.setValue("Comparison completed for provided Submodel.");
                    } catch (Exception fallbackError) {
                        userInputLink.setValue("Failed to parse input as AAS or Submodel: " + fallbackError.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            userInputLink.setValue("Comparison failed: " + e.getMessage());
            e.printStackTrace();
        }

        return new OperationVariable[]{new DefaultOperationVariable.Builder().value(userInputLink).build()};
    }

    private static void compareWithSchemas(Submodel userSubmodel, Property customInputLink)
            throws IOException, URISyntaxException, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException {

        List<String> schemaLinks;
        if (customInputLink == null || customInputLink.getValue() == null || customInputLink.getValue().isEmpty()) {
            schemaLinks = getPredefinedLinks();
        } else {
            schemaLinks = List.of(String.valueOf(customInputLink.getValue()));
        }

        boolean matchFound = false;

        for (String schemaLink : schemaLinks) {
            Submodel schemaSubmodel = fetchSubmodelFromUrl(schemaLink);
            if (schemaSubmodel.getSemanticId().getKeys().get(0).getValue()
                    .equals(userSubmodel.getSemanticId().getKeys().get(0).getValue())) {
                matchFound = true;
                ComparisonResult result = Comparator.compare(schemaSubmodel, userSubmodel);
                ResultSubmodelFactory.addResultToSubmodel(result, userSubmodel, schemaSubmodel);
                break;
            }
        }

        if (!matchFound) {
            System.out.println("No match found in remote. Checking local resources...");
            List<String> localSchemas = getAllSchemaFilesFromResources();
            matchFound = compareSchemasFromFiles(localSchemas, userSubmodel);
        }

        if (!matchFound) {
            System.out.println("Checking external schema folder...");
            List<String> externalSchemas = getAllSchemaFilesFromExternalFolder("external-schemas/");
            matchFound = compareSchemasFromFiles(externalSchemas, userSubmodel);
        }

        if (!matchFound) {
            ResultSubmodelFactory.addNoMatchResultToSubmodel(userSubmodel);
        }
    }


    private static List<String> getPredefinedLinks() {
        return Arrays.asList(
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQ29udGFjdEluZm9ybWF0aW9uLzEvMA==", // ContactInformation v.1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvU29mdHdhcmVOYW1lcGxhdGUvMS8w", // SoftwareNameplate v1.0
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvSGFuZG92ZXJEb2N1bWVudGF0aW9uLzEvMA==",  // HandoverDocumentation/1/0 v1.2
              //  "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9aVkVJL1RlY2huaWNhbERhdGEvU3VibW9kZWwvMS8y",  // TechnicalData/Submodel/1/2 v1.2
              //  "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvVGltZVNlcmllcy8xLzE=",  // TimeSeries/1/1 v1.1
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQ2FyYm9uRm9vdHByaW50LzAvOQ==",  // CarbonFootprint/0/9 v0.9
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvUmVsaWFiaWxpdHkvMS8w",  // Reliability/1/0 v1.0
                "http://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvRnVuY3Rpb25hbFNhZmV0eS8xLzA=",  // FunctionalSafety/1/0 v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvRGlnaXRhbE5hbWVwbGF0ZS8zLzA=",  // DigitalNameplate/3/0 v3.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvSGllcmFyY2hpY2FsU3RydWN0dXJlc0JvTS8xLzE=",  // HierarchicalStructures/1/1/Submodel v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvU2VydmljZVJlcXVlc3ROb3RpZmljYXRpb24vMS8w", // ServiceRequestNotification/1/0 v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQXNzZXRJbnRlcmZhY2VzRGVzY3JpcHRpb24=", // AssetInterfacesDescription/1/0/Submodel v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL0JhY2tlbmRTcGVjaWZpY01hdGVyaWFsSW5mb3JtYXRpb24vMS8wLw==", // BackendSpecificMaterial v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvd29ya3N0YXRpb25NYXRjaGluZy8xLzA=", // WorkstationWorkerMatchingData v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvRGF0YVJldGVudGlvblBvbGljaWVzLzEvMA==", // DataRetentionPolicies v1.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvREVYUEkvMS8w", // DEXPI
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvTW9kdWxlVHlwZVBhY2thZ2UvMS8w", // ModuleTypePackage
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvVGltZVNlcmllc1dpdGhPcGVyYXRpb25zLzEvMQ==", // TimeSeriesWithOperations
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvU2l6aW5nb2ZQb3dlckRyaXZlVHJhaW5zLzEvMA==", // PowerDriveTrainSizing
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvV2lyZWxlc3NDb21tdW5pY2F0aW9uLzEvMA==", // WirelessCommunication
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQXNzZXRJbnRlcmZhY2VzTWFwcGluZ0NvbmZpZ3VyYXRpb24vMS8wLw==", // AIMC
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvRGF0YU1vZGVsZm9yQXNzZXRMb2NhdGlvbi8xLzA=", // Asset Location
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvUHJvdmlzaW9uT2YzRE1vZGVscy8xLzA=", // Model3D
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvUEFNU3BlY2lmaWNhdGlvblNoZWV0LzEvMA==", // PAMSpecificationSheet
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQ29udHJvbENvbXBvbmVudC9JbnN0YW5jZS8yLzA=", // ControlComponentInstance v2.0
                "https://smt-repo.admin-shell-io.com/api/v3.0/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQ29udHJvbENvbXBvbmVudFR5cGUvMi8w" // ControlComponentType v2.0
        );
    }

    private static Submodel fetchSubmodelFromUrl(String url) throws IOException, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException {
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


                        ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, userSubmodel);
                        ResultSubmodelFactory.addResultToSubmodel(comparisonResult, userSubmodel, schemaSubmodel);

                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during schema comparison from files: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static OperationVariable[] creation(OperationVariable[] inputs) {

        Property in = (Property) inputs[0].getValue();
        Property customInput = (Property) inputs[1].getValue();
        String jsonString = String.valueOf(in.getValue());


        List<ComparisonResult> allComparisonResults = new ArrayList<>();
        try {
            System.out.println("Deserializing Input JSON");
            Environment inputEnv = Deserializer.deserializejsonFile(jsonString);
            System.out.println("Completed Deserializing Input JSON");

            for (AssetAdministrationShell inputAAS : inputEnv.getAssetAdministrationShells()) {
                System.out.println("Processing AAS: " + inputAAS.getIdShort());


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

                    System.out.println("Using the standardized schema files from IDTA for Comparison");
                    schemaFiles = getAllSchemaFilesFromResources();
                } else {

                    System.out.println("Using custom schema file from customAASFile");
                    schemaFiles = List.of(String.valueOf(customInput.getValue()));
                }
                boolean matchFound = false;

                for (String schemaFileContent : schemaFiles) {

                    Environment schemaEnv = Deserializer.deserializejsonFile(schemaFileContent);

                    List<Submodel> schemaSubmodels = schemaEnv.getSubmodels();
                    for (Submodel schemaSubmodel : schemaSubmodels) {
                        String schemaSemanticId = schemaSubmodel.getSemanticId().getKeys().get(0).getValue();

                        if (inputSemanticId.equals(schemaSemanticId)) {
                            matchFound = true;
                            System.out.println("Matching submodel found for semantic ID: " + inputSemanticId + " and ID short:" + inputSubmodelIdShort);

                            ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, inputSubmodel);
                            allComparisonResults.add(comparisonResult);
                            ResultSubmodelFactory.addResultToSubmodel(comparisonResult, inputSubmodel, schemaSubmodel);
                            break;
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


        StringBuilder resultBuilder = new StringBuilder();
        for (ComparisonResult result : allComparisonResults) {
            resultBuilder.append(result.toString()).append("\n");
        }
        String finalResultString = resultBuilder.toString();

        in.setValue(finalResultString);
        in.setIdShort("result");
        return new OperationVariable[]{createOperationVariable(in)};
    }

    private static List<String> getAllSchemaFilesFromResources() throws IOException, URISyntaxException {

        List<String> schemaFilesContent = new ArrayList<>();


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

    public static void processReceivedSubmodel(Submodel submodel) throws IOException, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException, URISyntaxException {
        System.out.println("Processing Submodel: " + submodel.getIdShort());
        List<String> schemaLinks = getPredefinedLinks();

        boolean matchFound = false;
        for (String schemaLink : schemaLinks) {
            Submodel schemaSubmodel = fetchSubmodelFromUrl(schemaLink);

            if (submodel.getSemanticId().getKeys().get(0).getValue().equals(schemaSubmodel.getSemanticId().getKeys().get(0).getValue())) {
                matchFound = true;
                System.out.println("Matching submodel found: " + schemaSubmodel.getIdShort());


                ComparisonResult comparisonResult = Comparator.compare(schemaSubmodel, submodel);
                ResultSubmodelFactory.addResultToSubmodel(comparisonResult, submodel, schemaSubmodel);

                break;
            }
        }
        if (!matchFound) {

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
