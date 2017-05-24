package eu.fbk.das.adaptation.api;

import java.util.List;

public class RoleCommand {
    private List<String> commands;
    private String role;

    public List<String> getCommands() {
	return commands;
    }

    public void setCommands(List<String> commands) {
	this.commands = commands;
    }

    public String getRole() {
	return role;
    }

    public void setRole(String role) {
	this.role = role;
    }

    public RoleCommand(List<String> commands, String role) {
	super();
	this.commands = commands;
	this.role = role;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("[" + role + "=");
	for (String c : commands) {
	    sb.append(c + ",");
	}
	return sb.toString();
    }

}
