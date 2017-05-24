package eu.fbk.das.adaptation.presentation.action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;

import eu.fbk.das.adaptation.EnsembleManager;
import eu.fbk.das.adaptation.presentation.CAWindow;

// Handler mouse click on activity
public class MouseTreeNodeListener extends MouseAdapter {

    private static final Logger logger = LogManager.getLogger(MouseTreeNodeListener.class);

    private mxGraphComponent graphComponent;

    private List<EnsembleManager> ems;
    private CAWindow window;

    private boolean clicked = false;

    public MouseTreeNodeListener(mxGraphComponent graphComponent, List<EnsembleManager> ens, CAWindow window) {
	this.graphComponent = graphComponent;
	this.ems = ens;
	this.window = window;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
	Object cell = graphComponent.getCellAt(e.getX(), e.getY());

	if (!clicked && cell != null) {
	    if (cell instanceof mxCell) {
		if (((mxCell) cell).isVertex()) {
		    String label = graphComponent.getGraph().getLabel(cell);
		    boolean found = false;
		    for (int i = 0; i < this.ems.size(); i++) {
			EnsembleManager em = ems.get(i);

			if (found) {
			    break;
			}

		    }

		    // show the possible solutions of the OR node

		    // window.updatePossibleSolutions(capID, issue, em,
		    // solvers);

		    // String user = window.getController().getCurrentUser();
		    /*
		     * switch (label) { case "UMS_UtilityRanking": if
		     * (window.getController() != null) {
		     * window.getUtilityView().setData(
		     * window.getController().getUserData(user));
		     * window.showUtilityFrame(true); } break; case
		     * "UMS_SecurityAndPrivacyFiltering": if
		     * (window.getController() != null) {
		     * window.getPSView().setData(
		     * window.getController().getUserData(user));
		     * 
		     * window.showPSFrame(true); } break; default:
		     * window.getController().post( new
		     * SelectedAbstractActivityEvent(label)); //
		     * JOptionPane.showMessageDialog(graphComponent, "cell=" //
		     * + graphComponent.getGraph().getLabel(cell)); break; }
		     */
		}
	    }
	}
    }
}
