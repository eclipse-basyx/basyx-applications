package org.eclipse.digitaltwin.basyx.dataintegrator.core;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;

public interface ConfigurationRepository {
	
	public void addConfiguration(Submodel config);
	
	public void updateConfiguration(Submodel config);
	
	public void removeConfiguration(String configId);

}
