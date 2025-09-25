package org.eclipse.digitaltwin.basyx.TestOrchestrator;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.config.AppConfig;
import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Base64;
import java.util.List;


@Service
public class MqttSubscriber {


    private static final String MQTT_BROKER = "tcp://mosquitto:1883";
    private static final String TOPIC_NEW = "sm-repository/sm-repo/submodels/created";
    private static final String TOPIC_UPDATE = "sm-repository/sm-repo/submodels/updated";
    private static final String TOPIC_DELETE = "sm-repository/sm-repo/submodels/deleted";
    private static final String TOPIC_UPDATE_SME = "sm-repository/sm-repo/submodels/#";


    private final SubmodelRepository submodelRepository;
    private final AppConfig appConfig;

    public MqttSubscriber(SubmodelRepository submodelRepository, AppConfig appConfig) {
        this.submodelRepository = submodelRepository;
        this.appConfig = appConfig;
        subscribeToMqtt();
    }

    private void subscribeToMqtt() {
        try {
            MqttClient client = new MqttClient(MQTT_BROKER, MqttClient.generateClientId());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("MQTT Connection Lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    System.out.println("\nMQTT Message Received on " + topic);

                    // Handle different topics
                    if (topic.equals(TOPIC_NEW)) {
                        System.out.println("New Submodel detected! Running tests...");
                        processSubmodel(payload);
                    } else if (topic.equals(TOPIC_UPDATE)) {
                        System.out.println("Submodel updated! Re-running tests...");
                        processDeletedSubmodel(payload);
                        processSubmodel(payload);
                    } else if (topic.equals(TOPIC_DELETE)) {
                        System.out.println("Submodel deleted! Logging removal...");
                        processDeletedSubmodel(payload);
                    }
                    if (topic.matches("sm-repository/sm-repo/submodels/.+/submodelElements/.+/updated")) {
                        String base64SubmodelId = extractBase64IdFromTopic(topic);
                        System.out.println("Detected update on SME. ID: " + base64SubmodelId);

                        boolean isAttachmentUpdate = topic.contains("/attachment/updated");

                        if (isAttachmentUpdate) {
                            System.out.println("Attachment update detected — skipping deletion and revalidation.");
                            return; // Skip processing
                        }

                        try {
                            deleteResultByBase64Id(base64SubmodelId);
                            System.out.println("Deleted successfully");
                            Submodel submodel = fetchSubmodelOverHttp(base64SubmodelId);

                            SubmodelFactory.processReceivedSubmodel(submodel);
                        } catch (Exception e) {
                            System.err.println("Error while processing SME update: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                private String extractBase64IdFromTopic(String topic) {
                    String[] parts = topic.split("/");
                    // parts[3] should be the base64-encoded ID
                    return parts.length >= 4 ? parts[3] : null;
                }

                private Submodel fetchSubmodelOverHttp(String base64EncodedId) throws IOException, DeserializationException {
                    String url = appConfig.getSubmodelApiBaseUrl() + "/submodels/" + base64EncodedId;
                    System.out.println("The submodel repo url: " + url);
                    java.net.URL apiUrl = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() != 200) {
                        throw new IOException("Failed : HTTP error code : " + conn.getResponseCode());
                    }

                    try (java.io.InputStream is = conn.getInputStream()) {
                        JsonDeserializer deserializer = new JsonDeserializer();
                        return deserializer.read(is, DefaultSubmodel.class);
                    }
                }


                private void deleteResultByBase64Id(String base64SubmodelId) {
                    try {

                        String decodedSubmodelId = new String(Base64.getUrlDecoder().decode(base64SubmodelId)).trim().toLowerCase();
                        Submodel testResultSubmodel = submodelRepository.getSubmodel("TestResults");

                        List<String> toDelete = testResultSubmodel.getSubmodelElements().stream()
                                .filter(e -> e instanceof SubmodelElementCollection)
                                .map(e -> (SubmodelElementCollection) e)
                                .filter(smc -> smc.getValue().stream()
                                        .filter(se -> se instanceof Property)
                                        .map(se -> (Property) se)
                                        .anyMatch(p -> "ComparedSubmodelId".equals(p.getIdShort()) &&
                                                decodedSubmodelId.equalsIgnoreCase(p.getValue().trim().toLowerCase())))
                                .map(SubmodelElementCollection::getIdShort)
                                .toList();

                        for (String idShort : toDelete) {
                            submodelRepository.deleteSubmodelElement("TestResults", idShort);
                            System.out.println("Deleted TestResults entry for ID: " + base64SubmodelId);
                        }

                    } catch (Exception e) {
                        System.err.println("Error during cleanup: " + e.getMessage());
                    }
                }


                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}

                private void processDeletedSubmodel(String submodelJson) {
                    try {
                        JsonDeserializer deserializer = new JsonDeserializer();
                        Submodel deletedSubmodel = deserializer.read(submodelJson, DefaultSubmodel.class);
                        String deletedSubmodelId = deletedSubmodel.getId().trim().toLowerCase();

                        System.out.println("Deleting test results for Submodel with ID: " + deletedSubmodelId);

                        // Retrieve the TestResults submodel
                        Submodel testResultSubmodel = submodelRepository.getSubmodel("TestResults");

                        // Collect matching SMC idShorts first (avoid concurrent modification)
                        List<String> collectionsToDelete = testResultSubmodel.getSubmodelElements().stream()
                                .filter(element -> element instanceof SubmodelElementCollection)
                                .map(element -> (SubmodelElementCollection) element)
                                .filter(collection -> collection.getValue().stream()
                                        .filter(subElement -> subElement instanceof Property)
                                        .map(subElement -> (Property) subElement)
                                        .anyMatch(property -> property.getIdShort().equals("ComparedSubmodelId") &&
                                                deletedSubmodelId.equals(property.getValue().trim().toLowerCase())))
                                .map(SubmodelElementCollection::getIdShort)  // Collect idShorts
                                .toList();

                        // Safely delete using the collected idShorts
                        for (String idShort : collectionsToDelete) {
                            try {
                                submodelRepository.deleteSubmodelElement("TestResults", idShort);
                                System.out.println("Deleted SMC with ComparedSubmodelId: " + deletedSubmodelId + " (idShort: " + idShort + ")");
                            } catch (Exception e) {
                                System.err.println("Failed to delete SMC (idShort: " + idShort + "): " + e.getMessage());
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Failed to process deleted submodel JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                }



                private void processSubmodel(String submodelJson) {

                    System.out.println(" Processing Submodel...");
                    try {
                        // Deserialize submodel JSON
                        JsonDeserializer deserializer = new JsonDeserializer();
                        Submodel submodel = deserializer.read(submodelJson, DefaultSubmodel.class);

                        System.out.println("Successfully Deserialized Submodel: " + submodel.getIdShort());

                        // Check for semanticId before processing
                        if (submodel.getSemanticId() == null || submodel.getSemanticId().getKeys().isEmpty()) {
                            System.err.println(" Skipping submodel '" + submodel.getIdShort() + "' due to missing SemanticId.");
                            ResultSubmodelFactory.addUnsuccessfulResultToSubmodel(submodel);
                            return;
                        }

                        // Pass full submodel to SubmodelFactory for processing
                        SubmodelFactory.processReceivedSubmodel(submodel);

                    } catch (Exception e) {
                        System.err.println("Failed to process submodel JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            client.connect();
            client.subscribe(TOPIC_NEW);
            client.subscribe(TOPIC_UPDATE);
            client.subscribe(TOPIC_DELETE);
            client.subscribe(TOPIC_UPDATE_SME);
            System.out.println("Subscribed to MQTT topics: " + TOPIC_NEW + ", " + TOPIC_UPDATE + ", " + TOPIC_DELETE + ", " + TOPIC_UPDATE_SME);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
