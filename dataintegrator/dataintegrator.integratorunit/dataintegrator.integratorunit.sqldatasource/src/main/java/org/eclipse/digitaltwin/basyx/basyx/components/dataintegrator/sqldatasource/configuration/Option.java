package org.eclipse.digitaltwin.basyx.basyx.components.dataintegrator.sqldatasource.configuration;

public class Option {
	
	private Boolean encrypt;
	private Boolean trustServerCertificate;
	
	public Option(Boolean encrypt, Boolean trustServerCertificate) {
		super();
		this.encrypt = encrypt;
		this.trustServerCertificate = trustServerCertificate;
	}

	public Boolean getEncrypt() {
		return encrypt;
	}

	public Boolean getTrustServerCertificate() {
		return trustServerCertificate;
	}
	
}
