package org.eclipse.digitaltwin.basyx.TestOrchestrator;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TestRunner {
    public static void main(String[] args) {
        try {
            // Load JSON from file
            InputStream inputStream = TestRunner.class.getClassLoader().getResourceAsStream("input/inputData_HandoverDoc.json");
            String jsonString = new String(Objects.requireNonNull(inputStream).readAllBytes(), StandardCharsets.UTF_8);

            Property inputProperty = new DefaultProperty.Builder()
                    .idShort("aasFile")
                    .value(jsonString)
                    .valueType(DataTypeDefXsd.STRING)
                    .build();

            OperationVariable[] inputs = new OperationVariable[]{new DefaultOperationVariable.Builder().value(inputProperty).build()};

            OperationVariable[] results = SubmodelFactory.creation(inputs);

            for (OperationVariable result : results) {
                Property resultProperty = (Property) result.getValue();
                System.out.println("Result: " + resultProperty.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
