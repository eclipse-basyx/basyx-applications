package org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class CompositeDataItemReader implements DataReader<Map<String, List<Object>>>, StepExecutionListener, InitializingBean {
	
	private Logger logger = LoggerFactory.getLogger(CompositeDataItemReader.class);

	private List<DataReader<Map<String, List<Object>>>> delegates;
	private final Iterator<DataReader<Map<String, List<Object>>>> iterator;
	private DataReader<Map<String, List<Object>>> currentReader;
	private StepExecution stepExecution;
	
	public CompositeDataItemReader(List<DataReader<Map<String, List<Object>>>> delegates) {
		this.delegates = delegates;
		this.iterator = delegates.iterator();
		this.currentReader = iterator.next();
	}
	
	@Override
    public void beforeStep(StepExecution stepExecution) {
//        JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
//        parameter = parameters.getString(ExecutionContextEnum.PARAMETER.getName());
//        this.rowMapper = new ValueRowMapper((SqlCursorDataReaderConfiguration) dataReaderConfiguration);
        //use your parameters
		this.stepExecution = stepExecution;
	}

	@Override
    public Map<String, List<Object>> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		logger.info("Reading started");
//		ObjectMapper mapper = new ObjectMapper();
		long start1 = System.currentTimeMillis();
		
		Map<String, List<Object>> consolidatedResult = new HashMap<>();
		
		Iterator<DataReader<Map<String, List<Object>>>> iter = delegates.iterator();
		while (iter.hasNext()) {
			DataReader<Map<String, List<Object>>> delegate = iter.next();
			
			Map<String, List<Object>> value = readItem(delegate);
			
			if (value == null || value.size() == 0) {
				iter.remove();
				continue;
			}
			
			value.forEach((key, list) -> consolidatedResult.merge(key, new ArrayList<>(list), (l1, l2) -> {
                l1.addAll(l2);
                return l1;
            }));
			
			if (delegates.isEmpty()) {
				long end0 = System.currentTimeMillis();
				long time0 = end0 - start1;

				logger.info("In Total time taken for read: {} ms", time0);
				
				return null;
			}
		}
		
		long end0 = System.currentTimeMillis();
		long time0 = end0 - start1;

		logger.info("Out Total time taken for read: {} ms", time0);

//		logger.info("Read data in Composite Data Reader : {}",
//				mapper.writeValueAsString(consolidatedResult));
		
	    return consolidatedResult.isEmpty() ? null : consolidatedResult;
    }
	
	private Map<String, List<Object>> readItem(DataReader<Map<String, List<Object>>> reader) throws Exception {
    	return reader.read();
    }

	@Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
		delegates.stream()
               .filter(reader -> reader instanceof ItemStream)
               .map(reader -> (ItemStream) reader)
               .forEach(reader -> initializeReader(reader, executionContext));
    }

    private void initializeReader(ItemStream reader, ExecutionContext executionContext) {
    	if (reader instanceof StepExecutionListener)
    		((StepExecutionListener) reader).beforeStep(stepExecution);
    	
    	reader.open(executionContext);
	}

	@Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    	delegates.stream()
               .filter(reader -> reader instanceof ItemStream)
               .map(reader -> (ItemStream) reader)
               .forEach(reader -> reader.update(executionContext));
    }

    @Override
    public void close() throws ItemStreamException {
    	delegates.stream()
               .filter(reader -> reader instanceof ItemStream)
               .map(reader -> (ItemStream) reader)
               .forEach(ItemStream::close);
    }

    @Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(delegates, "The 'delegates' may not be null");
		Assert.notEmpty(delegates, "The 'delegates' may not be empty");
	}

	@Override
	public Object getDataSourceConfiguration() {
		return currentReader.getDataSourceConfiguration();
	}

	@Override
	public Object getDataReaderConfiguration() {
		return currentReader.getDataReaderConfiguration();
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (currentReader instanceof StepExecutionListener)
			((StepExecutionListener) currentReader).afterStep(stepExecution);
		
		return stepExecution.getExitStatus();
	}

}
