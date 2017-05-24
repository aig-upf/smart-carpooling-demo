package eu.fbk.das.adaptation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import eu.fbk.das.adaptation.api.RoleCommand;
import eu.fbk.das.adaptation.ensemble.Analyzer;
import eu.fbk.das.adaptation.ensemble.Role;
import eu.fbk.das.adaptation.ensemble.Solver;
import eu.fbk.das.adaptation.model.IssueCommunication;
import eu.fbk.das.adaptation.model.IssueResolution;

public class RoleManager {

    Role role;
    private RoleCommand roleCommands;

    public RoleCommand getRoleCommands() {
	return roleCommands;
    }

    public void setRoleCommands(RoleCommand roleCommands) {
	this.roleCommands = roleCommands;
    }

    int minDepth;

    public int getMinDepth() {
	return minDepth;
    }

    public void setMinDepth(int minDepth) {
	this.minDepth = minDepth;
    }

    public int getMaxDepth() {
	return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
	this.maxDepth = maxDepth;
    }

    public int getMinExtent() {
	return minExtent;
    }

    public void setMinExtent(int minExtent) {
	this.minExtent = minExtent;
    }

    public int getMaxExtent() {
	return maxExtent;
    }

    public void setMaxExtent(int maxExtent) {
	this.maxExtent = maxExtent;
    }

    private List<Solver> solverInstances;

    public List<Solver> getSolverInstances() {
	return solverInstances;
    }

    public void setSolverInstances(List<Solver> solverInstances) {
	this.solverInstances = solverInstances;
    }

    int maxDepth;
    int minExtent;
    int maxExtent;
    int crossEnsembleIssues;

    ArrayList<Integer> initialValues;

    public ArrayList<Integer> getInitialValues() {
	return initialValues;
    }

    public void setInitialValues(ArrayList<Integer> initialValues) {
	this.initialValues = initialValues;
    }

    private ConcurrentHashMap<Solver, List<IssueCommunication>> solutions;

    public void AddSolution(Solver s, List<IssueCommunication> communications) {
	if (this.solutions == null) {
	    this.solutions = new ConcurrentHashMap<Solver, List<IssueCommunication>>();
	    this.solutions.put(s, communications);
	} else {
	    this.solutions.put(s, communications);
	}
    }

    public ConcurrentHashMap<Solver, List<IssueCommunication>> getSolutions() {
	return solutions;
    }

    public void setSolutions(ConcurrentHashMap<Solver, List<IssueCommunication>> solutions) {
	if (this.solutions == null) {
	    this.solutions = new ConcurrentHashMap<Solver, List<IssueCommunication>>();
	} else {
	    this.solutions = solutions;
	}
    }

    EnsembleManager Ensemble;

    public EnsembleManager getEnsemble() {
	return Ensemble;
    }

    public void setEnsemble(EnsembleManager ensemble) {
	Ensemble = ensemble;
    }

    // variable used to check if a role has a commit to do in the overall
    // adaptation solution.
    boolean commit;

    // int agentInvolvedinResolution;

    public boolean isCommit() {
	return commit;
    }

    public void setCommit(boolean commit) {
	this.commit = commit;
    }

    public int getCrossEnsembleIssues() {
	return crossEnsembleIssues;
    }

    public void setCrossEnsembleIssues(int crossEnsembleIssues) {
	this.crossEnsembleIssues = crossEnsembleIssues;
    }

    public Role getRole() {
	return role;
    }

    public void setRole(Role role) {
	this.role = role;
    }

    ArrayList<IssueResolution> issueResolutions;

    public ArrayList<IssueResolution> getIssueResolutions() {
	if (issueResolutions == null) {
	    issueResolutions = new ArrayList<IssueResolution>();
	    return issueResolutions;
	} else {
	    return issueResolutions;
	}
    }

    public void setResolutions(ArrayList<IssueResolution> resolutions) {
	this.issueResolutions = resolutions;
    }

    private Analyzer analyzer;

    public Analyzer getAnalyzer() {
	if (analyzer == null) {
	    analyzer = new Analyzer();
	    return analyzer;
	} else {
	    return analyzer;
	}
    }

    public void setAnalyzer(Analyzer analyzer) {
	this.analyzer = analyzer;
    }

    public RoleManager(Role role) {
	super();
	this.role = role;
	this.crossEnsembleIssues = 0;
    }

    public void addIssueResolution(IssueResolution resolution) {
	if (this.issueResolutions == null) {
	    this.issueResolutions = new ArrayList<IssueResolution>();
	    this.issueResolutions.add(resolution);
	} else {
	    this.issueResolutions.add(resolution);
	}

    }

    public void addcrossEnsembleIssues() {
	// TODO Auto-generated method stub
	// System.out.println("esterne ensemble: " + this.crossEnsembleIssues);
	this.crossEnsembleIssues++;
	// System.out.println("esterne ensemble: " + this.crossEnsembleIssues);

    }

}
