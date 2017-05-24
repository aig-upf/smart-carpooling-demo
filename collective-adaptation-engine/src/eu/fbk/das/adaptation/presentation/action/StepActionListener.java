package eu.fbk.das.adaptation.presentation.action;

import static eu.fbk.das.adaptation.Constant.STEP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StepActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
	switch (e.getActionCommand()) {
	case STEP:
	    // MainController.post(new StepEvent());
	    break;
	}
    }

}
