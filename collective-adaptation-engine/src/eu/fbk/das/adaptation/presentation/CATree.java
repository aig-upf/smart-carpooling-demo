package eu.fbk.das.adaptation.presentation;

import java.util.ArrayList;
import java.util.List;

import com.mxgraph.view.mxGraph;

public class CATree extends mxGraph {

    private Object firstNode;

    private List<Object> nodesHierarchy;

    public List<Object> getNodesHierarchy() {
	return nodesHierarchy;
    }

    public void setNodesHierarchy(List<Object> nodesHierarchy) {
	this.nodesHierarchy = nodesHierarchy;
    }

    public Object getFirstNode() {
	return firstNode;
    }

    public void setFirstNode(Object firstNode) {
	this.firstNode = firstNode;
    }

    public Object insertNode(Object parent, String idNode, String roleName, String style) {

	if (firstNode == null) {
	    firstNode = insertVertex(parent, idNode, roleName, 100, 100, 100, 30, style);

	    return firstNode;
	} else {
	    return insertVertex(parent, idNode, roleName, 100, 100, 100, 30, style);

	}

	// return insertVertex(parent, null, roleName, 0, 0, 100, 30, style);
    }

    public Object insertNodeHierarchy(Object parent, String idNode, String roleName, String style) {

	if (firstNode == null) {
	    firstNode = insertVertex(parent, idNode, roleName, 40, 40, 40, 40, style);
	    if (nodesHierarchy == null) {
		nodesHierarchy = new ArrayList<Object>();
	    } else {
		nodesHierarchy.add(firstNode);
	    }

	    return firstNode;
	} else {
	    firstNode = insertVertex(parent, idNode, roleName, 40, 40, 40, 40, style);
	    if (nodesHierarchy == null) {
		nodesHierarchy = new ArrayList<Object>();
	    } else {
		nodesHierarchy.add(firstNode);
	    }
	    return firstNode;
	}

	// return insertVertex(parent, null, roleName, 0, 0, 100, 30, style);
    }

}
