package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.eclipse.basyx.aas.factory.aasx.MetamodelToAASXConverter;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.aasserver.AASServer;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXConfigFactoryMultiReaders;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXConfigFactorySingleReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXStencilFactoryMultiReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.factory.sql.AASXStencilFactorySingleReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

@SpringBootTest(classes = DiAASEvaluation.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class DiAASEvaluationTest {
	
	private static final String AASX_FILE_PATH = "src/test/resources/aasx/EvaluationFile.aasx";
	private static final int STENCIL_LEVEL = 5;
	private static final String AAS_ID = "163834";
	private static Server mqttBroker;
	private static AASServer aasServer;
	
	@Autowired
	private JobRunner jobRunner;
	
	@BeforeAll
	public static void init() throws IOException, TransformerException, ParserConfigurationException {
		AASXConfigFactorySingleReader aasxConfigFactory = new AASXConfigFactorySingleReader(new AASXStencilFactorySingleReader(STENCIL_LEVEL));
		
		AasEnv env = aasxConfigFactory.create();
		
		prepareEvaluationFile(AASX_FILE_PATH, env);
		
		ZipSecureFile.setMinInflateRatio(0);
		
		aasServer = new AASServer(AASX_FILE_PATH);
		aasServer.startComponents();
		
		configureAndStartMqttBroker();
		
	}
	
	@Test
	void getAasEnv() throws Exception {
		jobRunner.getAasEnv(AAS_ID);
	}

	private static void prepareEvaluationFile(String filePath, AasEnv env) throws IOException, TransformerException, ParserConfigurationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MetamodelToAASXConverter.buildAASX(env, null, out);
		
		saveToFile(out, filePath);
	}

	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}
	
	private static void saveToFile(ByteArrayOutputStream out, String filePath) {

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
        	out.writeTo(fos);
            System.out.println("File saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@AfterAll
	public static void tearDown() {
		aasServer.stopComponents();
		
		mqttBroker.stopServer();
	}

}
