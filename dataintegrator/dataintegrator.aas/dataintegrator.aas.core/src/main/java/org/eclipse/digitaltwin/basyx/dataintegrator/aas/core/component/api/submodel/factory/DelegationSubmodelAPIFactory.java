package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.submodel.factory;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.restapi.api.ISubmodelAPI;
import org.eclipse.basyx.submodel.restapi.api.ISubmodelAPIFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.submodel.DelegationSubmodelAPI;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationSubmodelAPIFactory implements ISubmodelAPIFactory {
	
	private JobRunner jobRunner;

	public DelegationSubmodelAPIFactory(JobRunner jobRunner) {
		super();
		this.jobRunner = jobRunner;
	}

	@Override
	public ISubmodelAPI getSubmodelAPI(Submodel submodel) {
		return new DelegationSubmodelAPI(this.jobRunner, submodel);
	}

}
