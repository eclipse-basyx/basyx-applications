package org.eclipse.digitaltwin.basyx.dataintegrator.aas.component;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(scanBasePackages = "org.eclipse.digitaltwin.basyx", exclude = { MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class })
@EnableBatchProcessing
public class DataIntegratorAASComponentApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataIntegratorAASComponentApplication.class, args);
	}

}
