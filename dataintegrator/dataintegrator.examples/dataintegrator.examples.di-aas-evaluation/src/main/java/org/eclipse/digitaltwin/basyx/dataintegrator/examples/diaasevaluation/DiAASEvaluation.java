package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.aasserver.AASServer;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.http.HTTPServer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(scanBasePackages = "org.eclipse.digitaltwin.basyx", exclude = { MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class })
@EnableBatchProcessing
public class DiAASEvaluation {
	
	private static final int LEVEL = 100;
	
	private static final String AASX_FILE_PATH_PREFIX = "src/main/resources/aasx/EvaluationFile_" + LEVEL + ".aasx";
	
	public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException {
		EvaluationAASXFileGenerator.generate(LEVEL, LEVEL);
		
		AASServer server = new AASServer(AASX_FILE_PATH_PREFIX);
		server.startComponents();
		
		// Below server only when evaluating HTTP
		HTTPServer httpServer = new HTTPServer(5002);
		httpServer.start();
		
		SpringApplication.run(DiAASEvaluation.class, args);
	}

}