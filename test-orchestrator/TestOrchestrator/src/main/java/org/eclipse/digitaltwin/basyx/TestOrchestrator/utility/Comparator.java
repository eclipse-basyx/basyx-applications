package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

public class Comparator {

    public static ComparisonResult compare(Submodel schemaSubmodel, Submodel inputSubmodel) {
        ComparisonResult result = new ComparisonResult();

        RecursionFunc.compareSubmodelElements(schemaSubmodel.getSubmodelElements(), inputSubmodel.getSubmodelElements(), result);

        System.out.println("Comparison complete. Result: " + result + "\n");

        return result;
    }
}
