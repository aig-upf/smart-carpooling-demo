package eu.fbk.das.adaptation.api;

/**
 * An interface for classes able to apply {@link RoleCommand}
 */
public interface CollectiveAdaptationCommandExecution {

    /**
     * Apply {@link RoleCommand} for specificed ensemble
     * 
     * @param ensemble
     * @param command
     */
    public void applyCommand(String ensemble, RoleCommand command);

    /**
     * Notify that there are no more commands available
     */
    public void endCommand();

}
