package eu.fbk.das.adaptation.presentation.action;

import java.util.Map.Entry;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eu.fbk.das.adaptation.EnsembleManager;
import eu.fbk.das.adaptation.presentation.CATree;
import eu.fbk.das.adaptation.presentation.CAWindow;

public class RankedSolutionTableSelectionListener implements ListSelectionListener {

    private JTable table;
    private EnsembleManager ensembleManager;
    private CAWindow window;

    public RankedSolutionTableSelectionListener(JTable IssueResolutionsList, EnsembleManager ens, CAWindow window) {
	this.table = IssueResolutionsList;
	this.ensembleManager = ens;
	this.window = window;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

	// System.out.println("Eccolo");
	if (e.getSource() instanceof DefaultListSelectionModel) {

	    DefaultListSelectionModel lsm = (DefaultListSelectionModel) e.getSource();
	    for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
		if (lsm.isSelectedIndex(i)) {
		    String value = (String) table.getValueAt(i, 0);
		    // String roleManager = (String) table.getValueAt(i, 1);
		    // here I need to find the right CAP ID and the
		    // corresponding RoleManager
		    // System.out.println(value);
		    for (Entry<String, CATree> entry : ensembleManager.getTrees().entrySet()) {
			String key = entry.getKey();
			CATree tree = entry.getValue();

			if ((key.equals(value))) {
			    window.updateTree(tree);

			    break;
			}

		    }
		    break;

		}
	    }

	}

    }

}
