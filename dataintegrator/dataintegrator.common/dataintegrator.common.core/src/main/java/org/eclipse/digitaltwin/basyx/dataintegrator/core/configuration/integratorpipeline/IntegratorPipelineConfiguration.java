package org.eclipse.digitaltwin.basyx.dataintegrator.core.configuration.integratorpipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.processor.DataProcessor;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.DataReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.writer.DataWriter;

public class IntegratorPipelineConfiguration {

	private Map<String, DataReader<Map<String, List<Object>>>> dataReaders = new HashMap<>();
	private Map<String, DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessors = new HashMap<>();
	private Map<String, DataWriter<Map<String, List<Object>>>> dataWriters = new HashMap<>();

	private IntegratorPipelineConfiguration(Builder builder) {
		this.dataReaders = builder.dataReaders;
		this.dataProcessors = builder.dataProcessors;
		this.dataWriters = builder.dataWriters;
	}

	public Map<String, DataReader<Map<String, List<Object>>>> getDataReaders() {
		return dataReaders;
	}

	public Map<String, DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> getDataProcessors() {
		return dataProcessors;
	}

	public Map<String, DataWriter<Map<String, List<Object>>>> getDataWriters() {
		return dataWriters;
	}

	public static class Builder {
		private Map<String, DataReader<Map<String, List<Object>>>> dataReaders = new HashMap<>();
		private Map<String, DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessors = new HashMap<>();
		private Map<String, DataWriter<Map<String, List<Object>>>> dataWriters = new HashMap<>();

		public Builder addDataReader(DataReader<Map<String, List<Object>>> dataReader) {
			this.dataReaders.put(((Submodel) dataReader.getDataReaderConfiguration()).getIdentification().getId(), dataReader);
			return this;
		}

		public Builder addDataReaders(List<DataReader<Map<String, List<Object>>>> dataReaders) {
			dataReaders.stream().forEach(this::addDataReader);
			return this;
		}

		public Builder addDataProcessor(DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>> dataProcessor) {
			this.dataProcessors.put(((Submodel) dataProcessor.getDataProcessorConfiguration()).getIdentification().getId(), dataProcessor);
			return this;
		}

		public Builder addDataProcessors(
				List<DataProcessor<Map<String, List<Object>>, Map<String, List<Object>>>> dataProcessors) {
			dataProcessors.stream().forEach(this::addDataProcessor);
			return this;
		}

		public Builder addDataWriter(DataWriter<Map<String, List<Object>>> dataWriter) {
			this.dataWriters.put(((Submodel) dataWriter.getDataWriterConfiguration()).getIdentification().getId(), dataWriter);
			return this;
		}

		public Builder addDataWriters(List<DataWriter<Map<String, List<Object>>>> dataWriters) {
			dataWriters.stream().forEach(this::addDataWriter);
			return this;
		}

		public IntegratorPipelineConfiguration build() {
			return new IntegratorPipelineConfiguration(this);
		}
	}
}
