package org.eclipse.digitaltwin.basyx.dataintegrator.examples.calcertsmcalplandiaas;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.component.DataIntegratorAASComponentApplication;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

@SpringBootTest(classes = DataIntegratorAASComponentApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class CalCertSMCalPlanAasTest {
	
	private static final String AAS_ID = "163834";
	private static Server mqttBroker;
	private static AASServer aasServer;
	
	@Autowired
	private JobRunner jobRunner;

	@Test
	void contextLoads() {
	}
	
	@BeforeAll
	public static void init() throws IOException {
		configureAndStartMqttBroker();
		
		aasServer = new AASServer();
		aasServer.startComponents();
	}

	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}

	@Test
	void getAasEnv() throws Exception {
		String expectedJson = readJSONStringFromFile("classpath:ExpectedAASJson.json");
		
		ObjectMapper mapper = new ObjectMapper();
		
		AasEnv actualResult = jobRunner.getAasEnv(AAS_ID);
		
		String actualResultJson = mapper.writeValueAsString(actualResult);
		
		assertSameJSONContent(expectedJson, actualResultJson);
	}
	
	private static String readJSONStringFromFile(String fileName) throws FileNotFoundException, IOException {
		File file = ResourceUtils.getFile(fileName);
		InputStream in = new FileInputStream(file);
		return IOUtils.toString(in, StandardCharsets.UTF_8.name());
	}
	
	private static void assertSameJSONContent(String expected, String actual) throws JsonProcessingException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();

		assertEquals(mapper.readTree(expected), mapper.readTree(actual));
	}
	
	@AfterAll
	public static void tearDown() {
		aasServer.stopComponents();
		
		mqttBroker.stopServer();
	}

}
