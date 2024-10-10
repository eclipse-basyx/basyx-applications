package org.eclipse.digitaltwin.basyx.opc2aas;

import java.util.List;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;

public class NodeInfo {
    public NodeId nodeId;
    public String nodeClass;
    public QualifiedName browseName;
    public LocalizedText displayName;
    public LocalizedText description;
    public Object value;
    public String dataType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<NodeInfo> children;

    public NodeInfo(NodeId nodeId, String nodeClass, QualifiedName browseName, LocalizedText displayName, LocalizedText description, Object value, String dataType, List<NodeInfo> children) {
        this.nodeId = nodeId;
        this.nodeClass = nodeClass;
        this.browseName = browseName;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
        this.dataType = dataType;
        this.children = children;
    }

    /**
     * Gets the value of a NodeInfo object by key.
     *
     * @param node    The NodeInfo object to get the value from.
     * @param nodeKey The key of the value to get.
     * @return The value of the NodeInfo object.
     */
    public static Object getValueFromNodeByKey(NodeInfo node, String nodeKey) {
        if (node == null) {
            // Handle or log the null node case
            return null;
        }

        return switch (nodeKey) {
            case "nodeId" -> node.nodeId;
            case "nodeClass" -> node.nodeClass;
            case "browseName" -> node.browseName;
            case "displayName" -> node.displayName;
            case "description" -> node.description;
            case "value" -> node.value;
            case "dataType" -> node.dataType;
            case "children" -> node.children;
            default -> null;
        };
    }
}
