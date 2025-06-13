package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

public class Deserializer {

    public static Environment deserializejsonFile(String json) throws DeserializationException {
        try {
            JsonDeserializer deserializer = new JsonDeserializer();
            Environment environment = deserializer.read(json, Environment.class);


            for (Submodel submodel : environment.getSubmodels()) {
                DataValidator.recursivelyFetchSubmodelElements(submodel.getSubmodelElements());
            }
            return environment;
        } catch (org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException e) {
            throw new DeserializationException("Error deserializing JSON", e);
        }
    }
}
