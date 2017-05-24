package eu.fbk.das.adaptation.api;

import java.util.HashMap;
import java.util.List;

public class CollectiveAdaptationSolution {

    private String capID;
    private HashMap<String, List<RoleCommand>> ensembleCommands;

    public String getCapID() {
	return capID;
    }

    public void setCapID(String capID) {
	this.capID = capID;
    }

    public HashMap<String, List<RoleCommand>> getEnsembleCommands() {
	return ensembleCommands;
    }

    public void setEnsembleCommands(
	    HashMap<String, List<RoleCommand>> ensembleCommands) {
	this.ensembleCommands = ensembleCommands;
    }

    public CollectiveAdaptationSolution(String capID,
	    HashMap<String, List<RoleCommand>> ensembleCommands) {
	super();
	this.capID = capID;
	this.ensembleCommands = ensembleCommands;
    }

}
