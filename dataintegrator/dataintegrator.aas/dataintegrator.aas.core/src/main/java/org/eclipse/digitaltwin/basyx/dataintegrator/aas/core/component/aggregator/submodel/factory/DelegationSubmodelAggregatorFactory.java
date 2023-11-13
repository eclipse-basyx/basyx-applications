package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.submodel.factory;

import org.eclipse.basyx.submodel.aggregator.api.ISubmodelAggregator;
import org.eclipse.basyx.submodel.aggregator.api.ISubmodelAggregatorFactory;
import org.eclipse.basyx.submodel.restapi.api.ISubmodelAPIFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.submodel.DelegationSubmodelAggregator;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationSubmodelAggregatorFactory implements ISubmodelAggregatorFactory {
	
	private ISubmodelAPIFactory submodelAPIFactory;
	private JobRunner jobRunner;
	
	public DelegationSubmodelAggregatorFactory(JobRunner jobRunner, ISubmodelAPIFactory submodelAPIFactory) {
		super();
		this.submodelAPIFactory = submodelAPIFactory;
		this.jobRunner = jobRunner;
	}

	@Override
	public ISubmodelAggregator create() {
		return new DelegationSubmodelAggregator(this.jobRunner, this.submodelAPIFactory);
	}

}
