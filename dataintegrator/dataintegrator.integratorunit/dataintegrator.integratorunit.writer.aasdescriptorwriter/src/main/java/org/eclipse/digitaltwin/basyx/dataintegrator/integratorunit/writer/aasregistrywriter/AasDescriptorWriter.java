package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aasregistrywriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer.DataWriter;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ExecutionContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import com.fasterxml.jackson.databind.ObjectMapper;

@StepScope
public class AasDescriptorWriter implements DataWriter<Map<String, List<Object>>>, StepExecutionListener {

	private Logger logger = LoggerFactory.getLogger(AasDescriptorWriter.class);

	private Submodel dataWriterConfiguration;
	public static final String TEMP_DIRECTORY = "basyx-temp";
	private Map<String, List<Object>> collectiveResult = new HashMap<>();

	public AasDescriptorWriter(Submodel dataWriterConfiguration) {
		this.dataWriterConfiguration = dataWriterConfiguration;
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		AasDescriptorCreator aasDescriptorCreator = new AasDescriptorCreator(collectiveResult, dataWriterConfiguration);
		
		List<AASDescriptor> aasDescriptors = aasDescriptorCreator.create();
		
		stepExecution.getExecutionContext().put(ExecutionContextEnum.AAS_DESCRIPTOR.getName(), aasDescriptors);
		
		return stepExecution.getExitStatus();
	}

	@Override
	public void write(List<? extends Map<String, List<Object>>> resultDefinitions) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		
		logger.info("Input to RegistryWriter : {}",
				mapper.writeValueAsString(resultDefinitions));
		
		resultDefinitions.stream().forEach(this::collectResult);
	}
	
	private void collectResult(Map<String, List<Object>> result) {
		result.entrySet().stream().forEach(this::addEntryToFinalResult);
	}
	
	private void addEntryToFinalResult(Entry<String, List<Object>> result) {
		if (collectiveResult.containsKey(result.getKey())) {
			List<Object> newValues = new ArrayList<>();
			newValues.addAll(collectiveResult.get(result.getKey()));
			newValues.addAll(result.getValue());
			collectiveResult.put(result.getKey(), newValues);
		} else {
			collectiveResult.put(result.getKey(), result.getValue());
		}
	}

	@Override
	public Submodel getDataWriterConfiguration() {
		return this.dataWriterConfiguration;
	}

}
