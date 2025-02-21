package submodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;

import java.io.IOException;

@SpringBootApplication(
    scanBasePackages = {"org.eclipse.digitaltwin.basyx", "org.eclipse.digitaltwin.basyx.opc2aas", "submodel"},
    exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
public class SubmodelRepositoryComponentForOpc {
    public static void main(String[] args) throws IOException {

        ApplicationContext context= SpringApplication.run(SubmodelRepositoryComponentForOpc.class, args);

        SubmodelRepository repo = context.getBean(SubmodelRepository.class);

        repo.createSubmodel(SubmodelFactory.creationSubmodel());

    }
}
