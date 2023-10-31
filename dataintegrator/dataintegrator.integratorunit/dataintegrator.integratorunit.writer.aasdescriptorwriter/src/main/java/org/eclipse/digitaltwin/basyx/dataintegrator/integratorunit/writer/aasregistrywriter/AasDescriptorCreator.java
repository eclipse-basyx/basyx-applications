package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aasregistrywriter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;

public class AasDescriptorCreator {

	private Map<String, List<Object>> collectiveResult;
	private Submodel registryConfiguration;

	public AasDescriptorCreator(Map<String, List<Object>> collectiveResult,
			Submodel registryConfiguration) {
		super();
		this.collectiveResult = collectiveResult;
		this.registryConfiguration = registryConfiguration;
	}

	public List<AASDescriptor> create() {
		Map<String, ISubmodelElement> aasDescConfigMap = getAasDescConfigMap();
		
		List<String> aasIds = AasDescriptorWriterHelper
				.getAasIdsFromResult(collectiveResult, (String) aasDescConfigMap.get("aasIdValueId").getValue())
				.stream().map(String.class::cast).collect(Collectors.toList());

		String suffix = (String) aasDescConfigMap.get("suffix").getValue();

		return aasIds.stream().map(aasId -> createAasDescriptor(aasId, suffix))
				.collect(Collectors.toList());
	}

	private AASDescriptor createAasDescriptor(String aasId, String suffix) {
		final String updatedAasId = suffix != null && !suffix.isBlank() ? aasId + suffix : aasId;
		
		String aasApiUrl = (String) registryConfiguration.getSubmodelElement("aasAPIURL").getValue();

		List<String> smIds = AasDescriptorWriterHelper.getSubmodelIds(collectiveResult, updatedAasId).stream()
				.map(String.class::cast).collect(Collectors.toList());
		
		List<SubmodelDescriptor> submodelDescriptors = smIds.stream().map(smId -> createSubmodelDescriptor(aasId, smId, aasApiUrl)).collect(Collectors.toList());
		
		return createAasDescriptorWithSmDescriptor(aasId, submodelDescriptors, aasApiUrl);
	}

	private AASDescriptor createAasDescriptorWithSmDescriptor(String aasId,
			List<SubmodelDescriptor> submodelDescriptors, String aasApiUrl) {
		
		String endpoint = AasDescriptorWriterHelper.createAasDescriptorEndpoint(aasId, aasApiUrl);
		
		IdentifierType aasIdentifierType = IdentifierType.valueOf((String) getAasDescConfigMap().get("aasIdentifierType").getValue());
		
		AASDescriptor aasDescriptor;
		
		if (getAasDescConfigMap().containsKey("fixedIdShort"))
			aasDescriptor = new AASDescriptor((String) getAasDescConfigMap().get("fixedIdShort").getValue(), new Identifier(aasIdentifierType, aasId), endpoint);
		else 
			aasDescriptor = new AASDescriptor(new Identifier(aasIdentifierType, aasId), endpoint);
		
		submodelDescriptors.stream().forEach(aasDescriptor::addSubmodelDescriptor);
		
		return aasDescriptor;
	}

	private SubmodelDescriptor createSubmodelDescriptor(String aasId, String smId, String aasApiUrl) {
		
		Map<String, ISubmodelElement> smDescConfigMap = getSmDescConfigMap();
		
		String smIdShort = AasDescriptorWriterHelper.getIdShort(new RegexEvaluator((String) smDescConfigMap.get("smIdShortRegex").getValue(), smId));
		
		String endpoint = AasDescriptorWriterHelper.createSmDescriptorEndpoint(aasId, smIdShort, aasApiUrl);
		
		IdentifierType smIdentifierType = IdentifierType.valueOf((String) smDescConfigMap.get("smIdentifierType").getValue());
		
		return new SubmodelDescriptor(smIdShort, new Identifier(smIdentifierType, smId), endpoint);
	}
	
	private Map<String, ISubmodelElement> getAasDescConfigMap() {
		SubmodelElementCollection aasDescConfigSMC = (SubmodelElementCollection) registryConfiguration.getSubmodelElement("aasDescriptorConfig");
		
		return aasDescConfigSMC.getSubmodelElements();
	}
	
	private Map<String, ISubmodelElement> getSmDescConfigMap() {
		SubmodelElementCollection smDescConfigSMC = (SubmodelElementCollection) registryConfiguration.getSubmodelElement("smDescriptorConfig");
		
		return smDescConfigSMC.getSubmodelElements();
	}

}
