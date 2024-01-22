package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.aas;

import java.util.Optional;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.restapi.api.IAASAPI;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationAasAPI implements IAASAPI {
	
	private AssetAdministrationShell assetAdministrationShell;
	private String aasId;
	private JobRunner jobRunner;
	
	public DelegationAasAPI(JobRunner jobRunner, String aasId) {
		this.jobRunner = jobRunner;
		this.aasId = aasId;
	}

	public String getAasId() {
		return aasId;
	}

	public JobRunner getJobRunner() {
		return jobRunner;
	}

	public void setAAS(AssetAdministrationShell assetAdministrationShell) {
		this.assetAdministrationShell = assetAdministrationShell;
	}

	@Override
	public IAssetAdministrationShell getAAS() {
		if (assetAdministrationShell != null)
			return assetAdministrationShell;
		
		AasEnv aasEnv = this.jobRunner.getAasEnv(aasId);
		
		Optional<IAssetAdministrationShell> aas = retrieveAas(aasEnv);
		
		if (aas.isEmpty()) 
			throw new ResourceNotFoundException("The AAS " + aasId + " could not be found.");
		
		return aas.get();
	}

	@Override
	public void addSubmodel(IReference submodel) {
		// TODO Not implemented
		
	}

	@Override
	public void removeSubmodel(String idShort) {
		// TODO Not implemented
		
	}
	
	private Optional<IAssetAdministrationShell> retrieveAas(AasEnv aasEnv) {
		return aasEnv.getAssetAdministrationShells().stream().filter(aas -> aas.getIdentification().getId().equals(aasId)).findAny();
	}

}
