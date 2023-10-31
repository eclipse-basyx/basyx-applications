package org.eclipse.digitaltwin.basyx.dataintegrator.core.integratorunit.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;

public class IntegratorUnitFactory {
	
	private Class<?> targetClass;
	private  Submodel integratorUnitConfig;
	
	public IntegratorUnitFactory(Class<?> targetClass, Submodel integratorUnitConfig) {
		super();
		this.targetClass = targetClass;
		this.integratorUnitConfig = integratorUnitConfig;
	}
	
	public Object create() {
		Optional<Qualifier> integratorUnitClassQualifier = integratorUnitConfig.getQualifiers().stream().filter(qualifier -> qualifier instanceof Qualifier && ((Qualifier) qualifier).getType().equals("connectorFQCN")).map(Qualifier.class::cast).findAny();
		
		if (integratorUnitClassQualifier.isEmpty())
			throw new RuntimeException("The connector config doesn't contain a valid qualifier for connector fully qualified class name (connectorFQCN)");
		
		String integratorUnitClassName= (String) integratorUnitClassQualifier.get().getValue();
		
		Class<?> integratorUnitClass = getConnectorClass(integratorUnitClassName);
		
		Constructor<?> constructor = getDeclaredConstructor(integratorUnitClass);
		
		Object instance = createNewInstance(constructor);
		
		if (targetClass.isInstance(instance)) {
		    // The instantiated object is an instance of the target class
		    // Cast the instance to targetClass and return it
		    return targetClass.cast(instance);
		} else {
		    // The instantiated object is not an instance of the target class
		    // Throw a RuntimeException or a more specific exception as needed
		    throw new RuntimeException("Invalid class type");
		}
	}

	private Object createNewInstance(Constructor<?> constructor) {
		Object instance = null;
		try {
			instance = constructor.newInstance(integratorUnitConfig);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to create a new instance of " + integratorUnitConfig.getClass().getName());
		}
		return instance;
	}

	private Constructor<?> getDeclaredConstructor(Class<?> integratorUnitClass) {
		Constructor<?> constructor = null;
		try {
			constructor = integratorUnitClass.getDeclaredConstructor(Submodel.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to get declared construtor of " + integratorUnitConfig.getClass().getName());
		}
		return constructor;
	}

	private Class<?> getConnectorClass(String integratorUnitClassName) {
		Class<?> integratorUnitClass = null;
		try {
			integratorUnitClass = Class.forName(integratorUnitClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to get class " + integratorUnitClassName);
		}
		return integratorUnitClass;
	}

}
