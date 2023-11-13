package org.eclipse.digitaltwin.basyx.dataintegrator.aas.component.configuration.utils;

import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ConfigurationType;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DIAasConfigurationType implements ConfigurationType {
	
	@Override
	public String getDataReaderName() {
		return DIAasConfigurationEnum.DATA_READER.getName();
	}

	@Override
	public String getDataProcessorName() {
		return DIAasConfigurationEnum.DATA_PROCESSOR.getName();
	}

	@Override
	public String getDataWriterName() {
		return DIAasConfigurationEnum.DATA_WRITER.getName();
	}

	@Override
	public String getIntegratorPipelineName() {
		return DIAasConfigurationEnum.JOB.getName();
	}

}
