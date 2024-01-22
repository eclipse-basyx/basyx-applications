package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser.IdShortPathParser;

public class AasMetamodelCreatorHelper {
	
	private static final String CONFIGURATIONS = "configurations";
	private static final String SUFFIX = "suffix";
	private static final String IDSHORT_PATH = "idShortPath";
	private static final String METADATA = "metadata";
	
	private Submodel aasWriterConfig;
	
	public AasMetamodelCreatorHelper(Submodel aasWriterConfig) {
		super();
		this.aasWriterConfig = aasWriterConfig;
	}
	
	public SubmodelElementCollection getConfigurationsSMC() {
		return (SubmodelElementCollection) aasWriterConfig.getSubmodelElement(CONFIGURATIONS);
	}

	public String getSuffixFromConfig(String uniqueId) {
		SubmodelElementCollection smcConfig = (SubmodelElementCollection) getConfigurationsSMC().getSubmodelElement(uniqueId);

		return (String) smcConfig.getSubmodelElement(SUFFIX).getValue();
	}
	
	public SubmodelElementCollection getMetadataFromConfig(String uniqueId) {
		SubmodelElementCollection smcConfig = (SubmodelElementCollection) getConfigurationsSMC().getSubmodelElement(uniqueId);
		
		return (SubmodelElementCollection) smcConfig.getSubmodelElement(METADATA);
	}
	
	public String getIdShortPathFromConfig(String uniqueId) {
		SubmodelElementCollection smcConfig = (SubmodelElementCollection) getConfigurationsSMC().getSubmodelElement(uniqueId);

		return (String) smcConfig.getSubmodelElement(IDSHORT_PATH).getValue();
	}
	
	public List<ISubmodelElement> findSubmodelConfigs() {
		return getConfigurationsSMC().getSubmodelElements().values().stream().filter(configSMC -> IdShortPathParser.isValidSubmodelIdShortPath(getIdShortPathFromConfig(configSMC.getIdShort()))).collect(Collectors.toList());
	}
	
	public List<ISubmodelElement> findAASConfigs() {
		return getConfigurationsSMC().getSubmodelElements().values().stream().filter(configSMC -> IdShortPathParser.isValidAASIdShortPath(getIdShortPathFromConfig(configSMC.getIdShort()))).collect(Collectors.toList());
	}

}
