package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql;

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

public class AASXConfigFactorySingleReader {

	private static final String DEFAULT_IDSHORT_PREFIX = AASXStencilFactoryMultiReader.EVAL_AAS_ID_SHORT + "/"
			+ AASXStencilFactoryMultiReader.EVAL_SM_ID_SHORT;

	private static final String SMC_CONFIG_ID_PREFIX = "arb_";

	private AASXStencilFactorySingleReader aasxStencilFactory;

	public AASXConfigFactorySingleReader(AASXStencilFactorySingleReader aasxStencilFactory) {
		this.aasxStencilFactory = aasxStencilFactory;
	}

	public AasEnv create() {

		AssetAdministrationShell configAAS = new AssetAdministrationShell();
		configAAS.setIdentification(new CustomId("EvaluationAASConfigId"));
		configAAS.setIdShort("evaluationAASIdShort");
		
		List<ISubmodel> submodelList = new ArrayList<>();

		
		int nextId = 1;
		
		// Below code is for n readers

//		while (nextId <= aasxStencilFactory.getLevel()) {
//			String query = "SELECT DISTINCT address FROM test_address WHERE id=" + nextId;
//			submodelList.add(createReaderConfig(nextId, query));
//			configAAS.addSubmodelReference(
//					new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextId, KeyType.CUSTOM)));
//			nextId++;
//		}
		
		// This is for just 1 reader
		String query = "SELECT address FROM test_address WHERE id BETWEEN 1 AND " + aasxStencilFactory.getLevel();
		submodelList.add(createReaderConfig(nextId, query));
		configAAS.addSubmodelReference(
				new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextId, KeyType.CUSTOM)));
		
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
			urlBuilder.append("/" + AASXStencilFactoryMultiReader.SME_ID_PREFIX + nextId++);
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
		
//		while (nextAttr <= aasxStencilFactory.getLevel()) {
//			ReferenceElement reader0 = new ReferenceElement("reader" + nextAttr);
//			reader0.setSemanticId(new Reference(
//					new Key(KeyElements.GLOBALREFERENCE, true, "basyx-di:connector:config:reader", KeyType.IRI)));
//			reader0.setValue(new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextAttr, KeyType.CUSTOM)));
//
//			readers.addSubmodelElement(reader0);
//			nextAttr++;
//		}
		
		// Below code is for 1 reader reference
		ReferenceElement reader0 = new ReferenceElement("reader" + nextAttr);
		reader0.setSemanticId(new Reference(
				new Key(KeyElements.GLOBALREFERENCE, true, "basyx-di:connector:config:reader", KeyType.IRI)));
		reader0.setValue(new Reference(new Key(KeyElements.SUBMODEL, true, "readerEvalSMId" + nextAttr, KeyType.CUSTOM)));

		readers.addSubmodelElement(reader0);

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

	private Submodel createReaderConfig(int index, String query) {
		Submodel readerSM = new Submodel("readerEvalSMIdShort" + index, new CustomId("readerEvalSMId" + index));

		readerSM.setSemanticId(
				new Reference(new Key(KeyElements.SUBMODEL, false, "urn:di:aas:config:reader", KeyType.IRI)));

		Qualifier readerQualifier = new Qualifier();
		readerQualifier.setType("connectorFQCN");
		readerQualifier.setValue(
				"org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.sqlcursorreader.reader.SqlCursorDataReader");

		readerSM.setQualifiers(Arrays.asList(readerQualifier));

		SubmodelElementCollection dataSource = new SubmodelElementCollection("dataSource");

		SubmodelElementCollection sqlConnectionProperties = createSqlConnPropSMC(query);

		dataSource.addSubmodelElement(sqlConnectionProperties);

		Property mapperType = new Property("mapperType", "SimpleMapper");
		mapperType.setValueType(ValueType.String);

		SubmodelElementCollection valueMap = createValueMapSMC();

		readerSM.addSubmodelElement(dataSource);
		readerSM.addSubmodelElement(mapperType);
		readerSM.addSubmodelElement(valueMap);

		return readerSM;
	}

	private SubmodelElementCollection createValueMapSMC() {
		SubmodelElementCollection valueMap = new SubmodelElementCollection("valueMap");

		Property language = new Property("addr", "address");
		language.setValueType(ValueType.String);

		valueMap.addSubmodelElement(language);

		return valueMap;
	}

	private SubmodelElementCollection createSqlConnPropSMC(String query) {
		SubmodelElementCollection sqlConnectionPropertiesSMC = new SubmodelElementCollection("sqlConnectionProperties");

		Property host = new Property("host", "127.0.0.1");
		host.setValueType(ValueType.String);

		Property port = new Property("port", 1433);
		port.setValueType(ValueType.Int32);

		Property databaseName = new Property("databaseName", "scales-net");
		databaseName.setValueType(ValueType.String);

		SubmodelElementCollection credential = createCredentialSMC();

		SubmodelElementCollection options = createOptionSMC();

		Property sqlQuery = new Property("sqlQuery", query);
		databaseName.setValueType(ValueType.String);

		sqlConnectionPropertiesSMC.setElements(Arrays.asList(host, port, databaseName, credential, options, sqlQuery));

		return sqlConnectionPropertiesSMC;
	}

	private SubmodelElementCollection createCredentialSMC() {
		SubmodelElementCollection credentialSMC = new SubmodelElementCollection("credential");

		Property username = new Property("username", "danish");
		username.setValueType(ValueType.String);

		Property password = new Property("password", "Sajjad%786");
		password.setValueType(ValueType.String);

		credentialSMC.setElements(Arrays.asList(username, password));

		return credentialSMC;
	}

	private SubmodelElementCollection createOptionSMC() {
		SubmodelElementCollection optionSMC = new SubmodelElementCollection("options");

		Property encrypt = new Property("encrypt", "true");
		encrypt.setValueType(ValueType.Boolean);

		Property trustServerCertificate = new Property("trustServerCertificate", "true");
		trustServerCertificate.setValueType(ValueType.Boolean);

		optionSMC.setElements(Arrays.asList(encrypt, trustServerCertificate));

		return optionSMC;
	}

}
