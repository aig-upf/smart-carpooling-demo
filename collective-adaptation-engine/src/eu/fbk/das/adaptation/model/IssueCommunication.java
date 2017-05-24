package eu.fbk.das.adaptation.model;

import java.util.ArrayList;

import eu.fbk.das.adaptation.EnsembleManager;
import eu.fbk.das.adaptation.RoleManager;
import eu.fbk.das.adaptation.ensemble.Issue;

/**
 * Issue communication
 * 
 * @author heorhi
 * 
 */
public class IssueCommunication {
    private int id;

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    // ensemble that owns the communication
    private EnsembleManager ensembleOwner;

    public EnsembleManager getEnsembleOwner() {
	return ensembleOwner;
    }

    public void setEnsembleOwner(EnsembleManager ensembleOwner) {
	this.ensembleOwner = ensembleOwner;
    }

    /**
     * role initiating the communication
     */
    private RoleManager originManager;

    /**
     * set of communication targets
     */

    /**
     * ISsue communicated
     */

    private Issue issueToSolve;

    public Issue getIssueToSolve() {
	return issueToSolve;
    }

    public void setIssueToSolve(Issue issueToSolve) {
	this.issueToSolve = issueToSolve;
    }

    private ArrayList<Target> targets;

    public ArrayList<Target> getTargets() {
	return targets;
    }

    public void setTargets(ArrayList<Target> targets) {
	this.targets = targets;
    }

    private CommunicationStatus status;

    public CommunicationStatus getStatus() {
	return status;
    }

    public void setStatus(CommunicationStatus status) {
	this.status = status;
    }

    public IssueCommunication(int id, RoleManager originManager, Issue issueToSolve, ArrayList<Target> targets,
	    CommunicationStatus status, EnsembleManager ensembleManager) {
	super();
	this.id = id;
	this.originManager = originManager;
	this.issueToSolve = issueToSolve;
	this.targets = targets;
	this.status = status;
	this.ensembleOwner = ensembleManager;
    }

    public static enum CommunicationStatus {
	INIT, END
    }

}
