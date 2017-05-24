package eu.fbk.das.adaptation.utils;

public class Leg {

	public Leg(String mean, Double duration, Double fromLat, Double fromLon,
			Double toLat, Double toLon) {
		super();
		this.mean = mean;
		this.duration = duration;
		this.fromLat = fromLat;
		this.fromLon = fromLon;
		this.toLat = toLat;
		this.toLon = toLon;
	}

	private String mean;

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public Double getFromLat() {
		return fromLat;
	}

	public void setFromLat(Double fromLat) {
		this.fromLat = fromLat;
	}

	public Double getFromLon() {
		return fromLon;
	}

	public void setFromLon(Double fromLon) {
		this.fromLon = fromLon;
	}

	public Double getToLat() {
		return toLat;
	}

	public void setToLat(Double toLat) {
		this.toLat = toLat;
	}

	public Double getToLon() {
		return toLon;
	}

	public void setToLon(Double toLon) {
		this.toLon = toLon;
	}

	private Double duration;
	private Double fromLat;
	private Double fromLon;
	private Double toLat;
	private Double toLon;

}
