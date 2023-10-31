package org.eclipse.digitaltwin.basyx.dataintegrator.common.configuration.initializer;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "job.config.scanForExistingSmConfig", havingValue = "true")
public class ConfigurationInitializer {
	
	private static Logger logger = LoggerFactory.getLogger(ConfigurationInitializer.class);
	
	@Value("${job.config.scanForExistingSmConfig:false}")
    private boolean configInitializationEnabled;
	
	@Value("${config.aas.id}")
	private String configAasId;
	
	private ConfigurationRepository repository;
	
	private ConnectedAssetAdministrationShellManager manager;
	
	public ConfigurationInitializer(ConfigurationRepository repository, ConnectedAssetAdministrationShellManager manager) {
		this.repository = repository;
		this.manager = manager;
	}
	
	@PostConstruct
    public void initializeData() {
        if (!configInitializationEnabled)
        	return;
        
        logger.info("Initializing the configurations");
        
        logger.info("Scanning configurations");
        
        ConnectedAssetAdministrationShell aas = manager.retrieveAAS(new CustomId(configAasId));
        Map<String, ISubmodel> configSms = aas.getSubmodels();
        
        addConfigurationToRepository(configSms);
    }

	private void addConfigurationToRepository(Map<String, ISubmodel> configSms) {
		configSms.values().stream().map(ConnectedSubmodel.class::cast).map(ConnectedSubmodel::getLocalCopy).forEach(repository::addConfiguration);
	}

}
