package org.eclipse.digitaltwin.basyx.dashboard.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.GroupedOpenApi;
@ComponentScan
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Digital Twins Dashboard API", version = "0.0.1", description = "Documentation of Digital Twins Dashboard API"),
        servers = @Server(url = "/", description = "Default Server URL")
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("dashboard")
                .packagesToScan("org.eclipse.digitaltwin.basyx.dashboard.controller")
                .build();
    }
}
