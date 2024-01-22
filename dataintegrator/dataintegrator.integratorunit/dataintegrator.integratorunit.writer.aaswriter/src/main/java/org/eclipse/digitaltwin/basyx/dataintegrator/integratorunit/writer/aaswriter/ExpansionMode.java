package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter;

public enum ExpansionMode {
	INLINE("inline"),
	MULTI("multi");
	
	private final String name;

	ExpansionMode(String name) {
		this.name = name;
	}
	
	public static ExpansionMode valueOfLabel(String label) {
        for (ExpansionMode expansionMode : values()) {
            if (expansionMode.name.equals(label)) {
                return expansionMode;
            }
        }
        return null;
    }
	
	public String getName() {
        return name;
    }

}
