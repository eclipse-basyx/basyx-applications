package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper.utils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public class JsonPathExpressionResolver {
	
	private String expression;
	private String data;
	private ReadContext readContext;
	
	public JsonPathExpressionResolver(String expression, String data) {
		this.expression = expression;
		this.data = data;
		configureJsonPath();
		readContext = JsonPath.parse(data);
	}

	public String getExpression() {
		return expression;
	}

	public String getData() {
		return data;
	}
	
	private void configureJsonPath() {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}
	
	public List<Object> parse() {
//		TypeRef<List<Object>> typeReferenceValue = new TypeRef<List<Object>>() {
//		};
//		
//		return readContext.read(expression, typeReferenceValue);
		
		Object result = JsonPath.read(data, expression);
		
		return Arrays.asList(result);
	}

}
