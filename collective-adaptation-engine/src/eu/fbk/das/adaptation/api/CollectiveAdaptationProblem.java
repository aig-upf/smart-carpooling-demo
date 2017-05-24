package eu.fbk.das.adaptation.api;

import java.util.List;

public class CollectiveAdaptationProblem {

    private String capID;
    private List<CollectiveAdaptationEnsemble> ensembles;

    public List<CollectiveAdaptationEnsemble> getEnsembles() {
	return ensembles;
    }

    public void setEnsembles(List<CollectiveAdaptationEnsemble> ensembles) {
	this.ensembles = ensembles;
    }

    public String getCapID() {
	return capID;
    }

    public void setCapID(String capID) {
	this.capID = capID;
    }

    private String Issue;
    private String startingRole;
    private String startingRoleEnsemble;
    private String target;

    public String getIssue() {
	return Issue;
    }

    public void setIssue(String issue) {
	Issue = issue;
    }

    public CollectiveAdaptationProblem(String capID,
	    List<CollectiveAdaptationEnsemble> ensembles, String issue,
	    String startingRole, String ensemble, String target) {
	super();
	this.capID = capID;
	this.ensembles = ensembles;
	Issue = issue;
	this.startingRole = startingRole;
	this.startingRoleEnsemble = ensemble;
	this.target = target;
    }

    public String getStartingRole() {
	return startingRole;
    }

    public void setStartingRole(String startingRole) {
	this.startingRole = startingRole;
    }

    public String getStartingRoleEnsemble() {
	return startingRoleEnsemble;
    }

    public void setStartingRoleEnsemble(String startingRoleEnsemble) {
	this.startingRoleEnsemble = startingRoleEnsemble;
    }

    public String getTarget() {
	return target;
    }

    public void setTarget(String target) {
	this.target = target;
    }

}
