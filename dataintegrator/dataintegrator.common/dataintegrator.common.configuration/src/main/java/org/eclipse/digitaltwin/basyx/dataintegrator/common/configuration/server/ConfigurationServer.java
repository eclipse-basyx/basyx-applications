package org.eclipse.digitaltwin.basyx.dataintegrator.common.configuration.server;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationServer {
	
	@Value("${config.registry.url}")
	private String configServerRegistryUrl;

	@Bean
	public ConnectedAssetAdministrationShellManager getAdministrationShellManager() {
		return new ConnectedAssetAdministrationShellManager(getRegistryproxy());
	}

	private IAASRegistry getRegistryproxy() {
		return new AASRegistryProxy(configServerRegistryUrl);
	}

}
