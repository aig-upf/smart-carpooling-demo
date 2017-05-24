package eu.fbk.das.adaptation.utils;

import java.util.ArrayList;
import java.util.List;

public class Distance {
	public static void main(String[] args) {
		// [[46.0652427,11.123157],[46.065334,11.123119],[46.0653629,11.1231062]]
		// 46.064835, 11.156579
		// 46.066197, 11.154809
		// 46.067239, 11.151427
		Distance obj = new Distance();
		ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
		Coordinate a = new Coordinate();
		Coordinate b = new Coordinate();
		Coordinate c = new Coordinate();
		a.setLat(46.064835);
		a.setLng(11.156579);
		b.setLat(46.066197);
		b.setLng(11.154809);

		c.setLat(46.067239);
		c.setLng(11.151427);
		coordinates.add(a);
		coordinates.add(b);
		coordinates.add(c);
		double distance = distanceNew(46.064835, 11.156579, 46.066197,
				11.154809);
		double distance1 = distanceNew(46.066197, 11.154809, 46.067239,
				11.151427);
		double distance2 = distance + distance1;

		System.out.println(distance + " Meters\n");
		System.out.println(distance1 + " Meters\n");
		System.out.println(distance2 + " Meters\n");

		double Total = TotalDistance(coordinates);
		System.out.println(Total + " Meters\n");
	}

	public static double distanceNew(double lat1, double lng1, double lat2,
			double lng2) {
		int r = 6371; // average radius of the earth in km
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = r * c;
		return d * 1000;
	}

	public static double distance(double lat1, double lon1, double lat2,
			double lon2, String sr) {

		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (sr.equals("K")) {
			dist = dist * 1.609344;
		} else if (sr.equals("N")) {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	public static double TotalDistance(List<Coordinate> coordinates) {
		double result = 0;

		int size = coordinates.size();
		// System.out.println("size= " + size);
		if (coordinates.size() == 1) {
			result = 0;
		} else if (coordinates.size() == 2) {
			result = distanceNew(coordinates.get(0).getLat(), coordinates
					.get(0).getLng(), coordinates.get(1).getLat(), coordinates
					.get(1).getLng());
			// System.out.println("parziale: " + result);
		} else {

			double partial = distanceNew(coordinates.get(0).getLat(),
					coordinates.get(0).getLng(), coordinates.get(1).getLat(),
					coordinates.get(1).getLng());
			// System.out.println(partial);
			result = partial + TotalDistance(coordinates.subList(1, size));
			// System.out.println("parziale: " + result);
		}
		return result;

	}

	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

}
