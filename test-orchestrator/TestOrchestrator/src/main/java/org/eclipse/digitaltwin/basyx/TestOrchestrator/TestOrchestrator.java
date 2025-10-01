package org.eclipse.digitaltwin.basyx.TestOrchestrator;

import org.eclipse.digitaltwin.basyx.core.exceptions.RepositoryRegistryLinkException;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
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

        try {
            repo.createSubmodel(SubmodelFactory.creationSubmodel());
        } catch (RepositoryRegistryLinkException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ApiException apiEx && apiEx.getCode() == 409) {
                System.out.println("TestOrchestrator submodel already registered in registry. Skipping creation.");
            } else {
                throw ex;
            }
        }


        context.getBean(MqttSubscriber.class);
    }
}