package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer.DataWriter;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ExecutionContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import com.fasterxml.jackson.databind.ObjectMapper;

@StepScope
public class AasWriter implements DataWriter<Map<String, List<Object>>>, StepExecutionListener {

	private Logger logger = LoggerFactory.getLogger(AasWriter.class);

	private Submodel dataWriterConfiguration;
	public static final String TEMP_DIRECTORY = "basyx-temp";
	private AasWriterHelper aasWriterHelper = new AasWriterHelper();
	private AasEnv aasEnv;
	private Map<String, List<Object>> collectiveResult = new HashMap<>();

	public AasWriter(Submodel dataWriterConfiguration) {
		this.dataWriterConfiguration = dataWriterConfiguration;
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		AasMetamodelCreator metamodelCreator = new AasMetamodelCreator(collectiveResult, dataWriterConfiguration , getAasEnv());
		
		AasEnv createdAasEnv = metamodelCreator.create();
		
		stepExecution.getExecutionContext().put(ExecutionContextEnum.AAS_ENV.getName(), createdAasEnv);
		
		return stepExecution.getExitStatus();
	}

	@Override
	public void write(List<? extends Map<String, List<Object>>> resultDefinitions) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		
//		logger.info("Input to AasWriter : {}",
//				mapper.writeValueAsString(resultDefinitions));
		
		resultDefinitions.stream().forEach(this::collectResult);
//		AasServer.start();
		
//		resultDefinitions.stream().flatMap(Collection::stream).forEach(t -> {
//			try {
//				updateValueToAas(t);
//			} catch (InvalidFormatException | IOException | ParserConfigurationException | SAXException e) {
//				e.printStackTrace();
//			}
//		});
		
		aasEnv = aasWriterHelper.createAasEnv();
		
//		AasServer.stop();
	}

	@Override
	public Submodel getDataWriterConfiguration() {
		return this.dataWriterConfiguration;
	}

	public static String readFileAsString(String file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file)));
	}

	protected Path createTempDirectory() {
		return Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), TEMP_DIRECTORY);
	}
	
	private AasEnv getAasEnv() {
		ReferenceElement stencilReference = (ReferenceElement) dataWriterConfiguration.getSubmodelElement("stencil");
		
		IKey referenceKey = stencilReference.getValue().getKeys().get(0);
		
		KeyType stencilIdKeyType = referenceKey.getIdType();
		
		String stencilId = referenceKey.getValue();
		
		return new StencilLooker(new Identifier(IdentifierType.fromString(stencilIdKeyType.name()), stencilId)).findStencil();
	}
	
	private void collectResult(Map<String, List<Object>> result) {
		result.entrySet().stream().forEach(this::addEntryToFinalResult);
	}
	
	private void addEntryToFinalResult(Entry<String, List<Object>> result) {
		if (collectiveResult.containsKey(result.getKey())) {
			List<Object> newValues = new ArrayList<>();
			newValues.addAll(collectiveResult.get(result.getKey()));
			newValues.addAll(result.getValue());
			collectiveResult.put(result.getKey(), newValues);
		} else {
			collectiveResult.put(result.getKey(), result.getValue());
		}
	}
}
