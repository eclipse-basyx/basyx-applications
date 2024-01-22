package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.regression.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser.IdShortPathParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestIdShortPathParser {

	private AssetAdministrationShell assetAdministrationShell;
	private Submodel submodel;

	@BeforeEach
	public void setup() {
		submodel = new Submodel("testIdShort", new CustomId("dummySMId"));

		Property outerProp = new Property("testOuterProperty", "testOuterPropertyValue");

		SubmodelElementCollection smc = new SubmodelElementCollection("testSMCIdShort");

		Property prop = new Property("testProperty", "testPropertyValue");

		SubmodelElementCollection innerSmc = new SubmodelElementCollection("testInnerSMCIdShort");
		Property innerProp = new Property("testInnerProperty", "testInnerPropertyValue");
		innerSmc.addSubmodelElement(innerProp);

		smc.addSubmodelElement(prop);
		smc.addSubmodelElement(innerSmc);

		submodel.addSubmodelElement(outerProp);
		submodel.addSubmodelElement(smc);
		
		assetAdministrationShell = new AssetAdministrationShell();
		assetAdministrationShell.setIdentification(new CustomId("testAASId"));
		assetAdministrationShell.setIdShort("testAASIdShort");
		assetAdministrationShell.setSubmodelReferences(Arrays.asList(new Reference(new Key(KeyElements.SUBMODEL, true, submodel.getIdentification().getId(), KeyType.CUSTOM))));
	}

	@Test
	void testOuterPropertyParse() {
		String idShortPath = "testAASIdShort/testIdShort/testOuterProperty";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		ISubmodelElement actualSubmodelElement = idShortPathParser.parseNonSMC(idShortPath);

		assertEquals("testOuterProperty", actualSubmodelElement.getIdShort());
	}

	@Test
	void testOneLevelInnerPropertyParse() {
		String idShortPath = "testAASIdShort/testIdShort/testSMCIdShort/testProperty";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		ISubmodelElement actualSubmodelElement = idShortPathParser.parseNonSMC(idShortPath);

		assertEquals("testProperty", actualSubmodelElement.getIdShort());
	}

	@Test
	void testInnerMostPropertyParse() {
		String idShortPath = "testAASIdShort/testIdShort/testSMCIdShort/testInnerSMCIdShort/testInnerProperty";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		ISubmodelElement actualSubmodelElement = idShortPathParser.parseNonSMC(idShortPath);

		assertEquals("testInnerProperty", actualSubmodelElement.getIdShort());
	}

	@Test
	void testNotExistingSMEParse() {
		String idShortPath = "testAASIdShort/testIdShort/testSMCIdShort/nonExistingSMEIdShort";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		assertThrows(RuntimeException.class, () -> {
			idShortPathParser.parseNonSMC(idShortPath);
		});
	}
	
	@Test
	void testSMCParse() {
		String idShortPath = "testAASIdShort/testIdShort/testSMCIdShort/testInnerSMCIdShort";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		ISubmodelElement actualSubmodelElement = idShortPathParser.parseSMC(idShortPath);

		assertEquals("testInnerSMCIdShort", actualSubmodelElement.getIdShort());
	}
	
	@Test
	void nullWhenUsingParseSMCWithSMEPath() {
		String idShortPath = "testAASIdShort/testIdShort/testSMCIdShort/testInnerSMCIdShort/testInnerProperty";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		assertNull(idShortPathParser.parseSMC(idShortPath));
	}
	
	@Test
	void nullWhenUsingParseNonSMCWithSMCPath() {
		String idShortPath = "testAASIdShort/testIdShort/testSMCIdShort/testInnerSMCIdShort";

		IdShortPathParser idShortPathParser = new IdShortPathParser(Arrays.asList(assetAdministrationShell), Arrays.asList(submodel));

		assertNull(idShortPathParser.parseNonSMC(idShortPath));
	}

}
