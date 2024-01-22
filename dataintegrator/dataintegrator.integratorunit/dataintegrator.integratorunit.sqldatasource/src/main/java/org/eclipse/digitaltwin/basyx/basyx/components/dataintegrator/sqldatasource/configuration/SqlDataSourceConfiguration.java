package org.eclipse.digitaltwin.basyx.basyx.components.dataintegrator.sqldatasource.configuration;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;

public class SqlDataSourceConfiguration {
	
	private static final String DRIVER = "jdbc:sqlserver://";
	
	private SubmodelElementCollection dataSourceConfig;

	public SqlDataSourceConfiguration(SubmodelElementCollection dataSourceConfig) {
		this.dataSourceConfig = dataSourceConfig;
	}

	public String getDatabaseName() {
		return (String) dataSourceConfig.getSubmodelElement("databaseName").getValue();
	}

	public Option getOptions() {
		SubmodelElementCollection optionConfig = (SubmodelElementCollection) dataSourceConfig.getSubmodelElement("options");
				
		Boolean encrypt = (Boolean) optionConfig.getSubmodelElement("encrypt").getValue();
		Boolean trustServerCertificate = (Boolean) optionConfig.getSubmodelElement("trustServerCertificate").getValue();
		
		return new Option(encrypt, trustServerCertificate);
	}

	public Credential getCredential() {
		SubmodelElementCollection optionConfig = (SubmodelElementCollection) dataSourceConfig.getSubmodelElement("credential");
		
		String username = (String) optionConfig.getSubmodelElement("username").getValue();
		String password = (String) optionConfig.getSubmodelElement("password").getValue();
		
		return new Credential(username, password);
	}
	
	public String getHost() {
		return (String) dataSourceConfig.getSubmodelElement("host").getValue();
	}
	
	public Integer getPort() {
		return (Integer) dataSourceConfig.getSubmodelElement("port").getValue();
	}
	
	public String getConnectionUrl() {
		return DRIVER + this.getHost() + getPortIfConfigured() + ";databaseName=" + this.getDatabaseName() + getOptionsIfConfigured();
	}

	private String getOptionsIfConfigured() {
		return getEncryptOptionIfConfigured() + getTrustServerCertificateIfConfigured();
	}

	private String getEncryptOptionIfConfigured() {
		return this.getOptions().getEncrypt() == null ? StringUtils.EMPTY : ";encrypt=" + this.getOptions().getEncrypt();
	}
	
	private String getTrustServerCertificateIfConfigured() {
		return this.getOptions().getTrustServerCertificate() == null ? StringUtils.EMPTY : ";trustServerCertificate=" + this.getOptions().getTrustServerCertificate();
	}

	private String getPortIfConfigured() {
		return this.getPort() == null ? StringUtils.EMPTY : ":" + this.getPort();
	}

}
