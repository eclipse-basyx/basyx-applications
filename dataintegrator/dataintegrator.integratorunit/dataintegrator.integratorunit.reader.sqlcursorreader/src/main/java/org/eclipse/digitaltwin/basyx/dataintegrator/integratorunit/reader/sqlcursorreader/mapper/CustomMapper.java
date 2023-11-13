package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.sqlcursorreader.mapper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.springframework.jdbc.core.RowMapper;

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
public class CustomMapper implements RowMapper<Map<String, List<Object>>> {

	private Submodel sqlReaderConfiguration;

	public CustomMapper(Submodel sqlReaderConfiguration) {
		this.sqlReaderConfiguration = sqlReaderConfiguration;
	}

	@Override
	public Map<String, List<Object>> mapRow(ResultSet rs, int rowNum) {
		Map<String, ISubmodelElement> configMap = ((SubmodelElementCollection) sqlReaderConfiguration.getSubmodelElement("valueMap")).getSubmodelElements();
		return map(rs, configMap.entrySet().stream().collect(Collectors.toList()).get(0));
	}

	private Map<String, List<Object>> map(ResultSet resultSet, Entry<String, ISubmodelElement> configEntry) {
		Map<String, List<Object>> valueMap = new HashMap<>();
		
		String key = getKey(resultSet, configEntry.getKey());
		List<Object> value = getValue(resultSet, (String) configEntry.getValue().getValue());

		if (valueMap.containsKey(key)) {
			List<Object> newValues = new ArrayList<>();
			newValues.addAll(valueMap.get(key));
			newValues.addAll(value);
			valueMap.put(key, newValues);
			return valueMap;
		}
		
		valueMap.put(key, value);
		return valueMap;
	}

	private List<Object> getValue(ResultSet resultSet, String configValueKey) {
		List<Object> value = null;
		
		try {
			value = Arrays.asList(resultSet.getObject(configValueKey));
			return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}

	private String getKey(ResultSet resultSet, String configKey) {
		String key = null;
		try {
			key = (String) resultSet.getObject(configKey);
			return key;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return key;
	}

}
