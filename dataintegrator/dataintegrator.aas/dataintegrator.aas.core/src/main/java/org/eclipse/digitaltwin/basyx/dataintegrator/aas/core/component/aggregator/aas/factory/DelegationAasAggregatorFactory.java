package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.aas.factory;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregatorFactory;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.restapi.api.IAASAPIFactory;
import org.eclipse.basyx.submodel.aggregator.api.ISubmodelAggregatorFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.aas.DelegationAasAggregator;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationAasAggregatorFactory implements IAASAggregatorFactory {
	
	private JobRunner jobRunner;
	private IAASRegistry registry;
	private IAASAPIFactory aasAPIFactory;
	private ISubmodelAggregatorFactory submodelAggregatorFactory;

	public DelegationAasAggregatorFactory(JobRunner jobRunner, IAASAPIFactory aasAPIFactory, ISubmodelAggregatorFactory submodelAggregatorFactory, IAASRegistry registry) {
		super();
		this.jobRunner = jobRunner;
		this.aasAPIFactory = aasAPIFactory;
		this.submodelAggregatorFactory = submodelAggregatorFactory;
		this.registry = registry;
	}

	@Override
	public IAASAggregator create() {
		return new DelegationAasAggregator(jobRunner, submodelAggregatorFactory, aasAPIFactory, registry);
	}

}
