package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aasregistrywriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.aas.aggregator.AASAggregatorAPIHelper;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;

public class AasDescriptorWriterHelper {
	
	protected static List<Object> getAasIdsFromResult(Map<String, List<Object>> result, String aasIdValueId) {
		if (!result.containsKey(aasIdValueId))
			throw new RuntimeException("The result container defined in configuration with valueId : " + aasIdValueId + "  could not be found");
		
		return result.get(aasIdValueId);
	}

	protected static List<Object> getSubmodelIds(Map<String, List<Object>> result, String aasId) {
		if (!result.containsKey(aasId))
			return new ArrayList<>();
		
		return result.get(aasId);
	}

	protected static String createSmDescriptorEndpoint(String aasId, String smIdShort, String aasApiUrl) {
		return aasApiUrl + "/" + aasId + "/" + AASAggregatorAPIHelper.AAS_SUFFIX + "/" + AssetAdministrationShell.SUBMODELS + "/" + smIdShort;
	}

	public static String getIdShort(RegexEvaluator regexEvaluator) {
		if (regexEvaluator.getRegexPattern().isBlank())
			return regexEvaluator.getString();
		
		return regexEvaluator.evaluate();
	}

	public static String createAasDescriptorEndpoint(String aasId, String aasApiUrl) {
		return aasApiUrl + "/" + aasId + "/" + AASAggregatorAPIHelper.AAS_SUFFIX;
	}

}
