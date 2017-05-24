package eu.fbk.das.adaptation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import eu.fbk.das.adaptation.ensemble.Issue;
import eu.fbk.das.adaptation.ensemble.Solution;
import eu.fbk.das.adaptation.ensemble.Solver;
import eu.fbk.das.experiments.ExperimentResult;

/**
 * @author Antonio Bucchiarone
 * 
 */
public class Utilities {

	private static HashMap<String, ArrayList<RoleManager>> solversMapDrones;

	public static HashMap<String, ArrayList<RoleManager>> getSolversMap() {
		return solversMapDrones;
	}

	public static void setSolversMapDrones(
			HashMap<String, ArrayList<RoleManager>> solversMap) {
		Utilities.solversMapDrones = solversMap;
	}

	private static HashMap<String, ArrayList<RoleManager>> solversMapMobility;

	public static HashMap<String, ArrayList<RoleManager>> getSolversMapMobility() {
		return solversMapMobility;
	}

	public static void setSolversMapMobility(
			HashMap<String, ArrayList<RoleManager>> solversMap) {
		Utilities.solversMapMobility = solversMap;
	}

	/**
	 *
	 * Thanks to assylias - https://assylias.wordpress.com/
	 * 
	 * http://stackoverflow.com/questions/22380890/generate-n-random-numbers-
	 * whose-sum-is-m-and-all-numbers-should-be-greater-than
	 *
	 */
	public static List<Integer> generateRandomValues(int targetSum,
			int numberOfDraws) {
		Random r = new Random();
		List<Integer> load = new ArrayList<>();

		// random numbers
		int sum = 0;
		for (int i = 0; i < numberOfDraws; i++) {
			int next = r.nextInt(targetSum) + 1;
			load.add(next);
			sum += next;
		}

		// scale to the desired target sum
		double scale = 1d * targetSum / sum;
		sum = 0;
		for (int i = 0; i < numberOfDraws; i++) {
			load.set(i, (int) (load.get(i) * scale));
			sum += load.get(i);
		}

		// take rounding issues into account
		while (sum++ < targetSum) {
			int i = r.nextInt(numberOfDraws);
			load.set(i, load.get(i) + 1);
		}

		return load;
	}

	// public static void writeFile(List<Treatment> treatments, String fileName)
	// {
	//
	// // Delimiter used in CSV file
	// String commaDelimiter = ",";
	// String newLineSeparator = "\n";
	//
	// FileWriter fileWriter = null;
	//
	// try {
	// fileWriter = new FileWriter(fileName);
	//
	// // Write the CSV file header
	// fileWriter.append(treatments.get(0)
	// .getCsvFileHeader(commaDelimiter));
	// // Add a new line separator after the header
	// fileWriter.append(newLineSeparator);
	//
	// // Write a new treatment object list to the CSV file
	// for (Treatment treatment : treatments) {
	// fileWriter.append(treatment.toCsv(commaDelimiter));
	// fileWriter.append(newLineSeparator);
	// }
	// } catch (Exception e) {
	// // System.out.println("Error in CsvFileWriter.");
	// e.printStackTrace();
	// } finally {
	//
	// try {
	// fileWriter.flush();
	// fileWriter.close();
	// } catch (IOException e) {
	// // System.out.println("Error while flushing/closing
	// // fileWriter.");
	// e.printStackTrace();
	// }
	//
	// }
	// }

