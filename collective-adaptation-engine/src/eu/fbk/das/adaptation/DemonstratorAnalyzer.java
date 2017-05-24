package eu.fbk.das.adaptation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.jxmapviewer.viewer.GeoPosition;

import eu.fbk.das.adaptation.api.CollectiveAdaptationCommandExecution;
import eu.fbk.das.adaptation.api.CollectiveAdaptationEnsemble;
import eu.fbk.das.adaptation.api.CollectiveAdaptationInterface;
import eu.fbk.das.adaptation.api.CollectiveAdaptationProblem;
import eu.fbk.das.adaptation.api.CollectiveAdaptationSolution;
import eu.fbk.das.adaptation.api.RoleCommand;
import eu.fbk.das.adaptation.ensemble.Ensemble;
import eu.fbk.das.adaptation.ensemble.Issue;
import eu.fbk.das.adaptation.model.IssueCommunication;
import eu.fbk.das.adaptation.model.IssueResolution;
import eu.fbk.das.adaptation.presentation.CATree;
import eu.fbk.das.adaptation.presentation.CAWindow;
import eu.fbk.das.adaptation.utils.GoogleAPIWrapper;
import eu.fbk.das.adaptation.utils.Leg;
import eu.fbk.das.adaptation.utils.TripAlternative;
import eu.fbk.das.adaptation.utils.ViaggiaTrentoAPIWrapper;

public class DemonstratorAnalyzer implements CollectiveAdaptationInterface {

	private final static String PROP_PATH = "adaptation.properties";
	private static final String STYLE_INIT = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;fillColor=white;size=2";
	private static final String STYLE_ROLE = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;align=center;fontSize=9;";

	private static final String STYLE_ISSUE_EDGE = "fontColor=#FF0000;fontSize=8;endArrow=classic;html=1;fontFamily=Helvetica;align=left;";
	private static final Object CSV_SEPARATOR = ",";

	// private final static String PreferencesDir =
	// "scenario/ALLOWEnsembles/Preferences/";

	private CollectiveAdaptationCommandExecution executor;

	@Override
	public void executeCapNew(CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor) {
		this.executor = executor;

		Properties props = new Properties();
		try {
			props.load(getClass().getClassLoader().getResourceAsStream(
					PROP_PATH));
		} catch (FileNotFoundException e) {
			System.out.println("Error loading file " + e.getMessage());

			throw new NullPointerException(e.getMessage());

		} catch (IOException e) {
			System.out.println("Error loading file " + e.getMessage());

			throw new NullPointerException(e.getMessage());

		}

	}

