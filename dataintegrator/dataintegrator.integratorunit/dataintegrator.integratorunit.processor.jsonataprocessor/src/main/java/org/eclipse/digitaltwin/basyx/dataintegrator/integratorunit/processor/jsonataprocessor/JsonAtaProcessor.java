package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.processor.jsonataprocessor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import com.api.jsonata4java.expressions.EvaluateException;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@StepScope
public class JsonAtaProcessor implements DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>> {

	private Logger logger = LoggerFactory.getLogger(JsonAtaProcessor.class);
	
	private Submodel dataProcessorConfiguration;
	private ObjectMapper mapper = new ObjectMapper();

	public JsonAtaProcessor(Submodel dataProcessorConfiguration) {
		super();
		this.dataProcessorConfiguration = dataProcessorConfiguration;
	}
	
	@Override
	public Submodel getDataProcessorConfiguration() {
		return dataProcessorConfiguration;
	}

	@Override
	public Map<String, List<Object>> process(Map<String, List<Object>> resultDefinitions) throws Exception {
		logger.info("JSONata Process started");
//		logger.info("Input to jsonATAProcessor : {}",
//				mapper.writeValueAsString(resultDefinitions));
		
		resultDefinitions.entrySet().stream().forEach(this::applyTransformation);
		
//		logger.info("Output from jsonATAProcessor : {}",
//				mapper.writeValueAsString(resultDefinitions));
		
		logger.info("JSONata Process finished");
		
		return resultDefinitions;
	}
	
	@SuppressWarnings("unchecked")
	private void applyTransformation(Entry<String, List<Object>> resultEntry) {
		SubmodelElementCollection jsonATAConfigurations = (SubmodelElementCollection) dataProcessorConfiguration.getSubmodelElement("configurations");
		
		String uniqueId = resultEntry.getKey();
		
		if (!jsonATAConfigurations.getSubmodelElements().containsKey(uniqueId))
			return;
		
		Property targetTypeProperty = (Property) ((SubmodelElementCollection) jsonATAConfigurations.getSubmodelElement(uniqueId)).getSubmodelElement("expression");
		
		Object transformedValue = transformValue(resultEntry, (String) targetTypeProperty.getValue());
		
		if (transformedValue instanceof List<?>) {
			resultEntry.setValue((List<Object>) transformedValue);
		} else {
			mergeTransformedValue(resultEntry, transformedValue);
		}
	}
	
	
	private void mergeTransformedValue(Entry<String, List<Object>> resultEntry, Object transformedValue) {
		List<Object> values = resultEntry.getValue();
		
		IntStream.range(0, values.size())
        .forEach(i -> merge(values.get(i), transformedValue, resultEntry, i));
	}

	private void merge(Object val, Object transformedValue, Entry<String, List<Object>> resultEntry, int i) {
		if (val.equals(transformedValue))
			return;
		
		resultEntry.getValue().set(i, transformedValue);
	}

	private Object transformValue(Entry<String, List<Object>> result, String expression) {
		JsonNode inputJsonNode = mapper.convertValue(result, JsonNode.class);
		
		Expressions expressions = parseExpression(expression);
		
		JsonNode evaluatedJsonNode = evaluateExpression(inputJsonNode, expressions);
		
		return mapEvaluatedValueToObjectType(evaluatedJsonNode);
	}
	
	private Expressions parseExpression(String expression) {
		Expressions expressions = null;
		
		try {
			expressions = Expressions.parse(expression);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return expressions;
	}

	private JsonNode evaluateExpression(JsonNode inputJsonNode, Expressions expressions) {
		JsonNode evaluatedJsonNode = null;
		
		try {
			evaluatedJsonNode = expressions.evaluate(inputJsonNode);
		} catch (EvaluateException e) {
			e.printStackTrace();
		}
		
		return evaluatedJsonNode;
	}
	
	private Object mapEvaluatedValueToObjectType(JsonNode evaluatedJsonNode) {
		Object evaluatedValue = null;
		
		try {
			evaluatedValue = mapper.treeToValue(evaluatedJsonNode, Object.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		return evaluatedValue;
	}

}
