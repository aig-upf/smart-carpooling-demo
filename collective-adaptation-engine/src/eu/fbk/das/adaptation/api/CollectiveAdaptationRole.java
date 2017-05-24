package eu.fbk.das.adaptation.api;

public class CollectiveAdaptationRole {

    private String Role;
    private int id;
    private double travel_time;
    private double cost;
    private double weightTravelTime;
    private double weightCost;
    private long maxTravelTime;
    private double maxCost;

    public String getRole() {
	return Role;
    }

    public void setRole(String role) {
	Role = role;
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public double getTravel_time() {
	return travel_time;
    }

    public void setTravel_time(double travel_time) {
	this.travel_time = travel_time;
    }

    public double getCost() {
	return cost;
    }

    public void setCost(double cost) {
	this.cost = cost;
    }

    public double getWeightTravelTime() {
	return weightTravelTime;
    }

    public void setWeightTravelTime(double weightTravelTime) {
	this.weightTravelTime = weightTravelTime;
    }

    public double getWeightCost() {
	return weightCost;
    }

    public void setWeightCost(double weightCost) {
	this.weightCost = weightCost;
    }

    public long getMaxTravelTime() {
	return maxTravelTime;
    }

    public void setMaxTravelTime(long l) {
	this.maxTravelTime = l;
    }

    public double getMaxCost() {
	return maxCost;
    }

    public void setMaxCost(double maxCost) {
	this.maxCost = maxCost;
    }

}
