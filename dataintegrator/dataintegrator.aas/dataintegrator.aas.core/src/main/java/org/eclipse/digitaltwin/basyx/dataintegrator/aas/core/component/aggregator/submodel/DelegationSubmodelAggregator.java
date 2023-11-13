package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.submodel;

import org.eclipse.basyx.submodel.aggregator.SubmodelAggregator;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.restapi.api.ISubmodelAPIFactory;
import org.eclipse.basyx.vab.exception.FeatureNotImplementedException;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationSubmodelAggregator extends SubmodelAggregator {
	
	@SuppressWarnings("unused")
	private JobRunner jobRunner;

	public DelegationSubmodelAggregator(JobRunner jobRunner, ISubmodelAPIFactory submodelApiFactory) {
		super(submodelApiFactory);
		this.jobRunner = jobRunner;
	}
	
	@Override
	public void deleteSubmodelByIdentifier(IIdentifier identifier) {
		throw new FeatureNotImplementedException();
	}
	
	@Override
	public void deleteSubmodelByIdShort(String idShort) {
		throw new FeatureNotImplementedException();
	}

}
