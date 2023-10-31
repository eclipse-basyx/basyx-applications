package org.eclipse.digitaltwin.basyx.basyx.components.dataintegrator.sqldatasource.configuration;

public class Credential {
	
	private String username;
	private String password;
	
	public Credential(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
