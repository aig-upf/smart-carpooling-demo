package eu.fbk.das.adaptation.model;

import java.util.ArrayList;

import eu.fbk.das.adaptation.RoleManager;
import eu.fbk.das.adaptation.ensemble.Issue;
import eu.fbk.das.adaptation.ensemble.Solution;

/**
 * Issue resolution
 * 
 * @author Antonio Bucchiarone
 * 
 */
public class IssueResolution {

    /**
     * id of che CollectiveAdaptation problem
     */
    private int capID;

    private int issueResolutionID;

    public int getIssueResolutionID() {
	return issueResolutionID;
    }

    private boolean root;

    public boolean isRoot() {
	return root;
    }

    public void setRoot(boolean root) {
	this.root = root;
    }

    public void setIssueResolutionID(int issueResolutionID) {
	this.issueResolutionID = issueResolutionID;
    }

    private int communicationsCounter;

    public void updateCommCounter() {
	this.communicationsCounter++;
    }

    public int getCommunicationsCounter() {
	return communicationsCounter;
    }

    public void setCommunicationsCounter(int communicationsCounter) {
	this.communicationsCounter = communicationsCounter;
    }

    public int getCapID() {
	return capID;
    }

    public void setCapID(int capID) {
	this.capID = capID;
    }

    public String getState() {
	return state;
    }

    public ArrayList<IssueCommunication> getCommunications() {
	return communications;
    }

    public void setCommunications(ArrayList<IssueCommunication> communications) {
	this.communications = communications;
    }

    private ArrayList<IssueCommunication> communications;

    public void setState(String state) {
	this.state = state;
    }

    /**
     * Status of the Issue Resolution
     */
    private String state;

    /*
     * INIT: first state of the Issue Resolution when it is generate
     */

    public String getStatus() {
	return state;
    }

    public void setStatus(String state) {
	this.state = state;
    }

    /**
     * current role that solves the issue
     */
    private RoleManager roleCurrent;

    public RoleManager getRoleCurrent() {
	return roleCurrent;
    }

    public void setRoleCurrent(RoleManager roleCurrent) {
	this.roleCurrent = roleCurrent;
    }

    public RoleManager getRoleSource() {
	return roleSource;
    }

    public void setRoleSource(RoleManager roleSource) {
	this.roleSource = roleSource;
    }

    /**
     * role from which the issue instance arrived. Could be also internal
     */
    private RoleManager roleSource;

    /**
     * issue instance to be resolved
     */
    private Issue issueInstance;

    public Issue getIssueInstance() {
	return issueInstance;
    }

    public void setIssueInstance(Issue issueInstance) {
	this.issueInstance = issueInstance;
    }

    /**
     * list of alternative solutions
     */
    private ArrayList<Solution> solutions;

    public ArrayList<Solution> getSolutions() {
	return solutions;
    }

    public void setSolutions(ArrayList<Solution> solutions) {
	this.solutions = solutions;
    }

    public IssueResolution(int capid, String state, RoleManager roleSource, RoleManager currentRole,
	    Issue issueInstance, ArrayList<Solution> solutions) {
	super();

	this.state = state;
	this.roleSource = roleSource;
	this.roleCurrent = currentRole;
	this.issueInstance = issueInstance;
	this.solutions = solutions;
	this.capID = capid;
    }

}
