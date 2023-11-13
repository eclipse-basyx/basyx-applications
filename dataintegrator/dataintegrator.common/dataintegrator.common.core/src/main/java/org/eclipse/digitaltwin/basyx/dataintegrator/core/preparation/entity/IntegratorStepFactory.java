package org.eclipse.digitaltwin.basyx.dataintegrator.core.preparation.entity;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor.DataProcessor;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.CompositeDataItemReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.DataReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer.DataWriter;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntegratorStepFactory {
	
	private StepBuilderFactory stepBuilderFactory;
	private int chunk;
	
	@Autowired
	public IntegratorStepFactory(StepBuilderFactory stepBuilderFactory, int chunk) {
		this.stepBuilderFactory = stepBuilderFactory;
		this.chunk = chunk;
	}

	public Step create(String jobStepName, List<DataReader<Map<String, List<Object>>>> dataReaders,
			List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessors,
			DataWriter<Map<String, List<Object>>> dataWriter) {
		return this.stepBuilderFactory.get(jobStepName).<Map<String, List<Object>>, Map<String, List<Object>>>chunk(chunk)
				.reader(getCompositeReaders(dataReaders)).processor(getCompositeProcessors(dataProcessors)).writer(dataWriter).build();
	}
	
	private CompositeDataItemReader getCompositeReaders(List<DataReader<Map<String,List<Object>>>> dataReaders) {
		return new CompositeDataItemReader(dataReaders);
	}
	
	private CompositeItemProcessor<Map<String, List<Object>>, Map<String, List<Object>>> getCompositeProcessors(List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessor) {
		CompositeItemProcessor<Map<String, List<Object>>, Map<String, List<Object>>> compositeItemProcessor = new CompositeItemProcessor<>();
		
		compositeItemProcessor.setDelegates(dataProcessor);
		
		return compositeItemProcessor;
	}

}
