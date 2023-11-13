package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory.NonSmcSmeFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.utils.AasMetamodelCreatorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentNonSmcSmeFactory {
	
	private static Logger logger = LoggerFactory.getLogger(ConcurrentNonSmcSmeFactory.class);

	public Map<String, List<SubmodelElement>> resolveConcurrently(Map<String, List<Object>> collectiveResult,
			Map<String, SubmodelElement> mapOfNonSMC, AasMetamodelCreatorHelper creatorHelper)
			throws InterruptedException, ExecutionException {
		int mapSize = mapOfNonSMC.size();
        int maxParallelism = Runtime.getRuntime().availableProcessors(); // Maximum parallelism level
        
        int numberOfParts = Math.min(mapSize, maxParallelism); // Determine the number of parts based on map size and available processors
        
        logger.debug("Paralell processing with #cores : {}", numberOfParts);
        
		List<Map<String, SubmodelElement>> dividedMaps = divideMap(mapOfNonSMC,
				numberOfParts);

		// Execute tasks in parallel for each divided map part
		List<CompletableFuture<Map<String, List<SubmodelElement>>>> taskFutures = dividedMaps.stream()
				.map(mapPart -> CompletableFuture.supplyAsync(() -> performTask(collectiveResult, mapPart, creatorHelper)))
				.collect(Collectors.toList());

		// Combine the results from all tasks
		CompletableFuture<Void> allTasks = CompletableFuture.allOf(taskFutures.toArray(new CompletableFuture[0]));

		CompletableFuture<Map<String, List<SubmodelElement>>> combinedResults = allTasks.thenApply(
				v -> taskFutures.stream().map(CompletableFuture::join).collect(HashMap::new, Map::putAll, Map::putAll));

		return combinedResults.get();
	}

	private static List<Map<String, SubmodelElement>> divideMap(Map<String, SubmodelElement> inputMap,
			int numberOfParts) {
		List<Map<String, SubmodelElement>> dividedMaps = new ArrayList<>();
		int mapSize = inputMap.size();
		int batchSize = (int) Math.ceil((double) mapSize / numberOfParts);

		List<String> keys = new ArrayList<>(inputMap.keySet());
		for (int i = 0; i < numberOfParts; i++) {
			int startIndex = i * batchSize;
			int endIndex = Math.min(startIndex + batchSize, mapSize);

			Map<String, SubmodelElement> subMap = new HashMap<>();
			for (int j = startIndex; j < endIndex; j++) {
				String key = keys.get(j);
				subMap.put(key, inputMap.get(key));
			}

			dividedMaps.add(subMap);
		}

		return dividedMaps;
	}

	private static Map<String, List<SubmodelElement>> performTask(Map<String, List<Object>> collectiveResult,
			Map<String, SubmodelElement> mapNonSmcPart, AasMetamodelCreatorHelper creatorHelper) {

		return NonSmcSmeFactory.create(collectiveResult, mapNonSmcPart, creatorHelper);
	}

}
