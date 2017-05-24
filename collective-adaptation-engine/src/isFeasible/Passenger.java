package isFeasible;

public class Passenger {
    int id;
    double travel_time;
    double cost;
    double weightTravelTime;
    double weightCost;
    long maxTravelTime;
    double maxCost;

    public Passenger(int id, double travel_time, double cost, double wTravel,
	    double wCost, int maxTravel, double maxCost) {
	this.id = id;
	this.travel_time = travel_time;
	this.cost = cost;
	this.weightTravelTime = wTravel;
	this.weightCost = wCost;
	this.maxTravelTime = maxTravel;
	this.maxCost = maxCost;

    }

    public Passenger() {
	;
    }

    public int getId() {
	return id;
    }

    public double getTravelTime() {
	return travel_time;
    }

    public double getCost() {
	return cost;
    }

    public double getWTravel() {
	return weightTravelTime;
    }

    public double getWCost() {
	return weightCost;
    }

    public long getMaxTravel() {
	return maxTravelTime;
    }

    public double getMaxCost() {
	return maxCost;
    }

    public void setId(int id) {
	this.id = id;
    }

    public void setTravelTime(double travel_time) {
	this.travel_time = travel_time;
    }

    public void setCost(double cost) {
	this.cost = cost;
    }

    public void setWeightTravel(double wTravel) {
	this.weightTravelTime = wTravel;
    }

    public void setWeightCost(double wCost) {
	this.weightCost = wCost;
    }

    public void setMaxTravel(long maxTravel) {
	this.maxTravelTime = maxTravel;
    }

    public void setMaxCost(double maxCost) {
	this.maxCost = maxCost;
    }
}