package eu.fbk.das.adaptation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.ConfigurationException;
import javax.swing.UIManager;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.fbk.das.adaptation.api.CollectiveAdaptationEnsemble;
import eu.fbk.das.adaptation.api.CollectiveAdaptationProblem;
import eu.fbk.das.adaptation.api.CollectiveAdaptationRole;
import eu.fbk.das.adaptation.api.CollectiveAdaptationSolution;
import eu.fbk.das.adaptation.ensemble.Ensemble;
import eu.fbk.das.adaptation.ensemble.Issue;
import eu.fbk.das.adaptation.ensemble.Role;
import eu.fbk.das.adaptation.ensemble.Solution;
import eu.fbk.das.adaptation.ensemble.Solver;
import eu.fbk.das.adaptation.presentation.CATree;
import eu.fbk.das.multiagent.MultiagentPlannerCaller;

public class CollectiveMobility {
	private final static String REPO_PATH = "adaptation.properties";

	private static int idEnsembles = 0;
	private static int idRoles = 0;

	public static void main(String[] args) throws ConfigurationException,
			FileNotFoundException, JSONException {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {

		}

		System.gc();
		String propPath = REPO_PATH;
		if (args.length > 0) {
			propPath = args[0];
		}

		if (true) {
			// no random creation
			JsonArray plan = readJSONPlanDefaultFile();
			try {
				runEnsembles(plan);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Ensemble Creation using Randominit.py
			String scriptPath = "journey-planner/parser/";
			String worldPath = "Trento/";
			// create ensembles: 4 passengers and 2 car pools
			boolean created = false;
			while (!created) {
				created = createEnsembles(scriptPath, worldPath, 4, 2);
				// read the plan result and generates the Ensemble for the
				// execution
				if (created) {
					JsonArray plan = readJSONPlanFile();
					try {
						runEnsembles(plan);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (created) {
				System.out.println("creata");
			}
		}

	}

	// take role parameters from configTest.json
	private static JsonObject takeRoleParameters() {
		File currentFolder = new File(".");

		for (File f : currentFolder.listFiles()) {
			if (f.isFile()) {
				String fileName = f.getName();
				if (fileName.startsWith("configTest")
						&& fileName.endsWith(".json")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(
								fileName));
						JsonParser parser = new JsonParser();
						JsonObject object = parser.parse(br).getAsJsonObject();
						return object;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	private static void runEnsembles(JsonArray plan) throws JSONException,
			IOException {

		JsonObject elements = takeRoleParameters();

		// each passenger is assigned to the vehicle where it embarks
		// understand the number of ensembles to create
		ArrayList<String> ensembleNames = getEnsemblesNames(plan);
		List<CollectiveAdaptationEnsemble> ensemblesCAP = new ArrayList<CollectiveAdaptationEnsemble>();
		// create empty ensembles and assign its ID (name of the car pool
		// vehicle)
		for (int i = 0; i < ensembleNames.size(); i++) {
			CollectiveAdaptationEnsemble e = new CollectiveAdaptationEnsemble();
			e.setEnsembleName(ensembleNames.get(i));
			ensemblesCAP.add(e);
		}

		// System.out.println("Number of Ensembles: " + ensemblesCAP.size());

		// define the different Car Pool Rides ensembles
		for (int i = 0; i < plan.size(); i++) {
			JsonObject current = (JsonObject) plan.get(i);
			// Role Type
			JsonElement type = current.get("type");
			String typeValue = type.getAsString();
			// Role id
			JsonElement id = current.get("id");
			String idValue = id.getAsString();
			// System.out.println(idValue);

			// Plan Actions
			JsonElement events = current.get("events");
			JsonArray eventsValue = events.getAsJsonArray();
			for (int j = 0; j < eventsValue.size(); j++) {

				JsonObject eventCurrent = (JsonObject) eventsValue.get(j);

				// action
				JsonElement action = eventCurrent.get("action");
				String actionValue = action.getAsString();
				// System.out.println(actionValue);
				// duration
				JsonElement duration = eventCurrent.get("duration");
				String durationValue = duration.getAsString();
				// System.out.println(durationValue);

				if (!actionValue.equalsIgnoreCase("embark")
						&& !actionValue.equalsIgnoreCase("debark")) {

					// origin
					JsonElement origin = eventCurrent.get("origin");
					JsonArray originValue = origin.getAsJsonArray();
					Double originLat = originValue.get(0).getAsDouble();
					Double originLng = originValue.get(1).getAsDouble();
					// System.out.println(originValue);
					// destination
					JsonElement destination = eventCurrent.get("destination");
					JsonArray destinationValue = destination.getAsJsonArray();
					Double destinationLat = destinationValue.get(0)
							.getAsDouble();
					Double destinationLng = destinationValue.get(1)
							.getAsDouble();
					// System.out.println(destinationValue);
				} else {
					// vehicle to Join
					JsonElement vehicle = eventCurrent.get("vehicle");
					String vehicleValue = vehicle.getAsString();
					// System.out.println(vehicleValue);
					// addRoleToEnsemble(typeValue, idValue, vehicleValue,
					// ensemblesCAP);
					if (actionValue.equalsIgnoreCase("embark")) {
						// create the role
						CollectiveAdaptationRole role = new CollectiveAdaptationRole();
						role.setRole(idValue);
						// take Role parameter: origin, destination.

						// aggiungi all'ensemble giusta
						CollectiveAdaptationEnsemble ensemble = getRightEnsemble(
								ensemblesCAP, vehicleValue);
						// add passenger to the respective ensemble
						ensemble.addRole(role);

						// add the vehicle in the same ensemble if does not
						// already exist
						if (!alreadyInEnsemble(vehicleValue, ensemble)) {
							CollectiveAdaptationRole role1 = new CollectiveAdaptationRole();
							role1.setRole(vehicleValue);
							ensemble.addRole(role1);
						}

					}

				}

			}

		}

		// add the Car Pool Manager Role to each Car Pool Ensemble
		// and define the CarPool Company Ensembles with the Car Pool Manager
		// CollectiveAdaptationEnsemble cpc = new
		// CollectiveAdaptationEnsemble();
		// cpc.setEnsembleName("CPC");

		// add the CP Managers to the ensemble CP1 and CP2

		for (int i = 0; i < ensemblesCAP.size(); i++) {

			// Define the Role Car Pool Manager
			int j = i + 1;
			String idValue = "CPM" + j;
			CollectiveAdaptationRole role = new CollectiveAdaptationRole();
			role.setRole(idValue);
			if (ensemblesCAP.get(i).getEnsembleName().contains("c")) {
				ensemblesCAP.get(i).addRole(role);
				// cpc.addRole(role);
			}

		}

		// ensemblesCAP.add(cpc);

		System.out.println("Number of Ensembles: " + ensemblesCAP.size());

		// add the Car Pool Managers Role to

		for (int i = 0; i < ensemblesCAP.size(); i++) {
			System.out.println("Number of Roles: "
					+ ensemblesCAP.get(i).getEnsembleName() + "-"
					+ ensemblesCAP.get(i).getRoles().size());
		}

		// List<Treatment> treatments = createTreatmentMobility();

		// Problem definition
		CollectiveAdaptationProblem cap = new CollectiveAdaptationProblem(
				"CAP_1", ensemblesCAP, null, null, ensemblesCAP.get(0)
						.getEnsembleName(), null);

		/*
		 * DemoManagementSystem dms = null; try { dms = DemoManagementSystem
		 * .initializeSystem("scenario/Mobility/SASO2017/"); } catch
		 * (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		// create Ensemble Managers
		List<EnsembleManager> ensembles = new ArrayList<EnsembleManager>();
		for (int i = 0; i < ensemblesCAP.size(); i++) {
			CollectiveAdaptationEnsemble current = ensemblesCAP.get(i);

			ArrayList<RoleManager> rManagers = new ArrayList<RoleManager>();
			// creazione dell'ensemble con ruoli e relativi managers
			// qui anche i solver devo associare ai ruoli
			for (int j = 0; j < current.getRoles().size(); j++) {
				CollectiveAdaptationRole cr = current.getRoles().get(j);
				Role r = new Role();
				r.setType(cr.getRole());
				r.setId(cr.getRole());

				// System.out.println("RUOLO DA GESTIRE: " + r.getType());

				RoleManager rm = new RoleManager(r);
				// add solvers able to solve a specific Issue
				List<Solver> solverInstances = new ArrayList<Solver>();
				if (r.getType().matches("(CPM).*")) {
					// CAR POOL MANAGERS
					// define solvers for this specific role type
					// SOLVER
					// init
					List<Issue> triggeredIssues1 = new ArrayList<Issue>();
					List<Solution> solutions1 = new ArrayList<Solution>();
					// define solvers for this specific role type
					Solver s1 = new Solver();
					s1.setName("SolveRouteBlocked");
					// issue to manage
					Issue is1 = new Issue();
					is1.setIssueType("AskToManagerRePlan");
					s1.setIssue(is1);
					// System.out.println("Issue che risolve: "
					// + "AskToManagerRePlan");

					// solution for the specific issue - issues to trigger
					// outside

					Solution solution1 = new Solution();
					solution1.setName("CollectivePlanIntraEnsemble");
					solution1.setInternalSolution("GenerateCollectivePlan");

					// create a triggered issue for each role (not CPM) in the
					// same ensemble
					for (int k = 0; k < current.getRoles().size(); k++) {
						String roleType = current.getRoles().get(k).getRole();

						if (((roleType.matches("(p).*")) || (roleType
								.matches("(c).*")))
								&& (!roleType.matches("(CPM).*"))) {
							Issue generatedIssue1 = new Issue();
							generatedIssue1.setIssueType("Ask" + roleType
									+ "ToAdaptCollectively");

							triggeredIssues1.add(generatedIssue1);
							solution1.setIssue(triggeredIssues1);
							solutions1.add(solution1);
							// System.out.println("Issue che genera: " + "Ask"
							// + roleType + "ToAdaptCollectively");
						}
					}

					s1.setSolution(solutions1);

					// add solver to list of solvers
					solverInstances.add(s1);

					// set the solver to the role manager
					rm.setSolverInstances(solverInstances);
					// add the role manager at the list
					rManagers.add(rm);

				} else if (r.getType().matches("(p).*")) {
					// PEDESTRIANS
					// define solvers for this specific role type
					// SOLVER S1
					// init

					List<Issue> triggeredIssues1 = new ArrayList<Issue>();
					List<Solution> solutions1 = new ArrayList<Solution>();
					// define solvers for this specific role type
					Solver s1 = new Solver();
					s1.setName("ApplyCollectivePlan");
					// issue to manage
					Issue is1 = new Issue();
					String type1 = "Ask" + r.getType() + "ToAdaptCollectively";
					is1.setIssueType(type1);
					s1.setIssue(is1);
					// System.out.println("Issue che risolve: " + "Ask"
					// + r.getType() + "ToAdaptCollectively");

					// solution for the specific issue - issues to trigger
					// outside
					Solution solution1 = new Solution();
					solution1.setName("AdaptCollectively");
					solution1.setInternalSolution("ExecuteCollectivePlan");
					solutions1.add(solution1);
					s1.setSolution(solutions1);
					// System.out.println("Issue che genera: "
					// + "ExecuteCollectivePlan");
					// end setting solutions

					// add solver to list of solvers
					solverInstances.add(s1);

					// SOLVER S2 - Trigger Blocked Street
					// SOLVER
					// init
					List<Issue> triggeredIssues2 = new ArrayList<Issue>();
					List<Solution> solutions2 = new ArrayList<Solution>();
					// define solvers for this specific role type
					Solver s2 = new Solver();
					s2.setName("AskCPManager");
					// issue to manage
					Issue is2 = new Issue();
					is2.setIssueType("BlockedStreet");
					s2.setIssue(is2);
					// System.out.println("Issue che risolve: " +
					// "BlockedStreet");

					// solution for the specific issue - issues to trigger
					// outside
					Solution solution2 = new Solution();
					solution2.setName("AskCPManager");
					Issue generatedIssue2 = new Issue();
					generatedIssue2.setIssueType("AskToManagerRePlan");
					triggeredIssues2.add(generatedIssue2);
					solution2.setIssue(triggeredIssues2);
					solutions2.add(solution2);
					s2.setSolution(solutions2);
					// System.out.println("Issue che genera: "
					// / + "AskToManagerRePlan");
					// end setting solutions

					// add solver to list of solvers
					solverInstances.add(s2);

					// set the solver to the role manager
					rm.setSolverInstances(solverInstances);
					// add the role manager at the list
					rManagers.add(rm);

				} else if (r.getType().matches("(c).*")) {
					// CAR POOL DRIVER/CAR
					// SOLVER
					// init
					List<Issue> triggeredIssues = new ArrayList<Issue>();
					List<Solution> solutions = new ArrayList<Solution>();
					// define solvers for this specific role type
					Solver s = new Solver();
					s.setName("AskCPManager");
					// issue to manage
					Issue is = new Issue();
					is.setIssueType("BlockedStreet");
					s.setIssue(is);
					// System.out.println("Issue che risolve: " +
					// "BlockedStreet");

					// solution for the specific issue - issues to trigger
					// outside
					Solution solution = new Solution();
					solution.setName("AskCPManager");
					Issue generatedIssue = new Issue();
					generatedIssue.setIssueType("AskToManagerRePlan");
					triggeredIssues.add(generatedIssue);
					solution.setIssue(triggeredIssues);
					solutions.add(solution);
					s.setSolution(solutions);
					// System.out.println("Issue che genera: "
					// + "AskToManagerRePlan");
					// end setting solutions

					// add solver to list of solvers
					solverInstances.add(s);

					// SECOND SOLVER

					List<Issue> triggeredIssues2 = new ArrayList<Issue>();
					List<Solution> solutions2 = new ArrayList<Solution>();
					// define solvers for this specific role type
					Solver s2 = new Solver();
					s2.setName("ApplyCollectivePlan");
					// issue to manage

					Issue is2 = new Issue();
					is2.setIssueType("Ask" + r.getType()
							+ "ToAdaptCollectively");
					s2.setIssue(is2);

					// solution for the specific issue - issues to trigger
					// outside
					Solution solution2 = new Solution();
					solution2.setName("AdaptCollectively");
					solution2.setInternalSolution("ExecuteCollectivePlan");
					solutions2.add(solution2);
					s2.setSolution(solutions2);

					// add solver to list of solvers
					solverInstances.add(s2);

					// set the solver to the role manager
					rm.setSolverInstances(solverInstances);
					// add the role manager at the list
					rManagers.add(rm);

				}

			}
			Ensemble e = new Ensemble();
			e.setName(current.getEnsembleName());
			EnsembleManager manager = new EnsembleManager(e);
			manager.setRolesManagers(rManagers);
			ensembles.add(manager);

			// add roles to the ensemble
			List<Role> roles = new ArrayList<Role>();
			for (int j = 0; j < rManagers.size(); j++) {
				RoleManager currentRM = rManagers.get(j);
				Role role = rManagers.get(j).getRole();
				role.setSolver(currentRM.getSolverInstances());
				roles.add(role);
			}
			e.setRole(roles);

			// for each role manager of the ensemble we add its reference
			for (int j = 0; j < manager.getRolesManagers().size(); j++) {
				RoleManager rm = manager.getRolesManagers().get(j);
				rm.setEnsemble(manager);
			}

		}

		/*
		 * ensembles.add(e1Manager); ensembles.add(e2Manager);
		 * ensembles.add(e3Manager); ensembles.add(e4Manager);
		 * ensembles.add(e5Manager); ensembles.add(e6Manager);
		 * ensembles.add(e7Manager);
		 */

		// While cycle to read the trigger in the server and run
		// the issue resolution

		JSONObject json = readJsonFromUrl("http://178.239.178.239:8080/adaptations.json");
		JSONObject element = json.getJSONObject("adaptations");
		JSONObject agents = element.getJSONObject("agents");
		JSONArray blockedStreet = element.getJSONArray("blocked_streets");

		JSONObject street = (JSONObject) blockedStreet.get(0);
		String agentThatTrigger = street.getString("nearest_agent");
		System.out.println("Issue Triggered by agent: " + agentThatTrigger);
		RoleManager r = Utilities.pickRole(ensembles, agentThatTrigger);

		// run issue resolution
		// Treatment treatment = treatments.get(0);
		CollectiveAdaptationSolution solution1 = null;
		// for (int t = 0; t < treatment.getIssues().size(); t++) {

		int countIndex = 0;

		// RANDOM GENERATION OF ISSUE
		// Issue issue = treatment.getIssues().get(t);
		// MANUAL DEFINTION OF THE ISSUE
		Issue issue = new Issue();
		// issue.setIssueType("NewCarPool");
		issue.setIssueType("BlockedStreet");
		System.out.println("ISSUE TRIGGERED: " + issue.getIssueType());

		RoleManager r1 = Utilities.pickRoleRandom(ensembles);

		// System.out.println("Role that triggers the issue: "
		// + r.getRole().getType());

		// IssueResolution resolution1 = new IssueResolution(1,
		// "ISSUE_TRIGGERED", r, r, issue, null);
		// r.addIssueResolution(resolution1);

		DemonstratorAnalyzer demo = new DemonstratorAnalyzer();
		int crossEnsembles = 0;

		// input graph definition
		CATree cat = new CATree();
		// System.out.println(cat.getNodesHierarchy().size());
		DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		DefaultEdge startEdge = null;
		HashMap<CATree, Integer> solution = new HashMap<CATree, Integer>();

		HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult = new HashMap<RoleManager, HashMap<String, ArrayList<Integer>>>();
		// CAWindow window = new CAWindow(ensembles, cap, 0, null);
		// 1. CollectiveAdaptationProblem cap,
		// 2. List<EnsembleManager> ensembles,
		// 3. CAWindow window,
		// 4. String issueName,
		// 5. String capID,
		// 6. String startingRole,
		// 7. int issueIndex,
		// 8. HashMap<RoleManager, HashMap<String, ArrayList<Integer>>>
		// GlobalResult,
		// 9. CATree cat, DirectedGraph<String, DefaultEdge> graph,
		// 10. DefaultEdge lastEdge
		// 11. int crossEnsembles

		solution1 = demo.run(cap, ensembles, null, issue.getIssueType(),
				cap.getCapID(), r.getRole().getType(), 0, GlobalResult, cat,
				graph, startEdge, crossEnsembles);

		// }

		List<String> rolesInvolved = new ArrayList<String>();

		// RETRIEVE ALL COMMANDS FOR INVOLVED ROLES IN a CAP
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager emx = ensembles.get(i);
			System.out.println("******ENSEMBLE: " + emx.getEnsemble().getName()
					+ "********ROLES INVOLVED********");
			for (int j = 0; j < emx.getRolesManagers().size(); j++) {
				RoleManager rm = emx.getRolesManagers().get(j);

				if (rm.getRoleCommands() != null) {
					rolesInvolved.add(rm.getRole().getType());
					System.out.println(rm.getRole().getType());

				}

			}

		}

		// define the passengers involved as json object
		JsonArray passengersInvolved = new JsonArray();
		JsonArray passengers = elements.getAsJsonArray("pedestrians");
		for (int j = 0; j < rolesInvolved.size(); j++) {
			String roleToAdd = rolesInvolved.get(j);
			for (int i = 0; i < passengers.size(); i++) {

				JsonObject current = (JsonObject) passengers.get(i);
				String passengerID = current.get("id").getAsString();

				if (roleToAdd.equalsIgnoreCase(passengerID)) {
					passengersInvolved.add(current);
				}
			}
		}

		System.out.println("PAssengers Involved: " + passengersInvolved);

		// define the carpools involved as json object
		JsonArray carpoolsInvolved = new JsonArray();
		JsonArray carpools = elements.getAsJsonArray("carpools");
		for (int j = 0; j < rolesInvolved.size(); j++) {
			String roleToAdd = rolesInvolved.get(j);
			for (int i = 0; i < carpools.size(); i++) {

				JsonObject current = (JsonObject) carpools.get(i);
				String carpoolID = current.get("id").getAsString();

				if (roleToAdd.equalsIgnoreCase(carpoolID)) {
					carpoolsInvolved.add(current);
				}
			}
		}

		System.out.println("CarPools Involved: " + carpoolsInvolved);

		// /// DEFINE THE CONFIG FILE TO USE PLANNING
		JsonObject jsonObject = new JsonObject();

		// properties

		jsonObject.addProperty("map_path", "trento/Trento.world");
		JsonObject boundaries = new JsonObject();

		boundaries.addProperty("min_latitude", 46.0643);
		boundaries.addProperty("max_latitude", 46.0715);
		boundaries.addProperty("min_longitude", 11.1164);
		boundaries.addProperty("max_longitude", 11.1272);
		jsonObject.add("map_boundaries", boundaries);

		jsonObject.add("pedestrians", passengersInvolved);
		jsonObject.add("carpools", carpoolsInvolved);

		// try-with-resources statement based on post comment below :)
		try (FileWriter file = new FileWriter("configTest1.json")) {
			file.write(jsonObject.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + jsonObject);
		}

		// /////////////////// call the planner /////////////////////
		// call the AI planner to find a plan to solve the issue with the
		// respective agents
		// involved

		String scriptPath = "/Users/amministratore/git/CAS-ICSOC2016/journey-planner/parser/";
		String configPath = "/Users/amministratore/git/CAS-ICSOC2016/configTest1.json";

		// 120 (30) seconds to solve this problem
		JsonArray resArray = MultiagentPlannerCaller.plan(scriptPath,
				configPath, 30, 4096);
		if (resArray == null) {
			System.out.println("NO PLAN");

		} else {
			System.out.println(resArray.toString());
			System.out.println("PLAN CREATED");
		}

		// ////////////////// END - Call the Planner ////////////////

		// save the JSON file in the server to be visualized by the Python
		// visualizer
		// ?? chiedere A Daniel come visualizzare il nuovo piano.

		System.out.println("END SIMULATION");
		System.exit(1);

		// END OLD CODE

		// OLD CODE
		/*
		 * 
		 * List<CollectiveAdaptationRole> rolesRouteA = new
		 * ArrayList<CollectiveAdaptationRole>();
		 * 
		 * CollectiveAdaptationRole p1 = new CollectiveAdaptationRole();
		 * p1.setRole("RoutePassenger_33");
		 * 
		 * CollectiveAdaptationRole p2 = new CollectiveAdaptationRole();
		 * p2.setRole("RoutePassenger_30");
		 * 
		 * CollectiveAdaptationRole p3 = new CollectiveAdaptationRole();
		 * p3.setRole("RoutePassenger_36");
		 * 
		 * CollectiveAdaptationRole p4 = new CollectiveAdaptationRole();
		 * p3.setRole("RouteManagement_1");
		 * 
		 * CollectiveAdaptationRole p5 = new CollectiveAdaptationRole();
		 * p3.setRole("FlexibusDriver_13");
		 * 
		 * rolesRouteA.add(p1); rolesRouteA.add(p2); rolesRouteA.add(p3);
		 * rolesRouteA.add(p4); rolesRouteA.add(p5);
		 * 
		 * // ROUTE B List<CollectiveAdaptationRole> rolesRouteB = new
		 * ArrayList<CollectiveAdaptationRole>(); CollectiveAdaptationRole p6 =
		 * new CollectiveAdaptationRole(); p6.setRole("RoutePassenger_64");
		 * 
		 * CollectiveAdaptationRole p7 = new CollectiveAdaptationRole();
		 * p7.setRole("RoutePassenger_69");
		 * 
		 * CollectiveAdaptationRole p9 = new CollectiveAdaptationRole();
		 * p9.setRole("RoutePassenger_74");
		 * 
		 * CollectiveAdaptationRole p10 = new CollectiveAdaptationRole();
		 * p10.setRole("RouteManagement_2");
		 * 
		 * CollectiveAdaptationRole p11 = new CollectiveAdaptationRole();
		 * p11.setRole("FlexibusDriver_28");
		 * 
		 * rolesRouteB.add(p6); rolesRouteB.add(p7); rolesRouteB.add(p9);
		 * rolesRouteB.add(p10); rolesRouteB.add(p11);
		 * 
		 * // FlexiBusMngmt List<CollectiveAdaptationRole> rolesFBMngmt = new
		 * ArrayList<CollectiveAdaptationRole>(); CollectiveAdaptationRole p12 =
		 * new CollectiveAdaptationRole(); p12.setRole("RouteManagement_1");
		 * 
		 * CollectiveAdaptationRole p13 = new CollectiveAdaptationRole();
		 * p13.setRole("RouteManagement_2");
		 * 
		 * CollectiveAdaptationRole p14 = new CollectiveAdaptationRole();
		 * p14.setRole("FBCManager");
		 * 
		 * rolesRouteB.add(p12); rolesRouteB.add(p13); rolesRouteB.add(p14);
		 * 
		 * // CP Ride A List<CollectiveAdaptationRole> rolesCPRideA = new
		 * ArrayList<CollectiveAdaptationRole>(); CollectiveAdaptationRole p15 =
		 * new CollectiveAdaptationRole(); p15.setRole("CPDriver_A");
		 * 
		 * CollectiveAdaptationRole p16 = new CollectiveAdaptationRole();
		 * p16.setRole("CPPassenger_1");
		 * 
		 * rolesCPRideA.add(p15); rolesCPRideA.add(p16);
		 * 
		 * // CP Ride B List<CollectiveAdaptationRole> rolesCPRideB = new
		 * ArrayList<CollectiveAdaptationRole>(); CollectiveAdaptationRole p17 =
		 * new CollectiveAdaptationRole(); p17.setRole("CPDriver_B");
		 * 
		 * CollectiveAdaptationRole p18 = new CollectiveAdaptationRole();
		 * p18.setRole("CPPassenger_3");
		 * 
		 * rolesCPRideB.add(p17); rolesCPRideB.add(p18);
		 * 
		 * // CP Company List<CollectiveAdaptationRole> rolesCPCompany = new
		 * ArrayList<CollectiveAdaptationRole>(); CollectiveAdaptationRole p19 =
		 * new CollectiveAdaptationRole(); p19.setRole("CPDriver_A");
		 * 
		 * CollectiveAdaptationRole p20 = new CollectiveAdaptationRole();
		 * p20.setRole("CPDriver_B");
		 * 
		 * CollectiveAdaptationRole p21 = new CollectiveAdaptationRole();
		 * p21.setRole("CPManager");
		 * 
		 * rolesCPCompany.add(p19); rolesCPCompany.add(p20);
		 * rolesCPCompany.add(p21);
		 * 
		 * // UMS List<CollectiveAdaptationRole> rolesUMS = new
		 * ArrayList<CollectiveAdaptationRole>(); CollectiveAdaptationRole p22 =
		 * new CollectiveAdaptationRole(); p22.setRole("CPManager");
		 * 
		 * CollectiveAdaptationRole p23 = new CollectiveAdaptationRole();
		 * p23.setRole("FBC");
		 * 
		 * rolesUMS.add(p22); rolesUMS.add(p23);
		 * 
		 * List<CollectiveAdaptationEnsemble> ensemblesCAP = new
		 * ArrayList<CollectiveAdaptationEnsemble>(); ensemblesCAP .add(new
		 * CollectiveAdaptationEnsemble("RouteA", rolesRouteA)); ensemblesCAP
		 * .add(new CollectiveAdaptationEnsemble("RouteB", rolesRouteB));
		 * ensemblesCAP.add(new CollectiveAdaptationEnsemble("FlexiBusMngmt",
		 * rolesFBMngmt)); ensemblesCAP.add(new
		 * CollectiveAdaptationEnsemble("CPRideA", rolesCPRideA));
		 * ensemblesCAP.add(new CollectiveAdaptationEnsemble("CPRideB",
		 * rolesCPRideB)); ensemblesCAP.add(new
		 * CollectiveAdaptationEnsemble("CPCompany", rolesCPCompany));
		 * ensemblesCAP.add(new CollectiveAdaptationEnsemble("UMS", rolesUMS));
		 * 
		 * CollectiveAdaptationProblem cap = new CollectiveAdaptationProblem(
		 * "CAP_1", ensemblesCAP, null, null, ensemblesCAP.get(1)
		 * .getEnsembleName(), null);
		 * 
		 * DemoManagementSystem dms = DemoManagementSystem
		 * .initializeSystem("scenario/Mobility/"); List<Treatment> treatments =
		 * createTreatmentMobility();
		 * 
		 * // Ensemble Creation - Instance of Ensemble 1 Ensemble e1 =
		 * dms.getEnsemble("RouteA", cap); EnsembleManager e1Manager = new
		 * EnsembleManager(e1);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 2 Ensemble e2 =
		 * dms.getEnsemble("RouteB", cap); EnsembleManager e2Manager = new
		 * EnsembleManager(e2);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 3 Ensemble e3 =
		 * dms.getEnsemble("FlexiBusMngmt", cap); EnsembleManager e3Manager =
		 * new EnsembleManager(e3);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 4 Ensemble e4 =
		 * dms.getEnsemble("CPRideA", cap); EnsembleManager e4Manager = new
		 * EnsembleManager(e4);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 5 Ensemble e5 =
		 * dms.getEnsemble("CPRideB", cap); EnsembleManager e5Manager = new
		 * EnsembleManager(e5);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 6 Ensemble e6 =
		 * dms.getEnsemble("CPCompany", cap); EnsembleManager e6Manager = new
		 * EnsembleManager(e6);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 7 Ensemble e7 =
		 * dms.getEnsemble("UMS", cap); EnsembleManager e7Manager = new
		 * EnsembleManager(e7);
		 * 
		 * List<EnsembleManager> ensembles = new ArrayList<EnsembleManager>();
		 * ensembles.add(e1Manager); ensembles.add(e2Manager);
		 * ensembles.add(e3Manager); ensembles.add(e4Manager);
		 * ensembles.add(e5Manager); ensembles.add(e6Manager);
		 * ensembles.add(e7Manager);
		 * 
		 * Utilities.buildSolversMapMobility(ensembles);
		 * 
		 * System.gc(); try {
		 * System.out.println("Experiment starting in 5 seconds...");
		 * Thread.sleep(5000); } catch (InterruptedException e) {
		 * e.printStackTrace(); } runTreatments(cap, treatments, ensembles,
		 * "Mobility");
		 * 
		 * // remove the first element of the treatments list
		 * treatments.remove(0); Utilities.genericWriteFile(treatments,
		 * "treatmentsMobility.csv"); System.out.println("END SIMULATION");
		 * System.exit(1);
		 */
		// END OLD CODE

	}

	private static String getStartPositionPassenger(JsonObject elements,
			String index) {
		String result = null;
		JsonArray passengers = elements.getAsJsonArray("pedestrians");
		for (int i = 0; i < passengers.size(); i++) {
			JsonObject current = (JsonObject) passengers.get(i);
			String passengerID = current.get("id").getAsString();
			String startPosition = current.get("init_pos").getAsString();

			if (index.equalsIgnoreCase(passengerID)) {

				result = startPosition;
				break;

			}

		}

		return result;

	}

	private JsonObject getPassenger(JsonObject elements, String index) {
		JsonObject passenger = null;
		JsonArray passengers = elements.getAsJsonArray("pedestrians");
		for (int i = 0; i < passengers.size(); i++) {
			JsonObject current = (JsonObject) passengers.get(i);
			String passengerID = current.get("id").getAsString();

			if (index.equalsIgnoreCase(passengerID)) {

				passenger = current;
				break;

			}

		}
		return passenger;
	}

	private JsonObject getVehicle(JsonObject elements, String index) {
		JsonObject car = null;
		JsonArray passengers = elements.getAsJsonArray("carpools");
		for (int i = 0; i < passengers.size(); i++) {
			JsonObject current = (JsonObject) passengers.get(i);
			String carID = current.get("id").getAsString();

			if (index.equalsIgnoreCase(carID)) {

				car = current;
				break;

			}

		}
		return car;
	}

	private static String getTargetPositionPassenger(JsonObject elements,
			String index) {
		String result = null;
		JsonArray passengers = elements.getAsJsonArray("pedestrians");
		for (int i = 0; i < passengers.size(); i++) {
			JsonObject current = (JsonObject) passengers.get(i);
			String passengerID = current.get("id").getAsString();
			String targetPosition = current.get("target_pos").getAsString();

			if (index.equalsIgnoreCase(passengerID)) {

				result = targetPosition;
				break;

			}

		}

		return result;

	}

	private static String getTargetPositionCar(JsonObject elements, String index) {
		String result = null;
		JsonArray cars = elements.getAsJsonArray("carpools");
		for (int i = 0; i < cars.size(); i++) {
			JsonObject current = (JsonObject) cars.get(i);
			String carID = current.get("id").getAsString();
			String targetPosition = current.get("target_pos").getAsString();

			if (index.equalsIgnoreCase(carID)) {

				result = targetPosition;
				break;

			}

		}

		return result;

	}

	private static String getStartingPositionCar(JsonObject elements,
			String index) {
		String result = null;
		JsonArray cars = elements.getAsJsonArray("carpools");
		for (int i = 0; i < cars.size(); i++) {
			JsonObject current = (JsonObject) cars.get(i);
			String carID = current.get("id").getAsString();
			String startPosition = current.get("init_pos").getAsString();

			if (index.equalsIgnoreCase(carID)) {

				result = startPosition;
				break;

			}

		}

		return result;

	}

	private static String getWalkingRangePassenger(JsonObject elements,
			String index) {
		String result = null;
		JsonArray passengers = elements.getAsJsonArray("pedestrians");
		for (int i = 0; i < passengers.size(); i++) {
			JsonObject current = (JsonObject) passengers.get(i);
			String passengerID = current.get("id").getAsString();
			String walkRange = current.get("walk_range").getAsString();

			if (index.equalsIgnoreCase(passengerID)) {

				result = walkRange;
				break;

			}

		}

		return result;

	}

	private static boolean alreadyInEnsemble(String vehicleValue,
			CollectiveAdaptationEnsemble ensemble) {
		boolean result = false;
		for (int i = 0; i < ensemble.getRoles().size(); i++) {
			String roleName = ensemble.getRoles().get(i).getRole();
			if (roleName.equalsIgnoreCase(vehicleValue)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private static CollectiveAdaptationEnsemble getRightEnsemble(
			List<CollectiveAdaptationEnsemble> ensemblesCAP, String idValue) {
		CollectiveAdaptationEnsemble result = null;
		for (int i = 0; i < ensemblesCAP.size(); i++) {
			CollectiveAdaptationEnsemble current = ensemblesCAP.get(i);
			if (current.getEnsembleName().equalsIgnoreCase(idValue)) {
				result = current;
				break;
			}
		}
		return result;
	}

	private static ArrayList<String> getEnsemblesNames(JsonArray plan) {
		ArrayList<String> values = new ArrayList<String>();
		for (int i = 0; i < plan.size(); i++) {
			JsonObject current = (JsonObject) plan.get(i);
			// Role Type
			JsonElement type = current.get("type");
			String typeValue = type.getAsString();
			if (typeValue.equalsIgnoreCase("vehicle")) {
				// Role id
				JsonElement id = current.get("id");
				String idValue = id.getAsString();
				values.add(idValue);

			}

		}

		return values;

	}

	private static JsonArray readJSONPlanFile() {
		File currentFolder = new File(".");

		for (File f : currentFolder.listFiles()) {
			if (f.isFile()) {
				String fileName = f.getName();
				if (fileName.startsWith("tmp_sas_plan")
						&& fileName.endsWith(".json")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(
								fileName));
						JsonParser parser = new JsonParser();
						JsonArray array = parser.parse(br).getAsJsonArray();
						return array;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	private static JsonArray readJSONPlanDefaultFile() {
		File currentFolder = new File(".");

		for (File f : currentFolder.listFiles()) {
			if (f.isFile()) {
				String fileName = f.getName();
				if (fileName.startsWith("tmp_sas_plan_default")
						&& fileName.endsWith(".json")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(
								fileName));
						JsonParser parser = new JsonParser();
						JsonArray array = parser.parse(br).getAsJsonArray();
						return array;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	private static boolean createEnsembles(String scriptPath, String worldPath,
			int ensembles, int passengers) {

		boolean result = false;
		// python parser/randominit.py trento/Trento.world 4 2 46.0643 46.0715
		// 11.1164 11.1272 0 100 (min and max walking distance)

		String planCmd = "python " + scriptPath + "randominit.py " + worldPath
				+ "Trento.world " + ensembles + " " + passengers + " "
				+ "46.0643 46.0715 11.1164 11.1272 0 100";
		System.out.println(planCmd);
		try {
			// create random_init.json file with the ensembles configuration
			Process p = Runtime.getRuntime().exec(planCmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// read the init json file and create the respective ensembles
		result = readJSONStartFile();
		return result;

	}

	private static boolean readJSONStartFile() {
		boolean result = false;
		File currentFolder = new File(".");
		System.out.println(currentFolder.getAbsolutePath());
		for (File f : currentFolder.listFiles()) {
			if (f.isFile()) {
				String fileName = f.getName();

				if (fileName.startsWith("random_init")
						&& fileName.endsWith(".json")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(
								fileName));
						JsonParser parser = new JsonParser();
						JsonObject object = parser.parse(br).getAsJsonObject();
						JsonArray passengers = object
								.getAsJsonArray("pedestrians");
						JsonArray carPools = object.getAsJsonArray("carpools");

						// generate the config.json file and create the first
						// plan
						// it will create the different car pools and passengers
						// with the respective paths and goals
						result = startConfiguration(passengers, carPools);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	// method to create the FIRST PLAN for the first set of car pool
	// rides and their respective passengers
	private static boolean startConfiguration(JsonArray passengers,
			JsonArray carPools) {
		boolean result = false;
		// create the config.json
		JsonObject config = new JsonObject();
		// add a property call title to the albums object
		config.addProperty("map_path", "trento/Trento.world");

		// create the boundaries
		JsonObject boundaries = new JsonObject();
		boundaries.addProperty("min_latitude", 46.0643);
		boundaries.addProperty("max_latitude", 46.0715);
		boundaries.addProperty("min_longitude", 11.1164);
		boundaries.addProperty("max_longitude", 11.1272);
		config.add("map_boundaries", boundaries);

		// create the passengers from the input array
		config.add("pedestrians", passengers);

		// create the car pools from the input array
		config.add("carpools", carPools);

		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.create();
		System.out.println(gson.toJson(config));

		String json = gson.toJson(config);

		try {
			// write converted json data to a file named "config.json"
			FileWriter writer = new FileWriter("configTest.json");
			writer.write(json);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("JSON Object Successfully written to the file!!");

		// call the AI planner to create ensembles (2 car pools with the
		// respective passengers)
		String scriptPath = "/Users/amministratore/git/CAS-ICSOC2016/journey-planner/parser/";
		String configPath = "/Users/amministratore/git/CAS-ICSOC2016/configTest.json";

		// 120 seconds to solve this problem
		JsonArray resArray = MultiagentPlannerCaller.plan(scriptPath,
				configPath, 120, 4096);
		if (resArray == null) {
			System.out.println("NO PLAN");
			result = false;
		} else {
			System.out.println(resArray.toString());
			result = true;
		}

		return result;

	}

	public Ensemble getEnsemble(String type, CollectiveAdaptationProblem cap) {
		Ensemble ei = null;
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();

		if (ensembleInstances == null) {
			ensembleInstances = new ArrayList<Ensemble>();
		}

		File dir = new File(REPO_PATH);
		if (!dir.isDirectory()) {
			throw new NullPointerException(
					"Impossibile to load the ensemble type, mainDir not found "
							+ dir);
		}
		File f = new File(REPO_PATH + File.separator + type + ".xml");

		// retrieve the type from file
		EnsembleParser parser = new EnsembleParser();
		ei = parser.parseEnsemble(f);

		ensembleInstances.add(ei);

		return ei;
	}

	private ArrayList<Ensemble> ensembleInstances;

	public ArrayList<Ensemble> getEnsembleInstances() {

		if (ensembleInstances == null) {
			ensembleInstances = new ArrayList<Ensemble>();
			return ensembleInstances;
		} else {
			return ensembleInstances;
		}
	}

	public void setEnsembleInstances(ArrayList<Ensemble> ensembleInstances) {
		this.ensembleInstances = ensembleInstances;
	}

	public Ensemble getEnsembleInstance(String type,
			CollectiveAdaptationProblem cap) {
		Ensemble en = null;

		if (ensembleInstances == null) {
			ensembleInstances = new ArrayList<Ensemble>();

			en = this.getEnsemble(type, cap);

			idEnsembles++;
			ensembleInstances.add(en);

		} else {

			en = this.getEnsemble(type, cap);

			idEnsembles++;
			ensembleInstances.add(en);
		}

		return en;
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}
