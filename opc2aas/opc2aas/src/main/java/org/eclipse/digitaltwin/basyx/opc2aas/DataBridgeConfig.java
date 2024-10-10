package org.eclipse.digitaltwin.basyx.opc2aas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.hash.Hashing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class DataBridgeConfig {

    private static final Logger logger = LoggerFactory.getLogger(AasBuilder.class);

    /**
     * A class representing an entry in the opcuaconsumer.json file.
     */
    public static class OpcUaConsumerEntry {
        public String uniqueId;
        public String serverUrl;
        public int serverPort;
        public String pathToService;
        public String nodeInformation;
        @JsonInclude(JsonInclude.Include.NON_EMPTY) // This field will be skipped if it is null or an empty string
        public String username;
        @JsonInclude(JsonInclude.Include.NON_EMPTY) // This field will be skipped if it is null or an empty string
        public String password;
        public int requestPublishInterval;

        public OpcUaConsumerEntry() {
            // No-arg constructor for Jackson
        }

        public OpcUaConsumerEntry(String uniqueId, String serverUrl, int serverPort, String pathToService, String nodeInformation, String username, String password, int requestPublishInterval) {
            this.uniqueId = uniqueId;
            this.serverUrl = serverUrl;
            this.serverPort = serverPort;
            this.pathToService = pathToService;
            this.nodeInformation = nodeInformation;
            this.username = username;
            this.password = password;
            this.requestPublishInterval = requestPublishInterval;
        }
    }

    /**
     * A class representing an entry in the routes.json file.
     */
    public static class RouteEntry {
        public String datasource;
        public String[] transformers;
        public String[] datasinks;
        public String trigger;

        public RouteEntry() {
            // No-arg constructor for Jackson
        }

        public RouteEntry(String datasource, String[] transformers, String[] datasinks, String trigger) {
            this.datasource = datasource;
            this.transformers = transformers;
            this.datasinks = datasinks;
            this.trigger = trigger;
        }
    }

    /**
     * A class representing an entry in the aasserver.json file.
     */
    public static class AasServerEntry {
        public String uniqueId;
        public String submodelEndpoint;
        public String idShortPath;
        public String api;

        public AasServerEntry() {
            // No-arg constructor for Jackson
        }

        public AasServerEntry(String uniqueId, String submodelEndpoint, String idShortPath, String api) {
            this.uniqueId = uniqueId;
            this.submodelEndpoint = submodelEndpoint;
            this.idShortPath = idShortPath;
            this.api = api;
        }
    }

    /**
     * Creates the initial configuration files for the DataBridge.
     */
    public static void createConfigFiles() {

        String[] fileNames = {
                "aasserver.json",
                "jsonataExtractValue.json",
                "jsonatatransformer.json",
                "jsonjacksontransformer.json",
                "opcuaconsumer.json",
                "routes.json"
        };

        for (String fileName : fileNames) {
            try {
                File file = new File(fileName);
                FileWriter writer = new FileWriter(file, false); // false to overwrite

                switch (fileName) {
                    case "jsonataExtractValue.json":
                        writer.write("value.value");
                        break;

                    case "jsonjacksontransformer.json":
                        writer.write("[\r\n  {\r\n    \"uniqueId\": \"dataValueToJson\",\r\n    \"operation\": \"marshal\",\r\n    \"jacksonModules\": \"com.fasterxml.jackson.datatype.jsr310.JavaTimeModule\"\r\n  }\r\n]");
                        break;

                    case "jsonatatransformer.json":
                        writer.write("[\r\n  {\r\n    \"uniqueId\": \"jsonataExtractValue\",\r\n    \"queryPath\": \"jsonataExtractValue.json\",\r\n    \"inputType\": \"JsonString\",\r\n    \"outputType\": \"JsonString\"\r\n  }\r\n]");
                        break;

                    default:
                        writer.write("[]");
                        break;
                }

                writer.close();
            } catch (IOException e) {
                logger.error("An error occurred while creating the DataBridge configuration files: ", e);
            }
        }
    }

    /**
     * Writes an entry to the opcuaconsumer.json file.
     *
     * @param endpointUrl The endpoint URL of the OPC UA server.
     * @param username The username to use for the connection.
     * @param password The password to use for the connection.
     * @param node The NodeInfo object of the node to create an entry for.
     * @param submodelIdShort The idShort of the submodel.
     * @param idShortPath The path to the submodelElement.
     */
    public static void writeOpcUaConsumerEntry(String endpointUrl, String username, String password, NodeInfo node, String submodelIdShort, String idShortPath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL); // Ignore null and empty fields globally;

            File file = new File("opcuaconsumer.json");
            // Read existing content
            List<OpcUaConsumerEntry> existingEntries = new ArrayList<>();
            if (file.exists()) {
                String currentContent = Files.readString(file.toPath());
                existingEntries = objectMapper.readValue(currentContent, new TypeReference<List<OpcUaConsumerEntry>>() {});
            }

            // Create a new entry
            String idShort = node.displayName.getText();
            String hashedSourcePath = Hashing.sha256().hashString("source://" + submodelIdShort + "/" + idShortPath + idShort + node.nodeId.toParseableString() + endpointUrl, StandardCharsets.UTF_8).toString(); // hash the source path

            OpcUaConsumerEntry newEntry = new OpcUaConsumerEntry(
                    hashedSourcePath,
                    endpointUrl.split(":")[1].substring(2).equals("localhost") ? "host.docker.internal" : endpointUrl.split(":")[1].substring(2),
                    Integer.parseInt(endpointUrl.split(":")[2].split("/")[0]),
                    "",
                    node.nodeId.toParseableString(),
                    username,
                    password,
                    1000
            );

            // Append the new entry
            existingEntries.add(newEntry);

            // Write the updated content back to the file
            FileWriter writer = new FileWriter(file, false); // false to overwrite
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(existingEntries));
            writer.close();
        } catch (IOException e) {
            logger.error("An error occurred while writing to the opcuaconsumer.json file: ", e);
        }
    }

    /**
     * Writes an entry to the routes.json file.
     *
     * @param node The NodeInfo object of the node to create an entry for.
     * @param submodelIdShort The idShort of the submodel.
     * @param idShortPath The path to the submodelElement.
     */
    public static void writeRouteEntry(NodeInfo node, String submodelIdShort, String idShortPath, String endpointUrl) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read existing content
            File file = new File("routes.json");
            List<RouteEntry> existingEntries = new ArrayList<>();
            if (file.exists()) {
                String currentContent = Files.readString(file.toPath());
                existingEntries = objectMapper.readValue(currentContent, new TypeReference<List<RouteEntry>>() {});
            }

            // Create a new entry
            String idShort = node.displayName.getText();
            String hashedSourcePath = Hashing.sha256().hashString("source://" + submodelIdShort + "/" + idShortPath + idShort + node.nodeId.toParseableString() + endpointUrl, StandardCharsets.UTF_8).toString(); // hash the source path
            String hashedSinkPath = Hashing.sha256().hashString("sink://" + submodelIdShort + "/" + idShortPath + idShort  +  endpointUrl, StandardCharsets.UTF_8).toString(); // hash the sink path
            String[] transformers = {"dataValueToJson", "jsonataExtractValue"};
            String[] datasinks = {hashedSinkPath};

            RouteEntry newEntry = new RouteEntry(
                    hashedSourcePath,
                    transformers,
                    datasinks,
                    "event"
            );

            // Append the new entry
            existingEntries.add(newEntry);

            // Write the updated content back to the file
            FileWriter writer = new FileWriter(file, false); // false to overwrite
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(existingEntries));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes an entry to the aasserver.json file.
     *
     * @param node The NodeInfo object of the node to create an entry for.
     * @param submodelIdShort The idShort of the submodel.
     * @param submodelIdentifier The identifier of the submodel.
     * @param idShortPath The path to the submodelElement.
     * @param submodelRepositoryUrl The URL of the AAS Repository.
     */
    public static void writeAasServerEntry(NodeInfo node, String submodelIdShort, String submodelIdentifier, String idShortPath, String submodelRepositoryUrl, String endpointUrl) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read existing content
            File file = new File("aasserver.json");
            List<AasServerEntry> existingEntries = new ArrayList<>();
            if (file.exists()) {
                String currentContent = Files.readString(file.toPath());
                existingEntries = objectMapper.readValue(currentContent, new TypeReference<List<AasServerEntry>>() {});
            }

            // Create a new entry
            String idShort = node.displayName.getText();
            String hashedSinkPath = Hashing.sha256().hashString("sink://" + submodelIdShort + "/" + idShortPath + idShort  +  endpointUrl, StandardCharsets.UTF_8).toString(); // hash the sink path
            String submodelEndpoint = submodelRepositoryUrl + "/" + base64Encode(submodelIdentifier);
            String api = "DotAAS-V3";

            AasServerEntry newEntry = new AasServerEntry(
                    hashedSinkPath,
                    submodelEndpoint,
                    idShortPath + idShort,
                    api
            );

            // Append the new entry
            existingEntries.add(newEntry);

            // Write the updated content back to the file
            FileWriter writer = new FileWriter(file, false); // false to overwrite
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(existingEntries));
            writer.close();
        } catch (IOException e) {
            logger.error("An error occurred while writing to the aasserver.json file: ", e);
        }
    }

    /**
     * Encodes a string to base64.
     *
     * @param value The string to encode.
     * @return The encoded string.
     */
    public static String base64Encode(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
