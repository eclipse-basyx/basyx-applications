package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser.IdShortPathParser;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.utils.AasMetamodelCreatorHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NonSmcSmeFactory {

	public static Map<String, List<SubmodelElement>> create(Map<String, List<Object>> collectiveResult,
			Map<String, SubmodelElement> mapOfNonSMC, AasMetamodelCreatorHelper creatorHelper) {
		Map<String, List<SubmodelElement>> submodelElementMap = new HashMap<>();
		
		mapOfNonSMC.entrySet().stream().forEach(entry -> addSMEToMap(entry, submodelElementMap, collectiveResult, creatorHelper));
		
		return submodelElementMap;
	}
	
	private static void addSMEToMap(Entry<String, SubmodelElement> nonSMCEntry, Map<String, List<SubmodelElement>> submodelElementMap, Map<String, List<Object>> collectiveResult, AasMetamodelCreatorHelper creatorHelper) {
		String configUniqueId = nonSMCEntry.getKey();
		
		List<Object> allValuesForSME = getAllSMEOfType(configUniqueId, collectiveResult, creatorHelper);

		String idShortPath = creatorHelper.getIdShortPathFromConfig(configUniqueId);
		
		String suffix = creatorHelper.getSuffixFromConfig(configUniqueId);
		
		String oldIdShort = IdShortPathParser.parseSubmodelElementIdShort(idShortPath);

		SubmodelElement smElement = nonSMCEntry.getValue();

		AtomicInteger index = new AtomicInteger(0);

		List<SubmodelElement> submodelElements = allValuesForSME.stream()
				.map(smeValue -> setValueToSME(smElement, smeValue, createNewIdShort(oldIdShort, suffix, index.getAndIncrement(), allValuesForSME.size())))
				.collect(Collectors.toList());

		if (submodelElementMap.containsKey(configUniqueId)) {
			submodelElementMap.get(configUniqueId).addAll(submodelElements);
		} else {
			submodelElementMap.put(configUniqueId, submodelElements);
		}
	}
	
	private static List<Object> getAllSMEOfType(String key, Map<String, List<Object>> collectiveResult, AasMetamodelCreatorHelper creatorHelper) {
		SubmodelElementCollection configurations = creatorHelper.getConfigurationsSMC();
		
		SubmodelElementCollection config = (SubmodelElementCollection) ((SubmodelElementCollection) configurations.getSubmodelElement(key)).getSubmodelElement("valueIds");
		
		List<Property> valueIds = config.getSubmodelElements().values().stream().map(Property.class::cast).collect(Collectors.toList());
		
		return collectiveResult.get((String) valueIds.get(0).getValue());
	}
	
	private static SubmodelElement setValueToSME(SubmodelElement smElement, Object smeValue, String newIdShort) {

		ObjectMapper objectMapper = new ObjectMapper();
		String json = null;
		try {
			json = objectMapper.writeValueAsString(smElement);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		SubmodelElement newSubmodelElement = null;
		try {
			newSubmodelElement = (SubmodelElement) objectMapper.readValue(json,
					SubmodelElementFactory.getClass(smElement.getModelType()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		newSubmodelElement.setIdShort(newIdShort);
		newSubmodelElement.setValue(smeValue);

		return newSubmodelElement;
	}

	private static String createNewIdShort(String oldIdShort, String suffix, int index, int sizeOfSMEList) {
		if (sizeOfSMEList == 1)
			return oldIdShort + suffix;
		
		return oldIdShort + suffix + index;
	}

}
