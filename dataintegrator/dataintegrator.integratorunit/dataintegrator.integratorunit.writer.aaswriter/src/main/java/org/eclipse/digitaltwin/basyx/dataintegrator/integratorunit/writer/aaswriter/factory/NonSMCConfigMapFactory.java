package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser.IdShortPathParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NonSMCConfigMapFactory {
	
	private static Logger logger = LoggerFactory.getLogger(NonSMCConfigMapFactory.class);

	public static Map<String, SubmodelElement> create(Map<String, ISubmodelElement> dataWriterConfig, AasEnv aasEnv) {
		IdShortPathParser pathParser = new IdShortPathParser(aasEnv.getAssetAdministrationShells(), aasEnv.getSubmodels());

		return dataWriterConfig.entrySet().stream().map(configEntry -> toNonSMCConfigEntry(configEntry, pathParser))
				.filter(Objects::nonNull).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private static Entry<String, SubmodelElement> toNonSMCConfigEntry(Entry<String, ISubmodelElement> configEntry,
			IdShortPathParser pathParser) {
		
		String idShortPath = (String) ((SubmodelElementCollection) configEntry.getValue()).getSubmodelElement("idShortPath").getValue();
		
		ISubmodelElement smElem = pathParser.parseNonSMC(idShortPath); 
		
		if (smElem == null) {
			logger.debug("Couldn't find the DataElement with the id {}", idShortPath);
			return null;
		}

		if (!isInstanceOfNonSMC(smElem))
			return null;

		return new AbstractMap.SimpleEntry<>(configEntry.getKey(), (SubmodelElement) smElem);
	}

	private static boolean isInstanceOfNonSMC(ISubmodelElement smElem) {
		return smElem.getModelType().equals(KeyElements.PROPERTY.toString())
				|| smElem.getModelType().equals(KeyElements.RANGE.toString())
				|| smElem.getModelType().equals(KeyElements.FILE.toString())
				|| smElem.getModelType().equals(KeyElements.BLOB.toString());
	}

}
