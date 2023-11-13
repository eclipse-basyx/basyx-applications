package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;

public class ProcessedSMERepository {
	
	private Map<String, List<SubmodelElement>> processedSMEs;
	private Set<String> processedConfigurationIDs;
	
	public ProcessedSMERepository(Map<String, List<SubmodelElement>> processedSMEs,
			Set<String> processedConfigurationIDs) {
		super();
		this.processedSMEs = processedSMEs;
		this.processedConfigurationIDs = processedConfigurationIDs;
	}

	public Map<String, List<SubmodelElement>> getProcessedSMEs() {
		return processedSMEs;
	}

	public Set<String> getProcessedConfigurationIDs() {
		return processedConfigurationIDs;
	}

}
