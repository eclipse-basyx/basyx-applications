package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.sqlcursorreader.reader;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.digitaltwin.basyx.basyx.components.dataintegrator.sqldatasource.configuration.SqlDataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.DataReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.utils.ExecutionContextEnum;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.sqlcursorreader.mapper.CustomMapper;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.sqlcursorreader.mapper.SimpleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.RowMapper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SqlCursorDataReader extends JdbcCursorItemReader<Map<String, List<Object>>>
		implements DataReader<Map<String, List<Object>>>, StepExecutionListener {
	
	private Logger logger = LoggerFactory.getLogger(SqlCursorDataReader.class);

	private static final String PLACEHOLDER = "?";
	private Submodel dataReaderConfiguration;
	private SqlDataSourceConfiguration sqlDataSourceConfiguration;
	private DataSource dataSource;
	private String parameter;

	public SqlCursorDataReader(Submodel dataReaderConfiguration) {
		this.dataReaderConfiguration = dataReaderConfiguration;
		this.setName("reader" + dataReaderConfiguration.getIdShort());
	}
	
	@Override
    public void beforeStep(StepExecution stepExecution) {
        JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
        parameter = parameters.getString(ExecutionContextEnum.PARAMETER.getName());
//        this.rowMapper = new ValueRowMapper((SqlCursorDataReaderConfiguration) dataReaderConfiguration);
        //use your parameters
	}
	
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public SqlDataSourceConfiguration getDataSourceConfiguration() {
		return this.sqlDataSourceConfiguration;
	}

	@Override
	public Submodel getDataReaderConfiguration() {
		return dataReaderConfiguration;
	}

	@Override
	public void open(ExecutionContext executionContext) {
		prepareDataSource();
		
		this.setDataSource(dataSource);
		this.setSql(prepareSql(parameter));
//		this.setRowMapper(new ValueRowMapper((SqlCursorDataReaderConfiguration) dataReaderConfiguration));
		this.setRowMapper(createMapper((String) dataReaderConfiguration.getSubmodelElement("mapperType").getValue()));
		
		super.open(executionContext);
	}
	
	private RowMapper<Map<String, List<Object>>> createMapper(String rowMapper) {
		return rowMapper.equals("SimpleMapper") ? createSimpleMapper() : createCustomMapper();
	}

	private RowMapper<Map<String, List<Object>>> createCustomMapper() {
		return new CustomMapper(dataReaderConfiguration);
	}

	private RowMapper<Map<String, List<Object>>> createSimpleMapper() {
		return new SimpleMapper(dataReaderConfiguration);
	}

	@Override
	public void close() {
		if (dataSource != null && ((HikariDataSource) dataSource).isRunning())
			((HikariDataSource) dataSource).close();
		
		super.close();
	}

	private void prepareDataSource() {		
		SubmodelElementCollection dataConnectionProperties = getSQLConfigurationPropertiesSMC();
		
//		SqlDataSourceConfiguration dataSourceConfiguration = (SqlDataSourceConfiguration) new SqlDefaultDataSourceConfigurationMapperFactory(dataConnectionProperties).create();
		SqlDataSourceConfiguration dataSourceConfiguration = new SqlDataSourceConfiguration(dataConnectionProperties);

		HikariConfig config = new HikariConfig();
		
		config.setJdbcUrl(dataSourceConfiguration.getConnectionUrl());
		config.setUsername(dataSourceConfiguration.getCredential().getUsername());
		config.setPassword(dataSourceConfiguration.getCredential().getPassword());

		dataSource = new HikariDataSource(config);
	}

	private SubmodelElementCollection getDataSourceConfigSMC() {
		return (SubmodelElementCollection) dataReaderConfiguration.getSubmodelElement("dataSource");
	}
	
	private SubmodelElementCollection getSQLConfigurationPropertiesSMC() {
		return (SubmodelElementCollection) getDataSourceConfigSMC().getSubmodelElement("sqlConnectionProperties");
	}
	
	private String prepareSql(String parameter) {
		String query = (String) getSQLConfigurationPropertiesSMC().getSubmodelElement("sqlQuery").getValue();
		
		if (!query.contains(PLACEHOLDER))
			return query;
		
		if (parameter == null)
			throw new RuntimeException("The specified query contains placeholder '?' but no parameter specified");
		
		return query.replace(PLACEHOLDER, parameter);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return stepExecution.getExitStatus();
	}

//	private RowMapper<Map<String, List<Object>>> prepareRowMapper(
//			SqlCursorDataReaderConfiguration sqlDataReaderConfiguration) {
//		return new ValueRowMapper(sqlDataReaderConfiguration);
//	}

}
