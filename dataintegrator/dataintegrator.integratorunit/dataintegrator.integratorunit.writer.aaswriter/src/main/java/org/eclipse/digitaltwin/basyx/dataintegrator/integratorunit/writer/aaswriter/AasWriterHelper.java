package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;

public class AasWriterHelper {
	
	private List<IAssetAdministrationShell> assetAdministrationShells = new ArrayList<>();
	private List<ISubmodel> submodels = new ArrayList<>();
	private List<IAsset> assets = new ArrayList<>();
	private List<IConceptDescription> conceptDescriptions =  new ArrayList<>();
	
	public List<IAssetAdministrationShell> getAssetAdministrationShells() {
		return assetAdministrationShells;
	}
	
	public void addAssetAdministrationShells(List<IAssetAdministrationShell> assetAdministrationShells) {
		this.assetAdministrationShells.addAll(assetAdministrationShells);
	}
	
	public List<ISubmodel> getSubmodels() {
		return submodels;
	}
	
	public void addSubmodels(List<ISubmodel> submodels) {
		this.submodels.addAll(submodels);
	}
	
	public List<IAsset> getAssets() {
		return assets;
	}
	
	public void addAssets(List<IAsset> assets) {
		this.assets.addAll(assets);
	}
	
	public List<IConceptDescription> getConceptDescriptions() {
		return conceptDescriptions;
	}
	
	public void addConceptDescriptions(List<IConceptDescription> conceptDescriptions) {
		this.conceptDescriptions.addAll(conceptDescriptions);
	}
	
	public AasEnv createAasEnv() {
		return new AasEnv(this.assetAdministrationShells, this.assets, this.conceptDescriptions, this.submodels);
	}

}
