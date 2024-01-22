package org.eclipse.digitaltwin.basyx.dataintegrator.registry.component.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.ConfigurationRepository;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ConfigurationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DIRegistryConfigurationRepository implements ConfigurationRepository {
	
	private static Logger logger = LoggerFactory.getLogger(DIRegistryConfigurationRepository.class);
	
	private Map<String, Submodel> dataReaderConfigs = new HashMap<>();
	private Map<String, Submodel> dataProcessorConfigs = new HashMap<>();
	private Map<String, Submodel> dataWriterConfigs = new HashMap<>();
	private Map<String, Submodel> jobConfigs = new HashMap<>();
	
	@Autowired
	private ConfigurationType configurationType;
	
	public List<Submodel> getDataReaderConfigs() {
		return new ArrayList<>(dataReaderConfigs.values());
	}
	
	public void addDataReaderConfig(Submodel dataReaderConfig) {
		this.dataReaderConfigs.put(dataReaderConfig.getIdentification().getId(), dataReaderConfig);
	}
	
	public void removeDataReaderConfig(String id) {
		this.dataReaderConfigs.remove(id);
	}
	
	public List<Submodel> getDataProcessorConfigs() {
		return new ArrayList<>(dataProcessorConfigs.values());
	}
	
	public void addDataProcessorConfig(Submodel dataProcessorConfig) {
		this.dataProcessorConfigs.put(dataProcessorConfig.getIdentification().getId(), dataProcessorConfig);
	}
	
	public void removeDataProcessorConfig(String id) {
		this.dataProcessorConfigs.remove(id);
	}
	
	public List<Submodel> getDataWriterConfig() {
		return new ArrayList<>(dataWriterConfigs.values());
	}
	
	public void addDataWriterConfig(Submodel dataWriterConfig) {
		this.dataWriterConfigs.put(dataWriterConfig.getIdentification().getId(), dataWriterConfig);
	}
	
	public void removeDataWriterConfig(String id) {
		this.dataWriterConfigs.remove(id);
	}
	
	public List<Submodel> getJobConfigs() {
		return new ArrayList<>(jobConfigs.values());
	}
	
	public void addJobConfig(Submodel jobConfig) {
		this.jobConfigs.put(jobConfig.getIdentification().getId(), jobConfig);
	}
	
	public void removeJobConfig(String id) {
		this.jobConfigs.remove(id);
	}
	
	@Override
	public void addConfiguration(Submodel config) {
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getDataReaderName())) {
			addDataReaderConfig(config);
			logger.info("Data reader configuration added {}", config.getIdShort());
			return;
		}
		
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getDataProcessorName())) {
			addDataProcessorConfig(config);
			logger.info("Data processor configuration added {}", config.getIdShort());
			return;
		}
		
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getDataWriterName())) {
			addDataWriterConfig(config);
			logger.info("Data writer configuration added {}", config.getIdShort());
			return;
		}
		
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getIntegratorPipelineName())) {
			addJobConfig(config);
			logger.info("Job configuration added {}", config.getIdShort());
			return;
		}
		
		logger.error("Configuration addition failure. The semantic id of the provided configuration : " + config.getIdentification().getId() + " could not be matched");
	}
	
	@Override
	public void updateConfiguration(Submodel config) {
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getDataReaderName())) {
			addDataReaderConfig(config);
			logger.info("Data reader configuration updated {}", config.getIdShort());
			return;
		}
		
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getDataProcessorName())) {
			addDataProcessorConfig(config);
			logger.info("Data processor configuration updated {}", config.getIdShort());
			return;
		}
		
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getDataWriterName())) {
			addDataWriterConfig(config);
			logger.info("Data writer configuration updated {}", config.getIdShort());
			return;
		}
		
		if (hasMatchingSemantic(config.getSemanticId(), configurationType.getIntegratorPipelineName())) {
			addJobConfig(config);
			logger.info("Job configuration updated {}", config.getIdShort());
			return;
		}
		
		logger.error("Configuration updation failure. The semantic id of the provided configuration : " + config.getIdentification().getId() + " could not be matched");
	}
	
	@Override
	public void removeConfiguration(String configId) {
		if (this.dataReaderConfigs.containsKey(configId)) {
			removeDataReaderConfig(configId);
			logger.info("Data reader configuration removed {}", configId);
			return;
		}
		
		if (this.dataProcessorConfigs.containsKey(configId)) {
			removeDataReaderConfig(configId);
			logger.info("Data processor configuration removed {}", configId);
			return;
		}
		
		if (this.dataWriterConfigs.containsKey(configId)) {
			removeDataWriterConfig(configId);
			logger.info("Data writer configuration removed {}", configId);
			return;
		}
		
		if (this.jobConfigs.containsKey(configId)) {
			removeJobConfig(configId);
			logger.info("Job configuration removed {}", configId);
			return;
		}
		
		logger.error("Configuration removal failure. The id of the provided configuration : " + configId + " could not be found");
	}

	private boolean hasMatchingSemantic(IReference semanticId, String configurationType) {
		return semanticId.getKeys().stream().anyMatch(key -> key.getValue().equals(configurationType));
	}
	
}
