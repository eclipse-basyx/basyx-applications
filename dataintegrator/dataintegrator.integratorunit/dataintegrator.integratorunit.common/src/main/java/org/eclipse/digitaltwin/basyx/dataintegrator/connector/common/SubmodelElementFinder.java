package org.eclipse.digitaltwin.basyx.dataintegrator.connector.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;

public class SubmodelElementFinder {

	private SubmodelElementFinder() {
		throw new IllegalStateException("Utility class");
	}

	public static ISubmodelElement find(List<Submodel> submodels, String smElemIdShort) {

		Optional<ISubmodelElement> optionalSmElement = submodels.stream()
				.map(sm -> findMatchingSME(sm.getSubmodelElements(), smElemIdShort)).filter(Objects::nonNull)
				.findFirst();
		
		if (optionalSmElement.isEmpty())
			return null;

		return optionalSmElement.get();
	}

	private static ISubmodelElement findMatchingSME(Map<String, ISubmodelElement> smElemMap, String idShort) {
		if (smElemMap.containsKey(idShort))
			return smElemMap.get(idShort);

		Optional<ISubmodelElement> optionalSME = smElemMap.values().stream()
				.filter(SubmodelElementCollection.class::isInstance).map(SubmodelElementCollection.class::cast)
				.map(smc -> findMatchingSME(smc.getSubmodelElements(), idShort)).filter(Objects::nonNull).findFirst();

		if (optionalSME.isEmpty())
			return null;

		return optionalSME.get();
	}

}
