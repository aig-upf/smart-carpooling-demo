package eu.fbk.das.adaptation;

import eu.fbk.das.adaptation.api.CollectiveAdaptationCommandExecution;
import eu.fbk.das.adaptation.api.RoleCommand;

public class DummyExecution implements CollectiveAdaptationCommandExecution {

    @Override
    public void applyCommand(String ensemble, RoleCommand command) {
	// TODO Auto-generated method stub
	System.out.println(command);

    }

    @Override
    public void endCommand() {
	// TODO Auto-generated method stub
	System.out.println("End Command");

    }

}
