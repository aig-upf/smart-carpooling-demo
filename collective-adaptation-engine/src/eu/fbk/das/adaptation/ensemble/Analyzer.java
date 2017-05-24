package eu.fbk.das.adaptation.ensemble;

import isFeasible.Passenger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.tree.JGraphTreeLayout;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxTraversal;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraph.mxICellVisitor;

import eu.fbk.das.adaptation.EnsembleManager;
import eu.fbk.das.adaptation.RoleManager;
import eu.fbk.das.adaptation.api.CollectiveAdaptationEnsemble;
import eu.fbk.das.adaptation.api.CollectiveAdaptationProblem;
import eu.fbk.das.adaptation.api.CollectiveAdaptationRole;
import eu.fbk.das.adaptation.api.CollectiveAdaptationSolution;
import eu.fbk.das.adaptation.api.RoleCommand;
import eu.fbk.das.adaptation.model.IssueCommunication;
import eu.fbk.das.adaptation.model.IssueCommunication.CommunicationStatus;
import eu.fbk.das.adaptation.model.IssueResolution;
import eu.fbk.das.adaptation.model.Target;
import eu.fbk.das.adaptation.model.Target.TargetStatus;
import eu.fbk.das.adaptation.presentation.CANode;
import eu.fbk.das.adaptation.presentation.CATree;
import eu.fbk.das.adaptation.presentation.CAWindow;

/**
 * This is the class that realizes the Analyzer (of the MAPE) behavior of a Role
 * 
 * @author Antonio
 * 
 */

public class Analyzer {

	private List<Solver> localSolvers;
	private JGraphModelAdapter m_jgAdapter;
	private List<EnsembleManager> ensm;

	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

	private static final String STYLE_ROLE = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;align=center;fontSize=9;";
	private static final String STYLE_COM = "verticalAlign=middle;fillColor=FFF000";
	private static final String STYLE_OR = "verticalAlign=middle;shape=rhombus;fillColor=green;strokeColor=black";
	private static final String STYLE_INIT = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;fillColor=white";
	private static final String STYLE_AND = "verticalAlign=middle;shape=rhombus;fillColor=red;strokeColor=black";
	private static final String STYLE_ISSUE_EDGE = "fontColor=#FF0000;fontSize=8;endArrow=classic;html=1;fontFamily=Helvetica;align=left;";
	private static final String STYLE_DOTTED_EDGE = "dashed=1;fontColor=#FF0000;fontSize=8;align=left;";
	private static int distance = 0;
	private static int depth = 0;

	private static int miniumExtent = 1;
	private static int maximumExtent = 1;
	private static boolean printed = false;

	public List<Solver> getLocalSolver() {
		return localSolvers;

	}

