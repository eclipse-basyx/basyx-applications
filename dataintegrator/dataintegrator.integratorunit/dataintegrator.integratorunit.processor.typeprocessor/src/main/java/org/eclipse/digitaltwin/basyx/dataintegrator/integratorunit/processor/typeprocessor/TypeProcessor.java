package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.processor.typeprocessor;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.spel.support.StandardTypeConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

@StepScope
public class TypeProcessor implements DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>> {

	private Logger logger = LoggerFactory.getLogger(TypeProcessor.class);

	private Submodel dataProcessorConfiguration;
	private TypeConverter typeConverter = new StandardTypeConverter();

	public TypeProcessor(Submodel dataProcessorConfiguration) {
		super();
		this.dataProcessorConfiguration = dataProcessorConfiguration;
	}

	@Override
	public Submodel getDataProcessorConfiguration() {
		return dataProcessorConfiguration;
	}

	@Override
	public Map<String, List<Object>> process(Map<String, List<Object>> resultDefinitions) throws Exception {
//		ObjectMapper mapper = new ObjectMapper();
		
//		logger.info("Input to SqlProcessor : {}",
//				mapper.writeValueAsString(resultDefinitions));

		resultDefinitions.entrySet().stream().forEach(this::processResult);

//		logger.info("Output from SqlProcessor : {}",
//				mapper.writeValueAsString(resultDefinitions));
		
		return resultDefinitions;
	}

	private void processResult(Map.Entry<String, List<Object>> resultEntry) {
		List<Object> values = resultEntry.getValue();
	    IntStream.range(0, values.size())
	             .forEach(i -> convertValue(values.get(i), resultEntry, i));
	}

	private void convertValue(Object value, Map.Entry<String, List<Object>> entry, int i) {
		SubmodelElementCollection sqlProcessorConfigurations = (SubmodelElementCollection) dataProcessorConfiguration.getSubmodelElement("configurations");

		String uniqueId = entry.getKey();

		if (!sqlProcessorConfigurations.getSubmodelElements().containsKey(uniqueId))
			return;
		
		Property targetTypeProperty = (Property) ((SubmodelElementCollection) sqlProcessorConfigurations.getSubmodelElement(uniqueId)).getSubmodelElement("targetType");
		
		entry.getValue().set(i, convertValue(value, (String) targetTypeProperty.getValue()));
	}

	private Object convertValue(Object value, String targetType) {
		Class<?> targetClass = null;
		try {
			targetClass = Class.forName(targetType);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return typeConverter.convertValue(value, TypeDescriptor.forObject(value), TypeDescriptor.valueOf(targetClass));
	}

}