	public static void genericWriteFile(List<? extends Loggable> loggables,
			String fileName) {

		// Delimiter used in CSV file
		String commaDelimiter = ",";
		String newLineSeparator = "\n";

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(fileName);

			// Write the CSV file header
			fileWriter
					.append(loggables.get(0).getCsvFileHeader(commaDelimiter));
			// Add a new line separator after the header
			fileWriter.append(newLineSeparator);

			// Write a new treatment object list to the CSV file
			for (Loggable currentLoggable : loggables) {

				fileWriter.append(currentLoggable.toCsv(commaDelimiter));
				fileWriter.append(newLineSeparator);

			}
		} catch (Exception e) {
			// System.out.println("Error in CsvFileWriter.");
			e.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				// System.out.println("Error while flushing/closing
				// fileWriter.");
				e.printStackTrace();
			}

		}
	}

	public static RoleManager pickRoleForIssueMobility(Issue issue) {
		RoleManager result = null;

		int numElements = Utilities.solversMapMobility
				.get(issue.getIssueType()).size();
		if (numElements == 1) {
			result = Utilities.solversMapMobility.get(issue.getIssueType())
					.get(0);
		}
		if (numElements > 1) {
			int index = Utilities.generateRandomValues(numElements - 1, 1).get(
					0);
			result = Utilities.solversMapMobility.get(issue.getIssueType())
					.get(index);
		}
		return result;
	}

	public static RoleManager pickRoleForIssueDrones(Issue issue) {
		RoleManager result = null;

		int numElements = Utilities.solversMapDrones.get(issue.getIssueType())
				.size();
		if (numElements == 1) {
			result = Utilities.solversMapDrones.get(issue.getIssueType())
					.get(0);
		}
		if (numElements > 1) {
			int index = Utilities.generateRandomValues(numElements - 1, 1).get(
					0);
			result = Utilities.solversMapDrones.get(issue.getIssueType()).get(
					index);
		}
		return result;
	}

	public static void buildSolversMapMobility(List<EnsembleManager> ensembles) {
		HashMap<String, String[]> staticMap = new HashMap<String, String[]>();

		staticMap.put("RouteBlocked", new String[] { "FlexibusDriver_28" });
		staticMap.put("IntenseTraffic", new String[] { "FlexibusDriver_13" });
		staticMap.put("PassengerDelay", new String[] { "RoutePassenger_36" });
		staticMap.put("DriversStrike", new String[] { "FBC" });
		staticMap.put("CPAPassengerDelay", new String[] { "CPPassenger_1" });

		Utilities.solversMapMobility = new HashMap<String, ArrayList<RoleManager>>();
		Utilities.solversMapMobility.put("IntenseTraffic",
				new ArrayList<RoleManager>());
		Utilities.solversMapMobility.put("RouteBlocked",
				new ArrayList<RoleManager>());
		Utilities.solversMapMobility.put("PassengerDelay",
				new ArrayList<RoleManager>());
		Utilities.solversMapMobility.put("DriversStrike",
				new ArrayList<RoleManager>());
		Utilities.solversMapMobility.put("CPAPassengerDelay",
				new ArrayList<RoleManager>());

		Iterator<EnsembleManager> ensemblesIterator = ensembles.iterator();
		Iterator<RoleManager> rolesIterator;
		Iterator<Solver> solversIterator;
		Iterator<Solution> solutionsIterator;
		Iterator<Issue> issuesIterator;
		EnsembleManager currentEnsemble;
		RoleManager currentRole;
		Solver currentSolver;
		Solution currentSolution;
		String currentIssueName;
		List<RoleManager> roleManagers = new ArrayList<RoleManager>();
		while (ensemblesIterator.hasNext()) {
			currentEnsemble = (EnsembleManager) ensemblesIterator.next();
			rolesIterator = currentEnsemble.getRolesManagers().iterator();
			while (rolesIterator.hasNext()) {
				currentRole = (RoleManager) rolesIterator.next();
				roleManagers.add(currentRole);
				// solversIterator =
				// currentRole.getRole().getSolver().iterator();
				// while (solversIterator.hasNext()) {
				// currentSolver = solversIterator.next();
				// solutionsIterator = currentSolver.getSolution().iterator();
				// while (solutionsIterator.hasNext()) {
				// currentSolution = solutionsIterator.next();
				// issuesIterator = currentSolution.getIssue().iterator();
				// while (issuesIterator.hasNext()) {
				// currentIssueName = issuesIterator.next().getIssueType();
				// if (Utilities.solversMap.get(currentIssueName) != null) {
				// Utilities.solversMap.get(currentIssueName).add(currentRole);
				// }
				// }
				// }
				// }
			}
		}
		Utilities.assignStaticallyRolesMobility(staticMap, roleManagers);
		// Utilities.printSolversMap();
	}

	public static void buildSolversMapDrones(List<EnsembleManager> ensembles) {
		HashMap<String, String[]> staticMap = new HashMap<String, String[]>();
		staticMap.put("Drone1Fault", new String[] { "D1" });
		staticMap.put("Drone3Fault", new String[] { "D3" });
		staticMap.put("IntruderDetected", new String[] { "C1" });
		staticMap.put("ObstacleFound", new String[] { "D4" });
		staticMap.put("CameraFault", new String[] { "C3" });

		Utilities.solversMapDrones = new HashMap<String, ArrayList<RoleManager>>();
		Utilities.solversMapDrones.put("Drone1Fault",
				new ArrayList<RoleManager>());
		Utilities.solversMapDrones.put("Drone3Fault",
				new ArrayList<RoleManager>());
		Utilities.solversMapDrones.put("IntruderDetected",
				new ArrayList<RoleManager>());
		Utilities.solversMapDrones.put("ObstacleFound",
				new ArrayList<RoleManager>());
		Utilities.solversMapDrones.put("CameraFault",
				new ArrayList<RoleManager>());

		Iterator<EnsembleManager> ensemblesIterator = ensembles.iterator();
		Iterator<RoleManager> rolesIterator;
		Iterator<Solver> solversIterator;
		Iterator<Solution> solutionsIterator;
		Iterator<Issue> issuesIterator;
		EnsembleManager currentEnsemble;
		RoleManager currentRole;
		Solver currentSolver;
		Solution currentSolution;
		String currentIssueName;
		List<RoleManager> roleManagers = new ArrayList<RoleManager>();
		while (ensemblesIterator.hasNext()) {
			currentEnsemble = (EnsembleManager) ensemblesIterator.next();
			rolesIterator = currentEnsemble.getRolesManagers().iterator();
			while (rolesIterator.hasNext()) {
				currentRole = (RoleManager) rolesIterator.next();
				roleManagers.add(currentRole);
			}
		}
		Utilities.assignStaticallyRolesDrones(staticMap, roleManagers);
		// Utilities.printSolversMap();
	}

	private static void assignStaticallyRolesMobility(
			HashMap<String, String[]> staticMap, List<RoleManager> roleManagers) {
		Iterator iterator = staticMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			Utilities.solversMapMobility.put(
					(String) pair.getKey(),
					Utilities.getManagersByIssueNames(
							(String[]) pair.getValue(), roleManagers));
		}
	}

	private static void assignStaticallyRolesDrones(
			HashMap<String, String[]> staticMap, List<RoleManager> roleManagers) {
		Iterator iterator = staticMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			Utilities.solversMapDrones.put(
					(String) pair.getKey(),
					Utilities.getManagersByIssueNames(
							(String[]) pair.getValue(), roleManagers));
		}
	}

	private static ArrayList<RoleManager> getManagersByIssueNames(
			String[] names, List<RoleManager> roleManagers) {
		ArrayList<RoleManager> result = new ArrayList<RoleManager>();
		for (int i = 0; i < roleManagers.size(); i++) {
			if (Arrays.asList(names).contains(
					roleManagers.get(i).getRole().getType())) {
				result.add(roleManagers.get(i));
			}
		}
		return result;
	}

	private static void printSolversMapMobility() {
		ArrayList<RoleManager> currentRolesList;
		Iterator iterator = Utilities.solversMapMobility.entrySet().iterator();
		String currentIssue;
		String currentRolesNames;
		while (iterator.hasNext()) {
			currentRolesNames = "";
			Map.Entry pair = (Map.Entry) iterator.next();
			currentIssue = (String) pair.getKey();
			currentRolesList = (ArrayList<RoleManager>) pair.getValue();
			for (int i = 0; i < currentRolesList.size(); i++) {
				currentRolesNames += currentRolesList.get(i).getRole()
						.getType()
						+ ", ";
			}
			// System.out.println(currentIssue + " - [" + currentRolesNames +
			// "]");
		}
	}

	private static void printSolversMapDrones() {
		ArrayList<RoleManager> currentRolesList;
		Iterator iterator = Utilities.solversMapDrones.entrySet().iterator();
		String currentIssue;
		String currentRolesNames;
		while (iterator.hasNext()) {
			currentRolesNames = "";
			Map.Entry pair = (Map.Entry) iterator.next();
			currentIssue = (String) pair.getKey();
			currentRolesList = (ArrayList<RoleManager>) pair.getValue();
			for (int i = 0; i < currentRolesList.size(); i++) {
				currentRolesNames += currentRolesList.get(i).getRole()
						.getType()
						+ ", ";
			}
			// System.out.println(currentIssue + " - [" + currentRolesNames +
			// "]");
		}
	}

	public static RoleManager pickRoleRandom(List<EnsembleManager> ensembles) {
		// ritorna random un ruolo dell'intero sistema (tra le varie ensembles)
		RoleManager result = null;
		EnsembleManager em = ensembles.get(0);
		for (int i = 0; i < em.getRolesManagers().size(); i++) {
			RoleManager rm = em.getRolesManagers().get(i);
			if (rm.getRole().getType().equalsIgnoreCase("P2")) {
				result = rm;
				break;

			}

		}
		// RoleManager result = ensembles.get(0).getRolebyType("C1");
		return result;
	}

	public static RoleManager pickRole(List<EnsembleManager> ensembles,
			String agentThatTrigger) {
		RoleManager result = null;
		String toFind = null;
		if (agentThatTrigger.matches("(c).*")) {
			toFind = "(c).*";
		} else if (agentThatTrigger.matches("(p).*")) {

			toFind = "(p).*";
		}

		for (int j = 0; j < ensembles.size(); j++) {
			EnsembleManager em = ensembles.get(j);

			for (int i = 0; i < em.getRolesManagers().size(); i++) {
				RoleManager rm = em.getRolesManagers().get(i);
				// System.out.println(rm.getRole().getType());
				// solo se contiene la prima stringa del agentThatTrigger
				// "p: passenger", "c: car"

				if (rm.getRole().getType().matches("(p).*")
						|| rm.getRole().getType().matches("(c).*")) {
					result = rm;

					break;

				}

			}
		}
		// RoleManager result = ensembles.get(0).getRolebyType("C1");
		return result;
	}

	public static void genericWriteFileNew(List<ExperimentResult> results,
			String fileName) {
		// Delimiter used in CSV file
		String commaDelimiter = ",";
		String newLineSeparator = "\n";

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(fileName);

			// Write the CSV file header
			fileWriter.append(results.get(0).getCsvFileHeader(commaDelimiter));
			// Add a new line separator after the header
			fileWriter.append(newLineSeparator);

			// Write a new treatment object list to the CSV file
			for (ExperimentResult currentLoggable : results) {

				fileWriter.append(currentLoggable.toCsv(commaDelimiter));
				fileWriter.append(newLineSeparator);

			}
		} catch (Exception e) {
			// System.out.println("Error in CsvFileWriter.");
			e.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				// System.out.println("Error while flushing/closing
				// fileWriter.");
				e.printStackTrace();
			}

		}

	}
}
