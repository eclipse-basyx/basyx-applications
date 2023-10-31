package org.eclipse.digitaltwin.basyx.dataintegrator.aas.component.configuration.utils;

public enum DIAasConfigurationEnum {
	DATA_READER("urn:di:aas:config:reader"),
	DATA_PROCESSOR("urn:di:aas:config:processor"),
	DATA_WRITER("urn:di:aas:config:writer"),
	JOB("urn:di:aas:config:job");
	
    private final String name;

    DIAasConfigurationEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
