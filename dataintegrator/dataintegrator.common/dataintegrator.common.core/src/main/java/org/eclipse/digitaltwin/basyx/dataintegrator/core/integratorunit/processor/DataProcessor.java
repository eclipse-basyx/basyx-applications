package org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.IntegratorUnit;
import org.springframework.batch.item.ItemProcessor;

public interface DataProcessor<I extends Map<String, List<Object>>, O extends Map<String, List<Object>>>
		extends ItemProcessor<I, O>, IntegratorUnit {

	public O process(I resultDefinitions) throws Exception;
	
	public Object getDataProcessorConfiguration();

}
