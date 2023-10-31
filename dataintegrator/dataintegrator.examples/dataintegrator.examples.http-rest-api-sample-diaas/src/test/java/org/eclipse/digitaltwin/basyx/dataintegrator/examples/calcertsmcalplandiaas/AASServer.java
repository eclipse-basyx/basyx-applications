package org.eclipse.digitaltwin.basyx.dataintegrator.examples.calcertsmcalplandiaas;

import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASEventBackend;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;

public class AASServer {
	// Server URLs
	public static final String REGISTRYPATH = "http://localhost:4000/registry";
	public static final String AASSERVERPATH = "http://localhost:4001/aasServer";
	
	private RegistryComponent registry;
	private AASServerComponent aasServer;
	
	public AASServer() {
		configureRegistry();
		configureAAS();
	}

	public void startComponents() {
		registry.startComponent();
		aasServer.startComponent();
	}
	
	public void stopComponents() {
		registry.stopComponent();
		aasServer.stopComponent();
	}

	/**
	 * Starts an empty registry at "http://localhost:4000"
	 */
	private void configureRegistry() {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(4000, "/registry");
		BaSyxRegistryConfiguration registryConfig = new BaSyxRegistryConfiguration(RegistryBackend.INMEMORY);
		registry = new RegistryComponent(contextConfig, registryConfig);

	}

	/**
	 * Startup an empty server at "http://localhost:4001/"
	 */
	private void configureAAS() {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration();
		contextConfig.loadFromDefaultSource();
		
		BaSyxAASServerConfiguration aasServerConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "", REGISTRYPATH);
		aasServerConfig.setAASSourceAsList("src/test/resources/aasx/BasicHttpRestExample.aasx");
//		aasServerConfig.setAASEvents(AASEventBackend.MQTT);
		aasServer = new AASServerComponent(contextConfig, aasServerConfig);
	}
}
