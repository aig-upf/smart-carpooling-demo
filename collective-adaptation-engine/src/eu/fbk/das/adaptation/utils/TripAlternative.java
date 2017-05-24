package eu.fbk.das.adaptation.utils;

import java.util.ArrayList;

public class TripAlternative {

	public TripAlternative(String mean, Long price, Long duration,
			Long distance, ArrayList<Leg> legs) {
		super();
		this.mean = mean;
		this.price = price;
		this.duration = duration;
		this.distance = distance;
		this.legs = legs;
	}

	// constructor for ViaggiaTrento
	public TripAlternative(Long duration, ArrayList<Leg> legs) {
		super();

		this.duration = duration;
		this.legs = legs;
	}

	private ArrayList<Leg> legs;

	public ArrayList<Leg> getLegs() {
		return legs;
	}

	public void setLegs(ArrayList<Leg> legs) {
		this.legs = legs;
	}

	private String mean;
	private Long price;
	private Long duration;
	private Long distance;

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getDistance() {
		return distance;
	}

	public void setDistance(Long distance) {
		this.distance = distance;
	}
}
