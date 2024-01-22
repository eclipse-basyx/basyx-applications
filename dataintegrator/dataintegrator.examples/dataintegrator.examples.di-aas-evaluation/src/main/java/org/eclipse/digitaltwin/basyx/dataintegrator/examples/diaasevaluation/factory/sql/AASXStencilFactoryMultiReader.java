package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;

public class AASXStencilFactoryMultiReader {

	public static final String EVAL_AAS_ID_SHORT = "stencilEvalAASIdShort";
	public static final String EVAL_SM_ID_SHORT = "EvalSMId";

	public static final String SME_ID_PREFIX = "smcLvl_";
	
	private int level;
	
	public AASXStencilFactoryMultiReader(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public AasEnv create() {

		AssetAdministrationShell evalAAS = new AssetAdministrationShell();
		evalAAS.setIdentification(new Identifier(IdentifierType.IRI, "StencilEvalAASId"));
		evalAAS.setIdShort(EVAL_AAS_ID_SHORT);

		evalAAS.addSubmodelReference(new Reference(new Key(KeyElements.SUBMODEL, true, "EvalSMId", KeyType.CUSTOM)));

		List<IAssetAdministrationShell> aasList = new ArrayList<>();
		aasList.add(evalAAS);

		List<ISubmodel> smList = new ArrayList<>();
		smList.add(createSubmodel());

		return new AasEnv(aasList, Arrays.asList(), Arrays.asList(), smList);
	}

	private Submodel createSubmodel() {
		Submodel submodel = new Submodel(EVAL_SM_ID_SHORT, new CustomId("EvalSMId"));

		int prevId = 0;

		SubmodelElementCollection prevSMC = createSMC(SME_ID_PREFIX, prevId);

		submodel.addSubmodelElement(prevSMC);

		while (prevId < level) {
			SubmodelElementCollection currSMC = createSMC(SME_ID_PREFIX, ++prevId);

			prevSMC.addSubmodelElement(currSMC);

			prevSMC = currSMC;
		}

		Property legacySystemDataProperty = new Property("legacySysProperty", "");

		prevSMC.addSubmodelElement(legacySystemDataProperty);

		return submodel;
	}

	private SubmodelElementCollection createSMC(String smeIdPrefix, int prevId) {
		SubmodelElementCollection smc = new SubmodelElementCollection(smeIdPrefix + prevId);
		Property dummyFixedProperty = createDummyFixedProperty(prevId);
		smc.addSubmodelElement(dummyFixedProperty);

		return smc;
	}

	private Property createDummyFixedProperty(int level) {
		return new Property("dummyEvalProperty_" + level, "dummyValue_" + level);
	}

}
