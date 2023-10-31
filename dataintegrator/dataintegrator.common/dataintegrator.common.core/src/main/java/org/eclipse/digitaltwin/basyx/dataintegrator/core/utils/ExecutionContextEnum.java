package org.eclipse.digitaltwin.basyx.dataintegrator.core.utils;

public enum ExecutionContextEnum {
	AAS_BUNDLES("aasBundles"),
	AAS_ENV("aasEnv"),
	AAS_DESCRIPTOR("aasDescriptor"),
	ASSET_ADMINISTRATION_SHELLS("assetAdministrationShells"),
	ASSET_ADMINISTRATION_SHELL("assetAdministrationShell"),
	SUBMODELS("submodels"),
	SUBMODEL("submodel"),
	PARAMETER("parameter");
	
    private final String name;

    ExecutionContextEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