	@Override
	public HashMap<CATree, Integer> executeCap(
			CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor,
			String scenario,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge startEdge) throws JSONException {

		// call the analyzer with the specific CAP

		// reading property file
		// String propPath = PROP_PATH;

		HashMap<CATree, Integer> result = new HashMap<CATree, Integer>();

		this.executor = executor;
		/*
		 * Properties props = new Properties(); try {
		 * props.load(getClass().getClassLoader
		 * ().getResourceAsStream(PROP_PATH)); } catch (FileNotFoundException e)
		 * { System.out.println("Error loading file " + e.getMessage());
		 * 
		 * throw new NullPointerException(e.getMessage());
		 * 
		 * } catch (IOException e) { System.out.println("Error loading file " +
		 * e.getMessage());
		 * 
		 * throw new NullPointerException(e.getMessage());
		 * 
		 * }
		 */
		// demo management system construction

		DemoManagementSystem dms = null;
		try {
			if (scenario.equalsIgnoreCase("Mobility")) {
				dms = DemoManagementSystem
						.initializeSystem("scenario/Mobility/");
			} else {
				dms = DemoManagementSystem.initializeSystem("scenario/Drones/");
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// creation of ensembles
		List<EnsembleManager> ensembles = new ArrayList<EnsembleManager>();
		for (int i = 0; i < cap.getEnsembles().size(); i++) {
			CollectiveAdaptationEnsemble ensemble = cap.getEnsembles().get(i);
			String EnsembleName = ensemble.getEnsembleName();
			Ensemble e = dms.getEnsemble(EnsembleName, cap);
			EnsembleManager manager = new EnsembleManager(e);

			// set the Evoknowledge of the Ensemble
			// manager.setEk(ek);

			// add the ensemble to the list
			ensembles.add(manager);

		}

		int crossEnsembles = 0;
		this.run(cap, ensembles, null, cap.getIssue(), cap.getCapID(),
				cap.getStartingRole(), 0, GlobalResult, cat, graph, startEdge,
				crossEnsembles);
		result.put(cat, crossEnsembles);
		return result;

	}

	public CollectiveAdaptationSolution runNew(
			CollectiveAdaptationProblem cap,
			List<EnsembleManager> ensembles,
			String issueName,
			String capID,
			String startingRole,
			int issueIndex,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge lastEdge, int crossEnsembles) throws JSONException {

		CollectiveAdaptationSolution solution = new CollectiveAdaptationSolution(
				capID, null);
		Issue issue = new Issue();
		issue.setIssueType(issueName);

		// System.out.println("Ruolo da trovare: " + startingRole);

		EnsembleManager en = null;
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager e = ensembles.get(i);
			for (int j = 0; j < e.getRolesManagers().size(); j++) {
				RoleManager r = e.getRolesManagers().get(j);
				if (r.getRole().getType().equalsIgnoreCase(startingRole)) {
					en = e;
					break;
				}

			}

		}

		// search the role that can trigger the specific issue
		// EnsembleManager en = ensembles.stream()
		// .filter(e ->
		// e.getEnsemble().getName().equals(cap.getStartingRoleEnsemble())).findFirst().get();

		// EnsembleManager en = ensembles.stream().filter(e ->
		// e.getEnsemble().getName().equals(startingRole)).findFirst()
		// .get();

		RoleManager r = en.getRolebyType(startingRole);
		// System.out.println("ISSUE TRIGGERED: " + issue.getIssueType());

		IssueResolution resolution1 = new IssueResolution(1, "ISSUE_TRIGGERED",
				r, r, issue, null);
		resolution1.setRoot(true);
		// r.addIssueResolution(resolution1);

		EnsembleManager em = null;

		// add the issueresolution to the right Ensemble
		for (int i = 0; i < ensembles.size(); i++) {
			for (int j = 0; j < ensembles.get(i).getRolesManagers().size(); j++) {
				RoleManager currentManager = ensembles.get(i)
						.getRolesManagers().get(j);
				if (currentManager.getRole().getType()
						.equalsIgnoreCase(r.getRole().getType())) {
					ArrayList<IssueResolution> resolutions = new ArrayList<IssueResolution>();

					em = ensembles.get(i);
					if (em.getIssueCommunications() != null) {
						em.getIssueCommunications().clear();
					}

					solution.setCapID(capID);
					HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
					List<RoleCommand> commands = new ArrayList<RoleCommand>();
					ensembleCommands.put(em.getEnsemble().getName(), commands);

					solution.setEnsembleCommands(ensembleCommands);

					// update id of the issue resolution
					em.setIssueResolutionCount(1);
					resolution1.setIssueResolutionID(em
							.getIssueResolutionCount());

					resolutions.add(resolution1);
					em.setActiveIssueResolutions(resolutions);

					List<IssueCommunication> relatedComs = new ArrayList<IssueCommunication>();

					em.setCommunicationsRelations(resolution1, relatedComs);

					CATree hierarchyTree = createHierarchyTree(ensembles);

					// window.updateHierarchy(hierarchyTree);

					em.checkIssues(cap, capID, null, ensembles, solution,
							issueIndex, hierarchyTree, GlobalResult, cat,
							graph, lastEdge);

					break;
				}
			}
		}
		// retrieve the final solution for the ensemble
		List<RoleCommand> roleCommands = new ArrayList<RoleCommand>();
		solution.setCapID(capID);
		// RETRIEVE ALL COMMANDS FOR INVOLVED ROLES IN a CAP
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager emx = ensembles.get(i);
			System.out.println("******ENSEMBLE: " + emx.getEnsemble().getName()
					+ "********");
			for (int j = 0; j < emx.getRolesManagers().size(); j++) {
				RoleManager rm = emx.getRolesManagers().get(j);

				if (rm.getRoleCommands() != null) {
					RoleCommand command = rm.getRoleCommands();

					System.out.println("ROLE: " + rm.getRole().getType()
							+ " -- COMMAND: " + command.getCommands().get(0));

					this.addInfoEmissions(rm, command.getCommands().get(0));

					roleCommands.add(command);
				}

			}

		}

		/*
		 * VECCHIO SVILUPPO for (int i = 0; i < em.getRolesManagers().size();
		 * i++) { RoleManager rm = em.getRolesManagers().get(i);
		 * 
		 * if (rm.getRoleCommands() != null) { RoleCommand command =
		 * rm.getRoleCommands();
		 * 
		 * System.out.println("ROLE: " + rm.getRole().getType() +
		 * " -- COMMAND: " + command.getCommands().get(0));
		 * 
		 * roleCommands.add(command); }
		 * 
		 * }
		 */
		HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
		ensembleCommands.put(em.getEnsemble().getName(), roleCommands);
		solution.setEnsembleCommands(ensembleCommands);
		return solution;

	}

