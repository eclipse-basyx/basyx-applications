package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aaswriter.factory;

import java.util.List;

import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;

public class SubmodelElementFactory {
	
	private static final String EXPRESSION_PREFIX = "$..[?(@.idShort == '";
	private static final String EXPRESSION_SUFFIX = "')]";
	
	private String idShort;
	private String modelType;
	private ReadContext readContext;
	
	public SubmodelElementFactory(String idShort, String modelType, ReadContext readContext) {
		super();
		this.idShort = idShort;
		this.modelType = modelType;
		this.readContext = readContext;
	}

	public SubmodelElement create() {
		String exprForSME = String.format(EXPRESSION_PREFIX + "%s" + EXPRESSION_SUFFIX, this.idShort);

		TypeRef<?> typeRef = getTypeReference();

		@SuppressWarnings("unchecked")
		List<SubmodelElement> submodelElements = (List<SubmodelElement>) readContext.read(exprForSME, typeRef);
		
		if (submodelElements == null || submodelElements.isEmpty())
			throw new RuntimeException("Unable to create SubmodelElement for the idShort : " + idShort);
		
		return submodelElements.get(0);
	}

	private TypeRef<?> getTypeReference() {
		if (modelType.equals(KeyElements.PROPERTY.toString())) {
			return new TypeRef<List<Property>>() {
			};
		}
		
		if (modelType.equals(KeyElements.SUBMODELELEMENTCOLLECTION.toString())) {
			return new TypeRef<List<SubmodelElementCollection>>() {
			};
		}
		
		if (modelType.equals(KeyElements.SUBMODEL.toString())) {
			return new TypeRef<List<Submodel>>() {
			};
		}
		
		throw new RuntimeException("No matching class found for the provided modelType : " + modelType);
	}
	
	public static Class<?> getClass(String modelType) {
		if (modelType.equals(KeyElements.PROPERTY.toString())) {
			return Property.class;
		}
		
		if (modelType.equals(KeyElements.SUBMODELELEMENTCOLLECTION.toString())) {
			return SubmodelElementCollection.class;
		}
		
		if (modelType.equals(KeyElements.SUBMODEL.toString())) {
			return Submodel.class;
		}
		
		throw new RuntimeException("No matching class found for the provided modelType : " + modelType);
	}
}
