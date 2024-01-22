package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.sqlcursorreader.mapper;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SimpleMapper implements RowMapper<Map<String, List<Object>>> {

	private Submodel sqlReaderConfiguration;

	public SimpleMapper(Submodel sqlReaderConfiguration) {
		this.sqlReaderConfiguration = sqlReaderConfiguration;
	}

	@Override
	public Map<String, List<Object>> mapRow(ResultSet resultSet, int i) {
		Map<String, List<Object>> valueMap = new HashMap<>();
		Map<String, ISubmodelElement> columnNames = ((SubmodelElementCollection) sqlReaderConfiguration.getSubmodelElement("valueMap")).getSubmodelElements();
		columnNames.entrySet().stream().forEach(entry -> toMap(entry, resultSet, valueMap));
		
		return valueMap;
	}

	private void toMap(Entry<String, ISubmodelElement> mappingEntry, ResultSet resultSet, Map<String, List<Object>> valueMap) {
		String uniqueId = mappingEntry.getKey();
		String columnName = (String) mappingEntry.getValue().getValue();
		
		if (valueMap.containsKey(uniqueId)) {
			try {
				List<Object> newValues = new ArrayList<>();
				newValues.addAll(valueMap.get(uniqueId));
				newValues.add(resultSet.getObject(columnName));
				valueMap.put(uniqueId, newValues);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}
		
		try {
			valueMap.put(uniqueId, Arrays.asList(resultSet.getObject(columnName)));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}