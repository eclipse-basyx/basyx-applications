package org.eclipse.digitaltwin.basyx.opc2aas;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.Optional;
import java.util.UUID;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

public class OpcUtils {

    private static final Logger logger = LoggerFactory.getLogger(OpcUtils.class);

    /**
     * Creates and returns a connection to an OPC UA server.
     *
     * @param endpointUrl The URL of the OPC UA server endpoint.
     * @param username    The username to use for the connection.
     * @param password    The password to use for the connection.
     * @return An instance of OpcUaClient connected to the server.
     * @throws UaException If the connection cannot be created.
     */
    public static OpcUaClient createClientConnection(String endpointUrl, String username, String password) throws UaException {
        try {
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();

            // Sort or filter endpoints if needed, e.g., based on security level

            IdentityProvider identityProvider = (username == null || username.isEmpty() || password == null || password.isEmpty())
                    ? new AnonymousProvider()
                    : new UsernameProvider(username, password);

            UaException connectionException = null;

            for (EndpointDescription endpoint : endpoints) {
                try {
                    OpcUaClientConfig config = OpcUaClientConfig.builder()
                            .setEndpoint(endpoint)
                            .setIdentityProvider(identityProvider)
                            .setRequestTimeout(UInteger.valueOf(5000))
                            .build();

                    OpcUaClient client = OpcUaClient.create(config);
                    client.connect().get();
                    logger.info("Connected to OPC UA server at endpoint: {}", endpoint.getEndpointUrl());
                    return client; // Successful connection
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                } catch (ExecutionException e) {
                    logger.error("Connection to OPC UA server at endpoint {} failed: {}", endpoint.getEndpointUrl(), e.getMessage(), e);
                    connectionException = new UaException(StatusCodes.Bad_CommunicationError, "Error connecting to endpoint: " + endpoint.getEndpointUrl(), e.getCause());
                    // Continue trying other endpoints
                }
            }

            // If all endpoints failed, throw an exception
            throw connectionException != null ? connectionException :
                    new UaException(StatusCodes.Bad_ConfigurationError, "No endpoints could be connected to.");

        } catch (InterruptedException | ExecutionException e) {
            throw new UaException(StatusCodes.Bad_InternalError, "Error obtaining server endpoints.", e);
        }
    }