	public CollectiveAdaptationSolution run(
			CollectiveAdaptationProblem cap,
			List<EnsembleManager> ensembles,
			CAWindow window,
			String issueName,
			String capID,
			String startingRole,
			int issueIndex,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge lastEdge, int crossEnsembles) throws JSONException {

		CollectiveAdaptationSolution solution = new CollectiveAdaptationSolution(
				capID, null);
		Issue issue = new Issue();
		issue.setIssueType(issueName);

		// System.out.println("Ruolo da trovare: " + startingRole);

		EnsembleManager en = null;
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager e = ensembles.get(i);
			for (int j = 0; j < e.getRolesManagers().size(); j++) {
				RoleManager r = e.getRolesManagers().get(j);
				if (r.getRole().getType().equalsIgnoreCase(startingRole)) {
					en = e;
					break;
				}

			}

		}

		// search the role that can trigger the specific issue
		// EnsembleManager en = ensembles.stream()
		// .filter(e ->
		// e.getEnsemble().getName().equals(cap.getStartingRoleEnsemble())).findFirst().get();

		// EnsembleManager en = ensembles.stream().filter(e ->
		// e.getEnsemble().getName().equals(startingRole)).findFirst()
		// .get();

		RoleManager r = en.getRolebyType(startingRole);
		// System.out.println("ISSUE TRIGGERED: " + issue.getIssueType());

		IssueResolution resolution1 = new IssueResolution(1, "ISSUE_TRIGGERED",
				r, r, issue, null);
		resolution1.setRoot(true);
		// r.addIssueResolution(resolution1);

		EnsembleManager em = null;

