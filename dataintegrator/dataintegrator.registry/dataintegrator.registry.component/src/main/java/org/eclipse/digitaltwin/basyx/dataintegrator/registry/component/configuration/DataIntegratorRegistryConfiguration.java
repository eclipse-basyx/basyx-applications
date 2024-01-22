package org.eclipse.digitaltwin.basyx.dataintegrator.registry.component.configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.components.registry.servlet.RegistryServlet;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.configuration.cors.CorsFilter;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.configuration.integratorpipeline.IntegratorPipelineConfiguration;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.factory.IntegratorUnitFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor.DataProcessor;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.DataReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer.DataWriter;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.backend.InMemoryDIRegistry;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.runner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DataIntegratorRegistryConfiguration {
	
	private static Logger logger = LoggerFactory.getLogger(DataIntegratorRegistryConfiguration.class);
	
	@Value("${accessControlAllowOrigin:*}")
	private String accessControlAllowOrigin;
	
	@Value("${contextPath:}")
	private String contextPath;
	
	@Value("${chunkSize:2}")
	private int chunkSize;

	@Autowired
	private JobRunner jobRunner;
	
	@Autowired
	private DIRegistryConfigurationRepository repository;

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IntegratorPipelineConfiguration jobConnectorConfiguration() {
		
		logger.info("Using configuration : {}", this.getClass().getName());
		logger.info("Current Reader configurations in repository : {}", repository.getDataReaderConfigs().stream()
				.map(Submodel::getIdShort).collect(Collectors.toList()).toString());
		logger.info("Current Processor configurations in repository : {}", repository.getDataProcessorConfigs().stream()
				.map(Submodel::getIdShort).collect(Collectors.toList()).toString());
		logger.info("Current Writer configurations in repository : {}", repository.getDataWriterConfig().stream()
				.map(Submodel::getIdShort).collect(Collectors.toList()).toString());
		logger.info("Current Job configurations in repository : {}",
				repository.getJobConfigs().stream().map(Submodel::getIdShort).collect(Collectors.toList()).toString());

		List<DataReader<Map<String, List<Object>>>> dataReaders = getConfiguredDataReaders();

		List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessors = getConfiguredDataProcessors();

		DataWriter<Map<String, List<Object>>> dataWriter = getConfiguredDataWriter();

		return new IntegratorPipelineConfiguration.Builder().addDataReaders(dataReaders)
				.addDataProcessors(dataProcessors).addDataWriter(dataWriter).build();
	}
	
	@Bean
	@Qualifier(value = "Job")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public List<Submodel> getJobConfig() {
		return repository.getJobConfigs();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public int chunk() {
		return chunkSize;
	}

	@Bean
	public ServletRegistrationBean<HttpServlet> exampleServletBean() {
		ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(createRegistryServlet(),
				contextPath + "/*");
		bean.setLoadOnStartup(1);
		return bean;
	}
	
	@Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorsFilter(accessControlAllowOrigin));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }

	private HttpServlet createRegistryServlet() {
		IAASRegistry registryBackend = createInMemoryRegistryBackend();
		
		return new RegistryServlet(registryBackend);
	}

	private IAASRegistry createInMemoryRegistryBackend() {
		return new InMemoryDIRegistry(this.jobRunner);
	}
	
	@SuppressWarnings("unchecked")
	private DataWriter<Map<String, List<Object>>> getConfiguredDataWriter() {
		return repository.getDataWriterConfig().stream()
				.map(smConfig -> new IntegratorUnitFactory(DataWriter.class, smConfig).create()).map(DataWriter.class::cast)
				.collect(Collectors.toList()).get(0);
	}

	@SuppressWarnings("unchecked")
	private List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> getConfiguredDataProcessors() {
		return repository
				.getDataProcessorConfigs().stream()
				.map(smConfig -> (DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>) new IntegratorUnitFactory(
						DataProcessor.class, smConfig).create())
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private List<DataReader<Map<String, List<Object>>>> getConfiguredDataReaders() {
		return repository.getDataReaderConfigs().stream().map(
				smConfig -> (DataReader<Map<String, List<Object>>>) new IntegratorUnitFactory(DataReader.class, smConfig)
						.create())
				.collect(Collectors.toList());
	}

}
