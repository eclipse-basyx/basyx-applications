package org.eclipse.digitaltwin.basyx.dataintegrator.registry.component.configuration.utils;

public enum DIRegistryConfigurationEnum {
	DATA_READER("urn:basyx-di:registry:config:reader"),
	DATA_PROCESSOR("urn:basyx-di:registry:config:processor"),
	DATA_WRITER("urn:basyx-di:registry:config:writer"),
	JOB("urn:basyx-di:registry:config:job");
	
    private final String name;

    DIRegistryConfigurationEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