		// add the issueresolution to the right Ensemble
		for (int i = 0; i < ensembles.size(); i++) {
			for (int j = 0; j < ensembles.get(i).getRolesManagers().size(); j++) {
				RoleManager currentManager = ensembles.get(i)
						.getRolesManagers().get(j);
				if (currentManager.getRole().getType()
						.equalsIgnoreCase(r.getRole().getType())) {
					ArrayList<IssueResolution> resolutions = new ArrayList<IssueResolution>();

					em = ensembles.get(i);
					if (em.getIssueCommunications() != null) {
						em.getIssueCommunications().clear();
					}

					solution.setCapID(capID);
					HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
					List<RoleCommand> commands = new ArrayList<RoleCommand>();
					ensembleCommands.put(em.getEnsemble().getName(), commands);

					solution.setEnsembleCommands(ensembleCommands);

					// update id of the issue resolution
					em.setIssueResolutionCount(1);
					resolution1.setIssueResolutionID(em
							.getIssueResolutionCount());

					resolutions.add(resolution1);
					em.setActiveIssueResolutions(resolutions);

					List<IssueCommunication> relatedComs = new ArrayList<IssueCommunication>();

					em.setCommunicationsRelations(resolution1, relatedComs);

					CATree hierarchyTree = createHierarchyTree(ensembles);

					// window.updateHierarchy(hierarchyTree);

					em.checkIssues(cap, capID, window, ensembles, solution,
							issueIndex, hierarchyTree, GlobalResult, cat,
							graph, lastEdge);

					break;
				}
			}
		}
		// retrieve the final solution for the ensemble
		List<RoleCommand> roleCommands = new ArrayList<RoleCommand>();
		solution.setCapID(capID);
		// RETRIEVE ALL COMMANDS FOR INVOLVED ROLES IN a CAP
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager emx = ensembles.get(i);
			System.out.println("******ENSEMBLE: " + emx.getEnsemble().getName()
					+ "********");
			for (int j = 0; j < emx.getRolesManagers().size(); j++) {
				RoleManager rm = emx.getRolesManagers().get(j);

				if (rm.getRoleCommands() != null) {
					RoleCommand command = rm.getRoleCommands();

					System.out.println("ROLE: " + rm.getRole().getType()
							+ " -- COMMAND: " + command.getCommands().get(0));

					this.addInfoEmissions(rm, command.getCommands().get(0));

					roleCommands.add(command);
				}

			}

		}

		/*
		 * VECCHIO SVILUPPO for (int i = 0; i < em.getRolesManagers().size();
		 * i++) { RoleManager rm = em.getRolesManagers().get(i);
		 * 
		 * if (rm.getRoleCommands() != null) { RoleCommand command =
		 * rm.getRoleCommands();
		 * 
		 * System.out.println("ROLE: " + rm.getRole().getType() +
		 * " -- COMMAND: " + command.getCommands().get(0));
		 * 
		 * roleCommands.add(command); }
		 * 
		 * }
		 */
		HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
		ensembleCommands.put(em.getEnsemble().getName(), roleCommands);
		solution.setEnsembleCommands(ensembleCommands);
		return solution;

	}

	private void addInfoEmissions(RoleManager RoleManager, String Command)
			throws JSONException {

		HashMap<String, GeoPosition> geoPositions = new HashMap<String, GeoPosition>();

		// ROUTE A
		geoPositions.put("RoutePassenger_33", new GeoPosition(46.081935,
				11.120949));
		geoPositions.put("RoutePassenger_30", new GeoPosition(46.070087,
				11.137016));
		geoPositions.put("RoutePassenger_36", new GeoPosition(46.069366,
				11.140326));
		geoPositions.put("FlexibusDriver_13", new GeoPosition(46.107172,
				11.112409));
		geoPositions.put("RouteA", new GeoPosition(46.067301, 11.151351));

		// ROUTE B
		geoPositions.put("RoutePassenger_64", new GeoPosition(46.072046,
				11.099685));
		geoPositions.put("RoutePassenger_69", new GeoPosition(46.072046,
				11.099685));
		geoPositions.put("RoutePassenger_74", new GeoPosition(46.077286,
				11.109212));
		geoPositions.put("FlexibusDriver_28", new GeoPosition(46.067301,
				11.151351));
		geoPositions.put("RouteB", new GeoPosition(46.067301, 11.151351));

		// CAR POOL A
		geoPositions.put("CPDriver_A", new GeoPosition(46.061622, 11.236793));
		geoPositions
				.put("CPPassenger_1", new GeoPosition(46.084011, 11.183235));
		geoPositions.put("CPRideA", new GeoPosition(46.067301, 11.151351));

		// CAR POOL B
		geoPositions.put("CPDriver_B", new GeoPosition(46.189008, 11.131299));
		geoPositions
				.put("CPPassenger_3", new GeoPosition(46.141452, 11.111730));
		geoPositions.put("CPRideB", new GeoPosition(46.067301, 11.151351));

		Adaptation adapt = null;
		if (Command != null) {
			if (Command.equalsIgnoreCase("Nothing")) {
				adapt = new Adaptation("Selfish", RoleManager.getRole()
						.getType(), null);

				this.writeFile(adapt, "adaptations.csv");

				/*
				 * JSONObject jsonObj = new JSONObject();
				 * jsonObj.put("AdaptationType", "selfish"); jsonObj.put("Role",
				 * RoleManager.getRole().getType()); jsonObj.put("Legs", null);
				 * 
				 * // Writing the jsonObject into sample.json try {
				 * 
				 * // Writing to a file File file = new File("JsonFile.json");
				 * file.createNewFile(); FileWriter fileWriter = new
				 * FileWriter(file, true);
				 * System.out.println("Writing JSON object to file");
				 * System.out.println("-----------------------");
				 * System.out.print(jsonObj);
				 * 
				 * fileWriter.write(jsonObj.toJSONString()); fileWriter.flush();
				 * fileWriter.close();
				 * 
				 * } catch (IOException e) { e.printStackTrace(); }
				 */
			} else if (Command.equalsIgnoreCase("ChangePathAddPP")) {
				// ROLE: FlexibusDriver_13 -- COMMAND: ChangePathAddPP
				System.out.println("ChangePathAddPP");

			} else if (Command.equalsIgnoreCase("StayAndRePlan")) {
				// ROLE: RoutePassenger_36 -- COMMAND: StayAndRePlan
				System.out.println("StayAndRePlan");
			} else if (Command.equalsIgnoreCase("ExitAndChange")) {

			}

			else if (Command.equalsIgnoreCase("ComeBackDeposit")) {
				// FlexiBusDriver_13 come back to deposit 46.107172,
				// 11.112409
				// starting position before Via del Brennero - Via Bolzano
				//
				GeoPosition start = new GeoPosition(46.101816, 11.110778);
				// destination position its starting position
				GeoPosition destination = geoPositions.get(RoleManager
						.getRole().getType());
				// calculate journey using ViaggiaTrento Service
				ViaggiaTrentoAPIWrapper viaggia = new ViaggiaTrentoAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = viaggia
						.getViaggiaAlternatives(s, d, null, "BUS");

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Selfish", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "selfish");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			else if (Command.equalsIgnoreCase("JoinCPB")) {
				// System.out.println("qui unisciti al Car Pool B");
				// aggiungi pickup point per RoutePassenger_64 e
				// RoutePassenger_69
				// trento stazione 46.072839,11.120059
				GeoPosition start = geoPositions.get(RoleManager.getRole()
						.getType());
				GeoPosition destination = new GeoPosition(46.072839, 11.120059);
				// calculate journey using ViaggiaTrento Service
				ViaggiaTrentoAPIWrapper viaggia = new ViaggiaTrentoAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = viaggia.getViaggiaAlternatives(s, d, null,
						"TRANSIT");

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Collective", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "collective");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (Command.equalsIgnoreCase("ExitAndReplanCar")) {
				// replan from the current position to the Ensemble Final
				// Destination
				// using Google Transit Service

				// Retrieve Ensemble
				String EnsembleName = RoleManager.getEnsemble().getEnsemble()
						.getName();
				GeoPosition start = geoPositions.get(RoleManager.getRole()
						.getType());
				GeoPosition destination = geoPositions.get(EnsembleName);

				// calculate journey using ViaggiaTrento Service
				GoogleAPIWrapper google = new GoogleAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = google.getGoogleCarAlternatives(s, d);

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Selfish", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());

				// this.writeFile(adapt, "adaptations.csv");

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "selfish");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (Command.equalsIgnoreCase("JoinCPA")) {
				System.out.println("qui unisciti al Car Pool A");
				// aggiungi un pickup point per il passeggero P33 (46.069291,
				// 11.139575)
				// Ponte Lodovico
				GeoPosition start = geoPositions.get(RoleManager.getRole()
						.getType());
				GeoPosition destination = new GeoPosition(46.069291, 11.139575);

				// calculate journey using ViaggiaTrento Service
				ViaggiaTrentoAPIWrapper viaggia = new ViaggiaTrentoAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = viaggia.getViaggiaAlternatives(s, d, null,
						"TRANSIT");

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Collective", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "Collective");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (Command.equalsIgnoreCase("ExitAndReplanCar")) {
				// replan from the current position to the Ensemble Final
				// Destination
				// using Google Transit Service

				// Retrieve Ensemble
				String EnsembleName = RoleManager.getEnsemble().getEnsemble()
						.getName();
				GeoPosition start = geoPositions.get(RoleManager.getRole()
						.getType());
				GeoPosition destination = geoPositions.get(EnsembleName);

				// calculate journey using ViaggiaTrento Service
				GoogleAPIWrapper google = new GoogleAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = google.getGoogleCarAlternatives(s, d);

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Selfish", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());

				// this.writeFile(adapt, "adaptations.csv");

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "selfish");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (Command.equalsIgnoreCase("ExitAndReplan")) {
				// replan from the current position to the Ensemble Final
				// Destination
				// using ViaggiaTrento with TRANSIT

				// Retrieve Ensemble
				String EnsembleName = RoleManager.getEnsemble().getEnsemble()
						.getName();
				GeoPosition start = geoPositions.get(RoleManager.getRole()
						.getType());
				GeoPosition destination = geoPositions.get(EnsembleName);

				// calculate journey using ViaggiaTrento Service
				ViaggiaTrentoAPIWrapper viaggia = new ViaggiaTrentoAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = viaggia.getViaggiaAlternatives(s, d, null,
						"TRANSIT");

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Selfish", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());

				// this.writeFile(adapt, "adaptations.csv");

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "selfish");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (Command.equalsIgnoreCase("ExitAndReplanWalk")) {
				// replan from the current position to the Ensemble Final
				// Destination
				// using ViaggiaTrento WALK

				// Retrieve Ensemble
				String EnsembleName = RoleManager.getEnsemble().getEnsemble()
						.getName();
				GeoPosition start = geoPositions.get(RoleManager.getRole()
						.getType());
				GeoPosition destination = geoPositions.get(EnsembleName);

				// calculate journey using ViaggiaTrento Service
				ViaggiaTrentoAPIWrapper viaggia = new ViaggiaTrentoAPIWrapper();

				ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
				String s = start.getLatitude() + "," + start.getLongitude();
				String d = destination.getLatitude() + ","
						+ destination.getLongitude();

				alternatives = viaggia.getViaggiaAlternatives(s, d, null,
						"WALK");

				System.out.println(alternatives.toString());

				adapt = new Adaptation("Selfish", RoleManager.getRole()
						.getType(), alternatives.get(0).getLegs());

				// this.writeFile(adapt, "adaptations.csv");

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("AdaptationType", "selfish");
				jsonObj.put("Role", RoleManager.getRole().getType());

				// add Legs
				JSONArray listOfLegs = new JSONArray();

				// for each leg create a JsonObject
				for (int i = 0; i < alternatives.get(0).getLegs().size(); i++) {

					Leg currentLeg = alternatives.get(0).getLegs().get(i);
					JSONObject obj = new JSONObject();
					obj.put("Duration", currentLeg.getDuration());
					obj.put("Mean", currentLeg.getMean());
					obj.put("fromLat", currentLeg.getFromLat());
					obj.put("fromLon", currentLeg.getFromLon());
					obj.put("toLon", currentLeg.getToLon());
					obj.put("toLat", currentLeg.getToLat());
					listOfLegs.put(obj);

				}

				jsonObj.put("Legs", listOfLegs);

				// Writing the jsonObject into sample.json
				try {

					// Writing to a file
					File file = new File("JsonFile.json");
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file, true);
					// System.out.println("Writing JSON object to file");
					System.out.println("-----------------------");
					System.out.print(jsonObj);

					fileWriter.write(jsonObj.toJSONString());
					fileWriter.flush();
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	// to write adaptation solution to a CSV file

	private static void writeToCSV(Adaptation adapt) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("adaptations.csv"), "UTF-8"));
			// for (Adaptation adapt : adaptations) {
			StringBuffer oneLine = new StringBuffer();
			oneLine.append(adapt.getAdaptationType());
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(adapt.getRole());
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(adapt.getLegs());
			bw.write(oneLine.toString());
			bw.newLine();
			// }
			bw.flush();
			bw.close();
		} catch (UnsupportedEncodingException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private CATree createHierarchyTree(List<EnsembleManager> ensembles) {
		CATree hierarchyTree = new CATree();

		// CREATE FIRST PART OF THE HIERARCHY TREE

		Object root1 = hierarchyTree.insertNodeHierarchy(
				hierarchyTree.getDefaultParent(), null, "UMS", STYLE_INIT);

		Object v1 = hierarchyTree.insertNodeHierarchy(
				hierarchyTree.getDefaultParent(), null, "FBC", STYLE_INIT);
		hierarchyTree.insertEdge(hierarchyTree.getDefaultParent(), "", "",
				root1, v1, STYLE_ISSUE_EDGE);

		for (int k = 0; k < ensembles.size(); k++) {
			EnsembleManager e = ensembles.get(k);
			if (!(e.getEnsemble().getName().contains("Flexi"))) {
				List<RoleManager> roles = e.getRolesManagers();
				for (int m = 0; m < roles.size(); m++) {
					RoleManager role = roles.get(m);
					if (role.getRole().getType().contains("RouteManagement")) {
						Object v = hierarchyTree.insertNodeHierarchy(
								hierarchyTree.getDefaultParent(), null, role
										.getRole().getType(), STYLE_INIT);
						hierarchyTree.insertEdge(
								hierarchyTree.getDefaultParent(), "", "", v1,
								v, STYLE_ISSUE_EDGE);
						for (int n = 0; n < roles.size(); n++) {
							RoleManager role1 = roles.get(n);
							if (role1.getRole().getType()
									.contains("RoutePassenger")) {
								Object v2 = hierarchyTree.insertNodeHierarchy(
										hierarchyTree.getDefaultParent(), null,
										role1.getRole().getType(), STYLE_INIT);
								hierarchyTree.insertEdge(
										hierarchyTree.getDefaultParent(), "",
										"", v, v2, STYLE_ISSUE_EDGE);
							} else if (role1.getRole().getType()
									.contains("FlexibusDriver")) {
								Object v2 = hierarchyTree.insertNodeHierarchy(
										hierarchyTree.getDefaultParent(), null,
										role1.getRole().getType(), STYLE_INIT);
								hierarchyTree.insertEdge(
										hierarchyTree.getDefaultParent(), "",
										"", v, v2, STYLE_ISSUE_EDGE);
							}

						}
					}

				}

			}

		}
		return hierarchyTree;
	}

	private static int randomThree(List<Integer> numbers) {
		Random rand = new Random();
		return (rand.nextInt(numbers.size()));
	}

	public static void writeFile(Adaptation adaptation, String fileName) {

		// Delimiter used in CSV file
		String commaDelimiter = ",";
		String newLineSeparator = "\n";

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(fileName);

			// Write the CSV file header
			// fileWriter.append(adaptation.getCsvFileHeader(commaDelimiter));
			// Add a new line separator after the header
			// fileWriter.append(newLineSeparator);

			// Write a new adaptation object list to the CSV file
			// for (Adaptation adaptation : adaptations) {
			// String toAdd = adaptation.toCsv(commaDelimiter);
			// System.out.println("TO ADD: " + toAdd);
			fileWriter.append(adaptation.toCsv(commaDelimiter));
			fileWriter.append(newLineSeparator);

			// }
		} catch (Exception e) {
			// System.out.println("Error in CsvFileWriter.");
			e.printStackTrace();
		} /*
		 * finally {
		 * 
		 * try { // fileWriter.flush(); // fileWriter.close(); } catch
		 * (IOException e) {
		 * System.out.println("Error while flushing/closing fileWriter.");
		 * e.printStackTrace(); }
		 * 
		 * }
		 */
	}
}
