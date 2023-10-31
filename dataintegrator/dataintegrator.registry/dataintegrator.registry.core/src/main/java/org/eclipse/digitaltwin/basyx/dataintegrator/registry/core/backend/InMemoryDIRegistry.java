package org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.backend;

import org.eclipse.basyx.aas.registration.memory.AASRegistry;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.handler.IntegratorRegistryHandler;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.runner.JobRunner;

public class InMemoryDIRegistry extends AASRegistry {
	
	public InMemoryDIRegistry(JobRunner jobRunner) {
		super(new IntegratorRegistryHandler(jobRunner));
	}

}
