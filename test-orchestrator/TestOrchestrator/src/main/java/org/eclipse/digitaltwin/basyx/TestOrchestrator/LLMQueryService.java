package org.eclipse.digitaltwin.basyx.TestOrchestrator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.basyx.TestOrchestrator.utility.ComparisonResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LLMQueryService {

    private static String API_KEY;
    private static String MODEL;
    private static String ENDPOINT;
    private static final String CONFIG_PATH = "config/llm_config.txt";


    static {
        loadConfig();
    }


    private static void loadConfig() {
        try {
            Path path = Paths.get(CONFIG_PATH);
            if (!Files.exists(path)) {
                System.err.println("LLM config file not found: " + CONFIG_PATH);
                return;
            }


            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.startsWith("MODEL_NAME=")) MODEL = line.substring("MODEL_NAME=".length()).trim();
                else if (line.startsWith("API_KEY=")) API_KEY = line.substring("API_KEY=".length()).trim();
                else if (line.startsWith("API_ENDPOINT=")) ENDPOINT = line.substring("API_ENDPOINT=".length()).trim();
            }


            if (MODEL == null || API_KEY == null || ENDPOINT == null) {
                throw new IllegalArgumentException("Missing required fields in llm_config.txt");
            }
        } catch (IOException e) {
            System.err.println("Failed to load LLM config");
            e.printStackTrace();
        }
    }

    public static void queryAll(List<String> prompts, ComparisonResult result) {
        if (prompts.isEmpty()) return;

        StringBuilder batchedPrompt = new StringBuilder();
        for (int i = 0; i < prompts.size(); i++) {
            batchedPrompt.append(i + 1).append(". ").append(prompts.get(i)).append("\n");
        }


        try {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are an expert in finding if a unit is relevant for a specific property. Reply to each question with 'yes' or 'no'. If 'no', also suggest better unit(s) in format: no - Suggested: [unit1, unit2]"),
                            Map.of("role", "user", "content", batchedPrompt.toString())
                    )
            );

            String json = mapper.writeValueAsString(requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("LLM Response:\n" + response.body());
            extractAnswers(response.body(), prompts, result);

        } catch (Exception e) {
            System.err.println("Batch request failed");
            e.printStackTrace();
        }
    }

    public static List<String> extractAnswers(String jsonResponse, List<String> prompts, ComparisonResult result) {
        List<String> infos = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            String content = root.path("choices").get(0).path("message").path("content").asText();
            String[] lines = content.split("\\n");


            for (int i = 0; i < lines.length && i < prompts.size(); i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String answerPart;
                if (line.contains(". ")) {
                    answerPart = line.substring(line.indexOf(". ") + 2);
                } else {
                    answerPart = line;
                }

                if (answerPart.toLowerCase().startsWith("no")) {
                    String prompt = prompts.get(i);
                    String unit = extractBetween(prompt, "'", "'");
                    String property = extractAfter(prompt, "property '", "'");

                    String msg = "The unit '" + unit + "' is not compatible for the property '" + property + "'.";


                    int index = answerPart.toLowerCase().indexOf("suggested:");
                    if (index != -1) {
                        String originalSuggestedPart = answerPart.substring(index);

                        String cleaned = originalSuggestedPart
                                .replaceFirst("(?i)suggested", "|| Correction: Suggested Unit")
                                .replace("[", "")
                                .replace("]", "")
                                .replace(",", " or");

                        msg += " " + cleaned;
                    }

                    result.addInfo(" " + msg);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to parse LLM response");
            e.printStackTrace();
        }

        return infos;
    }

    private static String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        int endIndex = text.indexOf(end, startIndex + 1);
        if (startIndex != -1 && endIndex != -1) {
            return text.substring(startIndex + 1, endIndex);
        }
        return "UNKNOWN";
    }

    private static String extractAfter(String text, String marker, String until) {
        int start = text.indexOf(marker);
        int end = text.indexOf(until, start + marker.length());
        if (start != -1 && end != -1) {
            return text.substring(start + marker.length(), end);
        }
        return "UNKNOWN";
    }

}
