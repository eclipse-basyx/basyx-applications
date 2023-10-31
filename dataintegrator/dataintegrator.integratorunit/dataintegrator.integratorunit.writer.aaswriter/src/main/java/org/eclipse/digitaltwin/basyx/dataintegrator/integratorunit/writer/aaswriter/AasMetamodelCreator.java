package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.concurrent.ConcurrentNonSmcSmeFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.concurrent.NonSMCSMEConcurrentResolver;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.concurrent.SMCSMEConcurrentResolver;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory.SMCFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser.IdShortPathParser;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.utils.AasMetamodelCreatorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public class AasMetamodelCreator {

	private Logger logger = LoggerFactory.getLogger(AasMetamodelCreator.class);

	private Map<String, List<Object>> collectiveResult;
	private Submodel aasWriterConfiguration;
	private AasEnv aasEnv;
	private Map<String, List<SubmodelElement>> submodelElementMap = new HashMap<>();
	private Map<String, ISubmodel> matchingSMMap = new HashMap<>();
	private AasMetamodelCreatorHelper creatorHelper;

	public AasMetamodelCreator(Map<String, List<Object>> collectiveResult, Submodel aasWriterConfiguration,
			AasEnv aasEnv) {
		super();
		this.collectiveResult = collectiveResult;
		this.aasWriterConfiguration = aasWriterConfiguration;
		this.aasEnv = aasEnv;
		creatorHelper = new AasMetamodelCreatorHelper(this.aasWriterConfiguration);
		configureJsonPath();
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

	public Map<String, List<Object>> getCollectiveResult() {
		return collectiveResult;
	}

	public Submodel getAasWriterConfiguration() {
		return aasWriterConfiguration;
	}

	public AasEnv getAasEnv() {
		return aasEnv;
	}

	public AasEnv create() {
		Map<String, SubmodelElement> mapOfNonSmc = new HashMap<>();
		Map<String, SubmodelElement> mapOfSmc = new HashMap<>();

		SubmodelElementCollection dataWriterConfig = creatorHelper.getConfigurationsSMC();

		long start8 = System.currentTimeMillis();

		try {
			mapOfNonSmc = new NonSMCSMEConcurrentResolver().resolveConcurrently(dataWriterConfig, aasEnv);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		long end8 = System.currentTimeMillis();
		long time8 = end8 - start8;

		logger.info("#1: {} ms - [Delta] Filter out all non hierarchical SME config in a Map", time8);
		
		long start7 = System.currentTimeMillis();

		try {
			mapOfSmc = new SMCSMEConcurrentResolver().resolveConcurrently(dataWriterConfig, aasEnv);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		long end7 = System.currentTimeMillis();
		long time7 = end7 - start7;

		logger.info("#2: {} ms - [Delta] Filter out all hierarchical SME (SMC) config in a Map", time7);
		
		long start6 = System.currentTimeMillis();

		// Step 1 prepare all SME of type Non Collection
		try {
			submodelElementMap = new ConcurrentNonSmcSmeFactory().resolveConcurrently(collectiveResult, mapOfNonSmc,
					creatorHelper);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		long end6 = System.currentTimeMillis();
		long time6 = end6 - start6;

		logger.info("#3: {} ms - [Delta] Process all SMEs (Clone creation to not affect stencil)", time6);
		
		long start5 = System.currentTimeMillis();

		// Step 2 prepare all SME of type Collection
		ProcessedSMERepository processedSMERepository = new SMCFactory(mapOfSmc, creatorHelper, submodelElementMap,
				new IdShortPathParser(aasEnv.getAssetAdministrationShells(), aasEnv.getSubmodels())).create();

		Set<String> processedSMEs = processedSMERepository.getProcessedConfigurationIDs();

		Set<String> allExistingConfigurationIDs = dataWriterConfig.getSubmodelElements().keySet();

		// Dangling SMEs means that those SMEs whose self configuration is present in
		// the writer configuration but its parent (and grand parents or ancestors per
		// say) config is not present, it means if
		// these are not handled then it will not be updated in the final result.
		// The purpose of doing this is when there is a SME(including SMCs) present in a
		// stencil in a deepest level and users want to integrate only that particular
		// SME then users just need to provide the configuration of that SME only and
		// not his parents in a hierarchy fashion. So these danglingSMEs set contains
		// those SMEs whose parent config is not present in the writer config. Also, the
		// danglingSMEs could be direct SMEs of any Submodel and if true then these SMEs
		// are
		// automatically being handled by using below methods i.e.,
		// (replacePlaceholderSMEFromSubmodels and
		// replacePlaceholderCollectionSMEFromSubmodels), so eventually the direct child
		// SMEs will be removed from the danglingSMEs set.
		Set<String> danglingMetamodelsConfigIds = new HashSet<>(allExistingConfigurationIDs);
		danglingMetamodelsConfigIds.removeAll(processedSMEs);

//		logger.info("danglingSMEs before SM computation : {}", danglingMetamodelsConfigIds.toString());
		
		long end5 = System.currentTimeMillis();
		long time5 = end5 - start5;

		logger.info("#4: {} ms - [Delta] Process all SMCs (Clone creation to not affect stencil)", time5);
		
		long start4 = System.currentTimeMillis();

		replacePlaceholderSMEFromSubmodels(processedSMERepository.getProcessedSMEs(), danglingMetamodelsConfigIds);

		replacePlaceholderCollectionSMEFromSubmodels(processedSMERepository.getProcessedSMEs(),
				danglingMetamodelsConfigIds);

//		logger.info("danglingSMEs after SM computation : {}", danglingMetamodelsConfigIds.toString());
		
		long end4 = System.currentTimeMillis();
		long time4 = end4 - start4;

		logger.info("#5: {} ms - [Delta] Resolve Submodel for any SMCs or SME", time4);
		
		long start3 = System.currentTimeMillis();

		List<ISubmodel> matchingSubmodels = findSubmodelAsPerConfig(dataWriterConfig.getSubmodelElements());

		matchingSubmodels.stream().forEach(sm -> matchingSMMap.put(sm.getIdentification().getId(), sm));

		writeMetaDataToSubmodels(matchingSubmodels, dataWriterConfig.getSubmodelElements());
		
		long end3 = System.currentTimeMillis();
		long time3 = end3 - start3;

		logger.info("#6: {} ms - [Delta] Write Metadata to Submodels if configured", time3);
		
		long start2 = System.currentTimeMillis();

		updateAasReferenceCorrespondingToModifiedSMs(matchingSMMap);
		
		long end2 = System.currentTimeMillis();
		long time2 = end2 - start2;

		logger.info("#7 + #8: {} ms - [Delta] Update AAS Reference if Submodel key has been modified (for e.g., using metadata)", time2);
		
		long start1 = System.currentTimeMillis();
		
		List<ISubmodelElement> aasConfigCandidates = creatorHelper.findAASConfigs();
		
		if (!aasConfigCandidates.isEmpty()) {
			updateSubmodelParentRefIfAasIdIsModifiedUsingMetadata(dataWriterConfig, aasConfigCandidates);
		}
		
		long end1 = System.currentTimeMillis();
		long time1 = end1 - start1;

		logger.info("#9: {} ms - [Delta] Update parent ref of all submodels contained in the AAS if the ID of this AAS has been altered", time1);
		
		long start0 = System.currentTimeMillis();

		// AASs and SMs are out of scope of dangling because they are automatically
		// handled. In short dangling is only applicable to SMEs. So this method will
		// check if the set contains any AAS or SM config then it removes the config
		// from the set.
		removeAasAndSMConfigFromDanglingSMESet(danglingMetamodelsConfigIds, creatorHelper);

		resolveRemainingDanglingSMEs(danglingMetamodelsConfigIds, creatorHelper, processedSMERepository.getProcessedSMEs());

//		logger.info("danglingSMEs after removing AAS or SM config : {}", danglingMetamodelsConfigIds.toString());

		long end0 = System.currentTimeMillis();
		long time0 = end0 - start0;

		logger.info("#10: {} ms - [Delta] Resolve the dangling SMEs", time0);

		return aasEnv;
	}

	private void updateSubmodelParentRefIfAasIdIsModifiedUsingMetadata(SubmodelElementCollection dataWriterConfig,
			List<ISubmodelElement> aasConfigCandidates) {
		Map<String, IAssetAdministrationShell> oldAasIdMap = aasEnv.getAssetAdministrationShells().stream().map(this::toAASEntry).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		updateMetadataToAasIfConfigured(dataWriterConfig.getSubmodelElements(), aasConfigCandidates);
		
		Map<String, IAssetAdministrationShell> shellsWithChangedIds = oldAasIdMap.entrySet().stream().filter(entry -> !entry.getKey().equals(entry.getValue().getIdentification().getId())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
					
		shellsWithChangedIds.entrySet().stream().forEach(entry -> updateNewIdOfParentAas(entry, aasEnv.getSubmodels()));
	}
	
	private void updateNewIdOfParentAas(Entry<String, IAssetAdministrationShell> entry,
			Collection<ISubmodel> submodels) {
		
		String oldAasId = entry.getKey();
		
		IAssetAdministrationShell shell = entry.getValue();
		
		shell.getSubmodelReferences().stream().forEach(smRef -> updateSMParent(smRef.getKeys(), submodels, oldAasId, shell.getIdentification().getId()));
	}

	private void updateSMParent(List<IKey> keys, Collection<ISubmodel> submodels, String oldAasId, String newAasId) {
		keys.stream().filter(key -> key.getType().equals(KeyElements.SUBMODEL)).forEach(key -> findAndUpdateSMParent(key.getValue(), submodels, oldAasId, newAasId));
	}

	private void findAndUpdateSMParent(String submodelId, Collection<ISubmodel> submodels, String oldAasId, String newAasId) {
		Optional<ISubmodel> matchingSM = submodels.stream().filter(sm -> sm.getIdentification().getId().equals(submodelId)).findAny();
		
		if (matchingSM.isEmpty())
			return;
		
		Submodel sm = (Submodel) matchingSM.get();
		
		Optional<IKey> matchingKey = sm.getParent().getKeys().stream().filter(key -> key.getType().equals(KeyElements.ASSETADMINISTRATIONSHELL) && key.getValue().equals(oldAasId)).findAny();
		
		if (matchingKey.isEmpty())
			return;
		
		IKey oldParentReferenceKey = matchingKey.get();
		
		sm.setParent(new Reference(new Key(oldParentReferenceKey.getType(), oldParentReferenceKey.isLocal(), newAasId, oldParentReferenceKey.getIdType())));
	}

	private Entry<String, IAssetAdministrationShell> toAASEntry(IAssetAdministrationShell shell) {
		return new AbstractMap.SimpleEntry<>(shell.getIdentification().getId(), shell);
	}

	private void resolveRemainingDanglingSMEs(Set<String> danglingMetamodelsConfigIds,
			AasMetamodelCreatorHelper creatorHelper, Map<String, List<SubmodelElement>> processedSMEs) {
		if (danglingMetamodelsConfigIds.isEmpty())
			return;

		danglingMetamodelsConfigIds.stream()
				.forEach(danglingConfigId -> resolveDanglingSME(danglingConfigId, creatorHelper, processedSMEs));
	}

	private void resolveDanglingSME(String danglingConfigId, AasMetamodelCreatorHelper creatorHelper, Map<String, List<SubmodelElement>> processedSMEs) {
		String idShortPath = creatorHelper.getIdShortPathFromConfig(danglingConfigId);

		ISubmodelElement danglingSMEParent = new IdShortPathParser(
				aasEnv.getAssetAdministrationShells(), aasEnv.getSubmodels()).getParentElement(idShortPath);
		
		if (danglingSMEParent == null)
			return;
		
		String smeIdShort = IdShortPathParser.parseSubmodelElementIdShort(idShortPath);
		
		if (!((SubmodelElementCollection) danglingSMEParent).getSubmodelElements().containsKey(smeIdShort))
			return;
		
		((SubmodelElementCollection) danglingSMEParent).deleteSubmodelElement(smeIdShort);
		
		List<SubmodelElement> processedSMEList = processedSMEs.get(danglingConfigId);
		
		processedSMEList.stream().forEach(processedSME -> ((SubmodelElementCollection) danglingSMEParent).addSubmodelElement(processedSME));
	}

	private void removeAasAndSMConfigFromDanglingSMESet(Set<String> danglingSMEs,
			AasMetamodelCreatorHelper creatorHelper) {
		if (danglingSMEs.isEmpty())
			return;

		Iterator<String> iterator = danglingSMEs.iterator();
		while (iterator.hasNext()) {
			String configId = iterator.next();
			if (isAasOrSubmodelConfig(configId, creatorHelper)) {
				iterator.remove();
			}
		}
	}

	private boolean isAasOrSubmodelConfig(String configId, AasMetamodelCreatorHelper creatorHelper) {
		String idShortPath = creatorHelper.getIdShortPathFromConfig(configId);

		return IdShortPathParser.isValidAASIdShortPath(idShortPath)
				|| IdShortPathParser.isValidSubmodelIdShortPath(idShortPath);
	}

	private void updateMetadataToAasIfConfigured(Map<String, ISubmodelElement> dataWriterConfig, List<ISubmodelElement> aasConfigCandidates) {

		List<IAssetAdministrationShell> assetAdministrationShells = aasEnv.getAssetAdministrationShells().stream()
				.filter(aas -> isMatchingConfig(aas, aasConfigCandidates)).collect(Collectors.toList());

		assetAdministrationShells.stream().forEach(aas -> writeMetaData(aas, dataWriterConfig));
	}

	private boolean isMatchingConfig(IAssetAdministrationShell aas, List<ISubmodelElement> aasConfigCandidates) {
		Optional<ISubmodelElement> optionalAasConfig = aasConfigCandidates.stream()
				.filter(aasConfig -> aas.getIdShort()
						.equals(IdShortPathParser
								.parseAASIdShort(creatorHelper.getIdShortPathFromConfig(aasConfig.getIdShort()))))
				.findAny();

		return optionalAasConfig.isPresent();
	}

	private void updateAasReferenceCorrespondingToModifiedSMs(Map<String, ISubmodel> matchingSMMap) {
		aasEnv.getAssetAdministrationShells().stream().forEach(aas -> containsSmReference(aas, matchingSMMap));
	}

	private void containsSmReference(IAssetAdministrationShell shell, Map<String, ISubmodel> matchingSMMap) {
		shell.getSubmodelReferences().stream().forEach(ref -> containsSmId(matchingSMMap, ref.getKeys()));
	}

	private void containsSmId(Map<String, ISubmodel> matchingSMMap, List<IKey> keys) {
		Optional<IKey> optionalKey = keys.stream().filter(key -> matchingSMMap.containsKey(key.getValue())).findAny();

		if (optionalKey.isEmpty())
			return;

		((Key) optionalKey.get())
				.setValue(matchingSMMap.get(optionalKey.get().getValue()).getIdentification().getId());
	}

	private void writeMetaDataToSubmodels(List<ISubmodel> matchingSubmodels,
			Map<String, ISubmodelElement> dataWriterConfig) {
		matchingSubmodels.stream().forEach(sm -> writeMetaData(sm, dataWriterConfig));
	}

	private void writeMetaData(IAssetAdministrationShell aas, Map<String, ISubmodelElement> dataWriterConfig) {

		Optional<ISubmodelElement> optionalMatchingAASConfig = creatorHelper.findAASConfigs().stream().filter(
				smcConfig -> aas.getIdShort().equals(creatorHelper.getIdShortPathFromConfig(smcConfig.getIdShort())))
				.findAny();

//		TODO: Take oldId of SM into a variable
		if (optionalMatchingAASConfig.isEmpty())
			return;

		SubmodelElementCollection smcMetadata = creatorHelper
				.getMetadataFromConfig(optionalMatchingAASConfig.get().getIdShort());

		if (smcMetadata.getSubmodelElements().isEmpty())
			return;

		smcMetadata.getSubmodelElements().values().stream().map(SubmodelElementCollection.class::cast)
				.forEach(metadataSMC -> applyMetadataToAAS(metadataSMC, aas));
	}

	private void applyMetadataToAAS(SubmodelElementCollection metadata, IAssetAdministrationShell aas) {
		DocumentContext context = JsonPath.parse(aas);

		context.set((String) metadata.getSubmodelElement("expression").getValue(),
				collectiveResult.get((String) metadata.getSubmodelElement("valueId").getValue()).get(0));

		try {
//			logger.info("context : {}", context.jsonString());
			aas = new ObjectMapper().readValue(context.jsonString(), AssetAdministrationShell.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void writeMetaData(ISubmodel sm, Map<String, ISubmodelElement> dataWriterConfig) {

		Optional<ISubmodelElement> optionalMatchingSMConfig = creatorHelper.findSubmodelConfigs().stream().filter(
				smcConfig -> sm.getIdShort().equals(creatorHelper.getIdShortPathFromConfig(smcConfig.getIdShort())))
				.findAny();

//		TODO: Take oldId of SM into a variable
		if (optionalMatchingSMConfig.isEmpty())
			return;

		SubmodelElementCollection smcMetadata = creatorHelper
				.getMetadataFromConfig(optionalMatchingSMConfig.get().getIdShort());

		if (smcMetadata.getSubmodelElements().isEmpty())
			return;

		smcMetadata.values().stream().map(SubmodelElementCollection.class::cast)
				.forEach(metadataSMC -> applyMetadataToSM(metadataSMC, sm));

		// TODO: Take current id of the SM and compare it with oldId. if not same put it
		// in a global map submodelWithIdAlteredMap.
	}

	private void applyMetadataToSM(SubmodelElementCollection metadata, ISubmodel sm) {
		DocumentContext context = JsonPath.parse(sm);

		context.set((String) metadata.getSubmodelElement("expression").getValue(),
				collectiveResult.get((String) metadata.getSubmodelElement("valueId").getValue()).get(0));

		try {
//			logger.info("context : {}", context.jsonString());
			sm = new ObjectMapper().readValue(context.jsonString(), Submodel.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private List<ISubmodel> findSubmodelAsPerConfig(Map<String, ISubmodelElement> dataWriterConfig) {
		List<String> submodelConfigsPresent = creatorHelper.findSubmodelConfigs().stream()
				.map(smeConfig -> creatorHelper.getIdShortPathFromConfig(smeConfig.getIdShort()))
				.map(IdShortPathParser::parseSubmodelIdShort).collect(Collectors.toList());

		return aasEnv.getSubmodels().stream().filter(sm -> submodelConfigsPresent.contains(sm.getIdShort()))
				.collect(Collectors.toList());
	}

	private void replacePlaceholderCollectionSMEFromSubmodels(Map<String, List<SubmodelElement>> submodelElementMap,
			Set<String> danglingSMEs) {
		submodelElementMap.entrySet().stream().forEach(entry -> findAndReplaceSME(entry, danglingSMEs));
	}

	private void replacePlaceholderSMEFromSubmodels(Map<String, List<SubmodelElement>> submodelElementMap,
			Set<String> danglingSMEs) {
		submodelElementMap.entrySet().stream().forEach(entry -> findAndReplaceSME(entry, danglingSMEs));
	}

	private void findAndReplaceSME(Entry<String, List<SubmodelElement>> smeEntry, Set<String> danglingSMEs) {
		List<ISubmodel> submodels = aasEnv.getSubmodels().stream().collect(Collectors.toList());

		String uniqueId = smeEntry.getKey();

		String smeIdShortPath = creatorHelper.getIdShortPathFromConfig(uniqueId);

		String submodelIdShort = IdShortPathParser.parseSubmodelIdShort(smeIdShortPath);

		String smeIdShort = IdShortPathParser.parseSubmodelElementIdShort(smeIdShortPath);

		// TODO: Fetch the id of SMElements from the idShortPath
//		List<ISubmodel> submodelsContainingSME = submodels.stream()
//				.filter(submodel -> containsSME(submodel, smeIdShort)).collect(Collectors.toList());
		List<ISubmodel> submodelsContainingSME = submodels.stream()
				.filter(sm -> sm.getIdShort().equals(submodelIdShort))
				.filter(submodel -> containsSME(submodel, smeIdShort)).collect(Collectors.toList());

		if (submodelsContainingSME.isEmpty())
			return;

		// if the dangling SME is directly under the submodel then it is not a dangling
		// SME anymore because the parent is known i.e., submodel. Hence remove it from
		// dangling set.
		if (danglingSMEs.contains(uniqueId))
			danglingSMEs.remove(uniqueId);

		removePlaceholderSME(submodelsContainingSME, smeIdShort);

		List<SubmodelElement> submodelElementsToAdd = smeEntry.getValue();

		addNewlyGeneratedDynamicSMEs(submodelsContainingSME, submodelElementsToAdd);
	}

	private void addNewlyGeneratedDynamicSMEs(List<ISubmodel> submodelsContainingSME,
			List<SubmodelElement> submodelElementsToAdd) {
		submodelsContainingSME.stream().forEach(submodel -> addSMEs(submodel, submodelElementsToAdd));
	}

	private void addSMEs(ISubmodel submodel, List<SubmodelElement> submodelElementsToAdd) {
		submodelElementsToAdd.stream().forEach(submodel::addSubmodelElement);
	}

	private void removePlaceholderSME(List<ISubmodel> submodelsContainingSME, String smeIdShort) {
		submodelsContainingSME.stream().forEach(submodel -> removeSME(submodel, smeIdShort));
	}

	private void removeSME(ISubmodel submodel, String smeIdShort) {
		submodel.deleteSubmodelElement(smeIdShort);
	}

	private boolean containsSME(ISubmodel submodel, String smeIdShort) {
		return submodel.getSubmodelElements().containsKey(smeIdShort);
	}

}
