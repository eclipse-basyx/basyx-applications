package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.context.SpringContext;

public class StencilLooker {
	
	private IIdentifier stencilAASId;
	private ConnectedAssetAdministrationShellManager conncectedShellManager;
	
	public StencilLooker(IIdentifier stencilAASId) {
		this.stencilAASId = stencilAASId;
		this.conncectedShellManager = SpringContext.getBean(ConnectedAssetAdministrationShellManager.class);
	}
	
	public AasEnv findStencil() {
		AasEnv aasEnv = new AasEnv();
		
		Collection<IAssetAdministrationShell> shells = Arrays.asList(conncectedShellManager.retrieveAAS(stencilAASId).getLocalCopy());
		Collection<IAsset> assets = shells.stream().map(IAssetAdministrationShell::getAsset).collect(Collectors.toList());
		Collection<ISubmodel> submodels = conncectedShellManager.retrieveSubmodels(stencilAASId).values().stream().map(ConnectedSubmodel.class::cast).map(ConnectedSubmodel::getLocalCopy).collect(Collectors.toList());
		
		aasEnv.setAssetAdministrationShells(shells);
		aasEnv.setAssets(assets);
		aasEnv.setSubmodels(submodels);
		
		return aasEnv;
	}
	
}
