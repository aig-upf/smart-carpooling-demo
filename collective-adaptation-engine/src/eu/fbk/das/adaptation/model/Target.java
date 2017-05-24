package eu.fbk.das.adaptation.model;

import eu.fbk.das.adaptation.RoleManager;
import eu.fbk.das.adaptation.ensemble.Solver;

public class Target {

    public Target(RoleManager targetRoleManager, Solver solver, TargetStatus status) {
	super();
	this.targetRoleManager = targetRoleManager;
	this.solverInstance = solver;
	this.status = status;
    }

    private RoleManager targetRoleManager;

    public RoleManager getTargetRole() {
	return targetRoleManager;
    }

    public void setTargetRole(RoleManager targetRole) {
	this.targetRoleManager = targetRoleManager;
    }

    private Solver solverInstance;
    private TargetStatus status;

    // double fitness;

    public TargetStatus getStatus() {
	return status;
    }

    public void setStatus(TargetStatus status) {
	this.status = status;
    }

    public static enum TargetStatus {
	INIT, FORWARDED, END
    }

}
