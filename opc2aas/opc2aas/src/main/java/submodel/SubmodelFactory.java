package submodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.InvokableOperation;
import org.eclipse.digitaltwin.basyx.opc2aas.OpcToAas;


public class SubmodelFactory {

    public static Submodel creationSubmodel() {
        List<LangStringTextType> description = new ArrayList<LangStringTextType>();
        description.add(new DefaultLangStringTextType.Builder().language("de-DE")
            .text("CreationSubmodel")
            .build());
        List<LangStringNameType> displayName = new ArrayList<LangStringNameType>();
        displayName.add(new DefaultLangStringNameType.Builder().language("de-DE")
            .text("CreationSubmodel")
            .build());
        Operation creation = createAASFromOPCNodeStructure();
        List<SubmodelElement> smeList = Arrays.asList(creation);

        Submodel submodel = new DefaultSubmodel.Builder().category("TestCategory")
            .description(description)
            .displayName(displayName)
            .id("CreationSubmodel")
            .idShort("CreationSubmodel")
            .kind(ModellingKind.INSTANCE)
            .submodelElements(smeList)
            .build();

        return submodel;
    }

    public static Operation createAASFromOPCNodeStructure() {
        return new InvokableOperation.Builder()
            .idShort("AASfromOPC")
            .inputVariables(Arrays.asList(createStringOperationVariable("aasIdShort"),
                createStringOperationVariable("opcNodeId"),
                createStringOperationVariable("opcServerUrl"),
                createStringOperationVariable("opcUsername"),
                createStringOperationVariable("opcPassword"),
                createStringOperationVariable("submodelRepoUrl")))
            .invokable(SubmodelFactory::creation)
            .build();
    }

    private static DefaultOperationVariable createStringOperationVariable(String idShort) {
        return new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort(idShort).valueType(DataTypeDefXsd.STRING).build()).build();
    }

    public static OperationVariable[] creation(OperationVariable[] inputs) {

        String aasIdShort = ((Property) inputs[0].getValue()).getValue();
        String opcNodeId = ((Property) inputs[1].getValue()).getValue();
        String opcServerUrl = ((Property) inputs[2].getValue()).getValue();
        String opcUsername = ((Property) inputs[3].getValue()).getValue();
        String opcPassword = ((Property) inputs[4].getValue()).getValue();
        String submodelRepoUrl = ((Property) inputs[5].getValue()).getValue();

        OpcToAas.processOperation(aasIdShort, opcNodeId, opcServerUrl, opcUsername, opcPassword, submodelRepoUrl);

        return new OperationVariable[0];
    }
}
