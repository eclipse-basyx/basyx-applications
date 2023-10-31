package org.eclipse.digitaltwin.basyx.dataintegrator.common.mqtt;

import java.util.Optional;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.ConfigurationRepository;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

	private static final String TOPIC_INITIAL = "aas-registry";

	private static final String TOPIC_SUFFIX_CREATE = "submodels/created";
	private static final String TOPIC_SUFFIX_UPDATE = "submodels/updated";
	private static final String TOPIC_SUFFIX_DELETE = "submodels/deleted";

	private static Logger logger = LoggerFactory.getLogger(MqttConfig.class);

	@Autowired
	private ConfigurationRepository repository;

	@Value("${mqtt.server.url}")
	private String mqttServerUrl;

	@Value("${mqtt.username}")
	private String mqttUsername;

	@Value("${mqtt.password}")
	private String mqttPassword;

	@Value("${config.repo.id}")
	private String configRepoId;

	@Autowired
	private ConnectedAssetAdministrationShellManager assetAdministrationShellManager;

	private String[] topics = { "aas-registry/+/submodels/+" };

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();

		options.setServerURIs(new String[] { mqttServerUrl });
		options.setUserName(mqttUsername);
		options.setPassword(mqttPassword.toCharArray());
		options.setCleanSession(true);

		factory.setConnectionOptions(options);
		return factory;
	}

	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public MessageProducer inbound() {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
				"basyx-di.aas.mqtt.clientid", mqttClientFactory(), topics);

		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}

	@Bean
	public IntegrationFlow mqttInFlow() {
		return IntegrationFlows.from(mqttInputChannel()).route("headers['" + MqttHeaders.RECEIVED_TOPIC + "']").get();
	}

	@Bean
	public IntegrationFlow configrationCreateFlow() {
		return IntegrationFlows.from(TOPIC_INITIAL + "/" + configRepoId + "/" + TOPIC_SUFFIX_CREATE)
				.transform(Transformers.objectToString()).transform(Transformers.fromJson(SubmodelDescriptor.class))
				.handle((payload, headers) -> {
					Optional<ConnectedAssetAdministrationShell> optionalLocalAas = assetAdministrationShellManager
							.retrieveAASAll().stream().map(ConnectedAssetAdministrationShell.class::cast)
							.filter(conAas -> conAas.getSubmodels()
									.containsKey(((SubmodelDescriptor) payload).getIdShort()))
							.findAny();

					if (optionalLocalAas.isEmpty()) {
						logger.error("The configuration submodel to be added couldn't be found in any AAS");
						return null;
					}

					ConnectedSubmodel smConfig = (ConnectedSubmodel) assetAdministrationShellManager.retrieveSubmodel(
							optionalLocalAas.get().getLocalCopy().getIdentification(),
							((SubmodelDescriptor) payload).getIdentifier());

					repository.addConfiguration(smConfig.getLocalCopy());
					return null;
				}).get();
	}

	@Bean
	public IntegrationFlow configrationUpdateFlow() {
		return IntegrationFlows.from(TOPIC_INITIAL + "/" + configRepoId + "/" + TOPIC_SUFFIX_UPDATE)
				.transform(Transformers.objectToString()).transform(Transformers.fromJson(SubmodelDescriptor.class))
				.handle((payload, headers) -> {
					Optional<ConnectedAssetAdministrationShell> optionalLocalAas = assetAdministrationShellManager
							.retrieveAASAll().stream().map(ConnectedAssetAdministrationShell.class::cast)
							.filter(conAas -> conAas.getSubmodels()
									.containsKey(((SubmodelDescriptor) payload).getIdShort()))
							.findAny();

					if (optionalLocalAas.isEmpty()) {
						logger.error("The configuration submodel to be updated couldn't be found in any AAS");
						return null;
					}

					ConnectedSubmodel smConfig = (ConnectedSubmodel) assetAdministrationShellManager.retrieveSubmodel(
							optionalLocalAas.get().getLocalCopy().getIdentification(),
							((SubmodelDescriptor) payload).getIdentifier());

					repository.updateConfiguration(smConfig.getLocalCopy());
					return null;
				}).get();
	}

	@Bean
	public IntegrationFlow configrationDeleteFlow() {
		return IntegrationFlows.from(TOPIC_INITIAL + "/" + configRepoId + "/" + TOPIC_SUFFIX_DELETE)
				.transform(Transformers.objectToString()).transform(Transformers.fromJson(SubmodelDescriptor.class))
				.handle((payload, headers) -> {
					repository.removeConfiguration(((SubmodelDescriptor) payload).getIdentifier().getId());
					return null;
				}).get();
	}
}