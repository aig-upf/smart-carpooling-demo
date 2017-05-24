package eu.fbk.das.adaptation.api;

import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONException;

import eu.fbk.das.adaptation.RoleManager;
import eu.fbk.das.adaptation.presentation.CATree;

public interface CollectiveAdaptationInterface {

	public HashMap<CATree, Integer> executeCap(
			CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor,
			String scenario,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge startEdge) throws JSONException;

	public void executeCapNew(CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor);

}
