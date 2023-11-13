package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper.utils.JsonPathExpressionResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Special mapper that maps defined keyIndex as Key along with suffix if
 * provided and valueAtIndex as Value. Required when there are just 2 columns
 * and want one to be Key and other to be Value. e.g. In Registry when there
 * could be n AASDescriptor and for each AASDescriptor there could be m
 * SMDescriptor, there it comes handy to collect/group the SMDescriptor ID with
 * uniqueId defined in AASDescriptor
 * 
 * @author danish
 *
 */
public class MultiValueMapper implements Mapper {

	private SubmodelElementCollection valueMapConfigSMC;

	public MultiValueMapper(SubmodelElementCollection valueMapConfigSMC) {
		this.valueMapConfigSMC = valueMapConfigSMC;
	}

	@Override
	public Map<String, List<Object>> map(String data) {
		Collection<ISubmodelElement> valueMaps = valueMapConfigSMC.getSubmodelElements().values();

		return valueMaps.stream().map(Property.class::cast).map(propertyMap -> {
			try {
				return createResultEntry(propertyMap, data);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return null;
		})
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Entry<String, List<Object>> createResultEntry(Property propertyMap, String data) throws JsonProcessingException {
		ArrayList<String> valueMap = new ObjectMapper().readValue((String) propertyMap.getValue(), new TypeReference<ArrayList<String>>() {});
		
		List<Object> mappedValues = new ArrayList<>();
		
		for (String expression : valueMap) {
			mappedValues.addAll(new JsonPathExpressionResolver(expression, data).parse());
		}
		
		return new AbstractMap.SimpleEntry<>(propertyMap.getIdShort(),
				mappedValues);
	}

}
