package org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.handler;

import java.util.List;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.registration.memory.IRegistryHandler;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.runner.JobRunner;
import org.springframework.beans.factory.annotation.Autowired;

public class IntegratorRegistryHandler implements IRegistryHandler {
	
	private JobRunner jobRunner;
	
	@Autowired
	public IntegratorRegistryHandler(JobRunner jobRunner) {
		super();
		this.jobRunner = jobRunner;
	}
	
	public JobRunner getJobRunner() {
		return jobRunner;
	}

	@Override
	public boolean contains(IIdentifier id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove(IIdentifier id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(AASDescriptor descriptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(AASDescriptor descriptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AASDescriptor get(IIdentifier id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AASDescriptor> getAll() {
		return this.jobRunner.getAasDescriptor();
	}

}
