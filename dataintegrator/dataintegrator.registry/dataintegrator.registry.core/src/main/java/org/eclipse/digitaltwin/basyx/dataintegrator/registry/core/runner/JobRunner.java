package org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.runner;

import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.preparation.IntegratorPipelineFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.Constants;
import org.eclipse.digitaltwin.basyx.dataintegrator.registry.core.descriptor.factory.AasDescriptorAggregatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope( value=ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode=ScopedProxyMode.TARGET_CLASS)
public class JobRunner {
	private static final Logger logger = LoggerFactory.getLogger(JobRunner.class);

	private JobLauncher simpleJobLauncher;
	private IntegratorPipelineFactory jobFactory;

	@Autowired
	public JobRunner(IntegratorPipelineFactory jobFactory, JobLauncher jobLauncher) {
		this.simpleJobLauncher = jobLauncher;
		this.jobFactory = jobFactory;
	}
	
	public List<AASDescriptor> getAasDescriptor() {
		List<JobExecution> jobExecutions = getJobExecutions();
		
		return new AasDescriptorAggregatorFactory(jobExecutions).create();
	}

	public static String readFileAsString(String file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file)));
	}
	
	private JobExecution runJob(Job job) {
		JobParameters parameters = new JobParametersBuilder().addDate("date", new Date(), true).toJobParameters();
		try {
			JobExecution jobExecution = simpleJobLauncher.run(job, parameters);
			logger.info("Job Executed: {}", job.getName());
			return jobExecution;
		} catch (JobExecutionAlreadyRunningException e) {
			logger.info("Job with fileName={} is already running.",
					parameters.getParameters().get(Constants.FILE_NAME_CONTEXT_KEY));
		} catch (JobRestartException e) {
			logger.info("Job with fileName={} was not restarted.",
					parameters.getParameters().get(Constants.FILE_NAME_CONTEXT_KEY));
		} catch (JobInstanceAlreadyCompleteException e) {
			logger.info("Job with fileName={} already completed.",
					parameters.getParameters().get(Constants.FILE_NAME_CONTEXT_KEY));
		} catch (JobParametersInvalidException e) {
			logger.info("Invalid job parameters.", parameters.getParameters().get(Constants.FILE_NAME_CONTEXT_KEY));
		}
		
		return null;
	}
	
	private List<JobExecution> getJobExecutions() {
		return jobFactory.create().stream().map(this::runJob).collect(Collectors.toList());
	}

}
