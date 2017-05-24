package eu.fbk.das.adaptation.presentation;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

/** Small class to add some additionial behavior for nodes */
public class NodeLabel extends JLabel {

    private static final long serialVersionUID = -5833391150111660671L;
    Map<String, Point> connectionPoints = new HashMap<String, Point>();

    public NodeLabel(String text) {
	super(text);
	addComponentListener(new java.awt.event.ComponentAdapter() {
	    public void componentResized(java.awt.event.ComponentEvent evt) {
		mapConnectionPoints();
	    }

	    public void componentMoved(ComponentEvent e) {
		mapConnectionPoints();
	    }

	});
    }

    // updates the mapped positions of the connection points
    // called whenever the component get's resized or moved
    private void mapConnectionPoints() {
	connectionPoints.clear();
	Point point = new Point(getX(), getY() + getHeight() / 2);
	connectionPoints.put("LEFT", point);
	point = new Point(getX() + getWidth(), getY() + getHeight() / 2);
	connectionPoints.put("RIGHT", point);
	point = new Point(getX() + getWidth() / 2, getY());
	connectionPoints.put("TOP", point);
	point = new Point(getX() + getWidth() / 2, getY() + getHeight());
	connectionPoints.put("BOTTOM", point);
    }

    public Point getConnectionPoint(String key) {
	return connectionPoints.get(key);
    }
}