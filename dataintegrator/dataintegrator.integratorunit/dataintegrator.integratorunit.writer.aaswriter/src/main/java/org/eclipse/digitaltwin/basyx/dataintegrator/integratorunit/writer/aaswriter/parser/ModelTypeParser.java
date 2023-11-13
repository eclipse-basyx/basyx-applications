package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser;

import java.util.List;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;

public class ModelTypeParser {
	
	private static final String EXPRESSION_PREFIX = "$..[?(@.idShort == '";
	private static final String EXPRESSION_SUFFIX = "')].modelType.name";
	private static final String EXPRESSION_DEFAULT_PARSE = "$..modelType.name";
	
	private String idShort;
	private ReadContext readContext;
	
	public ModelTypeParser(String idShort, ReadContext readContext) {
		super();
		this.idShort = idShort;
		this.readContext = readContext;
	}
	
	public String parse() {
		String exprForModelType = String.format(EXPRESSION_PREFIX + "%s" + EXPRESSION_SUFFIX, this.idShort);

		TypeRef<List<String>> typeRefModelType = new TypeRef<List<String>>() {
		};

		List<String> modelType = readContext.read(exprForModelType, typeRefModelType);
		
		if (modelType.isEmpty())
			throw new RuntimeException("Unable to find the ModelType for the idShort : " + idShort);
		
		return modelType.get(0);
	}
	
	public String parseDefault() {		
		TypeRef<List<String>> typeRefModelType = new TypeRef<List<String>>() {
		};
		
		List<String> modelType = readContext.read(EXPRESSION_DEFAULT_PARSE, typeRefModelType);
		
		if (modelType.isEmpty())
			throw new RuntimeException("Unable to find the ModelType for the idShort : " + idShort);
		
		return modelType.get(0);
	}

}
