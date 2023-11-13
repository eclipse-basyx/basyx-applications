package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.ExpansionMode;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.ProcessedSMERepository;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser.IdShortPathParser;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.utils.AasMetamodelCreatorHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SMCFactory {

	private Set<String> traversedSubmodelElementSet = new HashSet<>();
	private Map<String, SubmodelElement> mapOfSMC;
	private AasMetamodelCreatorHelper creatorHelper;
	private Map<String, List<SubmodelElement>> submodelElementMap = new HashMap<>();
	private IdShortPathParser smElemParser;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private Set<String> allProcessedConfigurationIDs = new HashSet<>();

	public SMCFactory(Map<String, SubmodelElement> mapOfSMC, AasMetamodelCreatorHelper creatorHelper,
			Map<String, List<SubmodelElement>> submodelElementMap, IdShortPathParser smElemParser) {
		super();
		this.mapOfSMC = mapOfSMC;
		this.creatorHelper = creatorHelper;
		this.submodelElementMap = submodelElementMap;
		this.smElemParser = smElemParser;
	}

	public ProcessedSMERepository create() {
		mapOfSMC.entrySet().stream().forEach(entry -> addCollectionSMEToMap(entry.getKey(), entry.getValue()));
		
		ProcessedSMERepository processedSMERepository = new ProcessedSMERepository(submodelElementMap, allProcessedConfigurationIDs);

		return processedSMERepository;
	}

	private void addCollectionSMEToMap(String key, SubmodelElement value) {
		String uniqueId = key;

		// Base condition i.e. check if the entry is already resolved by recursive call
		if (traversedSubmodelElementSet.contains(uniqueId))
			return;

		List<String> valueIds = getValueIdsFromConfig(uniqueId);
		
		allProcessedConfigurationIDs.addAll(valueIds);

		// If the contained element inside this SMC is of type SMC then resolve inner
		// SMCs recursively
		valueIds.stream().filter(this::isInstanceOfSMC)
				.forEach(valueId -> addCollectionSMEToMap(valueId, mapOfSMC.get(valueId)));

		// Add this to the traversedSMSet to ensure that it will not be repeated from
		// prepareCollectionSME method as it iterates over
		// all the SMCs, and those SMCs which has been resolved via recursive calls
		// shouldn't be resolved again
		traversedSubmodelElementSet.add(uniqueId);

		ExpansionMode expansionMode = getModeFromConfig(uniqueId);

		String suffix = creatorHelper.getSuffixFromConfig(uniqueId);

		if (expansionMode == null || expansionMode.equals(ExpansionMode.INLINE)) {
			SubmodelElementCollection resultantSMC = performInlineModeOperation(
					new AbstractMap.SimpleEntry<>(key, value), valueIds, suffix);

			if (submodelElementMap.containsKey(uniqueId)) {
				submodelElementMap.get(uniqueId).add(resultantSMC);
			} else {
				submodelElementMap.put(uniqueId, Arrays.asList(resultantSMC));
			}

		} else if (expansionMode.equals(ExpansionMode.MULTI)) {
			List<SubmodelElement> resultantSMEs = performMultiModeOperation(new AbstractMap.SimpleEntry<>(key, value),
					valueIds, suffix);

			if (submodelElementMap.containsKey(uniqueId)) {
				submodelElementMap.get(uniqueId).addAll(resultantSMEs);
			} else {
				submodelElementMap.put(uniqueId, resultantSMEs);
			}
		}

	}

	private List<String> getValueIdsFromConfig(String uniqueId) {
		SubmodelElementCollection dataWriterConfig = creatorHelper.getConfigurationsSMC();

		SubmodelElementCollection valueIdSMC = (SubmodelElementCollection) ((SubmodelElementCollection) dataWriterConfig
				.getSubmodelElement(uniqueId)).getSubmodelElement("valueIds");

		List<Property> valueIdProperties = valueIdSMC.getSubmodelElements().values().stream().map(Property.class::cast)
				.collect(Collectors.toList());

		return valueIdProperties.stream().map(Property::getValue).map(String.class::cast).collect(Collectors.toList());
	}

	private boolean isInstanceOfSMC(String valueId) {
		String idShortPath = creatorHelper.getIdShortPathFromConfig(valueId);
		
		ISubmodelElement smElem = smElemParser.parseSMC(idShortPath);

		if (smElem == null)
			return false;

		return smElem.getModelType().equals(KeyElements.SUBMODELELEMENTCOLLECTION.toString());
	}

	private ExpansionMode getModeFromConfig(String uniqueId) {
		SubmodelElementCollection dataWriterConfig = creatorHelper.getConfigurationsSMC();

		Property expansionModeProperty = (Property) ((SubmodelElementCollection) dataWriterConfig
				.getSubmodelElement(uniqueId)).getSubmodelElement("expansionMode");

		return expansionModeProperty.getValue().equals("inline") ? ExpansionMode.INLINE : ExpansionMode.MULTI;
	}

	private SubmodelElementCollection performInlineModeOperation(Entry<String, SubmodelElement> entry,
			List<String> valueIds, String suffix) {
		List<SubmodelElement> submodelElements = getAllSubmodelElements(valueIds);

		SubmodelElementCollection oldSubmodelElementCollection = SubmodelElementCollection
				.createAsFacade(entry.getValue());

		String newIdShort = oldSubmodelElementCollection.getIdShort() + suffix;

		return setValueToCollectionSME(createClonedSMC(oldSubmodelElementCollection, newIdShort), valueIds,
				submodelElements);
	}

	private List<SubmodelElement> performMultiModeOperation(Entry<String, SubmodelElement> entry, List<String> valueIds,
			String suffix) {
		List<List<SubmodelElement>> listSubmodelElementsOfAllValueIds = valueIds.stream()
				.map(this::getAllSMEDefinedForValueId).collect(Collectors.toList());

		SubmodelElementCollection oldSubmodelElementCollection = SubmodelElementCollection
				.createAsFacade(entry.getValue());

		// It finds the maximum size of SME bundle inside
		// listSubmodeleElementsOfAllValueIds
		int maximumSizeOfSMEInTheList = findMaximumSizeOfSMEBundle(listSubmodelElementsOfAllValueIds);

		return IntStream.range(0, maximumSizeOfSMEInTheList).mapToObj(i -> prepareMultiSMC(oldSubmodelElementCollection,
				valueIds, listSubmodelElementsOfAllValueIds, i,
				createNewIdShort(oldSubmodelElementCollection.getIdShort(), suffix, i, maximumSizeOfSMEInTheList))).collect(Collectors.toList());
	}

	private List<SubmodelElement> getAllSubmodelElements(List<String> valueIds) {
		return valueIds.stream().map(this::getAllSMEDefinedForValueId).flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private SubmodelElementCollection createClonedSMC(SubmodelElementCollection oldSubmodelElementCollection,
			String newIdShort) {

		String json = null;
		try {
			json = objectMapper.writeValueAsString(oldSubmodelElementCollection);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		try {
			SubmodelElementCollection newSubmodelElementCollection = objectMapper.readValue(json,
					SubmodelElementCollection.class);
			newSubmodelElementCollection.setIdShort(newIdShort);
			return newSubmodelElementCollection;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to create clone of SubmodelElementCollection with id : "
					+ oldSubmodelElementCollection.getIdShort());
		}

	}

	private SubmodelElementCollection setValueToCollectionSME(SubmodelElementCollection newSubmodelElementCollection,
			List<String> containedSubmodelElementsIdShort, List<SubmodelElement> smes) {

		removeTheOldSubmodelElementsFromSMC(newSubmodelElementCollection, containedSubmodelElementsIdShort);

		List<ISubmodelElement> submodelElements = smes.stream().map(ISubmodelElement.class::cast)
				.collect(Collectors.toList());

		findAndReplaceFixedSMEsInsideSMCWithTypeSafeSMEs(newSubmodelElementCollection);

		submodelElements.stream().forEach(newSubmodelElementCollection::addSubmodelElement);

		return newSubmodelElementCollection;
	}

	private void findAndReplaceFixedSMEsInsideSMCWithTypeSafeSMEs(
			SubmodelElementCollection newSubmodelElementCollection) {
		Collection<ISubmodelElement> fixedSMEs = newSubmodelElementCollection.getValue();

		if (fixedSMEs.isEmpty())
			return;

		List<Object> fixedSMEAsObjects = new ArrayList<>(fixedSMEs);

		List<ISubmodelElement> typeSafeSMEs = fixedSMEAsObjects.stream().filter(Map.class::isInstance)
				.map(this::createClonedSME).collect(Collectors.toList());

		typeSafeSMEs.stream()
				.forEach(typeSafeSME -> replaceSMEWithTypeSafeSME(newSubmodelElementCollection, typeSafeSME));
	}

	private ISubmodelElement createClonedSME(Object object) {
		String json = null;
		try {
			json = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		try {
			return (ISubmodelElement) objectMapper.readValue(json, Property.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to create clone of SubmodelElementCollection");
		}

	}

	private void replaceSMEWithTypeSafeSME(SubmodelElementCollection newSubmodelElementCollection,
			ISubmodelElement smeFix) {
		// Delete this SME from SMC because it is just LinkedHashMap<K,V> and can't be
		// cast to
		// ISubmodelElement (issue when trying to access just the values of SMEs using
		// submodel/values API endpoint)
		newSubmodelElementCollection.deleteSubmodelElement(smeFix.getIdShort());

		// Now add the type safe SME again
		newSubmodelElementCollection.addSubmodelElement(smeFix);
	}

	private List<SubmodelElement> getAllSMEDefinedForValueId(String valueId) {
		return submodelElementMap.get(valueId);
	}

	private int findMaximumSizeOfSMEBundle(List<List<SubmodelElement>> listSubmodeleElementsOfAllValueIds) {

		return listSubmodeleElementsOfAllValueIds.stream().map(List::size).max(Integer::compare).orElse(0);
	}

	private SubmodelElementCollection prepareMultiSMC(SubmodelElementCollection oldSubmodelElementCollection,
			List<String> containedSubmodelElementsIdShort,
			List<List<SubmodelElement>> listSubmodelElementsOfAllValueIds, int index, String newIdShort) {

		SubmodelElementCollection newSubmodelElementCollection = createClonedSMC(oldSubmodelElementCollection,
				newIdShort);

		removeTheOldSubmodelElementsFromSMC(newSubmodelElementCollection, containedSubmodelElementsIdShort);

		listSubmodelElementsOfAllValueIds.stream()
				.forEach(smElements -> addSMEToSMC(smElements, newSubmodelElementCollection, index));

		return newSubmodelElementCollection;
	}

	private String createNewIdShort(String oldIdShort, String suffix, int index, int sizeOfSME) {
		if (sizeOfSME == 1)
			return oldIdShort + suffix;

		return oldIdShort + suffix + index;
	}

	private void removeTheOldSubmodelElementsFromSMC(SubmodelElementCollection newSubmodelElementCollection,
			List<String> containedSubmodelElementsIdShort) {
		containedSubmodelElementsIdShort.stream().map(creatorHelper::getIdShortPathFromConfig).map(IdShortPathParser::parseSubmodelElementIdShort)
				.forEach(idShort -> removeFromSMC(idShort, newSubmodelElementCollection));
	}

	private void addSMEToSMC(List<SubmodelElement> smElements, SubmodelElementCollection newSubmodelElementCollection,
			int index) {
		if (index >= smElements.size())
			return;

		newSubmodelElementCollection.addSubmodelElement(smElements.get(index));
	}

	private void removeFromSMC(String idShort, SubmodelElementCollection newSubmodelElementCollection) {
		if (!isSubmodelElementExists(idShort, newSubmodelElementCollection))
			return;

		newSubmodelElementCollection.deleteSubmodelElement(idShort);
	}

	private boolean isSubmodelElementExists(String idShort, SubmodelElementCollection newSubmodelElementCollection) {
		return newSubmodelElementCollection.getSubmodelElements().containsKey(idShort);
	}

}
