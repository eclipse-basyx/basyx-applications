package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.aas.factory;

import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.restapi.api.IAASAPI;
import org.eclipse.basyx.aas.restapi.api.IAASAPIFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.aas.DelegationAasAPI;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationAasAPIFactory implements IAASAPIFactory {
	
	private JobRunner jobRunner;	

	public DelegationAasAPIFactory(JobRunner jobRunner) {
		super();
		this.jobRunner = jobRunner;
	}

	@Override
	public IAASAPI getAASApi(AssetAdministrationShell aas) {
		DelegationAasAPI delegationAasAPI = new DelegationAasAPI(this.jobRunner, aas.getIdentification().getId());
		delegationAasAPI.setAAS(aas);
		
		return delegationAasAPI;
	}

}
