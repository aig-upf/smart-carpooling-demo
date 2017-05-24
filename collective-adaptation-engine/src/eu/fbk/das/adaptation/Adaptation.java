package eu.fbk.das.adaptation;

import java.util.ArrayList;

import eu.fbk.das.adaptation.utils.Leg;

public class Adaptation extends Loggable {

	private String AdaptationType;

	public String getAdaptationType() {
		return AdaptationType;
	}

	public void setAdaptationType(String adaptationType) {
		AdaptationType = adaptationType;
	}

	public String getRole() {
		return Role;
	}

	public void setRole(String role) {
		Role = role;
	}

	public ArrayList<Leg> getLegs() {
		return Legs;
	}

	public void setLegs(ArrayList<Leg> legs) {
		Legs = legs;
	}

	private String Role;
	private ArrayList<Leg> Legs = new ArrayList<Leg>();

	public Adaptation() {
		super();
	}

	public Adaptation(String type, String role, ArrayList<Leg> legs) {
		this.AdaptationType = type;
		this.Role = role;
		this.Legs = legs;

		return;
	}

	/*
	 * public String toStringExtended(String scenario) { String result = ""; if
	 * (scenario.equalsIgnoreCase("Mobility")) { ivIssues = ivIssuesMobility;
	 * result = toString(); } else { ivIssues = ivIssuesDrones; result =
	 * toString(); } return result; }
	 * 
	 * @Override public String toString() {
	 * 
	 * if (this.scenario.equalsIgnoreCase("Drones")) { return "Treatment [id=" +
	 * this.id + ", iv1=" + iv1 + ", ivIssuesDrones=" +
	 * Arrays.toString(ivIssuesDrones) + "]";
	 * 
	 * } else { return "Treatment [id=" + this.id + ", iv1=" + iv1 +
	 * ", ivIssuesMobility=" + Arrays.toString(ivIssuesMobility) + "]";
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public String getCsvFileHeader(String commaDelimiter) { String result =
	 * "id" + commaDelimiter + "iv1" + commaDelimiter + "iv2" + commaDelimiter +
	 * "iv3" + commaDelimiter + "iv4" + commaDelimiter + "iv5" + commaDelimiter
	 * + "iv6"; return result; }
	 */
	/*
	 * public String toCsvExtended(String commaDelimiter, String scenario) {
	 * String result = ""; if (scenario.equalsIgnoreCase("Mobility")) { ivIssues
	 * = ivIssuesMobility; result = toCsv(commaDelimiter); } else { ivIssues =
	 * ivIssuesDrones; result = toCsv(commaDelimiter); } return result; }
	 */
	public String toCsv(String commaDelimiter) {
		String result = "";
		result += this.AdaptationType + commaDelimiter;
		result += this.Role + commaDelimiter;
		result += this.Legs + commaDelimiter;

		/*
		 * if (this.scenario.equalsIgnoreCase("Drones")) { result +=
		 * Arrays.toString(ivIssuesDrones).replace(" ", "") .replace("[",
		 * "").replace("]", "") .replace(",", commaDelimiter); } else { result
		 * += Arrays.toString(ivIssuesMobility).replace(" ", "") .replace("[",
		 * "").replace("]", "") .replace(",", commaDelimiter); }
		 */
		return result;
	}

	public Adaptation clone() {
		Adaptation result = new Adaptation();
		result.AdaptationType = this.AdaptationType;
		result.Role = this.Role;
		result.Legs = this.Legs;
		return result;
	}

	@Override
	String getCsvFileHeader(String commaDelimiter) {
		// TODO Auto-generated method stub
		return null;
	}

}