package org.eclipse.digitaltwin.basyx.TestOrchestrator;

import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(
        scanBasePackages = {"org.eclipse.digitaltwin.basyx", "org.eclipse.digitaltwin.basyx.TestOrchestrator"},
        exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
public class TestOrchestrator {

    public static void main(String[] args) {

        ApplicationContext context= SpringApplication.run(TestOrchestrator.class, args);

        SubmodelRepository repo = context.getBean(SubmodelRepository.class);
        repo.createSubmodel(SubmodelFactory.creationSubmodel());

        // Ensure MQTT Subscriber is initialized
        context.getBean(MqttSubscriber.class);
    }
}
