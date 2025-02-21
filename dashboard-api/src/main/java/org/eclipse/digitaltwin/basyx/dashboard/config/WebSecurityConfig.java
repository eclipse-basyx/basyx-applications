package org.eclipse.digitaltwin.basyx.dashboard.config;

import org.eclipse.digitaltwin.basyx.dashboard.configproperties.LdSsoConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfig {

    private static final String JWKS_PATH = "/.well-known/jwks.json";

    private final LdSsoConfigProperties ldSsoConfigProperties;

    public WebSecurityConfig(LdSsoConfigProperties ldSsoConfigProperties) {

        this.ldSsoConfigProperties = ldSsoConfigProperties;
    }

    @Bean
    public  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .oauth2ResourceServer(oauth2ResourceServerCustomizer -> oauth2ResourceServerCustomizer
                        .jwt(jwtCustomizer -> {
                            jwtCustomizer.jwkSetUri(ldSsoConfigProperties.getBaseUrl() + JWKS_PATH);
                            jwtCustomizer.jwtAuthenticationConverter(
                                    new JwtAuthenticationConverter()
                            );
                        })
                )
                .authorizeHttpRequests(customizer -> customizer
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.debug(false);
    }
}
