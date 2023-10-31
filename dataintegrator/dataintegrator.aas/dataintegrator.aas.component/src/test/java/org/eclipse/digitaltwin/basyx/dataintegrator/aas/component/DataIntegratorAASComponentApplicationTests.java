package org.eclipse.digitaltwin.basyx.dataintegrator.aas.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.component.aasserver.AASServer;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DataIntegratorAASComponentApplicationTests {
	
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
		
		aasServer = new AASServer("src/test/resources/aasx/ModBasicCal_Cert_Cal_Plan.aasx");
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

	@Test
	void jsonPathCheck() throws IOException {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});

		String filePath = "src/test/resources/TestJson.json";

		String jsonStr = Files.readString(Paths.get(filePath));

		ReadContext ctx = JsonPath.parse(jsonStr);

		String exprForModelType = "$..[?(@.parent.keys[?(@.type == 'Submodel' && @.value == ['https://example.com/ids/sm/6224_7130_5032_8347'])].value)]";

		TypeRef<List<Object>> typeRefModelType = new TypeRef<List<Object>>() {
		};

		ctx.read(exprForModelType, typeRefModelType);
	}
	
	@Test
	void jsonPathValidate() throws IOException {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});

		String expectedJson = Files.readString(Paths.get("src/test/resources/ExpectedJsonPathValidationJson.json"));
		
		String filePath = "src/test/resources/Test2Json.json";

		String jsonStr = Files.readString(Paths.get(filePath));
		
		DocumentContext context = JsonPath.parse(jsonStr);

		String exprForAsset = "$..[?(@.identification.id=='163834')].asset.keys[0].value";

		context.set(exprForAsset, "AFDGTYHY");
		
		assertSameJSONContent(expectedJson, context.jsonString());
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
