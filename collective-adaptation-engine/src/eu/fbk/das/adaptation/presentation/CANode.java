package eu.fbk.das.adaptation.presentation;

import com.mxgraph.model.mxCell;

public class CANode extends mxCell {

    private String roleName;

    public String getRoleName() {
	return roleName;
    }

    public void setRoleName(String roleName) {
	this.roleName = roleName;
    }

    private boolean isRole;

    public boolean isRole() {
	return isRole;
    }

    public void setRole(boolean isRole) {
	this.isRole = isRole;
    }

    public boolean isCom() {
	return isCom;
    }

    public void setCom(boolean isCom) {
	this.isCom = isCom;
    }

    public boolean isOr() {
	return isOr;
    }

    public void setOr(boolean isOr) {
	this.isOr = isOr;
    }

    private boolean isCom;
    private boolean isOr;

}
