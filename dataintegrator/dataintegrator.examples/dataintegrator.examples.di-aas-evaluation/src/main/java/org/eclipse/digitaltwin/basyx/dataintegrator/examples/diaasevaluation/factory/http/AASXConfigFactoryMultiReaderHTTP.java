package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;

public class AASXConfigFactoryMultiReaderHTTP {

	private static final String DEFAULT_IDSHORT_PREFIX = AASXStencilFactorySingleReaderHTTP.EVAL_AAS_ID_SHORT + "/"
			+ AASXStencilFactorySingleReaderHTTP.EVAL_SM_ID_SHORT;

	private static final String SMC_CONFIG_ID_PREFIX = "arb_";

	private AASXStencilFactorySingleReaderHTTP aasxStencilFactory;

	public AASXConfigFactoryMultiReaderHTTP(AASXStencilFactorySingleReaderHTTP aasxStencilFactory) {
		this.aasxStencilFactory = aasxStencilFactory;
	}

	public AasEnv create() {

		AssetAdministrationShell configAAS = new AssetAdministrationShell();
		configAAS.setIdentification(new CustomId("EvaluationAASConfigId"));
		configAAS.setIdShort("evaluationAASIdShort");
		
		List<ISubmodel> submodelList = new ArrayList<>();

		
		int nextId = 1;
		
		// Below code is for n readers

		while (nextId <= aasxStencilFactory.getLevel()) {
			String apiUrl = "http://localhost:5002/sample/data";
			submodelList.add(createReaderConfig(nextId, apiUrl));
			configAAS.addSubmodelReference(
					new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextId, KeyType.CUSTOM)));
			nextId++;
		}
		
		// This is for just 1 reader
//		String query = "SELECT address FROM test_address WHERE id BETWEEN 1 AND " + aasxStencilFactory.getLevel();
//		submodelList.add(createReaderConfig(nextId, query));
//		configAAS.addSubmodelReference(
//				new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextId, KeyType.CUSTOM)));
		
		Submodel processorConfig = createProcessorConfig();
		Submodel writerConfig = createWriterConfig();
		Submodel jobConfig = createJobConfig();

		configAAS.addSubmodelReference(
				new Reference(new Key(KeyElements.SUBMODEL, true, "processorEvalSMId", KeyType.CUSTOM)));
		configAAS.addSubmodelReference(
				new Reference(new Key(KeyElements.SUBMODEL, true, "writerEvalSMId", KeyType.CUSTOM)));
		configAAS.addSubmodelReference(
				new Reference(new Key(KeyElements.SUBMODEL, true, "jobEvalSMId", KeyType.CUSTOM)));

		AasEnv stencilEnv = aasxStencilFactory.create();

		List<IAssetAdministrationShell> aasList = new ArrayList<>();
		aasList.add(configAAS);
		aasList.addAll(new ArrayList<>(stencilEnv.getAssetAdministrationShells()));

		submodelList.addAll(Arrays.asList(processorConfig, writerConfig, jobConfig));
		submodelList.addAll(new ArrayList<>(stencilEnv.getSubmodels()));

		return new AasEnv(aasList, Arrays.asList(), Arrays.asList(), submodelList);
	}

	/**
	 * Generates the idShort path of the last element
	 * 
	 * i.e., it will generate the idShortPath excluding the base URI as
	 * http://host:port/contextPath/shells/AASId/aas/submodels/SMId/submodel
	 * 
	 * @return
	 */
	public String generateIdShortPath() {
		StringBuilder urlBuilder = new StringBuilder("/submodelElements");

		int nextId = 0;

		while (nextId <= aasxStencilFactory.getLevel()) {
			urlBuilder.append("/" + AASXStencilFactorySingleReaderHTTP.SME_ID_PREFIX + nextId++);
		}

		urlBuilder.append("/legacySysProperty");

		return urlBuilder.toString();
	}

	private Submodel createJobConfig() {
		Submodel jobSM = new Submodel("jobEvalSMIdShort", new CustomId("jobEvalSMId"));

		jobSM.setSemanticId(new Reference(new Key(KeyElements.SUBMODEL, false, "urn:di:aas:config:job", KeyType.IRI)));

		Property jobName = new Property("jobName", "EvaluationJob");
		
		SubmodelElementCollection processors = createProcessorReferences();
		ReferenceElement writer = createWriterReferences();

		jobSM.addSubmodelElement(createReaderReferences());
		jobSM.addSubmodelElement(jobName);
		jobSM.addSubmodelElement(processors);
		jobSM.addSubmodelElement(writer);

		return jobSM;
	}

	private ReferenceElement createWriterReferences() {
//		SubmodelElementCollection writers = new SubmodelElementCollection("writer");

		ReferenceElement writer = new ReferenceElement("writer");
		writer.setSemanticId(new Reference(
				new Key(KeyElements.GLOBALREFERENCE, true, "basyx-di:connector:config:writer", KeyType.IRI)));
		writer.setValue(new Reference(new Key(KeyElements.SUBMODEL, true, "writerEvalSMId", KeyType.CUSTOM)));

//		writers.addSubmodelElement(writer0);

		return writer;
	}

	private SubmodelElementCollection createProcessorReferences() {
		SubmodelElementCollection processors = new SubmodelElementCollection("processors");

		ReferenceElement processor0 = new ReferenceElement("processor0");
		processor0.setSemanticId(new Reference(
				new Key(KeyElements.GLOBALREFERENCE, true, "basyx-di:connector:config:processor", KeyType.IRI)));
		processor0.setValue(new Reference(new Key(KeyElements.SUBMODEL, true, "processorEvalSMId", KeyType.CUSTOM)));

		processors.addSubmodelElement(processor0);

		return processors;
	}

	private SubmodelElementCollection createReaderReferences() {
		SubmodelElementCollection readers = new SubmodelElementCollection("readers");
		
		
		int nextAttr = 1;
		
		// Below code is for n readers references
		
		while (nextAttr <= aasxStencilFactory.getLevel()) {
			ReferenceElement reader0 = new ReferenceElement("reader" + nextAttr);
			reader0.setSemanticId(new Reference(
					new Key(KeyElements.GLOBALREFERENCE, true, "basyx-di:connector:config:reader", KeyType.IRI)));
			reader0.setValue(new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextAttr, KeyType.CUSTOM)));

			readers.addSubmodelElement(reader0);
			nextAttr++;
		}
		
		// Below code is for 1 reader reference
