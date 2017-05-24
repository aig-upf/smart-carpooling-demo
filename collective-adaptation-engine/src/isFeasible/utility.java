package isFeasible;
import java.util.List;

public class utility{
	
	public static boolean isFeasible(){
		return true;
	}
	
	private static double calculateCost(double travelTime, double oldCost, double delay){
		double cost = 0.0;

		if(delay>0 && delay<travelTime)
			cost = oldCost -(0.9*oldCost*delay)/travelTime; 
		else if (travelTime<=delay)
			cost = 0.1*oldCost;
		
		return cost;
	}
	
	private static double computeUtility(double travelTime, double cost, double wTravelTime, double wCost, double maxTravelTime, double maxCost){
		double utility = 0.0;
		double a = 0.5;
		double b = 0.5;
		
		utility = Math.exp(wTravelTime*(-(a*travelTime)/maxTravelTime)+wCost*(-(b*cost)/maxCost));
		
		return utility;
	}
	
	public static int acceptSolution(double travelTime, double cost, double newTravelTime, double newCost, double wTravelTime, double wCost, double maxTravelTime, double maxCost){
		int ans=0;
		double oldUtility = 0.0;
		double newUtility = 0.0;
		
		oldUtility = computeUtility(travelTime, cost, wTravelTime, wCost, maxTravelTime, maxCost);
		newUtility = computeUtility(newTravelTime, newCost, wTravelTime, wCost, maxTravelTime, maxCost);
		if(oldUtility<newUtility)
			ans=1;
		else
			ans=0;
		
		return ans;
	}
	
	public static boolean isFeasible(List<Passenger> passengers, double delay){
		boolean ans = true;
		double cost = 0.0;
		int response = 0;
		
		for(Passenger p:passengers){
			cost = calculateCost(p.getTravelTime(),p.getCost(),delay);
			response = acceptSolution(p.getTravelTime(),p.getCost(),p.getTravelTime()+delay,cost,p.getWTravel(),p.getWCost(), p.getMaxTravel(),p.getMaxCost());
			if(response == 0)
				ans = false;
		}
		return ans;
	}

}