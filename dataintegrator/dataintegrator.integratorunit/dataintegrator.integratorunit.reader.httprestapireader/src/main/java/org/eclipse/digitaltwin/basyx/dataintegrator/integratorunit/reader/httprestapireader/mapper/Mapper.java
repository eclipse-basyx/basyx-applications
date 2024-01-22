package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.reader.httprestapireader.mapper;

import java.util.List;
import java.util.Map;

public interface Mapper {
	
	public Map<String, List<Object>> map(String data);

}
