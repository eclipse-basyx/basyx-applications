package org.eclipse.digitaltwin.basyx.opc2aas;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultDataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResource;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EUInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import submodel.SubmodelFactory;

public class AasBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AasBuilder.class);

    private static final String UPPER_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static final String SUBMODEL_OPC_UA_TREE_ID = "http://opc2aas.com/type/1/1/" + UUID.randomUUID();

    private static final List<ConceptDescription> conceptDescriptions = new ArrayList<>();



    /**
     * Creates an AAS based on the OPC UA server subtree.
     *
     * @param aasIdShort           The idShort of the AAS.
     * @param serverApplicationUri The server application URI of the OPC UA server.
     * @return The created AAS.
     */
    public static AssetAdministrationShell createAAS(String aasIdShort, String serverApplicationUri) {
        return new DefaultAssetAdministrationShell.Builder()
                .idShort(aasIdShort)
                .id(createIdentifier("aas"))
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId(createGlobalId(11, "asset"))
                        .specificAssetIds(new DefaultSpecificAssetId.Builder()
                                .name("OpcUaServerID")
                                .value(serverApplicationUri)
                                .externalSubjectId(new DefaultReference.Builder()
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value(createGlobalId(11, "Systems"))
                                                .build())
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .build())
                                .build())
                        .defaultThumbnail(new DefaultResource.Builder()
                                .contentType("image/jpg")
                                .path("https://opcfoundation.org/wp-content/themes/opc/images/logo.jpg")
                                .build())
                        .build())
                .submodels(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value(SUBMODEL_OPC_UA_TREE_ID)
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .build();
    }
    public static AssetAdministrationShell createNewAAS(String aasIdShort, String serverApplicationUri){
        return new DefaultAssetAdministrationShell.Builder()
                .idShort(aasIdShort)
                .id(createIdentifier("aas"))
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId(createGlobalId(11, "asset"))
                        .specificAssetIds(new DefaultSpecificAssetId.Builder()
                                .name("OpcUaServerID")
                                .value(serverApplicationUri)
                                .externalSubjectId(new DefaultReference.Builder()
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value(createGlobalId(11, "Systems"))
                                                .build())
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .build())
                                .build())
                        .defaultThumbnail(new DefaultResource.Builder()
                                .contentType("image/jpg")
                                .path("https://opcfoundation.org/wp-content/themes/opc/images/logo.jpg")
                                .build())
                        .build())
                .build();
    }

    /**
     * Creates a Submodel from an OPC UA subtree.
     *
     * @param subTree The OPC UA subtree.
     * @param endpointUrl The endpoint URL of the OPC UA server.
     * @param username The username of the OPC UA server.
     * @param password The password of the OPC UA server.
     * @param submodelRepositoryUrl The URL of the Submodel Repository.
     * @return The created Submodel.
     */
    public static Submodel createSubmodelOpcUaTree(NodeInfo subTree, String endpointUrl, String username, String password, String submodelRepositoryUrl) {
        String submodelIdShort = subTree.displayName.getText();
        Submodel generatedSM = new DefaultSubmodel.Builder()
                .semanticId(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value(generateIrdi("0173", "0173-1#01-AAA650#002"))
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .idShort(submodelIdShort)
                .id(SUBMODEL_OPC_UA_TREE_ID)
                .build();
        List<SubmodelElement> submodelElements = buildSubmodelElements(subTree.children, endpointUrl, username, password, submodelIdShort, "", submodelRepositoryUrl);
        generatedSM.setSubmodelElements(submodelElements); // add the submodel elements to the submodel
        return generatedSM;
    }

    /**
     * Recursively builds a list of submodel elements from a list of nodes.
     *
     * @param nodes The nodes to build the submodel elements from.
     * @param endpointUrl The endpoint URL of the OPC UA server.
     * @param username The username of the OPC UA server.
     * @param password The password of the OPC UA server.
     * @param submodelIdShort The idShort of the Submodel.
     * @param idShortPath The path of the idShort of the Submodel.
     * @param submodelRepositoryUrl The URL of the Submodel Repository.
     * @return The list of submodel elements.
     */
    public static List<SubmodelElement> buildSubmodelElements(List<NodeInfo> nodes, String endpointUrl, String username, String password, String submodelIdShort, String idShortPath, String submodelRepositoryUrl) {
        // Initialize the list to avoid NullPointerException
        List<SubmodelElement> submodelElements = new ArrayList<>();

        if (nodes == null) {
            // Handle the case where the input list is null
            System.out.println("Input node list is null.");
            return submodelElements;
        }

        // Loop through all nodes
        for (NodeInfo node : nodes) {
            try {
                if (node != null && node.displayName != null) {
                    SubmodelElement value = null;
                    String idShort = node.displayName.getText();
                    // Create a SubmodelElementCollection if the node has a nodeClass of Object
                    if (node.nodeClass != null && node.nodeClass.equals("Object")) {
                        if (node.children != null && !node.children.isEmpty()) { // give the SubmodelElementCollection a value if the node has children
                            List<SubmodelElement> childElements = buildSubmodelElements(node.children, endpointUrl, username, password, submodelIdShort, idShortPath + idShort + ".", submodelRepositoryUrl);
                            value = new DefaultSubmodelElementCollection.Builder()
                                    .idShort(idShort)
                                    .description(localizedTextToLangStringText(node, "description"))
                                    .value(childElements)
                                    .build();
                        } else { // create an empty SubmodelElementCollection if the node has no children
                            value = new DefaultSubmodelElementCollection.Builder()
                                    .idShort(idShort)
                                    .description(localizedTextToLangStringText(node, "description"))
                                    .build();
                        }
                    } else if (node.nodeClass != null && node.nodeClass.equals("Variable")) { // Create a (MultiLanguage)Property if the node has no children and has a nodeClass of Variable
                        if (node.dataType != null && node.dataType.equals("LocalizedText")) { // Create a MultiLanguageProperty if the node has a dataType of LocalizedText
                            value = new DefaultMultiLanguageProperty.Builder()
                                    .idShort(idShort)
                                    .description(localizedTextToLangStringText(node, "description"))
                                    .value(localizedTextToLangStringText(node, "value"))
                                    .build();
                        } else { // Create a Property if the node has a dataType other than LocalizedText
                            value = new DefaultProperty.Builder()
                                    .idShort(idShort)
                                    .description(localizedTextToLangStringText(node, "value"))
                                    .valueType(translateDatatype(node))
                                    .value(createValue(node))
                                    .build();
                            Reference semanticId =  createConceptDescription(node);
                            value.setSemanticId(semanticId); // add the ConceptDescription to the Property
                            DataBridgeConfig.writeOpcUaConsumerEntry(endpointUrl, username, password, node, submodelIdShort, idShortPath); // write the OPC UA Consumer entry to the DataBridge configuration file
                            DataBridgeConfig.writeRouteEntry(node, submodelIdShort, idShortPath, endpointUrl); // write the route entry to the DataBridge configuration file
                            DataBridgeConfig.writeAasServerEntry(node, submodelIdShort, SUBMODEL_OPC_UA_TREE_ID, idShortPath, submodelRepositoryUrl, endpointUrl); // write the AAS Server entry to the DataBridge configuration file
                        }
                    } else {
                        // Handle the case where the node is neither an Object nor a Variable
                        System.out.println("Node '" + node.displayName + "' has unsupported nodeClass: " + node.nodeClass + ".");
                    }

                    submodelElements.add(value);
                } else {
                    // Log the error or throw a custom exception
                    System.err.println("Node or displayName is null or empty.");
                }
            } catch (Exception e) {
                // Log the exception for debugging
                logger.error("An error occurred while processing a node: ", e);
            }
        }

        return submodelElements;
    }

    /**
     * Creates a ConceptDescription from a node.
     *
     * @param node The node to create the ConceptDescription from.
     */
    public static Reference createConceptDescription(NodeInfo node) {
        if (node == null || node.children == null || node.children.isEmpty()) {
            // Handle or log the situation where the node is null
            // System.err.println("Node is null.");
            return null;
        }

        try {
            // get the first child of the node and check if its dataType is EUInformation
            NodeInfo firstChild = node.children.get(0);
            if (firstChild.dataType.equals("EUInformation")) {
                // get the value of the first child
                EUInformation embeddedValue = (EUInformation) firstChild.value;
                String unitId = Integer.toString(embeddedValue.getUnitId());
                String id = "https://opc2aas.com/cd/1/1/" + unitId;
                LocalizedText displayName = embeddedValue.getDisplayName();
                LocalizedText description = embeddedValue.getDescription();

                Reference semanticId = new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value(id)
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build();

                // check if the ConceptDescription already exists by comparing the unitId of the ConceptDescription with the unitId of the EUInformation
                for (ConceptDescription conceptDescription : conceptDescriptions) {
                    if (conceptDescription.getId().equals(id)) {
                        // System.out.println("ConceptDescription already exists.");
                        return semanticId;
                    }
                }

                // System.out.println("Creating ConceptDescription for node: " + node.displayName.getText());

                // create the ConceptDescription
                ConceptDescription conceptDescription = new DefaultConceptDescription.Builder()
                        .idShort("EngineeringUnits")
                        .id(id)
                        .category("Property")
                        .embeddedDataSpecifications(new DefaultEmbeddedDataSpecification.Builder()
                                .dataSpecification(new DefaultReference.Builder()
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("https://opc2aas.com/aas/3/0/RC02/DataSpecificationIEC61360")
                                                .build())
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .build())
                                .dataSpecificationContent(new DefaultDataSpecificationIec61360.Builder()
                                        .preferredName(new DefaultLangStringPreferredNameTypeIec61360.Builder()
                                                .text(description.getText())
                                                .language(description.getLocale() != null ? description.getLocale() : "no-lang")
                                                .build())
                                        .shortName(new DefaultLangStringShortNameTypeIec61360.Builder()
                                                .text(displayName.getText())
                                                .language(displayName.getLocale() != null ? displayName.getLocale() : "no-lang")
                                                .build())
                                        .unit(displayName.getText())
                                        // .sourceOfDefinition("ExampleString")
                                        .dataType(translateDatatypeIec61360(node))
                                        .definition(new DefaultLangStringDefinitionTypeIec61360.Builder()
                                                .text("Unit of measure for the value of the property")
                                                .language("EN")
                                                .build())
                                        .definition(new DefaultLangStringDefinitionTypeIec61360.Builder()
                                                .text("Maßeinheit für den Wert der Eigenschaft")
                                                .language("DE")
                                                .build())
                                        .build())
                                .build())
                        .build();

                // add the ConceptDescription to the list of ConceptDescriptions
                conceptDescriptions.add(conceptDescription);
                return semanticId;
            } else {
                // Handle or log the situation where the first child is not of type EUInformation
                // System.err.println("First child is not of type EUInformation.");
                return null;
            }
        } catch (Exception e) {
            // Log the exception for debugging
            logger.error("An error occurred while processing a node: ", e);
            return null;
        }
    }

    /**
     * Creates a list of LangStringTextType from a nodes LocalizedText element.
     *
     * @param node    The node to create the LangStringTextType from.
     * @param nodeKey The key of the node to create the LangStringTextType from.
     * @return The list of LangStringTextType.
     */
    public static List<LangStringTextType> localizedTextToLangStringText(NodeInfo node, String nodeKey) {
        List<LangStringTextType> langStringText = new ArrayList<>();

        Object value = NodeInfo.getValueFromNodeByKey(node, nodeKey);
        // System.out.println("nodeKey: " + nodeKey + " displayName: " + node.displayName.getText() + " Value: " + value);

        if (!(value instanceof LocalizedText localizedText)) {
            // Handle or log that the value is null or not a list
            // System.err.println("Node or description is null.");
            return langStringText;
        }

        try {
            String locale = localizedText.getLocale();
            String text = localizedText.getText();

            if (locale == null) {
                locale = "no-lang"; // Set to "no-lang" if locale is null
            }

            if (text != null) {
                LangStringTextType langStringTextType = new DefaultLangStringTextType.Builder()
                        .language(locale)
                        .text(text)
                        .build();
                langStringText.add(langStringTextType);
            }

        } catch (Exception e) {
            // Log the exception for debugging
            logger.error("An error occurred while processing a node: ", e);
        }
        return langStringText;
    }

    /**
     * Translates an OPC UA datatype to an AAS ValueType.
     *
     * @param node The node to translate the datatype from.
     * @return The translated datatype.
     */
    public static DataTypeDefXsd translateDatatype(NodeInfo node) {
        String opcUaDataType = node.dataType;

        // Log the datatype being translated for debugging
        // System.out.println("Translating datatype: " + opcUaDataType);

        // Map OPC UA types to AAS types
        return switch (opcUaDataType) {
            // decimal types
            case "SByte" -> DataTypeDefXsd.BYTE;
            case "Byte" -> DataTypeDefXsd.UNSIGNED_BYTE;
            case "Int16" -> DataTypeDefXsd.SHORT;
            case "Int32" -> DataTypeDefXsd.INT;
            case "Int64" -> DataTypeDefXsd.LONG;
            case "UInt16" -> DataTypeDefXsd.UNSIGNED_SHORT;
            case "UInt32" -> DataTypeDefXsd.UNSIGNED_INT;
            case "UInt64" -> DataTypeDefXsd.UNSIGNED_LONG;
            // floating point types
            case "Float" -> DataTypeDefXsd.FLOAT;
            case "Double" -> DataTypeDefXsd.DOUBLE;
            // date and time types
            case "DateTime" -> DataTypeDefXsd.DATE_TIME;
            // Boolean type
            case "Boolean" -> DataTypeDefXsd.BOOLEAN;
            // String type
            case "String", "Guid" -> DataTypeDefXsd.STRING;
            case "ByteString" -> DataTypeDefXsd.BASE64BINARY;
            default ->
                // If no mapping exists, return String
                    DataTypeDefXsd.STRING;
        };
    }

    /**
     * Translates an OPC UA datatype to an AAS DataTypeIec61360.
     *
     * @param node The node to translate the datatype from.
     * @return The translated datatype.
     */
    public static DataTypeIec61360 translateDatatypeIec61360(NodeInfo node) {
        String opcUaDataType = node.dataType;

        // Log the datatype being translated for debugging
        // System.out.println("Translating datatype: " + opcUaDataType);

        // Map OPC UA types to AAS types
        return switch (opcUaDataType) {
            // decimal types
            case "SByte", "Byte", "Int16", "Int32", "Int64", "UInt16", "UInt32", "UInt64" ->
                    DataTypeIec61360.INTEGER_MEASURE;
            // floating point types
            case "Float", "Double" -> DataTypeIec61360.REAL_MEASURE;
            // date and time types
            case "DateTime" -> DataTypeIec61360.DATE;
            // Boolean type
            case "Boolean" -> DataTypeIec61360.BOOLEAN;
            // String type
            case "String", "Guid", "ByteString" -> DataTypeIec61360.STRING_TRANSLATABLE;
            default ->
                // If no mapping exists, return String
                    DataTypeIec61360.STRING_TRANSLATABLE;
        };
    }

    /**
     * Creates a string value from a nodes value.
     *
     * @param node The node to create the value from.
     * @return The string value.
     */
    public static String createValue(NodeInfo node) {
        String value = null;

        if (node == null) {
            // Handle or log the situation where the node is null
            System.err.println("Node is null.");
            return null;
        }

        try {
            if (node.value != null) {
                value = node.value.toString();
            }
        } catch (Exception e) {
            // Log the exception for debugging
            logger.error("An error occurred while processing a node: ", e);
        }

        return value;
    }

    /**
     * Creates a random identifier for an AAS element.
     *
     * @param type The type of the element.
     * @return The identifier.
     */
    public static String createIdentifier(String type) {
        Random rand = new Random();

        int part1 = rand.nextInt(10000);
        int part2 = rand.nextInt(10000);
        int part3 = rand.nextInt(10000);
        int part4 = rand.nextInt(10000);

        return String.format("http://opc2aas.com/%s/%04d_%04d_%04d_%04d", type, part1, part2, part3, part4);
    }

    /**
     * Creates a random global identifier for an AAS element.
     *
     * @param length The length of the identifier.
     * @param type   The type of the element.
     * @return The global identifier.
     */
    public static String createGlobalId(int length, String type) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(UPPER_ALPHABET.length());
            char randomChar = UPPER_ALPHABET.charAt(index);
            sb.append(randomChar);
        }

        return String.format("http://opc2aas.com/%s/%s", type, sb);
    }

    /**
     * Generates an IRDI string.
     *
     * @param organizationCode     The organization code.
     * @param identifierForOPCUA   The identifier for OPC UA.
     * @return The generated IRDI string.
     */
    public static String generateIrdi(String organizationCode, String identifierForOPCUA) {
        // Generate a unique identifier for the submodel
        String uniqueId = UUID.randomUUID().toString();

        // Create the IRDI string

        return String.format("http://opc2aas.com/IRDI/%s/%s/autogenerated/%s", organizationCode, identifierForOPCUA, uniqueId);
    }

    /**
     * Creates an AAS environment from an OPC UA subtree.
     *
     * @param aasIdShort           The idShort of the AAS.
     * @param serverApplicationUri The server application URI of the OPC UA server.
     * @param subTree              The OPC UA subtree.
     * @param endpointUrl          The endpoint URL of the OPC UA server.
     * @param submodelRepositoryUrl The URL of the Submodel Repository.
     * @return The created environment.
     */
    public static Environment createEnvironment(String aasIdShort, String serverApplicationUri, NodeInfo subTree, String endpointUrl, String username, String password, String submodelRepositoryUrl){

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(createAAS(aasIdShort, serverApplicationUri))
                .submodels(createSubmodelOpcUaTree(subTree, endpointUrl, username, password, submodelRepositoryUrl))
                .conceptDescriptions(conceptDescriptions)
                .build();
    }
    public static Environment createNewEnvironment(String aasIdShort, String serverApplicationUri, NodeInfo subTree, String endpointUrl, String username, String password, String submodelRepositoryUrl) throws IOException {
        Submodel creationSubmodel = SubmodelFactory.creationSubmodel();
        List<Submodel> submodels = new ArrayList<>();
        submodels.add(creationSubmodel);
        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(createNewAAS(aasIdShort, serverApplicationUri))
                .submodels(submodels)
                .conceptDescriptions(conceptDescriptions)
                .build();
    }
}