	public void setLocalSolver(List<Solver> localSolvers) {
		this.localSolvers = localSolvers;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DefaultEdge resolveIssue(CollectiveAdaptationProblem cap,
			String capID, IssueResolution res, EnsembleManager en,
			CAWindow window, CATree cat, List<EnsembleManager> ensembles,
			CollectiveAdaptationSolution solution1, int issueIndex,
			CATree hierarchyTree, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge lastEdge) {
		RoleManager rm = res.getRoleCurrent();
		// System.out.println("ROLE: " + rm.getRole().getType());
		// System.out.println("ACTIVE ISSUES: " +
		// rm.getIssueResolutions().size());

		// for (int i = 0; i < rm.getIssueResolutions().size(); i++) {
		// System.out.println("active issue: " +
		// rm.getIssueResolutions().get(i).getIssueInstance().getIssueType());
		// System.out.println("issue state: " +
		// rm.getIssueResolutions().get(i).getStatus());
		// }

		// SWITCH on the Issue Resolution State for the specific role

		switch (res.getStatus()) {
		case "ISSUE_RECEIVED": {

			lastEdge = manageIssueReceived(cap, capID, res, en, cat, window,
					ensembles, issueIndex, hierarchyTree, graph, lastEdge);

		}
			break;

		case "SOLUTION_FORWARDED": {

			// if the role has received a commit it executes its own solution
			// and will end
			// otherwise it exits from this case.
			// rel = relations;
			// RoleManager rm = res.getRoleCurrent();

			if (rm.isCommit()) {
				System.out.println(res.getRoleCurrent().getRole().getType()
						+ " *****COMMIT_REQUESTED*****");

			} else {

				break;
			}

		}
			break;

		case "ISSUE_TARGETED": {

			// rel = relations;
			// here the role is wainting for all the solution from invoked
			// partners
			// for the issue we have the list of solutions, if this contains all
			// the
			// solution the role decides and commit (if it is the root)
			// otherwise forward
			// the solutions to the parent.

			// if all the communications are solved we change the issue status
			// to END

			// System.out.println("CURRENT ROLE: "
			// + res.getRoleCurrent().getRole().getType());

			// controllare se ha ricevuto la soluzione da tutti i target di una
			// soluzione
			if (rm.getSolutions() != null) {
				for (ConcurrentHashMap.Entry<Solver, List<IssueCommunication>> solution : rm
						.getSolutions().entrySet()) {
					Solver solver = solution.getKey();
					List<IssueCommunication> coms = solution.getValue();
					int numOfComs = coms.size();

					// we consider only the selected solver
					// if (solver.isSelected()) {
					// we check the target of the solution proposed by the
					// selected solver
					Solution sol = solver.getSolution().get(0);

					int countComs = 0;
					for (int i = 0; i < coms.size(); i++) {
						IssueCommunication com = coms.get(i);
						int numOFTargets = com.getTargets().size();
						int countTargets = 0;

						// check if all the target status are in forwarded
						for (int j = 0; j < com.getTargets().size(); j++) {
							Target t = com.getTargets().get(j);
							// we take the last active issue resolution
							int size = t.getTargetRole().getIssueResolutions()
									.size();
							// System.out.println(t.getTargetRole()
							// .getIssueResolutions().size());
							if (size > 0) {

								if (t.getTargetRole().getIssueResolutions()
										.get(size - 1).getStatus()
										.equals("SOLUTION_FORWARDED")) {
									countTargets++;
								}
							}

						}

						if (countTargets == numOFTargets) {
							// all targets are in the status "SOLUTION
							// FORWARDED" the
							// communication change its stat
							countComs++;

						}
					}
					if (countComs == numOfComs) {
						// All the communications have been solved
						// System.out.println(res.getRoleCurrent().getRole().getType()
						// + " *****SOLUTIONS_RECEIVED*****");
						res.setStatus("SOLUTION_RECEIVED");

					} else {
						// System.out.println("WAITING SOLUTIONS FROM PARTNERS: "
						// + res.getRoleCurrent().getRole().getType());

					}

					break;
				}
			}
			// decommenta }

		}

			break;
		case "SOLUTION_RECEIVED": {

			Object currentNode = new CANode();
			Object root = cat.insertNode(cat.getDefaultParent(), null, "INIT",
					STYLE_INIT);

			String roleName = rm.getRole().getType();
			String label1 = "SOLUTIONS_RECEIVED";
			Object v1 = cat.insertNode(cat.getDefaultParent(), null, roleName,
					STYLE_ROLE);
			cat.insertEdge(cat.getDefaultParent(), "", label1, root, v1,
					STYLE_ISSUE_EDGE);
			currentNode = v1;

			// here we differentiate if the role is a root or not of the tree
			// if it is the root he choose the solution and commit
			// this.getFather(rm, en) == null)
			if (res.isRoot()) {
				CommitSolution(capID, res, en, window, cat, label1, v1, rm);
			} else {
				// forward the solution to father
				// System.out.println("FORWARD SOLUTION TO FATHER");
				// local solution - no extra issues generated
				String roleType = rm.getRole().getType();
				// System.out.println(roleType + " *****SOLUTION_FOUND*****");

				String label2 = "INTERNAL_SOLUTION";
				cat.insertEdge(cat.getDefaultParent(), "", label2, currentNode,
						currentNode, STYLE_ISSUE_EDGE);

				// change status of the current issue resolution

				// System.out.println(res.getRoleCurrent().getRole().getType() +
				// " *****SOLUTION_FORWARDED*****");
				res.setStatus("SOLUTION_FORWARDED");

				// RoleManager role = en.searchFather(res);

				// Search Father to forward the solution
				RoleManager father = this.getFather(rm, en, ensembles);

				String label3 = "SOLUTION_FORWARDED";
				Object v2 = cat.insertNode(cat.getDefaultParent(), null, father
						.getRole().getType(), STYLE_ROLE);
				cat.insertEdge(cat.getDefaultParent(), "", label3, currentNode,
						v2, STYLE_ISSUE_EDGE);
				currentNode = v2;
				// change the status of the father issue

			}
			// update tree
			// window.updateResolutions(capID, res, en);
			// String count = Integer.toString(window.counter);
			// en.getTrees().put(count, cat);

		}
			break;

		case "COMMIT_REQUESTED": {

			Object root = cat.insertNode(cat.getDefaultParent(), null, "INIT",
					STYLE_INIT);

			String roleName = rm.getRole().getType();
			String label1 = "COMMIT_REQUESTED";
			Object v1 = cat.insertNode(cat.getDefaultParent(), null, roleName,
					STYLE_ROLE);
			cat.insertEdge(cat.getDefaultParent(), "", label1, root, v1,
					STYLE_ISSUE_EDGE);

			String label2 = "COMMIT ADAPTATION";
			boolean leaf = false;
			Solver solver = null;
			if (rm.getSolutions() != null) {
				for (ConcurrentHashMap.Entry<Solver, List<IssueCommunication>> solution : rm
						.getSolutions().entrySet()) {
					solver = solution.getKey();
					// if (solver.isSelected()) {
					List<IssueCommunication> coms = solution.getValue();
					if (coms.size() == 0) {
						// System.out.println("leaf node");
						// memorize the solution for the specific Role
						List<String> commands = new ArrayList<String>();
						String command = solver.getSolution().get(0)
								.getInternalSolution();
						commands.add(solver.getSolution().get(0)
								.getInternalSolution());
						RoleCommand roleCommands = new RoleCommand(commands,
								roleName);
						rm.setRoleCommands(roleCommands);

						cat.insertEdge(cat.getDefaultParent(), "", label2
								+ ": " + command, v1, v1, STYLE_ISSUE_EDGE);
						res.setStatus("End");
						leaf = true;

						// UPDATE HIERARCHY TREE WITH NODE COLOR
						// window.updateNodeColor(hierarchyTree, roleName);
						break;
					} else {
						// vadi in giu nei suoi figli e cambio lo stato da
						// SOLUTION_FORWARDED
						// a COMMIT_REQUESTED

					}
					// decommenta }
				}
			}
			if (leaf == false) {
				if (this.getFather(rm, en, ensembles) != null) {
					// intermediate node
					CommitSolution(capID, res, en, window, cat, label2, v1, rm);
					// System.out.println("ROLe: " + rm.getRole().getType());
					if (solver != null) {
						// System.out.println("Command " +
						// solver.getSolution().get(0).getInternalSolution());
						List<String> commands = new ArrayList<String>();
						commands.add(solver.getSolution().get(0)
								.getInternalSolution());
						RoleCommand command = new RoleCommand(commands, rm
								.getRole().getType());
						rm.setRoleCommands(command);
					}
				} else {
					// root note
					// cat.insertEdge(cat.getDefaultParent(), "", label2, v1,
					// v1, STYLE_ISSUE_EDGE);
					res.setStatus("End");

					// save the RoleCommand
					// System.out.println("ROLE: " + roleName);
					// System.out.println("COMMAND: " +
					// solver.getSolution().get(0).getInternalSolution());
					List<String> commands = new ArrayList<String>();
					commands.add(solver.getSolution().get(0)
							.getInternalSolution());
					RoleCommand roleCommands = new RoleCommand(commands,
							roleName);

					rm.setRoleCommands(roleCommands);

				}
			}

			// update tree
			// window.updateResolutions(capID, res, en);
			// String count = Integer.toString(window.counter);
			// en.getTrees().put(count, cat);

		}
			break;
		case "COMMIT_SENT": {

			int countTargets = 0;
			int targets = 0;
			if (res.getCommunications() == null) {
				// nodo foglia
				res.setStatus("End");
			} else {
				if (res.getCommunications().size() > 1) {

					int numberofBranches = res.getCommunications().size();
					int countEnded = 0;
					for (int i = 0; i < res.getCommunications().size(); i++) {
						countTargets = 0;
						targets = res.getCommunications().get(i).getTargets()
								.size();

						for (int j = 0; j < res.getCommunications().get(i)
								.getTargets().size(); j++) {
							Target t = res.getCommunications().get(i)
									.getTargets().get(j);

							RoleManager rm1 = t.getTargetRole();

							for (int k = 0; k < rm1.getIssueResolutions()
									.size(); k++) {

								IssueResolution current = rm1
										.getIssueResolutions().get(k);

								if (!current.isRoot()
										&& current.getStatus().equals("End")) {

									countTargets++;

								}
							}
						}
						if (countTargets >= targets) {
							// all targets have committed the issue is solved
							// Local COMMIT

							Object root = cat.insertNode(
									cat.getDefaultParent(), null, "INIT",
									STYLE_INIT);

							String roleName = rm.getRole().getType();
							String label1 = "COMMIT_DONE";
							Object v1 = cat.insertNode(cat.getDefaultParent(),
									null, roleName, STYLE_ROLE);
							cat.insertEdge(cat.getDefaultParent(), "", label1,
									root, v1, STYLE_ISSUE_EDGE);

							String label2 = "LOCAL_COMMIT";
							cat.insertEdge(cat.getDefaultParent(), "", label2,
									v1, v1, STYLE_ISSUE_EDGE);
							countEnded = countEnded + 1;
							// update tree
							// window.updateResolutions(capID, res, en);
							// String count = Integer.toString(window.counter);
							// en.getTrees().put(count, cat);

						} else {
							// System.out.println("Targets are committing their
							// solutions");

						}

					}
					if (countEnded == numberofBranches) {
						res.setStatus("End");

					}

				} else {

					targets = res.getCommunications().get(0).getTargets()
							.size();
					for (int i = 0; i < res.getCommunications().get(0)
							.getTargets().size(); i++) {
						Target t = res.getCommunications().get(0).getTargets()
								.get(i);

						RoleManager rm1 = t.getTargetRole();

						for (int j = 0; j < rm1.getIssueResolutions().size(); j++) {

							IssueResolution current = rm1.getIssueResolutions()
									.get(j);

							if (!current.isRoot()
									&& current.getStatus().equals("End")) {

								countTargets++;

							}
						}
					}
					if (countTargets == targets) {
						// all targets have committed the issue is solved
						// Local COMMIT

						Object root = cat.insertNode(cat.getDefaultParent(),
								null, "INIT", STYLE_INIT);

						String roleName = rm.getRole().getType();
						String label1 = "COMMIT_DONE";
						Object v1 = cat.insertNode(cat.getDefaultParent(),
								null, roleName, STYLE_ROLE);
						cat.insertEdge(cat.getDefaultParent(), "", label1,
								root, v1, STYLE_ISSUE_EDGE);

						String label2 = "LOCAL_COMMIT";
						cat.insertEdge(cat.getDefaultParent(), "", label2, v1,
								v1, STYLE_ISSUE_EDGE);
						res.setStatus("End");
						// update tree
						// window.updateResolutions(capID, res, en);
						// String count = Integer.toString(window.counter);
						// en.getTrees().put(count, cat);

					} else {
						// System.out.println("Targets are committing their
						// solutions");
					}
				}

				/* } */

			}
		}
			break;
		case "ISSUE_TRIGGERED": {

			// RoleManager rm = res.getRoleCurrent();
			Issue is = res.getIssueInstance();
			String roleName = rm.getRole().getType();
			// [attiva]
			// window.updateResolutions(capID, res, en);
			// generate the local tree to the role that triggers the issue
			// System.out.println(rm.getRaisedIssues());

			generateSubTree(cap, capID, res, en, cat, rm, is, roleName, window,
					ensembles, issueIndex, graph, lastEdge);
			// showTree(graph);

			// window.updateResolutions(capID, res, en);
			// String count = Integer.toString(window.counter);

			// en.getTrees().put(count, cat);
			// System.out.println(graph.edgeSet().toString());
			// System.out.println(lastEdge);
			// System.out.println("ISSUE TRIGGERED END");

			// System.out.println("ISSUE TRIGGERED END");

		}
			break;
		case "End":

		{
		}
			break;

		default:
			// System.out.println("*****DEFAULT*****");
			break;

		}

		// System.out.println(graph.edgeSet().toString());
		// System.out.println(lastEdge);

		// this.showCAT(cat);
		// showTree(graph);
		// showCAT(cat);

		return lastEdge;

	}

	private void CommitSolution(String capID, IssueResolution res,
			EnsembleManager en, CAWindow window, CATree cat, String label1,
			Object v1, RoleManager rm) {

		if (rm.getSolutions() != null)

		{
			for (ConcurrentHashMap.Entry<Solver, List<IssueCommunication>> solution : rm
					.getSolutions().entrySet()) {
				Solver solver = solution.getKey();
				// if (solver.isSelected()) {

				List<IssueCommunication> coms = solution.getValue();
				if (coms.size() > 1) {
					// more then one communication we insert the AND node
					// first

					Object currentNode;
					// System.out.println(res.getRoleCurrent().getRole().getType()
					// + " *****SOLUTIONS_CHOOSEN*****");
					res.setStatus("SOLUTION_CHOOSEN");

					Object and = cat.insertNode(cat.getDefaultParent(), null,
							"AND", STYLE_AND);
					cat.insertEdge(cat.getDefaultParent(), "", "", v1, and,
							STYLE_DOTTED_EDGE);
					currentNode = and;
					for (int i = 0; i < coms.size(); i++) {

						IssueCommunication currentCom = coms.get(i);
						int count = currentCom.getId();
						String countLabel = Integer.toString(count);
						String ComLabel = "COM" + countLabel;

						Object v2 = cat.insertNode(cat.getDefaultParent(),
								null, ComLabel, STYLE_COM);
						cat.insertEdge(cat.getDefaultParent(), "", "",
								currentNode, v2, STYLE_ISSUE_EDGE);

						// AND node if exists more then one target

						// create one node for each target
						if (currentCom.getTargets().size() > 1) {
							// AND node added
							Object v3 = cat.insertNode(cat.getDefaultParent(),
									null, "AND", STYLE_AND);
							cat.insertEdge(cat.getDefaultParent(), "", "", v2,
									v3, STYLE_DOTTED_EDGE);
							currentNode = v3;
							for (int k = 0; k < currentCom.getTargets().size(); k++) {

								Target currentTarget = currentCom.getTargets()
										.get(k);
								String name = currentTarget.getTargetRole()
										.getRole().getType();

								Object v4 = cat.insertNode(
										cat.getDefaultParent(), null, name,
										STYLE_ROLE);
								cat.insertEdge(cat.getDefaultParent(), "",
										"Commit", v3, v4, STYLE_DOTTED_EDGE);

							}
						} else {

							for (int k = 0; k < currentCom.getTargets().size(); k++) {

								Target currentTarget = currentCom.getTargets()
										.get(k);
								// System.out.println(currentTarget.getTargetRole().getRole().getType());
								String name = currentTarget.getTargetRole()
										.getRole().getType();

								Object v3 = cat.insertNode(
										cat.getDefaultParent(), null, name,
										STYLE_ROLE);
								cat.insertEdge(cat.getDefaultParent(), "",
										"Commit", v2, v3, STYLE_DOTTED_EDGE);

							}
						}

						List<Target> targets = currentCom.getTargets();
						for (int j = 0; j < targets.size(); j++) {

							Target t = targets.get(j);
							RoleManager rm1 = t.getTargetRole();
							// update status of the target
							for (int k = 0; k < rm1.getIssueResolutions()
									.size(); k++) {
								IssueResolution current = rm1
										.getIssueResolutions().get(k);
								if (!current.isRoot()) {
									current.setStatus("COMMIT_REQUESTED");

								}
							}
							res.setStatus("COMMIT_SENT");

						}

					}

				} else if (coms.size() == 1) {
					Object currentNode;
					// System.out.println(res.getRoleCurrent().getRole().getType()
					// + " *****SOLUTIONS_CHOOSEN*****");
					res.setStatus("SOLUTION_CHOOSEN");

					String label2 = "SOLUTION_CHOOSEN";
					cat.insertEdge(cat.getDefaultParent(), "", label2, v1, v1,
							STYLE_ISSUE_EDGE);
					currentNode = v1;

					IssueCommunication currentCom = coms.get(0);
					int count = currentCom.getId();
					String countLabel = Integer.toString(count);
					String ComLabel = "COM" + countLabel;

					Object v2 = cat.insertNode(cat.getDefaultParent(), null,
							ComLabel, STYLE_COM);
					cat.insertEdge(cat.getDefaultParent(), "", "sol",
							currentNode, v2, STYLE_ISSUE_EDGE);
					currentNode = v2;
					List<Target> targets = currentCom.getTargets();
					// for (int j = 0; j < targets.size(); j++) {

					Target t = targets.get(0);
					RoleManager rm1 = t.getTargetRole();
					String roleName1 = rm1.getRole().getType();
					// insert node to the tree
					Object v3 = cat.insertNode(cat.getDefaultParent(), null,
							roleName1, STYLE_ROLE);
					cat.insertEdge(cat.getDefaultParent(), "",
							"COMMIT_REQUESTED", currentNode, v3,
							STYLE_ISSUE_EDGE);

					// update status of the target
					if (rm1.getIssueResolutions().size() > 1) {

						IssueResolution issue = rm1.getIssueResolutions()
								.get(1);
						issue.setStatus("COMMIT_REQUESTED");

						res.setStatus("COMMIT_SENT");
					} else {
						IssueResolution issue = rm1.getIssueResolutions()
								.get(0);
						issue.setStatus("COMMIT_REQUESTED");

						res.setStatus("COMMIT_SENT");
					}

				} else {
					res.setStatus("End");
					// System.out.println("LEAF NODE");
				}
			}

			// decommenta }

		} else

		{
			// no solutions for the current role
			// it is a leaf node
			// System.out.println(rm.getRole().getType());
			res.setStatus("End");
		}

		// insert the tree
		// window.updateResolutions(capID, res, en);
		// String count = Integer.toString(window.counter);
		// en.getTrees().put(count, cat);
	}

	private DefaultEdge manageIssueReceived(CollectiveAdaptationProblem cap,
			String capID, IssueResolution res, EnsembleManager en, CATree cat,
			CAWindow window, List<EnsembleManager> ensembles, int issueIndex,
			CATree hierarchyTree, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge lastEdge) {

		Object currentNode = new CANode();
		Object root = cat.insertNode(cat.getDefaultParent(), null, "INIT",
				STYLE_INIT);
		currentNode = root;

		Role r = res.getRoleCurrent().getRole();
		RoleManager rm = res.getRoleCurrent();
		String roleType = r.getType();

		Object v = cat.insertNode(cat.getDefaultParent(), null, roleType,
				STYLE_ROLE);

		cat.insertEdge(cat.getDefaultParent(), "", "ISSUE_RECEIVED: "
				+ res.getIssueInstance().getIssueType(), currentNode, v,
				STYLE_ISSUE_EDGE);
		currentNode = v;

		String issueName = res.getIssueInstance().getIssueType();
		// System.out.println(roleType + " *****ISSUE_RECEIVED*****");

		lastEdge = ExecuteSolvers(cap, capID, res, en, cat, rm,
				res.getIssueInstance(), r.getType(), window, currentNode,
				ensembles, issueIndex, graph, lastEdge);

		return lastEdge;

	}

	private RoleManager getFather(RoleManager rm, EnsembleManager em,
			List<EnsembleManager> ensembles) {
		RoleManager result = null;

		// first select the right ensemble
		// EnsembleManager currentEM = rm.getEnsemble();
		// second find the Role Father using the active communications
		for (int m = 0; m < ensembles.size(); m++) {
			EnsembleManager currentEM = ensembles.get(m);

			for (int j = 0; j < currentEM.getRolesManagers().size(); j++) {
				RoleManager currentRole = currentEM.getRolesManagers().get(j);
				// if
				// (!(currentRole.getRole().getType().equals(rm.getRole().getType())))
				// {
				if (currentRole.getSolutions() != null) {
					// System.out.println("RUOLO SOTTO ANALISI: " +
					// currentRole.getRole().getType());
					for (ConcurrentHashMap.Entry<Solver, List<IssueCommunication>> solution : currentRole
							.getSolutions().entrySet()) {
						Solver solver = solution.getKey();

						// if (solver.isSelected()) {
						// System.out.println(solver.getName());
						List<IssueCommunication> communications = solution
								.getValue();
						for (int i = 0; i < communications.size(); i++) {
							IssueCommunication com = communications.get(i);
							for (int k = 0; k < com.getTargets().size(); k++) {
								Target t = com.getTargets().get(k);
								// System.out.println("RUOLO TARGET: " +
								// t.getTargetRole().getRole().getType());

								if (t.getTargetRole().getRole().getType()
										.equals(rm.getRole().getType())) {
									result = currentRole;
								}
							}

						}
						// decommente}
					}
				}
			}
		}
		if (result == null) {
			// System.out.println("search father in another ensemble");
		}
		return result;
	}

	/**
	 * This method generates the tree to solve a specific issue in a specific
	 * Role
	 */

	private DefaultEdge generateSubTree(CollectiveAdaptationProblem cap,
			String capID, IssueResolution res, EnsembleManager en, CATree cat,
			RoleManager rm, Issue is, String roleName, CAWindow window,
			List<EnsembleManager> ensembles, int issueIndex,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {

		// / GRAPH UPDATE - OVERALL TREE CONSTRUCTION
		int width = 0;

		// int random = (int) (Math.random() * 100 + 1);
		String index = "" + en.getCount();
		en.setCount(en.getCount() + 1);

		String ensembleName = rm.getEnsemble().getEnsemble().getName();

		graph.addVertex("INIT");
		graph.addVertex(roleName + "_" + index + "_" + ensembleName);
		graph.addEdge("INIT", roleName + "_" + index + "_" + ensembleName);
		// GraphIterator<String, DefaultEdge> iterator1 = new
		// BreadthFirstIterator<String, DefaultEdge>(graph);
		// while (iterator1.hasNext()) {
		// width++;
		// System.out.println(iterator1.next());
		// }

		// ////

		// creation of the tree root - the role triggers an issue internally
		Object currentNode = new CANode();
		Object root = cat.insertNode(cat.getDefaultParent(), null, "INIT",
				STYLE_INIT);
		currentNode = root;
		String issueName = res.getIssueInstance().getIssueType();
		// cat.setDefaultParent(root);

		String label = "ISSUE_TRIGGERED: " + issueName;

		Object v1 = cat.insertNode(cat.getDefaultParent(), null, roleName,
				STYLE_ROLE);
		String id = ((mxCell) v1).getId();

		cat.insertEdge(cat.getDefaultParent(), "", label, currentNode, v1,
				STYLE_ISSUE_EDGE);
		currentNode = v1;

		// System.out.println(roleName + " *****ISSUE_TRIGGERED*****");

		// Execute Solvers of the agent that can solve the issue
		ExecuteSolvers(cap, capID, res, en, cat, rm, is, roleName, window,
				currentNode, ensembles, issueIndex, graph, lastEdge);
		// showTree(graph);
		// System.out.println(roleName + " *****ISSUE_TRIGGERED*****");
		return lastEdge;

	}

	private DefaultEdge ExecuteSolvers(CollectiveAdaptationProblem cap,
			String capID, IssueResolution res, EnsembleManager en, CATree cat,
			RoleManager rm, Issue is, String roleName, CAWindow window,
			Object currentNode, List<EnsembleManager> ensembles,
			int issueIndex, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge lastEdge) {
		// we found the solvers inside the agent tha can solve the issue
		List<Solver> solvers = this.callLocalSolver(rm, is, ensembles);

		// update the list of possible solvers for the current issue
		// window.updatePossibleSolutions(capID, res, en, this.localSolvers);

		// Find only one Local Solver - to solve a specifice issue
		if (solvers.size() == 1) {
			// System.out.println("SOLVER ABLE TO RESOLVE THE ISSUE: "
			// + solvers.get(0).getName());
			// this.localSolvers = solvers;
			rm.setSolverInstances(solvers);
			Solver solver = rm.getSolverInstances().get(0);
			solver.setSelected(true);

			lastEdge = createSingleCom(capID, res, en, cat, currentNode, rm,
					roleName, window, solver, ensembles, graph, lastEdge);
			// showTree(graph);
			// showCAT(cat);
			// System.out.println("qio");
			// window.updateActiveIssueResolutionTable(capID, res, en);
		} else if (solvers.size() > 1) {
			// more the one solver for the same Issue
			// this.localSolvers = solvers;
			// System.out.println("SOLVER ABLE TO RESOLVE THE ISSUE: "
			// + solvers.get(0).getName());
			// System.out.println("SOLVER ABLE TO RESOLVE THE ISSUE: "
			// + solvers.get(1).getName());
			rm.setSolverInstances(solvers);
			createMultipleCom(cap, capID, res, en, cat, currentNode, rm,
					roleName, window, ensembles, issueIndex, graph, lastEdge);

		} else {
			System.out.println("External Com to another Ensemble");
		}
		// showTree(graph);
		// showCAT(cat);
		return lastEdge;
	}

	private DefaultEdge createMultipleCom(CollectiveAdaptationProblem cap,
			String capID, IssueResolution res, EnsembleManager en, CATree cat,
			Object currentNode, RoleManager rm, String roleName,
			CAWindow window, List<EnsembleManager> ensembles, int issueIndex,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {

		res.setStatus("LOCAL_SOLVER_CALLED");

		// update the count of decision points
		en.updateDecisionPoint();
		int count = en.getNumOfDecisionPoints();

		String orLabel = "OR" + count;
		// System.out.println(roleName + " *****LOCAL_SOLVER_CALLED*****");
		Object v3 = cat.insertNode(cat.getDefaultParent(), null, orLabel,
				STYLE_OR);
		cat.insertEdge(cat.getDefaultParent(), "", "", currentNode, v3,
				STYLE_DOTTED_EDGE);
		currentNode = v3;

		// / GRAPH UPDATE - OVERALL TREE CONSTRUCTION
		int width = 0;

		// int random = (int) (Math.random() * 100 + 1);
		String index = "" + en.getCount();
		en.setCount(en.getCount() + 1);

		String last = "";
		GraphIterator<String, DefaultEdge> iterator3 = new BreadthFirstIterator<String, DefaultEdge>(
				graph);
		while (iterator3.hasNext()) {
			last = iterator3.next();

		}
		String lastNode = last;

		// String lastNode = graph.getEdgeTarget(lastEdge);
		graph.addVertex("OR_" + index);
		lastEdge = graph.addEdge(lastNode, "OR_" + index);
		lastNode = "OR_" + index;

		if (rm.getSolverInstances().size() > 1) {
			// we select only one solution (the best)

			for (int i = 1; i < rm.getSolverInstances().size(); i++) {

				rm.getSolverInstances().get(i).setSelected(true);
			}
			rm.getSolverInstances().get(0).setSelected(true);

		} else {
			rm.getSolverInstances().get(0).setSelected(true);
		}

		for (int i = 0; i < rm.getSolverInstances().size(); i++) {

			// consider each solver provided by the role
			Solver currentSolver = rm.getSolverInstances().get(i);

			Solution currentSolution = currentSolver.getSolution().get(0);

			// if no extra issue generated, add only a node
			if (currentSolution.getIssue().size() == 0) {

				String label = "INTERNAL_SOLUTION";
				Object v1 = cat.insertNode(cat.getDefaultParent(), null,
						roleName, STYLE_ROLE);
				cat.insertEdge(cat.getDefaultParent(), "", label, currentNode,
						v1, STYLE_ISSUE_EDGE);
				currentNode = v1;

				res.setStatus("SOLUTION_FORWARDED");

			} else {

				// extra issue triggered
				targetWithIssues(res, en, cat, currentNode, rm, roleName,
						currentSolver, ensembles, graph, lastEdge);

			}

		}
		// showTree(graph);
		return lastEdge;
	}

	private List<Passenger> retrievePassengers(CollectiveAdaptationProblem cap,
			EnsembleManager en, List<IssueCommunication> communications) {
		List<Passenger> passengers = new ArrayList<Passenger>();

		// retrieve the set of Roles involved
		// map role, issueToSolve
		Map<RoleManager, Map<Integer, Double>> roles = new HashMap<RoleManager, Map<Integer, Double>>();
		for (int i = 0; i < communications.size(); i++) {
			IssueCommunication com = communications.get(i);
			for (int j = 0; j < com.getTargets().size(); j++) {

				Target t = com.getTargets().get(j);
				String IssueToSolve = com.getIssueToSolve().getIssueType();

				RoleManager role = t.getTargetRole();

				// search the rigth solver to retrieve time and cost
				Solver solv = null;
				for (int k = 0; k < role.getRole().getSolver().size(); k++) {
					Solver current = role.getRole().getSolver().get(k);
					if (current.getIssue().getIssueType().equals(IssueToSolve)) {
						solv = current;
						break;
					}
				}

				if ((role.getRole().getId().equals("USER_6"))) {
					if (solv.getSolution() != null
							&& solv.getSolution().get(0) != null
							&& solv.getSolution().get(0).getTime() != null
							&& solv.getSolution().get(0).getCost() != null) {
						Map<Integer, Double> timeCost = new HashMap<Integer, Double>();
						timeCost.put(solv.getSolution().get(0).getTime(), solv
								.getSolution().get(0).getCost());

						roles.put(role, timeCost);
					} else {
						Map<Integer, Double> timeCost = new HashMap<Integer, Double>();
						timeCost.put(0, 0.0);
						roles.put(role, timeCost);

					}
					break;

				}
				if ((role.getRole().getId().equals("USER_3"))) {
					if (solv.getSolution() != null
							&& solv.getSolution().get(0) != null
							&& solv.getSolution().get(0).getTime() != null
							&& solv.getSolution().get(0).getCost() != null) {
						Map<Integer, Double> timeCost = new HashMap<Integer, Double>();
						timeCost.put(solv.getSolution().get(0).getTime(), solv
								.getSolution().get(0).getCost());

						roles.put(role, timeCost);
					} else {
						Map<Integer, Double> timeCost = new HashMap<Integer, Double>();
						timeCost.put(0, 0.0);
						roles.put(role, timeCost);

					}
					break;

				}
			}
		}

		// System.out.println("RUOLI COINVOLTI NELLA SOLUZIONE: " +
		// roles.size());

		for (Map.Entry<RoleManager, Map<Integer, Double>> entry : roles
				.entrySet()) {
			RoleManager key = entry.getKey();
			Map<Integer, Double> timeCostValue = entry.getValue();
			int time = 0;
			double cost = 0;

			for (Map.Entry<Integer, Double> entry1 : timeCostValue.entrySet()) {
				time = entry1.getKey();
				cost = entry1.getValue();

			}

			Passenger p = this.TakePassenger(key, cap, en, time, cost);
			passengers.add(p);

		}

		// System.out.println("PASSENGERS CREATI: " + passengers.size());

		return passengers;
	}

	private Passenger TakePassenger(RoleManager rm,
			CollectiveAdaptationProblem cap, EnsembleManager en, int time,
			double cost) {

		Passenger p = new Passenger();
		for (int i = 0; i < cap.getEnsembles().size(); i++) {
			CollectiveAdaptationEnsemble current = cap.getEnsembles().get(i);
			if (current.getEnsembleName().equals(en.getEnsemble().getName())) {

				String target = cap.getTarget();
				Optional<CollectiveAdaptationRole> t = current.getRoles()
						.stream().filter(r -> r.getRole().equals(target))
						.findFirst();
				if (t.isPresent()) {
					CollectiveAdaptationRole role = t.get();
					p.setCost(cost);
					p.setId(role.getId());
					p.setMaxCost(role.getMaxCost());
					p.setMaxTravel(role.getMaxTravelTime());
					p.setTravelTime(time);
					p.setWeightCost(role.getWeightCost());
					p.setWeightTravel(role.getWeightTravelTime());
					break;

				}

			}
		}

		return p;

	}

	private DefaultEdge createSingleCom(String capID, IssueResolution res,
			EnsembleManager en, CATree cat, Object currentNode, RoleManager rm,
			String roleName, CAWindow window, Solver solver,
			List<EnsembleManager> ensembles,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {

		// A Solver Exists
		res.setStatus("SOLVER_CALLED");

		// we create the subtree related to the unique solver identified
		createSolverTree(res, en, cat, currentNode, rm, roleName, solver,
				ensembles, graph, lastEdge);

		return lastEdge;
	}

	private DefaultEdge createSolverTree(IssueResolution res,
			EnsembleManager en, CATree cat, Object currentNode, RoleManager rm,
			String roleName, Solver solver, List<EnsembleManager> ensembles,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {

		if (solver.getSolution().get(0).getIssue().size() > 0) {
			// the solver has an issue to trigger
			targetWithIssues(res, en, cat, currentNode, rm, roleName, solver,
					ensembles, graph, lastEdge);

		} else {

			// local solution - no extra issues generated
			String roleType = rm.getRole().getType();
			// System.out.println(roleType + " *****SOLUTION_FOUND*****");

			String label1 = "INTERNAL_SOLUTION";
			cat.insertEdge(cat.getDefaultParent(), "", label1, currentNode,
					currentNode, STYLE_ISSUE_EDGE);
			List<IssueCommunication> coms = new ArrayList<IssueCommunication>();
			rm.AddSolution(solver, coms);

			// change status of the current issue resolution

			// System.out.println(res.getRoleCurrent().getRole().getType() + "
			// *****SOLUTION_FORWARDED*****");
			res.setStatus("SOLUTION_FORWARDED");

			// RoleManager role = en.searchFather(res);

			// Search Father to forward the solution
			RoleManager father = this.getFather(rm, en, ensembles);
			String label2 = "SOLUTION_FORWARDED";
			Object v2 = cat.insertNode(cat.getDefaultParent(), null, father
					.getRole().getType(), STYLE_ROLE);
			cat.insertEdge(cat.getDefaultParent(), "", label2, currentNode, v2,
					STYLE_ISSUE_EDGE);
			currentNode = v2;

		}

		return lastEdge;
	}

	private void targetWithIssues(IssueResolution res, EnsembleManager en,
			CATree cat, Object currentNode, RoleManager rm, String roleName,
			Solver solver, List<EnsembleManager> ensembles,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {

		res.setStatus("SOLUTION_FOUND");
		// System.out.println(roleName + " *****SOLUTION_FOUND WITH
		// TARGETS*****SOLVER: " + solver.getName());

		ArrayList<IssueCommunication> communications = new ArrayList<IssueCommunication>();

		// here we create the set of communications, one for each issue to solve
		// if the set of communications is empty means we need to find in
		// another ensemble
		communications = this.targetIssues(solver.getSolution().get(0)
				.getIssue(), en, rm, res, ensembles);

		// creo la soluzione <Solver, List<IssueCommunication>> per il Route
		// Manager
		// e poi creo il SubTree
		res.setCommunications(communications);
		rm.AddSolution(solver, communications);

		// here we creare the tree with the different communications
		createSubTree(en, cat, currentNode, rm, solver, communications, graph,
				lastEdge);
		res.setStatus("ISSUE_TARGETED");

		// return lastEdge;
	}

	private EnsembleManager findEnsemble(EnsembleManager startingEnsemble,
			Issue issueToFind, List<EnsembleManager> ensembles,
			RoleManager originRoleManager) {
		EnsembleManager result = null;
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager current = ensembles.get(i);
			if (!(current.getEnsemble().getName().equals(startingEnsemble
					.getEnsemble().getName()))) {
				ArrayList<Target> targets = new ArrayList<Target>();
				targets = retrieveTargets(issueToFind, current,
						originRoleManager);
				if (targets.size() > 0) {
					result = current;
					break;
				}
			}
		}

		return result;
	}

	private void createSubTree(EnsembleManager en, CATree cat,
			Object currentNode, RoleManager rm, Solver solver,
			ArrayList<IssueCommunication> communications,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {
		String issues = "";
		for (int i = 0; i < solver.getSolution().get(0).getIssue().size(); i++) {

			String currentIssue = solver.getSolution().get(0).getIssue().get(i)
					.getIssueType();

			if (i == solver.getSolution().get(0).getIssue().size() - 1) {
				issues += currentIssue;
			} else {
				issues += currentIssue + ", ";
			}

		}

		// ///

		// showTree(graph);
		// String solverLabel = "Solver: " + solver.getName() + "\n" +
		// "ISSUE_TRIGGERED: {" + issues + "}";
		String solverLabel = solver.getName();
		String last = "";
		GraphIterator<String, DefaultEdge> iterator3 = new BreadthFirstIterator<String, DefaultEdge>(
				graph);

		String nodeOk = "";
		while (iterator3.hasNext()) {

			last = iterator3.next();
			// decommenta
			if (last.contains(rm.getRole().getType())) {
				break;
			}

		}
		String lastNode = "";
		// String lastNode = last;
		if (lastEdge == null) {
			lastNode = last;
		} else {
			lastNode = graph.getEdgeTarget(lastEdge);
		}

		Object v2 = null;
		if (solver.getSolution().get(0).getIssue().size() > 1) {
			v2 = cat.insertNode(cat.getDefaultParent(), null, "AND", STYLE_AND);
			cat.insertEdge(cat.getDefaultParent(), "", solverLabel,
					currentNode, v2, STYLE_DOTTED_EDGE);
			currentNode = v2;
			// UPDATE OVERLLA GRAPH

			// / GRAPH UPDATE - OVERALL TREE CONSTRUCTION
			int width = 0;

			// int random = (int) (Math.random() * 100 + 1);
			String index = "" + en.getCount();
			en.setCount(en.getCount() + 1);

			graph.addVertex("AND_" + index);
			DefaultEdge lastEd = graph.addEdge(lastNode, "AND_" + index);

			/*
			 * GraphIterator<String, DefaultEdge> iterator1 = new
			 * BreadthFirstIterator<String, DefaultEdge>(graph); while
			 * (iterator1.hasNext()) { width++;
			 * System.out.println(iterator1.next()); }
			 */
			// System.out.println("profondita': " + width);
			// showTree(graph);
			// showCAT(cat);

			// ////

			addMultipleTargets(en, cat, rm, solver, communications, v2, graph,
					lastEd);
		} else {
			// only one issue, only one communication

			// UPDATE OVERLLA GRAPH

			// / GRAPH UPDATE - OVERALL TREE CONSTRUCTION
			int width = 0;
			// int random = (int) (Math.random() * 100 + 1);
			String index = "" + en.getCount();
			en.setCount(en.getCount() + 1);

			graph.addVertex("COM_" + index);
			DefaultEdge e = graph.addEdge(lastNode, "COM_" + index);

			/*
			 * GraphIterator<String, DefaultEdge> iterator1 = new
			 * BreadthFirstIterator<String, DefaultEdge>(graph); while
			 * (iterator1.hasNext()) { width++;
			 * System.out.println(iterator1.next()); } System.out.println(
			 * "profondita': " + width);
			 */
			// ////

			// first add the COM node
			int count = communications.get(0).getId();
			String countLabel = Integer.toString(count);
			Object v5 = cat.insertNode(cat.getDefaultParent(), null, "COM"
					+ countLabel, STYLE_COM);
			String issueToSolve = communications.get(0).getIssueToSolve()
					.getIssueType();
			cat.insertEdge(cat.getDefaultParent(), "", issueToSolve,
					currentNode, v5, STYLE_ISSUE_EDGE);
			currentNode = v5;

			// add the target role
			addTargetNode(en, cat, currentNode, rm, communications, 0, solver,
					graph, e);
			// showTree(graph);

			// if there are extra issue recall recursively

		}

		// return lastEdge;
	}

	@SuppressWarnings("unchecked")
	private DefaultEdge addMultipleTargets(EnsembleManager en, CATree cat,
			RoleManager rm, Solver solver,
			ArrayList<IssueCommunication> communications, Object v2,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {
		Object currentNode;

		String last = "";
		GraphIterator<String, DefaultEdge> iterator3 = new BreadthFirstIterator<String, DefaultEdge>(
				graph);
		while (iterator3.hasNext()) {
			last = iterator3.next();

		}
		// String lastNode = last;

		String lastNode = graph.getEdgeTarget(lastEdge);

		for (int j = 0; j < communications.size(); j++) {

			int count = communications.get(j).getId();
			String countLabel = Integer.toString(count);
			Object v5 = cat.insertNode(cat.getDefaultParent(), null, "COM"
					+ countLabel, STYLE_COM);
			String issueToSolve = communications.get(j).getIssueToSolve()
					.getIssueType();
			cat.insertEdge(cat.getDefaultParent(), "", issueToSolve, v2, v5,
					STYLE_ISSUE_EDGE);
			currentNode = v5;

			// / GRAPH UPDATE - OVERALL TREE CONSTRUCTION
			int width = 0;

			// int random = (int) (Math.random() * 100 + 1);
			String index = "" + en.getCount();
			en.setCount(en.getCount() + 1);

			graph.addVertex("COM_" + index);
			lastEdge = graph.addEdge(lastNode, "COM_" + index);
			/*
			 * GraphIterator<String, DefaultEdge> iterator1 = new
			 * BreadthFirstIterator<String, DefaultEdge>(graph); while
			 * (iterator1.hasNext()) { width++;
			 * System.out.println(iterator1.next()); } System.out.println(
			 * "profondita': " + width);
			 */
			// showTree(graph);
			// showCAT(cat);

			List<IssueResolution> resolutions = new ArrayList<IssueResolution>();

			if (communications.get(j).getTargets().size() > 1) {
				// FIRST CREATE THE OR od AND NODE - retrieved using the issue
				// contraint (forall, exist)
				String constraint = communications.get(j).getIssueToSolve()
						.getIssueCondition();
				/*
				 * if (constraint.equalsIgnoreCase("Forall")) { Object v3 =
				 * cat.insertNode(cat.getDefaultParent(), null, "AND",
				 * STYLE_AND); cat.insertEdge(cat.getDefaultParent(), "", "",
				 * v5, v3, STYLE_DOTTED_EDGE); currentNode = v3; } else { Object
				 * v3 = cat.insertNode(cat.getDefaultParent(), null, "OR",
				 * STYLE_OR); cat.insertEdge(cat.getDefaultParent(), "", "", v5,
				 * v3, STYLE_DOTTED_EDGE); currentNode = v3; }
				 */
				// CREATE ONE NODE FOR EACH TARGET

				for (int k = 0; k < communications.get(j).getTargets().size(); k++) {

					Target currentTarget = communications.get(j).getTargets()
							.get(k);
					String name = currentTarget.getTargetRole().getRole()
							.getType();
					String solverName = solver.getName();
					// String isName =
					// currentTarget.getTargetRole().getRole().getSolver().get(0).getName();
					String Label = "Solver: " + solverName;
					Object v4 = cat.insertNode(cat.getDefaultParent(), null,
							name, STYLE_ROLE);
					cat.insertEdge(cat.getDefaultParent(), "", "", currentNode,
							v4, STYLE_DOTTED_EDGE);
					// currentNode = v4;
					// add an Issue Resolution to the global list
					IssueCommunication currentCom = communications.get(j);

					RoleManager targetManager = currentTarget.getTargetRole();

					en.updateNumOfIssueResolutions();

					// add issue resolution only if the solver has been selected
					// if (solver.isSelected()) {
					IssueResolution ir = new IssueResolution(2,
							"ISSUE_RECEIVED", rm, targetManager,
							currentCom.getIssueToSolve(), null);
					ir.setRoot(false);
					ir.setIssueResolutionID(en.getIssueResolutionCount());
					targetManager.getIssueResolutions().add(ir);
					en.addIssueResolution(ir);
					resolutions.add(ir);

					// update the hashmap of issues relations
					List<IssueCommunication> coms = new ArrayList<IssueCommunication>();

					System.out.println("ISSUE RESOLUTION TO ADD:"
							+ ir.getIssueInstance().getIssueType());
					en.AddRelations(ir, coms);
					// }

				}
			} else {
				// only one target -- add target

				lastEdge = addTargetNode(en, cat, currentNode, rm,
						communications, j, solver, graph, lastEdge);
				// showTree(graph);
				// showCAT(cat);

			}

		}
		return lastEdge;
	}

	private void showCAT(CATree cat) {

		// frame to see the issue resolution tree mxGraphComponent
		mxGraphComponent graphComponent = new mxGraphComponent(cat);

		CATree hierarchy = new CATree();
		mxGraphComponent graphComponentHierarchy = new mxGraphComponent(
				hierarchy);

		// layout = new mxParallelEdgeLayout(graphComponent.getGraph());

		// layout = new mxHierarchicalLayout(graphComponent.getGraph());
		// layout1 = new mxCompactTreeLayout(graphComponent.getGraph());

		graphComponent.setEnabled(true);

		graphComponent.setBounds(600, 125, 650, 600);
		graphComponent.setBorder(javax.swing.BorderFactory
				.createLineBorder(new Color(0, 0, 0)));

		mxHierarchicalLayout layout1 = new mxHierarchicalLayout(
				graphComponent.getGraph());

		layout1.execute(graphComponent.getGraph().getDefaultParent());

		JFrame frame = new JFrame();
		frame.getContentPane().add(graphComponent);
		frame.setTitle("ADAPTATION RESOLUTION TREE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();

		frame.setVisible(true);

	}

	private void showTree(DirectedGraph<String, DefaultEdge> graph) {

		// VISUALIZE GRAPH
		// create a visualization using JGraph, via the adapter
		JGraphModelAdapter jgAdapter = new JGraphModelAdapter<>(graph);

		JGraph jgraph = new JGraph(jgAdapter);
		/* Apply tree Layout */

		// Object roots = getRoots(); // replace getRoots with your own

		JGraphFacade facade = new JGraphFacade(jgraph);
		// Pass the facade the JGraph instance
		JGraphLayout layout = new JGraphTreeLayout();
		// Create an instance of the appropriate layout
		layout.run(facade); // Run the layout on the facade.
		Map nested = facade.createNestedMap(true, true);
		// Obtain a map of the resulting attribute changes from the facade
		jgraph.getGraphLayoutCache().edit(nested);
		// Apply the results to the actual graph

		JFrame frame = new JFrame();
		frame.getContentPane().add(jgraph);

		frame.setBounds(600, 125, 650, 600);

		frame.setTitle("COLLECTIVE ADAPTATION TREE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();

		frame.setVisible(true);
	}

	private DefaultEdge addTargetNode(EnsembleManager en, CATree cat,
			Object currentNode, RoleManager rm,
			ArrayList<IssueCommunication> communications, int j, Solver solver,
			DirectedGraph<String, DefaultEdge> graph, DefaultEdge lastEdge) {

		ArrayList<IssueResolution> resolutions = new ArrayList<IssueResolution>();
		// add first che communication NODE, before the target role

		Target currentTarget = communications.get(j).getTargets().get(0);
		String name = currentTarget.getTargetRole().getRole().getType();

		IssueCommunication currentCom = communications.get(j);
		int count = currentCom.getId();
		String countLabel = Integer.toString(count);
		// String ComLabel = "COM" + countLabel;
		String isName = currentTarget.getTargetRole().getRole().getSolver()
				.get(0).getName();

		// Object v1 = cat.insertNode(cat.getDefaultParent(), null, ComLabel,
		// STYLE_COM);
		// cat.insertEdge(cat.getDefaultParent(), "", isName, currentNode, v1);
		// currentNode = v1;

		String Label = "Solver: " + isName;
		Object v3 = cat.insertNode(cat.getDefaultParent(), null, name,
				STYLE_ROLE);
		cat.insertEdge(cat.getDefaultParent(), "", "", currentNode, v3,
				STYLE_DOTTED_EDGE);
		currentNode = v3;

		// / GRAPH UPDATE - OVERALL TREE CONSTRUCTION
		int width = 0;
		// UPDATE OVERLLA GRAPH
		String last = "";
		GraphIterator<String, DefaultEdge> iterator3 = new BreadthFirstIterator<String, DefaultEdge>(
				graph);
		while (iterator3.hasNext()) {
			last = iterator3.next();

		}
		String lastNode = "";
		if (lastEdge == null) {
			lastNode = last;
		} else {
			lastNode = graph.getEdgeTarget(lastEdge);
		}

		// String lastNode = graph.getEdgeTarget(lastEdge);

		// int random = (int) (Math.random() * 100 + 1);
		String index = "" + en.getCount();
		en.setCount(en.getCount() + 1);
		String ensembleName = currentTarget.getTargetRole().getEnsemble()
				.getEnsemble().getName();

		graph.addVertex(name + "_" + index + "_" + ensembleName);
		graph.addEdge(lastNode, name + "_" + index + "_" + ensembleName);

		/*
		 * GraphIterator<String, DefaultEdge> iterator1 = new
		 * BreadthFirstIterator<String, DefaultEdge>(graph); while
		 * (iterator1.hasNext()) { width++;
		 * System.out.println(iterator1.next()); } System.out.println(
		 * "profondita': " + width);
		 */
		// ////
		// showTree(graph);
		// showCAT(cat);
		// add an Issue Resolution to the global list
		// IssueCommunication currentCom = communications.get(j);

		// if (solver.isSelected()) {
		RoleManager targetManager = currentTarget.getTargetRole();
		en.updateNumOfIssueResolutions();

		IssueResolution ir = new IssueResolution(1, "ISSUE_RECEIVED", rm,
				targetManager, currentCom.getIssueToSolve(), null);
		ir.setIssueResolutionID(en.getIssueResolutionCount());
		ir.setRoot(false);

		targetManager.getIssueResolutions().add(ir);
		en.addIssueResolution(ir);
		resolutions.add(ir);

		// update the hashmap of issues relations
		List<IssueCommunication> coms = new ArrayList<IssueCommunication>();
		en.AddRelations(ir, coms);
		// }

		return lastEdge;
	}

	/*
	 * Method to send Issue to different target roles through Issue
	 * Communications it returns the set of communications created For each
	 * Issue it returns a communication with targets
	 */
	private ArrayList<IssueCommunication> targetIssues(List<Issue> issues,
			EnsembleManager e, RoleManager originRoleManager,
			IssueResolution res, List<EnsembleManager> ensembles) {
		ArrayList<Target> targets = new ArrayList<Target>();
		ArrayList<IssueCommunication> communications = new ArrayList<IssueCommunication>();
		ArrayList<IssueCommunication> ExtraCommunications = new ArrayList<IssueCommunication>();

		boolean result = false;
		List<Issue> extraEnsembleIssues = new ArrayList<Issue>();
		EnsembleManager extraEnsemble = null;

		for (int i = 0; i < issues.size(); i++) {
			// here we create an Issue Communication with roles able to solve
			// the specific issue

			Issue currentIssue = issues.get(i);

			// find targets locally to the same ensemble of the
			// originRoleManager
			targets = this.find_targets(currentIssue, e, originRoleManager,
					ensembles);

			if (targets.size() > 0) {
				res.updateCommCounter();
				int count = res.getCommunicationsCounter();

				IssueCommunication com = new IssueCommunication(count,
						originRoleManager, currentIssue, targets,
						CommunicationStatus.INIT, e);
				communications.add(com);

			} else {
				// System.out.println("issue solved in another ensemble");
				extraEnsemble = this.findEnsemble(e, currentIssue, ensembles,
						originRoleManager);
				if (extraEnsemble == null) {
					System.out.println("No Ensembles able to solve the Issue: "
							+ currentIssue.getIssueType());
				} else {

					extraEnsembleIssues.add(currentIssue);

				}

			}

		}
		// issues solvable in extra ensembles
		// System.out.println("Extra Ensemble Issues: " +
		// extraEnsembleIssues.size());
		if (extraEnsembleIssues.size() > 0) {
			ExtraCommunications = targetIssues(extraEnsembleIssues,
					extraEnsemble, originRoleManager, res, ensembles);
			communications.addAll(ExtraCommunications);
		}

		return communications;

	}

	/* find target roles able to solve a specific issue */
	private ArrayList<Target> find_targets(Issue currentIssue,
			EnsembleManager em, RoleManager origin,
			List<EnsembleManager> ensembles) {

		ArrayList<Target> targets = new ArrayList<Target>();
		targets = retrieveTargets(currentIssue, em, origin);
		return targets;

	}

	private ArrayList<Target> retrieveTargets(Issue currentIssue,
			EnsembleManager em, RoleManager origin) {
		ArrayList<Target> targets = new ArrayList<Target>();

		// System.out.println("Issue to find a target: " +
		// currentIssue.getIssueType());
		//

		for (int i = 0; i < em.getRolesManagers().size(); i++) {
			RoleManager currentRoleManager = em.getRolesManagers().get(i);
			Role currentRole = currentRoleManager.getRole();
			Role originRole = origin.getRole();
			for (int j = 0; j < currentRole.getSolver().size(); j++) {
				Solver solver = currentRole.getSolver().get(j);
				// if (!(currentRole.getId().equals(originRole.getId()))) {
				if (solver.getIssue().getIssueType()
						.equals(currentIssue.getIssueType())) {
					// The Role is able to manage the current Issue
					// System.out.println("ROLE ABLE TO SOLVE THE ISSUE: " +
					// currentRole.getType());
					Target target = new Target(currentRoleManager, solver,
							TargetStatus.INIT);
					targets.add(target);
					// }
				}
			}

		}

		return targets;
	}

	/* Method to check if an issue can be solved locally to a role */

	public List<Solver> callLocalSolver(RoleManager roleManager, Issue issue,
			List<EnsembleManager> ensembles) {

		List<Solver> solvers = new ArrayList<Solver>();
		// System.out.println("ISSUE TO SOLVE: " + issue.getIssueType());
		// System.out.println("ROLE TO FIND SOLVER: "
		// + roleManager.getRole().getType());
		// boolean result = false;

		// Find Local Solver and set in the analyzer il all the ensembles

		for (int i = 0; i < roleManager.getRole().getSolver().size(); i++) {
			Solver currentSolver = roleManager.getRole().getSolver().get(i);

			if (currentSolver.getIssue().getIssueType()
					.equalsIgnoreCase(issue.getIssueType())) {
				// solver provided for the current issue
				// this.localSolver = currentSolver;
				solvers.add(currentSolver);

				// System.out.println(roleManager.getRole().getType()
				// + "***** LOCAL SOLVER CALLED***"
				// + currentSolver.getName());
				// //System.out.println(this.localSolver.getSolution().get(0).getIssue().get(0).getIssueType());
				// result = true;
				// break;
			}

		}

		// System.out.println("NUMERO DI LOCAL SOLVER per la Issue "
		// + issue.getIssueType() + ": " + solvers.size());
		return solvers;
	}

	public class Graph extends JPanel {
		public Graph() {
			setSize(500, 500);
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D gr = (Graphics2D) g; // This is if you want to use
			// Graphics2D
			// Now do the drawing here
			ArrayList<Integer> scores = new ArrayList<Integer>(10);

			Random r = new Random();

			for (int j : scores) {
				j = r.nextInt(20);
				// System.out.println(r);
			}

			int y1;
			int y2;

			for (int i = 0; i < scores.size() - 1; i++) {
				y1 = (scores.get(i)) * 10;
				y2 = (scores.get(i + 1)) * 10;
				gr.drawLine(i * 10, y1, (i + 1) * 10, y2);
			}
		}

	}

	/* max depth */
	private int maxDepth(CATree cat) {
		// build graph
		distance = 0;
		mxGraph graph = new mxGraph();

		mxAnalysisGraph aGraph = new mxAnalysisGraph();
		aGraph.setGraph(cat);

		// apply dfs to find depth of a tree
		mxTraversal.dfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

			@Override
			public boolean visit(Object vertex, Object edge) {
				mxCell v = (mxCell) vertex;
				mxCell e = (mxCell) edge;
				String eVal = "N/A";

				if (e != null) {
					if (e.getValue() == null) {
						eVal = "1.0";
					} else {
						eVal = e.getValue().toString();
					}
				}

				if (!eVal.equals("N/A")) {
					distance = distance + 1;
				}

				System.out.print("(v: " + v.getValue() + " e: " + eVal + ")");

				return false;
			}
		});
		// System.out.println("MaxDepth= " + distance);
		return distance;

	}

	private int MinimumDepth(CATree cat) {

		// init analysis
		mxAnalysisGraph aGraph = new mxAnalysisGraph();
		aGraph.setGraph(cat);

		// apply bfs
		mxTraversal.bfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

			@Override
			public boolean visit(Object vertex, Object edge) {
				mxCell v = (mxCell) vertex;
				mxCell e = (mxCell) edge;

				if (e != null) {
					depth++;
					// System.out.println("Visit " + v.getValue());
					if (hasChild(v) == 0) {
						if (!printed) {
							printed = true;
							// System.out.println("Minimum depth = " + depth);
						}
					}

				}

				return false;
			}
		});
		printed = false;

		return depth;
	}

	private int MinExtent(CATree cat) {

		// init analysis
		mxAnalysisGraph aGraph = new mxAnalysisGraph();
		aGraph.setGraph(cat);

		// apply bfs
		mxTraversal.bfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

			@Override
			public boolean visit(Object vertex, Object edge) {
				mxCell v = (mxCell) vertex;
				mxCell e = (mxCell) edge;

				if (e != null) {
					// System.out.println("Visit " + v.getValue());
					int c = hasChild(v);
					if (c != 0) {
						if (c < miniumExtent) {
							miniumExtent = c;
						}
					}

				}

				return false;
			}
		});
		// System.out.println("minimum Extent : " + miniumExtent);
		return miniumExtent;

	}

	private int MaxExtent(CATree cat) {

		// init analysis
		mxAnalysisGraph aGraph = new mxAnalysisGraph();
		aGraph.setGraph(cat);

		// apply bfs
		mxTraversal.bfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

			@Override
			public boolean visit(Object vertex, Object edge) {
				mxCell v = (mxCell) vertex;
				mxCell e = (mxCell) edge;

				if (e != null) {
					// System.out.println("Visit " + v.getValue());
					int c = hasChild(v);

					if (c != 0) {

						if (c > maximumExtent) {
							maximumExtent = c;
						}
					}

				}

				return false;
			}
		});
		// System.out.println("maximum Extent : " + maximumExtent);
		return maximumExtent;

	}

	private static int hasChild(mxICell cell) {

		if (cell.getChildCount() == 0) {
			return 0;
		}
		int childs = 0;
		for (int i = 0; i < cell.getChildCount(); i++) {
			mxICell child = cell.getChildAt(i);
			if (child != null && child.isVertex()) {
				childs++;
			}
		}
		return childs;
	}

}
