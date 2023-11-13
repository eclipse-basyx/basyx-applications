package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.aggregator.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.submodel.metamodel.api.IElement;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.aggregator.AasResultAggregator;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.aggregator.ResultAggregator;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.aggregator.SubmodelResultAggregator;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ExecutionContextEnum;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

public class ResultAggregatorFactory {

	private List<JobExecution> jobExecutions;
	private List<IAssetAdministrationShell> assetAdministrationShells = new ArrayList<>();
	private List<ISubmodel> submodels = new ArrayList<>();
	private List<IAsset> assets = new ArrayList<>();
	private List<IConceptDescription> conceptDescriptions =  new ArrayList<>();

	public ResultAggregatorFactory(List<JobExecution> jobExecutions) {
		super();
		this.jobExecutions = jobExecutions;
	}

	public List<JobExecution> getJobExecutions() {
		return jobExecutions;
	}

	public List<IElement> create(ResultAggregator<?> resultAggregator) {
		List<StepExecution> stepExecutions = jobExecutions.stream().flatMap(jobExecution -> getStepExecutions(jobExecution).stream()).collect(Collectors.toList());			

		if (resultAggregator instanceof AasResultAggregator) {			
			return stepExecutions.stream().flatMap(stepExecution -> getAasFromStep(stepExecution).stream()).collect(Collectors.toList());
		} else if (resultAggregator instanceof SubmodelResultAggregator) {
			return stepExecutions.stream().flatMap(stepExecution -> getSubmodelsFromStep(stepExecution).stream()).collect(Collectors.toList());
		}

		return new ArrayList<>();

	}
	
	public AasEnv createAasEnv() {
		List<StepExecution> stepExecutions = jobExecutions.stream().flatMap(jobExecution -> getStepExecutions(jobExecution).stream()).collect(Collectors.toList());
		
		stepExecutions.stream().forEach(this::addToAasEnv);
		
		return new AasEnv(this.assetAdministrationShells, this.assets, this.conceptDescriptions, this.submodels);
	}

	private List<StepExecution> getStepExecutions(JobExecution jobExecution) {
		return jobExecution.getStepExecutions().stream().collect(Collectors.toList());
	}

	private List<IAssetAdministrationShell> getAasFromStep(StepExecution stepExecution) {
		@SuppressWarnings("unchecked")
		List<IAssetAdministrationShell> assetAdministrationShells = (List<IAssetAdministrationShell>) stepExecution
				.getExecutionContext().get(ExecutionContextEnum.ASSET_ADMINISTRATION_SHELLS.getName());

		return assetAdministrationShells;
	}
	
	private List<ISubmodel> getSubmodelsFromStep(StepExecution stepExecution) {
		@SuppressWarnings("unchecked")
		List<ISubmodel> submodels = (List<ISubmodel>) stepExecution
				.getExecutionContext().get(ExecutionContextEnum.SUBMODELS.getName());

		return submodels;
	}
	
	private void addToAasEnv(StepExecution stepExecution) {
		AasEnv aasEnvContext = (AasEnv) stepExecution.getExecutionContext().get(ExecutionContextEnum.AAS_ENV.getName());
		assetAdministrationShells.addAll(aasEnvContext.getAssetAdministrationShells());
		submodels.addAll(aasEnvContext.getSubmodels());
		assets.addAll(aasEnvContext.getAssets());
		conceptDescriptions.addAll(aasEnvContext.getConceptDescriptions());
	}

}
