package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.submodel;

import java.util.Optional;

import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.restapi.operation.DelegatedInvocationManager;
import org.eclipse.basyx.submodel.restapi.vab.VABSubmodelAPI;
import org.eclipse.basyx.vab.exception.FeatureNotImplementedException;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;

public class DelegationSubmodelAPI extends VABSubmodelAPI {
	
	private String submodelId;
	private JobRunner jobRunner;
	private Submodel submodel;
	
	public DelegationSubmodelAPI(JobRunner jobRunner, Submodel submodel) {
		this(jobRunner, submodel, new DelegatedInvocationManager(new HTTPConnectorFactory()));
	}
	
	public DelegationSubmodelAPI(JobRunner jobRunner, Submodel submodel, DelegatedInvocationManager invocationManager) {
		super(new VABLambdaProvider(submodel));
		this.jobRunner = jobRunner;
		this.submodelId = submodel.getIdentification().getId();
		this.submodel = submodel;
	}
	
	public String getSubmodelId() {
		return submodelId;
	}

	@Override
	public ISubmodel getSubmodel() {
		if (this.submodel != null)
			return submodel;
		
		AasEnv aasEnv = this.jobRunner.getAasEnv("");
		
		Optional<ISubmodel> submodel = aasEnv.getSubmodels().stream().filter(sm -> sm.getIdentification().getId().equals(submodelId)).findAny();
		
		if (submodel.isEmpty())
			throw new ResourceNotFoundException("The submodel " + submodelId + " could not be found.");
		
		return submodel.get();
	}

	@Override
	public void addSubmodelElement(ISubmodelElement elem) {
		throw new FeatureNotImplementedException();
		
	}

	@Override
	public void addSubmodelElement(String idShortPath, ISubmodelElement elem) {
		throw new FeatureNotImplementedException();
	}

	@Override
	public void deleteSubmodelElement(String idShortPath) {
		throw new FeatureNotImplementedException();
		
	}

	@Override
	public void updateSubmodelElement(String idShortPath, Object newValue) {
		throw new FeatureNotImplementedException();
		
	}

}
