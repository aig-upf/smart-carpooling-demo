package eu.fbk.das.adaptation.presentation.action;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eu.fbk.das.adaptation.presentation.CAWindow;

public class SelectIssueResolutionListener implements ListSelectionListener {

    private CAWindow window;

    public SelectIssueResolutionListener(CAWindow mainWindow) {
	this.window = mainWindow;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
	if (e != null && e.getSource() instanceof JList<?>) {
	    JList<?> lsm = (JList<?>) e.getSource();
	    for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
		if (lsm.isSelectedIndex(i)) {
		    // window.getController().setCurrentProcessWithName(lsm.getSelectedValue());
		    break;
		}
	    }
	}

    }
}