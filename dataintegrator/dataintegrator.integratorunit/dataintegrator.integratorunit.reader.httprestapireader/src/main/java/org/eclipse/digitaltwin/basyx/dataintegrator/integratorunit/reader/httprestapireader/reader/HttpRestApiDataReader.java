package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.reader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.reader.DataReader;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper.SimpleMapper;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper.Mapper;
import org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper.MultiValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;

public class HttpRestApiDataReader implements DataReader<Map<String, List<Object>>> {
	
	private Logger logger = LoggerFactory.getLogger(HttpRestApiDataReader.class);

    private CloseableHttpClient httpClient;
    private boolean dataFetched;
    private Submodel dataReaderConfiguration;

    public HttpRestApiDataReader(Submodel dataReaderConfiguration) {
        this.dataReaderConfiguration = dataReaderConfiguration;
        this.dataFetched = false;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        httpClient = HttpClients.createDefault();
        // Other initialization logic, if needed
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // Update logic, if needed
    }

    @Override
    public void close() throws ItemStreamException {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new ItemStreamException("Error while closing the HTTP client", e);
            }
        }
        // Other cleanup logic, if needed
    }

    @Override
    public Map<String, List<Object>> read() throws Exception {
    	if (!dataFetched) {
            URI uri = buildUriWithParameters(getApiUrlFromConfig(), getRequestParamFromConfig());
            HttpGet request = new HttpGet(uri);
            
            if (isRequestHeaderConfigPresent())
            	addRequestHeadersToRequest(request);

            HttpResponse response = httpClient.execute(request);
            
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            	throw new RuntimeException("Unable to fetch data from API Endpoint : " + request.getURI());
            
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String responseData = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                
                dataFetched = true;
                
                return getMapper(getMapperTypeFromConfig()).map(responseData); // Return the first item
            }
        }
    	
        return null; // Return null when all data has been fetched
    }
    
    private Mapper getMapper(String mapperType) {
    	if (mapperType.equals("SimpleMapper"))
    		return new SimpleMapper(getValueMapSMCFromConfig()); // Return the first item
    	
    	return new MultiValueMapper(getValueMapSMCFromConfig());
    }
    
    private SubmodelElementCollection getValueMapSMCFromConfig() {
		return (SubmodelElementCollection) dataReaderConfiguration.getSubmodelElement("valueMap");
	}
    
    private String getMapperTypeFromConfig() {
    	return (String) dataReaderConfiguration.getSubmodelElement("mapperType").getValue();
    }

	private void addRequestHeadersToRequest(HttpGet request) {
		SubmodelElementCollection requestHeaderSMC = getRequestHeaderSMCFromConfig();
		
		Collection<ISubmodelElement> requestHeaders = requestHeaderSMC.getSubmodelElements().values();
		
		List<Header> headers = requestHeaders.stream().map(Property.class::cast).map(this::createBasicHeader).collect(Collectors.toList());
		
		headers.stream().forEach(request::addHeader);
	}
	
	private Header createBasicHeader(Property requestHeaderProperty) {
		return new BasicHeader(requestHeaderProperty.getIdShort(), (String) requestHeaderProperty.getValue());
	}

	private boolean isRequestHeaderConfigPresent() {
		return !getRequestHeaderSMCFromConfig().getSubmodelElements().isEmpty();
	}

	private SubmodelElementCollection getRequestHeaderSMCFromConfig() {
		return (SubmodelElementCollection) getHTTPConfigurationPropertiesSMC().getSubmodelElement("requestHeaders");
	}

	private Map<String, String> getRequestParamFromConfig() {
		SubmodelElementCollection requestParamSMC = (SubmodelElementCollection) getHTTPConfigurationPropertiesSMC().getSubmodelElement("requestParameters");
		
		return retrieveRequestParamFromConfigAsMap(requestParamSMC);
	}

	private Map<String, String> retrieveRequestParamFromConfigAsMap(SubmodelElementCollection requestParamSMC) {
		Collection<ISubmodelElement> requestParams = requestParamSMC.getSubmodelElements().values();
		
		return requestParams.stream().map(Property.class::cast).map(this::prepareMapEntry).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
	private Entry<String, String> prepareMapEntry(Property requestParamProperty) {
		return new AbstractMap.SimpleEntry<>(requestParamProperty.getIdShort(), (String) requestParamProperty.getValue());
	}

	private String getApiUrlFromConfig() {
		SubmodelElementCollection dataSourceConfigSMC = getHTTPConfigurationPropertiesSMC();
		
		return (String) dataSourceConfigSMC.getSubmodelElement("apiUrl").getValue();
	}

	private SubmodelElementCollection getDataSourceConfigSMC() {
		return (SubmodelElementCollection) dataReaderConfiguration.getSubmodelElement("dataSource");
	}
	
	private SubmodelElementCollection getHTTPConfigurationPropertiesSMC() {
		return (SubmodelElementCollection) getDataSourceConfigSMC().getSubmodelElement("httpConnectionProperties");
	}

	@Override
	public Object getDataSourceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDataReaderConfiguration() {
		return dataReaderConfiguration;
	}
	
	private URI buildUriWithParameters(String apiUrl, Map<String, String> requestParameters) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(apiUrl);
        if (requestParameters != null) {
            for (Map.Entry<String, String> parameter : requestParameters.entrySet()) {
                uriBuilder.addParameter(parameter.getKey(), parameter.getValue());
            }
        }
        return uriBuilder.build();
    }

}
