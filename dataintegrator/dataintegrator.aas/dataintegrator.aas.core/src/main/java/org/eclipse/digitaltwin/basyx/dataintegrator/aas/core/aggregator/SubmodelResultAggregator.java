package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.aggregator;

import java.util.List;

import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;

public class SubmodelResultAggregator implements ResultAggregator<List<ISubmodel>>{
	
	private List<ISubmodel> submodels;
	
	public void setSubmodels(List<ISubmodel> submodels) {
		this.submodels = submodels;
	}

	public List<ISubmodel> getValue() {
		return submodels;
	}

}
