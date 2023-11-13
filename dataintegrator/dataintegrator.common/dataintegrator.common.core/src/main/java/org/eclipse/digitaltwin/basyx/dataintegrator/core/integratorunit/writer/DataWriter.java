package org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.IntegratorUnit;
import org.springframework.batch.item.ItemWriter;

public interface DataWriter<I extends Map<String, List<Object>>> extends ItemWriter<I>, IntegratorUnit {
	
	public void write(List<? extends I> processedResultDefinitions) throws Exception;
	
	public Object getDataWriterConfiguration();
}
