package org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.descriptor.factory;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ExecutionContextEnum;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

public class AasDescriptorAggregatorFactory {
	
	private List<JobExecution> jobExecutions;
	
	public AasDescriptorAggregatorFactory(List<JobExecution> jobExecutions) {
		super();
		this.jobExecutions = jobExecutions;
	}
	
	public List<AASDescriptor> create() {
		List<StepExecution> stepExecutions = jobExecutions.stream().flatMap(jobExecution -> getStepExecutions(jobExecution).stream()).collect(Collectors.toList());
		
		return stepExecutions.stream().flatMap(stepExecution -> addToAasEnv(stepExecution).stream()).collect(Collectors.toList());
	}
	
	private List<StepExecution> getStepExecutions(JobExecution jobExecution) {
		return jobExecution.getStepExecutions().stream().collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	private List<AASDescriptor> addToAasEnv(StepExecution stepExecution) {
		return (List<AASDescriptor>) stepExecution.getExecutionContext().get(ExecutionContextEnum.AAS_DESCRIPTOR.getName());
	}

}
