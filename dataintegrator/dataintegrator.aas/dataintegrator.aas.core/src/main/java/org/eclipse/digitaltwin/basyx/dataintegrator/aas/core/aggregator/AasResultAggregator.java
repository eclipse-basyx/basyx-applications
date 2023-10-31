package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.aggregator;

import java.util.List;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;

public class AasResultAggregator implements ResultAggregator<List<IAssetAdministrationShell>> {
	
	private List<IAssetAdministrationShell> assetAdministrationShells;
	
	public void setAssetAdministrationShells(List<IAssetAdministrationShell> assetAdministrationShells) {
		this.assetAdministrationShells = assetAdministrationShells;
	}
	
	public List<IAssetAdministrationShell> getValue() {
		return assetAdministrationShells;
	}

}
