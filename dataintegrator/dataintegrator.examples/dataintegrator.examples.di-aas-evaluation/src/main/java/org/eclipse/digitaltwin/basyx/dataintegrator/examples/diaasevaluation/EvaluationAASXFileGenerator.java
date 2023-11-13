package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.eclipse.basyx.aas.factory.aasx.MetamodelToAASXConverter;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.http.AASXConfigFactorySingleReaderHTTP;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.http.AASXStencilFactorySingleReaderHTTP;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXConfigFactoryMultiReaders;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXConfigFactorySingleReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXStencilFactoryMultiReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXStencilFactorySingleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationAASXFileGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(EvaluationAASXFileGenerator.class);
	
	private static final String AASX_FILE_PATH_PREFIX = "src/main/resources/aasx";
	private static final String AASX_FILE_NAME_PREFIX = "EvaluationFile";
	private static final String AASX_FILE_NAME_SUFFIX = ".aasx";
	
	public static void generate(int limit, int stepSize) throws IOException, TransformerException, ParserConfigurationException {
		if (limit < stepSize) {
			logger.error("Limit {} can't be less than step size {}", limit, stepSize);
			return;
		}
		
		ZipSecureFile.setMinInflateRatio(0);
		
		AASXStencilFactorySingleReaderHTTP stencilFactory = new AASXStencilFactorySingleReaderHTTP(stepSize);
		
		AASXConfigFactorySingleReaderHTTP aasxConfigFactory = new AASXConfigFactorySingleReaderHTTP(stencilFactory);
		
		for (int i = stepSize; i <= limit; i += stepSize) {
			AasEnv env = aasxConfigFactory.create();
			
			prepareEvaluationFile(AASX_FILE_PATH_PREFIX + "/" + AASX_FILE_NAME_PREFIX + "_" + i + AASX_FILE_NAME_SUFFIX, env);
			
			stencilFactory.setLevel(i);
		}
		
		logger.info("AASX Generated Successfully!");
		
		logger.info("IdShortPath of the leaf element {}", aasxConfigFactory.generateIdShortPath());
	
	}
	
	private static void prepareEvaluationFile(String filePath, AasEnv env) throws IOException, TransformerException, ParserConfigurationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MetamodelToAASXConverter.buildAASX(env, null, out);
		
		saveToFile(out, filePath);
	}
	
	private static void saveToFile(ByteArrayOutputStream out, String filePath) {

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
        	out.writeTo(fos);
            logger.info("File {} saved successfully.", filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
