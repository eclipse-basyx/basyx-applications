package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

public class ReadContextFactory {
	
	private String serializedJson;
	
	public ReadContextFactory(String serializedJson) {
		this.serializedJson = serializedJson;
	}
	
	public ReadContext create() {
		return JsonPath.parse(serializedJson);
	}

}
