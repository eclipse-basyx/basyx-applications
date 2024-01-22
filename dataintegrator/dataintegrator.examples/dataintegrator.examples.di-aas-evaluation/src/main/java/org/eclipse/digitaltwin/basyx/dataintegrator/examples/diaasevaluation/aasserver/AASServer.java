package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.aasserver;

import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;

public class AASServer {
	// Server URLs
	public static final String REGISTRYPATH = "http://localhost:4000/registry";
	public static final String AASSERVERPATH = "http://localhost:4002/aasServer";
	
	private RegistryComponent registry;
	private AASServerComponent aasServer;
	
	public AASServer(String filePath) {
		configureRegistry();
		configureAAS(filePath);
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
//		registryConfig.setProperty("registry.id", "config-registry-repo");
//		registryConfig.setRegistryEvents(RegistryEventBackend.MQTTV2);
		registry = new RegistryComponent(contextConfig, registryConfig);

	}

	/**
	 * Startup an empty server at "http://localhost:4001/"
	 * @param filePath 
	 * @param filePath 
	 */
	private void configureAAS(String filePath) {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(4002, "/aasServer");
		
		BaSyxAASServerConfiguration aasServerConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "", REGISTRYPATH);
		aasServerConfig.setAASSourceAsList(filePath);
//		aasServerConfig.setAASEvents(AASEventBackend.MQTT);
		aasServer = new AASServerComponent(contextConfig, aasServerConfig);
	}
}
