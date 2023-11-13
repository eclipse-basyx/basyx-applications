package org.eclipse.digitaltwin.basyx.dataintegrator.registry.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.component.aasserver.AASServer;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.runner.JobRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class DataIntegratorRegistryComponentApplicationTests {

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

		List<AASDescriptor> actualResult = jobRunner.getAasDescriptor();

		String actualResultJson = mapper.writeValueAsString(actualResult);

		assertSameJSONContent(expectedJson, actualResultJson);
	}

	private static String readJSONStringFromFile(String fileName) throws FileNotFoundException, IOException {
		File file = ResourceUtils.getFile(fileName);
		InputStream in = new FileInputStream(file);
		return IOUtils.toString(in, StandardCharsets.UTF_8.name());
	}

	private static void assertSameJSONContent(String expected, String actual)
			throws JsonProcessingException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();

		assertEquals(mapper.readTree(expected), mapper.readTree(actual));
	}

	@AfterAll
	public static void tearDown() {
		aasServer.stopComponents();

		mqttBroker.stopServer();
	}
}