    /**
     * Returns the server application URI of an OPC UA server.
     *
     * @param client The OpcUaClient instance to use for reading.
     * @return The server application URI.
     */
    public static String getServerApplicationUri(OpcUaClient client) {
        try {
            List<EndpointDescription> endpoints = DiscoveryClient
                    .getEndpoints(client.getConfig().getEndpoint().getEndpointUrl()).get();

            if (!endpoints.isEmpty()) {
                return endpoints.get(0).getServer().getApplicationUri();
            } else {
                logger.info("No endpoints were found for the server.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Proper handling of InterruptedException
            logger.error("Thread was interrupted during endpoint discovery: ", e);
        } catch (Exception e) {
            logger.error("Error occurred during endpoint discovery: ", e);
        }
        return null;
    }

    /**
     * Reads an entire subtree from an OPC UA server.
     *
     * @param client    The OpcUaClient instance to use for reading.
     * @param startNode The NodeId of the node to start reading from.
     * @return A NodeInfo object representing the subtree.
     */
    public static NodeInfo readEntireSubtree(OpcUaClient client, NodeId startNode) throws Exception {
        NodeInfo nodeInfo = readNode(client, startNode);
        List<NodeInfo> children = new ArrayList<>();

        try {
            BrowseDescription browse = new BrowseDescription(
                    startNode,
                    BrowseDirection.Forward,
                    Identifiers.HierarchicalReferences,
                    true,
                    null,
                    null);

            BrowseResult browseResult = client.browse(browse).get();
            if (browseResult.getReferences() != null) {
                for (ReferenceDescription reference : browseResult.getReferences()) {
                    NamespaceTable namespaceTable = client.getNamespaceTable();
                    NodeId nodeId = reference.getNodeId().toNodeId(namespaceTable).orElse(null);
                    NodeInfo child = readEntireSubtree(client, nodeId); // Recursive call
                    children.add(child);
                }
            }
        } catch (InterruptedException | UaException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Error during browsing: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new Exception("Error during execution: " + e.getCause().getMessage(), e.getCause());
        }

        return new NodeInfo(startNode, nodeInfo.nodeClass, nodeInfo.browseName, nodeInfo.displayName, nodeInfo.description, nodeInfo.value, nodeInfo.dataType, children.isEmpty() ? null : children);
    }

    /**
     * Reads a single node from an OPC UA server.
     *
     * @param client The OpcUaClient instance to use for reading.
     * @param nodeId The NodeId of the node to read.
     * @return A NodeInfo object representing the node.
     */
    public static NodeInfo readNode(OpcUaClient client, NodeId nodeId) throws Exception {
        UInteger[] attributesToRead = new UInteger[] {
                AttributeId.NodeClass.uid(),
                AttributeId.BrowseName.uid(),
                AttributeId.DisplayName.uid(),
                AttributeId.Description.uid(),
                AttributeId.Value.uid(),
                AttributeId.DataType.uid()
        };
        ReadValueId[] readValueIds = new ReadValueId[attributesToRead.length];

        for (int i = 0; i < attributesToRead.length; i++) {
            readValueIds[i] = new ReadValueId(nodeId, attributesToRead[i], null, null);
        }

        CompletableFuture<ReadResponse> future = client.read(0.0, TimestampsToReturn.Neither, Arrays.asList(readValueIds));
        ReadResponse response = future.get();
        DataValue[] results = response.getResults();

        String nodeClass = null;
        QualifiedName browseName = null;
        LocalizedText displayName = null;
        Object value = null;
        NodeId dataTypeNodeId = null;
        String dataType = null;
        LocalizedText description = null;

        // Handle the results:
        // NodeClass
        assert results[0].getStatusCode() != null;
        if (results[0].getStatusCode().isGood()) {
            Object nodeClassResult = results[0].getValue().getValue();
            if (nodeClassResult instanceof Integer nodeClassInt) {
                NodeClass nodeClassEnum = NodeClass.from(nodeClassInt);
                if (nodeClassEnum != null) {
                    nodeClass = nodeClassEnum.name();
                }
            }
        }
        // BrowseName
        assert results[1].getStatusCode() != null;
        if (results[1].getStatusCode().isGood()) {
            browseName = (QualifiedName) results[1].getValue().getValue();
        }
        // DisplayName
        assert results[2].getStatusCode() != null;
        if (results[2].getStatusCode().isGood()) {
            Object displayNameResult = results[2].getValue().getValue();
            displayName = (LocalizedText) displayNameResult;
        }
        // Description
        assert results[3].getStatusCode() != null;
        if (results[3].getStatusCode().isGood()) { // Handle Description result
            Object descriptionResult = results[3].getValue().getValue();
            description = (LocalizedText) descriptionResult;
        }
        // Value
        assert results[4].getStatusCode() != null;
        if (results[4].getStatusCode().isGood()) {
            value = results[4].getValue().getValue();
        }
        // DataType
        assert results[5].getStatusCode() != null;
        if (results[5].getStatusCode().isGood()) {
            dataTypeNodeId = (NodeId) results[5].getValue().getValue();
            // get the human-readable DataType
            dataType = getDataTypeDisplayName(client, dataTypeNodeId);
        }

        // resolve complex data types
        if (dataType != null && value instanceof ExtensionObject) {
            value = resolveComplexDataType(client, value, dataType);
        }

        // resolve enumerations
        if (dataType != null && value instanceof Integer && !"Int64".equals(dataType)) {
            value = resolveEnumerationValue(client, value, dataTypeNodeId);
        }

        return new NodeInfo(nodeId, nodeClass, browseName, displayName, description, value, dataType, null);
    }

    /**
     * Reads the display name of a DataType node from an OPC UA server.
     *
     * @param client The OpcUaClient instance to use for reading.
     * @param dataTypeNodeId The NodeId of the node to read.
     * @return The display name of the node.
     * @throws ExecutionException   If the read operation fails.
     * @throws InterruptedException If the operation is interrupted.
     */
    public static String getDataTypeDisplayName(OpcUaClient client, NodeId dataTypeNodeId) throws ExecutionException, InterruptedException {
        CompletableFuture<ReadResponse> futureDataType = client.read(
                0.0,
                TimestampsToReturn.Neither,
                List.of(new ReadValueId(dataTypeNodeId, AttributeId.DisplayName.uid(), null, null))
        );

        ReadResponse responseDataType = futureDataType.get();
        DataValue[] dataTypeResults = responseDataType.getResults();

        assert dataTypeResults[0].getStatusCode() != null;
        if (dataTypeResults[0].getStatusCode().isGood()) {
            LocalizedText dataTypeDisplayName = (LocalizedText) dataTypeResults[0].getValue().getValue();
            return dataTypeDisplayName.getText();
        }

        return null;
    }

    /**
     * Resolves a complex data type.
     *
     * @param client   The OpcUaClient instance to use for reading.
     * @param value    The value to resolve.
     * @param dataType The data type of the value.
     * @return The resolved value.
     */
    public static Object resolveComplexDataType(OpcUaClient client, Object value, String dataType) {
        ExtensionObject xo = (ExtensionObject) value;
        // Try to decode using the static serialization context
        try {
            return xo.decode(client.getStaticSerializationContext());
        } catch (UaSerializationException e) {
            System.err.println("Failed to decode value of type " + dataType + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Resolves an enumeration value.
     *
     * @param client         The OpcUaClient instance to use for reading.
     * @param value          The value to resolve.
     * @param dataTypeNodeId The NodeId of the data type of the value.
     * @return The resolved value.
     * @throws Exception If the read operation fails.
     */
    public static String resolveEnumerationValue(OpcUaClient client, Object value, NodeId dataTypeNodeId) throws Exception {
        // System.out.println("Resolving enumeration value: " + value + " (" + dataTypeNodeId + ")");
        // retrieve the namespace table
        NamespaceTable namespaceTable = client.getNamespaceTable();

        BrowseDescription browse = new BrowseDescription(
                dataTypeNodeId,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue()));

        try {
            BrowseResult browseResult = client.browse(browse).get();
            List<ReferenceDescription> references = toList(browseResult.getReferences());

            for (ReferenceDescription rd : references) {
                // System.out.println("Reference: " + rd.getDisplayName().getText());
                if ("EnumStrings".equals(rd.getDisplayName().getText())) {
                    Optional<NodeId> optionalEnumStringsNodeId = rd.getNodeId().toNodeId(namespaceTable);

                    if (optionalEnumStringsNodeId.isPresent()) {
                        NodeId enumStringsNodeId = optionalEnumStringsNodeId.get();
                        return getEnumStringValue(client, enumStringsNodeId, (int) value);
                    } else {
                        throw new Exception("Failed to get NodeId for EnumStrings");
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Failed to resolve enumeration value: " + e.getMessage());
        }

        return null; // or an appropriate default or error message
    }

    /**
     * Reads the string value of an enumeration.
     *
     * @param client     The OpcUaClient instance to use for reading.
     * @param nodeId     The NodeId of the EnumStrings node.
     * @param enumValue  The value of the enumeration.
     * @return The string value of the enumeration.
     */
    public static String getEnumStringValue(OpcUaClient client, NodeId nodeId, int enumValue) {
        try {
            ReadValueId readValueId = new ReadValueId(
                    nodeId,
                    AttributeId.Value.uid(),
                    null,
                    null
            );

            CompletableFuture<ReadResponse> future = client.read(0.0, TimestampsToReturn.Both, Collections.singletonList(readValueId));
            ReadResponse response = future.get();
            DataValue dataValue = response.getResults()[0];

            assert dataValue.getStatusCode() != null;
            if (dataValue.getStatusCode().isGood()) {
                LocalizedText[] enumStrings = (LocalizedText[]) dataValue.getValue().getValue();
                return enumStrings[enumValue].getText();
            }
        } catch (Exception e) {
            System.out.println("Failed to get enum string value: " + e.getMessage());
        }

        return null; // or an appropriate default or error message
    }

    /**
     * Converts a String containing a NodeId to a NodeId object.
     *
     * @param nodeIdStr The String to convert.
     * @return The NodeId object.
     */
    public static NodeId stringToNodeId(String nodeIdStr) {
        String[] parts = nodeIdStr.split(";");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid NodeId format");
        }

        int namespaceIndex = parseNamespaceIndex(parts[0]);
        String identifierStr = parts[1];

        if (identifierStr.startsWith("i=")) {
            int identifier = Integer.parseInt(identifierStr.substring(2));
            return new NodeId(namespaceIndex, identifier);
        } else if (identifierStr.startsWith("s=")) {
            String identifier = identifierStr.substring(2);
            return new NodeId(namespaceIndex, identifier);
        } else if (identifierStr.startsWith("g=")) {
            UUID identifier = UUID.fromString(identifierStr.substring(2));
            return new NodeId(namespaceIndex, identifier);
        } else if (identifierStr.startsWith("b=")) {
            ByteString identifier = ByteString.of(Base64.getDecoder().decode(identifierStr.substring(2)));
            return new NodeId(namespaceIndex, identifier);
        } else {
            throw new IllegalArgumentException("Unsupported NodeId type");
        }
    }

    /**
     * Parses the namespace index from a namespace string.
     *
     * @param nsPart The namespace string to parse.
     * @return The namespace index.
     */
    private static int parseNamespaceIndex(String nsPart) {
        if (!nsPart.startsWith("ns=")) {
            throw new IllegalArgumentException("Namespace index must start with 'ns='");
        }
        return Integer.parseInt(nsPart.substring(3));
    }
}
