package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import java.util.List;

public class DataValidator {

    public static void recursivelyFetchSubmodelElements(List<SubmodelElement> elements) {
        for (SubmodelElement element : elements) {

            if (element instanceof SubmodelElementCollection) {
                SubmodelElementCollection collection = (SubmodelElementCollection) element;
                recursivelyFetchSubmodelElements(collection.getValue());
            }

        }
    }
}
