package org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.IntegratorUnit;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public interface DataReader<T extends Map<String, List<Object>>> extends ItemStreamReader<T>, IntegratorUnit {
	
	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
	
	public Object getDataSourceConfiguration();
	
	public Object getDataReaderConfiguration();

}
