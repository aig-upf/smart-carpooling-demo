package eu.fbk.das.multiagent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MultiagentPlannerCaller {
	private static void runPlanner(String scriptPath, String configPath,
			int numSeconds, int numMegabytes) {

		try {
			String planCmd = scriptPath
					+ "parser.py --plan --json --visualize --time "
					+ numSeconds + " --memory " + numMegabytes + " "
					+ configPath;

			/*
			 * String planCmd = scriptPath + "parser.py --plan --json --time " +
			 * numSeconds + " --memory " + numMegabytes + " " + configPath;
			 */
			System.out.println("PLANNER CALL");
			Process p = Runtime.getRuntime().exec(planCmd);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			String s = null;
			while ((s = stdInput.readLine()) != null) {
				// System.out.println(s);
			}

			while ((s = stdError.readLine()) != null) {
				// System.out.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public static JsonArray plan(String scriptPath, String configPath,
			int numSeconds, int numMegabytes) {
		runPlanner(scriptPath, configPath, numSeconds, numMegabytes);
		return readJSONPlanFile();
	}

	public static void main(String[] args) {

		// example
		String scriptPath = "	/Users/amministratore/Desktop/workspaceDO/CollectiveAdaptationEngine/journey-planner/parser/";
		String configPath = "	/Users/amministratore/Desktop/workspaceDO/CollectiveAdaptationEngine/configTest.json";

		JsonArray resArray = MultiagentPlannerCaller.plan(scriptPath,
				configPath, 30, 4096);

		for (JsonElement e : resArray) {
			JsonObject agent = e.getAsJsonObject();
			String type = agent.get("type").getAsString();
			String id = agent.get("id").getAsString();

			for (JsonElement ev : agent.get("events").getAsJsonArray()) {
				JsonObject event = ev.getAsJsonObject();
				double duration = event.get("duration").getAsDouble();
				double timestamp = event.get("timestamp").getAsDouble();

				String action = event.get("action").getAsString();
				if (action.equals("embark") || action.equals("debark")) {
					String vehicle = event.get("vehicle").getAsString();
					JsonArray location = event.get("location").getAsJsonArray();
					double latitude = location.get(0).getAsDouble();
					double longitude = location.get(1).getAsDouble();
				} else if (action.equals("travel") || action.equals("walk")) {
					JsonArray origin = event.get("origin").getAsJsonArray();
					double originLatitude = origin.get(0).getAsDouble();
					double originLongitude = origin.get(1).getAsDouble();

					JsonArray destination = event.get("destination")
							.getAsJsonArray();
					double destinationLatitude = destination.get(0)
							.getAsDouble();
					double destinationLongitude = destination.get(1)
							.getAsDouble();

					JsonArray path = event.get("path").getAsJsonArray();
					for (JsonElement pe : path) {
						JsonArray pathLocation = pe.getAsJsonArray();
						double locationLatitude = pathLocation.get(0)
								.getAsDouble();
						double locationLongitude = pathLocation.get(1)
								.getAsDouble();
					}
				}
			}
		}
	}
}
