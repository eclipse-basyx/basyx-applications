package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component;

import java.util.List;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregatorFactory;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.restapi.api.IAASAPIFactory;
import org.eclipse.basyx.components.aas.aascomponent.AbstractAASServerComponentFactory;
import org.eclipse.basyx.components.aas.aascomponent.IAASServerDecorator;
import org.eclipse.basyx.submodel.aggregator.api.ISubmodelAggregatorFactory;
import org.eclipse.basyx.submodel.restapi.api.ISubmodelAPIFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.aas.factory.DelegationAasAggregatorFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.submodel.factory.DelegationSubmodelAggregatorFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.aas.factory.DelegationAasAPIFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.submodel.factory.DelegationSubmodelAPIFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationAasServerComponentFactory extends AbstractAASServerComponentFactory {
	
	private JobRunner jobRunner;
	
	public DelegationAasServerComponentFactory(JobRunner jobRunner, List<IAASServerDecorator> decorators, IAASRegistry aasServerRegistry) {
		this.jobRunner = jobRunner;
		this.aasServerRegistry = aasServerRegistry;
		this.aasServerDecorators = decorators;
	}

	@Override
	protected ISubmodelAPIFactory createSubmodelAPIFactory() {
		return new DelegationSubmodelAPIFactory(this.jobRunner);
	}

	@Override
	protected ISubmodelAggregatorFactory createSubmodelAggregatorFactory(ISubmodelAPIFactory submodelAPIFactory) {
		return new DelegationSubmodelAggregatorFactory(this.jobRunner, submodelAPIFactory);
	}

	@Override
	protected IAASAPIFactory createAASAPIFactory() {
		return new DelegationAasAPIFactory(this.jobRunner);
	}

	@Override
	protected IAASAggregatorFactory createAASAggregatorFactory(IAASAPIFactory aasAPIFactory,
			ISubmodelAggregatorFactory submodelAggregatorFactory) {
		return new DelegationAasAggregatorFactory(this.jobRunner, aasAPIFactory, submodelAggregatorFactory, this.aasServerRegistry);
	}

}
