package eu.fbk.das.adaptation.presentation;

import java.util.ArrayList;

public class TreeNode {

    protected String name;

    private int order;

    private int source;

    public ArrayList<Integer> getTarget() {
	return target;
    }

    public void setTarget(ArrayList<Integer> target) {
	this.target = target;
    }

    public void setSource(int source) {
	this.source = source;
    }

    private ArrayList<Integer> target;

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

    private int id;

    private String from;

    public TreeNode() {

    }

    public TreeNode(String name) {
	this.name = name;

    }

    public TreeNode(int SourceState, int TargetState, String name, String type) {
	this.name = name;
	// this.type = type;

    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public int getSource() {
	return source;
    }

    public void setFrom(String from) {
	this.from = from;
    }

    public String getFrom() {
	return this.from;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

}
