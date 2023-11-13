package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.parser;

import java.util.Collection;
import java.util.Optional;
import java.util.Stack;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.DataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdShortPathParser {

	private static Logger logger = LoggerFactory.getLogger(IdShortPathParser.class);

	private Collection<IAssetAdministrationShell> assetAdministrationShells;
	private Collection<ISubmodel> submodels;

	public IdShortPathParser(Collection<IAssetAdministrationShell> assetAdministrationShells,
			Collection<ISubmodel> submodels) {
		super();
		this.assetAdministrationShells = assetAdministrationShells;
		this.submodels = submodels;
	}

	public ISubmodelElement parseSMC(String idShortPath) {
		
		Stack<String> tokenStack = createTokenStack(idShortPath);

		ISubmodelElement nextSmElement = traverseAndGetSMElement(idShortPath, tokenStack);

		if (nextSmElement == null) {
			logger.debug("Error while trying to find the SubmodelElementCollection with provided idShortPath {}",
					idShortPath);
			return null;
		}

		if (!(nextSmElement instanceof SubmodelElementCollection)) {
			logger.debug(
					"The submodel element with id {} defined in idShortPath is not of type SubmodelElementCollection",
					nextSmElement.getIdShort());
			return null;
		}

		return nextSmElement;
	}

	public ISubmodelElement parseNonSMC(String idShortPath) {
		
		Stack<String> tokenStack = createTokenStack(idShortPath);

		ISubmodelElement nextSmElement = traverseAndGetSMElement(idShortPath, tokenStack);

		if (nextSmElement == null) {
			logger.debug("Error while trying to find the DataElement with provided idShortPath {}", idShortPath);
			return null;
		}

		if (!(nextSmElement instanceof DataElement)) {
			logger.debug("The submodel element with id {} defined in idShortPath is not of type DataElement",
					nextSmElement.getIdShort());
			return null;
		}

		return nextSmElement;
	}
	
	public ISubmodelElement getParentElement(String idShortPath) {
		
		idShortPath = removeSelfIdFromIdShortPath(idShortPath);
		
		Stack<String> tokenStack = createTokenStack(idShortPath);

		ISubmodelElement nextSmElement = traverseAndGetSMElement(idShortPath, tokenStack);

		if (nextSmElement == null) {
			logger.debug("Error while trying to find the Parent Element with provided idShortPath {}", idShortPath);
			return null;
		}

		if (!(nextSmElement instanceof SubmodelElementCollection)) {
			logger.debug("Parent : {} is not of type SMC",
					nextSmElement.getIdShort());
			return null;
		}

		return nextSmElement;
	}

	private String removeSelfIdFromIdShortPath(String idShortPath) {
		int lastSlashIndex = idShortPath.lastIndexOf('/');
		
        return idShortPath.substring(0, lastSlashIndex);
	}

	public static String parseSubmodelElementIdShort(String idShortPath) {
		String[] parts = idShortPath.split("/");

		if (parts.length < 3)
			throw new RuntimeException("Invalid idShortPath" + idShortPath
					+ "Couldn't parse submodel element idShort from the provided idShortPath");

		int lastDelimiterIndex = idShortPath.lastIndexOf("/");

		return idShortPath.substring(lastDelimiterIndex + 1);
	}

	public static String parseSubmodelIdShort(String idShortPath) {
		String[] parts = idShortPath.split("/");

		if (parts.length < 2)
			throw new RuntimeException("Invalid idShortPath" + idShortPath
					+ "Couldn't parse submodel idShort from the provided idShortPath");

		return parts[1];
	}
	
	public static String parseAASIdShort(String idShortPath) {
		String[] parts = idShortPath.split("/");
		
		if (parts.length < 1)
			throw new RuntimeException("Invalid idShortPath" + idShortPath
					+ "Couldn't parse AAS idShort from the provided idShortPath");
		
		return parts[0];
	}
	
	public static boolean isValidSubmodelIdShortPath(String idShortPath) {
		String[] parts = idShortPath.split("/");
		
		return parts.length == 2;
	}
	
	public static boolean isValidAASIdShortPath(String idShortPath) {
		String[] parts = idShortPath.split("/");
		
		return parts.length == 1;
	}

	private ISubmodelElement traverseAndGetSMElement(String idShortPath, Stack<String> tokenStack) {
		String nextToken = tokenStack.pop();

		ISubmodelElement nextSmElement = getFirstSMElement(tokenStack, nextToken);

		if (nextSmElement == null)
			return nextSmElement;

		while (!tokenStack.isEmpty()) {
			nextToken = tokenStack.pop();
			nextSmElement = getNextSMElement(nextSmElement, nextToken);
		}
		return nextSmElement;
	}

	private ISubmodelElement getFirstSMElement(Stack<String> tokenStack, String nextToken) {
		IAssetAdministrationShell aas = getAas(nextToken);

		if (tokenStack.isEmpty()) {
			logger.debug("The given idShortPath is for AAS with idShort {} only", aas.getIdShort());
			return null;
		}

		ISubmodel submodel = getSubmodel(aas, tokenStack.pop());

		if (tokenStack.isEmpty()) {
			logger.debug("The given idShortPath is for Submodel with idShort {} only", submodel.getIdShort());
			return null;
		}

		String smeIdShort = tokenStack.pop();

		return getSubmodelElement(smeIdShort, submodel);
	}

	private IAssetAdministrationShell getAas(String aasIdShort) {
		return assetAdministrationShells.stream().filter(aas -> aas.getIdShort().equals(aasIdShort)).findAny()
				.orElseThrow(() -> new RuntimeException(
						"The AAS with id " + aasIdShort + " defined in idShortPath could not be found"));
	}

	private ISubmodelElement getNextSMElement(ISubmodelElement nextSmElement, String nextToken) {
		return ((SubmodelElementCollection) nextSmElement).getSubmodelElements().values().stream()
				.filter(sme -> sme.getIdShort().equals(nextToken)).findAny().orElseThrow(() -> new RuntimeException(
						"The submodel element with id " + nextToken + " defined in idShortPath could not be found"));
	}

	private ISubmodelElement getSubmodelElement(String smeIdShort, ISubmodel submodel) {
		return submodel.getSubmodelElements().values().stream().filter(sme -> sme.getIdShort().equals(smeIdShort))
				.findAny().orElseThrow(() -> new RuntimeException(
						"The submodel element with id " + smeIdShort + " defined in idShortPath could not be found"));
	}

	private ISubmodel getSubmodel(IAssetAdministrationShell aas, String smIdShort) {
		
		return submodels.stream().filter(sm -> hasMatchingParentReference(sm.getParent().getKeys(), aas.getIdentification().getId()) && sm.getIdShort().equals(smIdShort)).findAny()
		.orElseThrow(() -> new RuntimeException(
				"The submodel with id " + smIdShort + " defined in idShortPath could not be found"));
	}
	
	private boolean hasMatchingParentReference(Collection<IKey> keys, String parentAASId) {
		Optional<IKey> optionalKey = keys.stream().filter(key -> key.getValue().equals(parentAASId)).findAny();
		
		return optionalKey.isPresent();
	}

	private Stack<String> createTokenStack(String idShortPath) {
		Stack<String> tokenStack = new Stack<>();

		String[] parts = idShortPath.split("/");

		for (int i = parts.length - 1; i >= 0; i--) {
			tokenStack.push(parts[i]);
		}

		return tokenStack;
	}

}
