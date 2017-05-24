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

public class Rome2RioAPIWrapper {

	public ArrayList<TripAlternative> getRome2RioAlternatives(String partenza,
			String destinazione) throws JSONException {

		ArrayList<TripAlternative> alternatives = new ArrayList<TripAlternative>();
		String result = callURL("http://free.rome2rio.com/api/1.4/json/Search?key=Yt1V3vTI&oName="
				+ partenza + "&dName=" + destinazione);
		if (result.equalsIgnoreCase("erroreAPI")) {
			return alternatives;
		} else {
			// System.out.println(result);
			JSONObject jsonObj = new JSONObject(result);
			JSONArray routes = new JSONArray();
			routes = jsonObj.getJSONArray("routes");
			for (int i = 0; i < routes.length(); i++) {

				System.out.println("Soluzione " + i + ":");
				JSONObject route = (JSONObject) routes.get(i);
				System.out.println(route);
				// transportation mean
				String mean = route.getString("name");

				// PRICES
				JSONArray prices = new JSONArray();
				prices = route.getJSONArray("indicativePrices");
				JSONObject price = (JSONObject) prices.get(0);
				// System.out.println(price);

				// price
				Long priceInd = price.getLong("price");

				// distance
				Long distance = route.getLong("distance");

				// duration
				Long duration = route.getLong("totalDuration");

				TripAlternative alternative = new TripAlternative(mean,
						priceInd, distance, duration, null);
				alternatives.add(alternative);

			}
		}
		return alternatives;
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
