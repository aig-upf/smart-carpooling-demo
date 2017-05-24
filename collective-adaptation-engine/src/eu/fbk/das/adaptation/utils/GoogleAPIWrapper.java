package eu.fbk.das.adaptation.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleAPIWrapper {

	public String getAddress(Double lat, Double longit) {
		String GoogleAPIKey = "AIzaSyBnLrMivSthmUmUipPfk5sidv7f0QvvDjg";
		String latString = lat.toString();
		String longString = longit.toString();
		String latlng = latString + ',' + longString;
		String URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ latlng + "&key=" + GoogleAPIKey;
		System.out.println(URL);
		String result = callURL(URL);
		// elaboro il JSON in uscita dalla call API
		String indirizzo = "";

		if (result.equalsIgnoreCase("erroreAPI")) {
			return indirizzo;
		} else {
			// System.out.println(result);
			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray routes = new JSONArray();
			try {
				routes = jsonObj.getJSONArray("results");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < routes.length(); i++) {

				System.out.println("Soluzione " + i + ":");
				JSONObject route = null;
				try {
					route = (JSONObject) routes.get(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(route);
				// indirizzo corrente
				String ind = null;
				try {
					ind = route.getString("formatted_address");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				indirizzo = ind;
				break;

			}
		}

		return indirizzo;
	}

	// Method to retrieve alternative by CAR

	public ArrayList<TripAlternative> getGoogleCarAlternatives(String from,
			String to) throws JSONException {

		// call the Google Transit API by CAR (Default)
		String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
				+ from
				+ "&destination="
				+ to
				+ "&key=AIzaSyBnLrMivSthmUmUipPfk5sidv7f0QvvDjg";

		ArrayList<TripAlternative> alternativesResult = new ArrayList<TripAlternative>();
		String result = callURL(url);

		// JSON Elaboration from the google result
		JSONArray routes = new JSONArray();
		ArrayList<Leg> tripLegs = new ArrayList<Leg>();
		System.out.println(result);
		JSONObject jsonObj = new JSONObject(result);
		routes = jsonObj.getJSONArray("routes");
		JSONArray legs = new JSONArray();
		JSONObject route = (JSONObject) routes.get(0);
		legs = route.getJSONArray("legs");
		if (legs.length() == 1) {
			JSONObject currentLeg = (JSONObject) legs.get(0);
			JSONObject distanceObject = currentLeg.getJSONObject("distance");
			Double distance = distanceObject.getDouble("value");
			JSONObject durationObject = currentLeg.getJSONObject("duration");
			Double duration = durationObject.getDouble("value");
			JSONObject fromObject = currentLeg.getJSONObject("start_location");
			Double fromLon = fromObject.getDouble("lng");
			Double fromLat = fromObject.getDouble("lat");
			JSONObject toObject = currentLeg.getJSONObject("end_location");
			Double toLon = fromObject.getDouble("lng");
			Double toLat = fromObject.getDouble("lat");

			Leg leg = new Leg("CAR", duration, fromLon, fromLat, toLon, toLat);
			tripLegs.add(leg);

		} else {
			for (int i = 0; i < legs.length(); i++) {
				JSONObject currentLeg = (JSONObject) legs.get(i);
				Double duration = currentLeg.getDouble("duration");

			}

		}
		// add the trip alternative to the final list
		TripAlternative alternativeToAdd = new TripAlternative(null, tripLegs);
		alternativesResult.add(alternativeToAdd);

		/*
		 * // add the trip alternative to the final list TripAlternative
		 * alternativeToAdd = new TripAlternative( alternativeDuration,
		 * tripLegs); alternativesResult.add(alternativeToAdd);
		 */

		return alternativesResult;
	}

	/*
	 * public String getCoordinates(String address) { String GoogleAPIKey =
	 * "AIzaSyBnLrMivSthmUmUipPfk5sidv7f0QvvDjg"; String URL =
	 * "https://maps.googleapis.com/maps/api/geocode/json?address=" + address +
	 * "&key=" + GoogleAPIKey; System.out.println(URL); String result =
	 * callURL(URL); // elaboro il JSON in uscita dalla call API String latlong
	 * = "";
	 * 
	 * if (result.equalsIgnoreCase("erroreAPI")) { return latlong; } else { //
	 * System.out.println(result); JSONObject jsonObj = new JSONObject(result);
	 * JSONArray routes = new JSONArray(); routes =
	 * jsonObj.getJSONArray("results"); for (int i = 0; i < routes.length();
	 * i++) {
	 * 
	 * JSONObject info = (JSONObject) routes.get(i); System.out.println(info);
	 * JSONObject geometry = info.getJSONObject("geometry"); JSONObject viewport
	 * = geometry.getJSONObject("viewport"); JSONObject coord =
	 * viewport.getJSONObject("southwest"); System.out.println(coord);
	 * 
	 * Double lat = coord.getDouble("lat"); Double lng = coord.getDouble("lng");
	 * 
	 * String latString = lat.toString(); String lngString = lng.toString();
	 * 
	 * latlong = latString + "," + lngString;
	 * 
	 * break;
	 * 
	 * } }
	 * 
	 * return latlong; }
	 */
	// returns the result of the API call as string
	public static String callURL(String myURL) {
		System.out.println(myURL);
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
			in.close();
		} catch (Exception e) {
			// throw {new RuntimeException("Exception while calling URL:"+
			// myURL, e);
			return "erroreAPI";

		}

		return sb.toString();
	}

}
