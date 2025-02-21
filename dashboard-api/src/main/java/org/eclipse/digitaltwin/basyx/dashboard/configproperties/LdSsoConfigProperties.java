package org.eclipse.digitaltwin.basyx.dashboard.configproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties("ld-sso")
public class LdSsoConfigProperties {

    String apiToken;

    String baseUrl;
}
