package eu.fbk.das.adaptation.presentation;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class test extends JFrame {

    private mxGraphComponent graphComponent;

    private int idgenerator = 0;

    public test() {
	super("Test");

	mxGraph graph = new mxGraph();
	Object parent = graph.getDefaultParent();

	graph.getModel().beginUpdate();
	try {
	    Object v1 = graph.insertVertex(parent, String.valueOf(++idgenerator), "Hello", 0, 0, 80, 30,
		    "shape=hexagon;perimeter=ellipsePerimeter");
	    Object v2 = graph.insertVertex(parent, String.valueOf(++idgenerator), "World!", 0, 0, 80, 30);
	    Object v3 = graph.insertVertex(parent, String.valueOf(++idgenerator), "Brando!", 0, 0, 80, 30);
	    Object v4 = graph.insertVertex(parent, String.valueOf(++idgenerator), "Jotaro!", 0, 0, 80, 30);
	    // graph.insertEdge(parent, String.valueOf(++idgenerator), "Edge",
	    // v3,
	    // v4);

	} finally {
	    graph.getModel().endUpdate();
	}

	graphComponent = new mxGraphComponent(graph);
	graphComponent.setBounds(0, 0, 600, 300);
	getContentPane().add(graphComponent);
    }

    public static void main(String[] args) {
	test frame = new test();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(800, 600);
	frame.setVisible(true);
    }

}
