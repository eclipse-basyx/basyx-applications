package org.eclipse.digitaltwin.basyx.dataintegrator.core.preparation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.configuration.integratorpipeline.IntegratorPipelineConfiguration;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor.DataProcessor;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.DataReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer.DataWriter;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.preparation.entity.IntegratorStepFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IntegratorPipelineFactory {

	private Logger logger = LoggerFactory.getLogger(IntegratorPipelineFactory.class);

	private List<Submodel> integratorUnitConfigurations;
	private IntegratorPipelineConfiguration integratorPipelineConfiguration;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private IntegratorStepFactory integratorStepFactory;

	@Autowired
	public IntegratorPipelineFactory(@Qualifier("Job") List<Submodel> integratorUnitConfigurations,
			IntegratorPipelineConfiguration integratorPipelineConfiguration) {
		super();
		this.integratorUnitConfigurations = integratorUnitConfigurations;
		this.integratorPipelineConfiguration = integratorPipelineConfiguration;
	}

	public List<Submodel> getJobConfigurations() {
		return integratorUnitConfigurations;
	}

	public IntegratorPipelineConfiguration getJobConnectorConfiguration() {
		return integratorPipelineConfiguration;
	}

	public void setJobConnectorConfiguration(IntegratorPipelineConfiguration jobConnectorConfiguration) {
		this.integratorPipelineConfiguration = jobConnectorConfiguration;
	}

	public List<Job> create() {
		Map<String, Step> steps = integratorUnitConfigurations.stream().map(this::createStep)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return steps.entrySet().stream().map(this::createJob).collect(Collectors.toList());
	}

	private Job createJob(Map.Entry<String, Step> step) {
		return this.jobBuilderFactory.get(step.getKey()).incrementer(new RunIdIncrementer()).start(step.getValue())
				.build();
	}

	private Map.Entry<String, Step> createStep(Submodel jobConfiguration) {
		String jobId = jobConfiguration.getIdShort();
		String jobName = (String) jobConfiguration.getSubmodelElement("jobName").getValue();
		List<String> dataReaderIds = getReaders(jobConfiguration);
		List<String> dataProcessorIds = getProcessors(jobConfiguration);
		String dataWriterId = getWriter(jobConfiguration);
		List<DataReader<Map<String, List<Object>>>> dataReader = getDataReaders(dataReaderIds);
		List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessors = getDataProcessors(
				dataProcessorIds);
		DataWriter<Map<String, List<Object>>> dataWriter = integratorPipelineConfiguration.getDataWriters()
				.get(dataWriterId);

		return new AbstractMap.SimpleEntry<>(jobName,
				integratorStepFactory.create(jobId, dataReader, dataProcessors, dataWriter));
	}

	private List<String> getReaders(Submodel jobConfiguration) {
		if (!jobConfiguration.getSubmodelElements().containsKey("readers"))
			throw new RuntimeException("Atleast one reader should be configured!");

		Map<String, ISubmodelElement> readersMap = ((SubmodelElementCollection) jobConfiguration
				.getSubmodelElement("readers")).getSubmodelElements();

		if (readersMap.isEmpty())
			throw new RuntimeException("Atleast one reader should be configured!");

		// Change to retrieve ID of the SM here
		return readersMap.values().stream().map(ReferenceElement.class::cast).map(this::getConfigId)
				.collect(Collectors.toList());
	}

	private List<String> getProcessors(Submodel jobConfiguration) {
		if (!jobConfiguration.getSubmodelElements().containsKey("processors")) {
			logger.info("No processors configured!");
			return new ArrayList<>();
		}

		Map<String, ISubmodelElement> processorsMap = ((SubmodelElementCollection) jobConfiguration
				.getSubmodelElement("processors")).getSubmodelElements();

		if (processorsMap.isEmpty()) {
			logger.info("No processors configured!");
			return new ArrayList<>();
		}

		return processorsMap.values().stream().map(ReferenceElement.class::cast).map(this::getConfigId)
				.collect(Collectors.toList());
	}
	
	private String getConfigId(ReferenceElement configReference) {
		IKey referenceKey = configReference.getValue().getKeys().get(0);

		return referenceKey.getValue();
	}

	private String getWriter(Submodel jobConfiguration) {
		if (!jobConfiguration.getSubmodelElements().containsKey("writer"))
			throw new RuntimeException("No writer configured!");

		return ((ReferenceElement) jobConfiguration.getSubmodelElement("writer")).getValue().getKeys().get(0).getValue();
	}

	private List<DataReader<Map<String, List<Object>>>> getDataReaders(List<String> dataReadersName) {
		return dataReadersName.stream().map(this::getDataReader).collect(Collectors.toList());
	}

	private DataReader<Map<String, List<Object>>> getDataReader(String dataReaderName) {
		return integratorPipelineConfiguration.getDataReaders().get(dataReaderName);
	}

	private List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> getDataProcessors(
			List<String> dataProcessorsName) {
		return dataProcessorsName.stream().map(this::getDataProcessor).collect(Collectors.toList());
	}

	private DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>> getDataProcessor(
			String dataProcessorName) {
		return integratorPipelineConfiguration.getDataProcessors().get(dataProcessorName);
	}

}
