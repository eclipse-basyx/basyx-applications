package org.eclipse.digitaltwin.basyx.dataintegrator.registry.component.configuration.utils;

import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ConfigurationType;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DIRegistryConfigurationType implements ConfigurationType {
    
    @Override
	public String getDataReaderName() {
		return DIRegistryConfigurationEnum.DATA_READER.getName();
	}

	@Override
	public String getDataProcessorName() {
		return DIRegistryConfigurationEnum.DATA_PROCESSOR.getName();
	}

	@Override
	public String getDataWriterName() {
		return DIRegistryConfigurationEnum.DATA_WRITER.getName();
	}

	@Override
	public String getIntegratorPipelineName() {
		return DIRegistryConfigurationEnum.JOB.getName();
	}

}