//		ReferenceElement reader0 = new ReferenceElement("reader" + nextAttr);
//		reader0.setSemanticId(new Reference(
//				new Key(KeyElements.GLOBALREFERENCE, true, "basyx-di:connector:config:reader", KeyType.IRI)));
//		reader0.setValue(new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextAttr, KeyType.CUSTOM)));
//
//		readers.addSubmodelElement(reader0);

		return readers;
	}

	private Submodel createWriterConfig() {
		Submodel writerSM = new Submodel("writerEvalSMIdShort", new CustomId("writerEvalSMId"));
		writerSM.setSemanticId(
				new Reference(new Key(KeyElements.SUBMODEL, false, "urn:di:aas:config:writer", KeyType.IRI)));

		Qualifier writerQualifier = new Qualifier();
		writerQualifier.setType("connectorFQCN");
		writerQualifier.setValue("org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.AasWriter");

		writerSM.setQualifiers(Arrays.asList(writerQualifier));

		SubmodelElementCollection configurations = new SubmodelElementCollection("configurations");

//		int nextId = 1;
//		StringBuilder prevIdShortPath = new StringBuilder(
//				DEFAULT_IDSHORT_PREFIX + "/" + AASXStencilFactory.SME_ID_PREFIX + 0);
//
//		SubmodelElementCollection initialSMCConfig = createSMCConfig("arb_0", prevIdShortPath.toString(), "arb_1");
//		configurations.addSubmodelElement(initialSMCConfig);
//
//		while (nextId <= aasxStencilFactory.getLevel()) {
//			prevIdShortPath.append("/" + AASXStencilFactory.SME_ID_PREFIX + nextId);
//
//			SubmodelElementCollection internalSMCConfig = createSMCConfig(SMC_CONFIG_ID_PREFIX + nextId,
//					prevIdShortPath.toString(), SMC_CONFIG_ID_PREFIX + ++nextId);
//
//			configurations.addSubmodelElement(internalSMCConfig);
//		}

		SubmodelElementCollection lastPropertyConfig = createSMCConfig(SMC_CONFIG_ID_PREFIX + 1,
				"stencilEvalAASIdShort/EvalSMId/location0/addresses0/address", "addr");

		configurations.addSubmodelElement(lastPropertyConfig);

		ReferenceElement stencil = new ReferenceElement("stencil",
				new Reference(new Key(KeyElements.ASSETADMINISTRATIONSHELL, true, "StencilEvalAASId", KeyType.IRI)));

		writerSM.addSubmodelElement(configurations);
		writerSM.addSubmodelElement(stencil);

		return writerSM;
	}

	private SubmodelElementCollection createSMCConfig(String confId, String idShortPathValue, String valueId) {
		SubmodelElementCollection configSMC = new SubmodelElementCollection(confId);

		Property idShortPath = createIdShortPathProperty(idShortPathValue);

		SubmodelElementCollection valueIds = createValueIds(valueId);

		Property expansionMode = createExpansionMode();
		Property suffix = createSuffix();

		SubmodelElementCollection metadata = createMetadata();

		configSMC.setElements(Arrays.asList(idShortPath, valueIds, expansionMode, suffix, metadata));

		return configSMC;
	}

	private SubmodelElementCollection createMetadata() {
		return new SubmodelElementCollection("metadata");
	}

	private Property createSuffix() {
		return new Property("suffix", "");
	}

	private Property createExpansionMode() {
		return new Property("expansionMode", "inline");
	}

	private SubmodelElementCollection createValueIds(String valueId) {
		SubmodelElementCollection valueIds = new SubmodelElementCollection("valueIds");
		Property valueId0 = new Property("valueId0", valueId);

		valueIds.addSubmodelElement(valueId0);
		return valueIds;
	}

	private Property createIdShortPathProperty(String value) {
		return new Property("idShortPath", value);
	}

	private Submodel createProcessorConfig() {
		Submodel processorSM = new Submodel("processorEvalSMIdShort", new CustomId("processorEvalSMId"));
		processorSM.setSemanticId(
				new Reference(new Key(KeyElements.SUBMODEL, false, "urn:di:aas:config:processor", KeyType.IRI)));

		Qualifier processorQualifier = new Qualifier();
		processorQualifier.setType("connectorFQCN");
		processorQualifier
				.setValue("org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.processor.jsonataprocessor.JsonAtaProcessor");

		processorSM.setQualifiers(Arrays.asList(processorQualifier));

		SubmodelElementCollection configurations = new SubmodelElementCollection("configurations");
		
		SubmodelElementCollection addrConfig = new SubmodelElementCollection("addr");
		
		Property expression = new Property("expression", "$map($.addr[], function($v) {$v & ' (: Address)'})");
		
		addrConfig.addSubmodelElement(expression);
		
		configurations.addSubmodelElement(addrConfig);

		processorSM.addSubmodelElement(configurations);

		return processorSM;
	}

	private Submodel createReaderConfig(int index, String apiUrl) {
		Submodel readerSM = new Submodel("readerEvalSMIdShort" + index, new CustomId("readerEvalSMId" + index));

		readerSM.setSemanticId(
				new Reference(new Key(KeyElements.SUBMODEL, false, "urn:di:aas:config:reader", KeyType.IRI)));

		Qualifier readerQualifier = new Qualifier();
		readerQualifier.setType("connectorFQCN");
		readerQualifier.setValue(
				"org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.reader.HttpRestApiDataReader");

		readerSM.setQualifiers(Arrays.asList(readerQualifier));

		SubmodelElementCollection dataSource = new SubmodelElementCollection("dataSource");

		SubmodelElementCollection sqlConnectionProperties = createHttpConnPropSMC(apiUrl);

		dataSource.addSubmodelElement(sqlConnectionProperties);

		Property mapperType = new Property("mapperType", "SimpleMapper");
		mapperType.setValueType(ValueType.String);

		SubmodelElementCollection valueMap = createValueMapSMC(index);

		readerSM.addSubmodelElement(dataSource);
		readerSM.addSubmodelElement(mapperType);
		readerSM.addSubmodelElement(valueMap);

		return readerSM;
	}

	private SubmodelElementCollection createValueMapSMC(int index) {
		SubmodelElementCollection valueMap = new SubmodelElementCollection("valueMap");

		Property address = new Property("addr", "$.address.address" + index);
		address.setValueType(ValueType.String);

		valueMap.addSubmodelElement(address);

		return valueMap;
	}

	private SubmodelElementCollection createHttpConnPropSMC(String api) {
		SubmodelElementCollection httpConnectionProperties = new SubmodelElementCollection("httpConnectionProperties");

		Property apiUrl = new Property("apiUrl", api);
		apiUrl.setValueType(ValueType.String);

		SubmodelElementCollection reqHeaders = createRequestHeadersSMC();

		SubmodelElementCollection reqParam = createRequestParameters();

		httpConnectionProperties.setElements(Arrays.asList(apiUrl, reqHeaders, reqParam));

		return httpConnectionProperties;
	}

	private SubmodelElementCollection createRequestHeadersSMC() {
		SubmodelElementCollection requestHeaders = new SubmodelElementCollection("requestHeaders");

		Property contentType = new Property("Content-Type", "application/json");
		contentType.setValueType(ValueType.String);

		requestHeaders.setElements(Arrays.asList(contentType));

		return requestHeaders;
	}

	private SubmodelElementCollection createRequestParameters() {
		SubmodelElementCollection requestParameters = new SubmodelElementCollection("requestParameters");

		Property apiKey = new Property("api_key", "c66c5dad-395c-4ec6-afdf-7b78eb94166a");
		apiKey.setValueType(ValueType.String);

		Property name = new Property("name", "Orb%20Intelligence");
		name.setValueType(ValueType.String);
		
		Property orbNum = new Property("orb_num", "23248971");
		orbNum.setValueType(ValueType.String);

		requestParameters.setElements(Arrays.asList(apiKey, name, orbNum));

		return requestParameters;
	}

}
