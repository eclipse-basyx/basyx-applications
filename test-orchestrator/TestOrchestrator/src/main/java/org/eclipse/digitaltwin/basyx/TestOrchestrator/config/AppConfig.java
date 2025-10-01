package org.eclipse.digitaltwin.basyx.TestOrchestrator.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component

public class AppConfig {

    @Value("${submodel.api.base-url}")
    private String submodelApiBaseUrl;
    private static String staticSubmodelApiBaseUrl;

    @PostConstruct
    public void init(){
        staticSubmodelApiBaseUrl = submodelApiBaseUrl;
    }

    public String getSubmodelApiBaseUrl() {
        return submodelApiBaseUrl;
    }

    public static String getStaticSubmodelApiBaseUrl() {
        return staticSubmodelApiBaseUrl;
    }
}
