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

/*import org.json.JSONArray;
 import org.json.JSONObject;

 import com.google.maps.GeoApiContext;
 import com.google.maps.GeocodingApi;
 import com.google.maps.model.GeocodingResult;
 */

public class ViaggiaTrentoAPIWrapper {

	// https://dev.smartcommunitylab.it/ smart-planner2/ trentino/
	// rest/plan?from=46.066591%2C11.15&to=46.164298%2C11.002572
	// &date=02%2F11%2F2017&departureTime=09%3A44
	// &transportType=TRANSIT&routeType=fastest&numOfItn=3

	public ArrayList<TripAlternative> getViaggiaAlternatives(String from,
			String to, String time, String mean) throws JSONException {

		// call the Viaggia Trento service
		String url = "https://dev.smartcommunitylab.it/smart-planner2/trentino/rest/plan?from="
				+ from
				+ "&to="
				+ to
				+ "&date=03/11/2017&departureTime=09%3A44&transportType="
				+ mean + "&routeType=greenest&numOfItn=3";
		// System.out.println(url);
		ArrayList<TripAlternative> alternativesResult = new ArrayList<TripAlternative>();
		String result = callURL(url);

		JSONArray alternatives = new JSONArray(result);

		for (int i = 0; i < alternatives.length(); i++) {
			JSONObject alternative = (JSONObject) alternatives.get(i);
			Long alternativeDuration = alternative.getLong("duration");
			// for each alternative takes the legs
			JSONArray legs = new JSONArray();
			legs = alternative.getJSONArray("leg");

			ArrayList<Leg> tripLegs = new ArrayList<Leg>();
			for (int j = 0; j < legs.length(); j++) {
				JSONObject leg = (JSONObject) legs.get(j);
				// FROM
				JSONObject fromLeg = leg.getJSONObject("from");
				Double fromLon = fromLeg.getDouble("lon");
				Double fromLat = fromLeg.getDouble("lat");

				// TO
				JSONObject toLeg = leg.getJSONObject("to");
				Double toLon = toLeg.getDouble("lon");
				Double toLat = toLeg.getDouble("lat");

				// MEAN
				JSONObject transport = leg.getJSONObject("transport");
				String mean1 = transport.getString("type");

				// DURATIION
				Double duration = leg.getDouble("duration");

				Leg currentLeg = new Leg(mean1, duration, fromLon, fromLat,
						toLon, toLat);
				tripLegs.add(currentLeg);
			}

			// add the trip alternative to the final list
			TripAlternative alternativeToAdd = new TripAlternative(
					alternativeDuration, tripLegs);
			alternativesResult.add(alternativeToAdd);

		}

		return alternativesResult;
	}

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
