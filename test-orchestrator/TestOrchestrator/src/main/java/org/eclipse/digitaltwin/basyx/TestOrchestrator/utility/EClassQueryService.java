package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EClassQueryService {
    private static final String DOCKER_KEYS_PATH = "/app/keys";
    private static final String LOCAL_KEY_PATH = "";
    private static final String CERT_PASSWORD = "";
    private static final String API_PREFIX = "https://eclass-cdp.com/jsonapi/v1/properties/";
    private static final boolean eclassAvailable = checkKeyAvailability();

    private static boolean checkKeyAvailability() {
        File dockerDir = new File(DOCKER_KEYS_PATH);
        if (dockerDir.exists() && dockerDir.isDirectory()) {
            File[] files = dockerDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pfx"));
            if (files != null && files.length > 0) {
                System.out.println("ECLASS certificate found in Docker path. ECLASS validation enabled.");
                return true;
            }
        }

        if (new File(LOCAL_KEY_PATH).exists()) {
            System.out.println("ECLASS certificate found in local path. ECLASS validation enabled.");
            return true;
        }

        System.out.println("ECLASS certificate not found. ECLASS validation will be skipped.");
        return false;
    }

    public static boolean isEClassEnabled() {
        return eclassAvailable;
    }

    public static Map<String, Object> fetchEClassData(String irdi) {
        if (!eclassAvailable) return null;
        try {
            String keyPath = findKeyFile();

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(keyPath), CERT_PASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, CERT_PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            URL url = new URL(API_PREFIX + irdi);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                System.err.println("ECLASS API request failed: HTTP " + conn.getResponseCode());
                return null;
            }

            InputStream is = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, Map.class);

        } catch (Exception e) {
            System.err.println("ECLASS fetch failed: " + e.getMessage());
            return null;
        }
    }

    private static String findKeyFile() {
        // Check Docker keys folder first
        File dockerDir = new File(DOCKER_KEYS_PATH);
        if (dockerDir.exists() && dockerDir.isDirectory()) {
            File[] files = dockerDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pfx"));
            if (files != null && files.length > 0) {
                return files[0].getAbsolutePath(); // Use the first .pfx found
            }
        }

        // Fallback to local dev path
        return LOCAL_KEY_PATH;
    }
}
