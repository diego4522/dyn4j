package org.dyn4j.sandbox.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.sandbox.utilities.UIUtilities;

/**
 * Panel used to create a polygon using arbitrary points.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArbitraryConvexPolygonPanel extends ConvexShapePanel implements InputPanel, ActionListener {
	/** The version id */
	private static final long serialVersionUID = 7259833235547997274L;

	/** The default point count of the unit circle polygon */
	private static final int DEFAULT_COUNT = 5;
	
	/** The default radius of the unit circle polygon */
	private static final double DEFAULT_RADIUS = 0.5;
	
	/** The default polygon is just a unit circle polygon */
	private static final Polygon DEFAULT_POLYGON = Geometry.createUnitCirclePolygon(DEFAULT_COUNT, DEFAULT_RADIUS);
	
	/** The list of point panels */
	private List<PointPanel> pointPanels = new ArrayList<PointPanel>();
	
	/** The panel containing the point panels */
	private JPanel pnlPanel;
	
	/** The scroll panel for the point panels */
	private JScrollPane scrPane;
	
	/** The text label for the polygon help */
	private JTextPane lblText;

	/** Panel used to preview the current shape */
	private PreviewPanel pnlPreview;
	
	/**
	 * Default constructor.
	 */
	public ArbitraryConvexPolygonPanel() {
		this.pnlPanel = new JPanel();
		
		this.lblText = new JTextPane();
		this.lblText.setBackground(null);
		this.lblText.setFont(UIUtilities.getDefaultLabelFont());
		this.lblText.setContentType("text");
		this.lblText.setText(
				"A polygon must have 3 or more vertices, counter-clockwise winding, " +
				"must be convex, and cannot have coincident vertices.");
		this.lblText.setEditable(false);
		this.lblText.setPreferredSize(new Dimension(350, 50));
		
		this.scrPane = new JScrollPane(this.pnlPanel);
		
		Vector2[] points = DEFAULT_POLYGON.getVertices();
		for (int i = 0; i < DEFAULT_COUNT; i++) {
			Vector2 p = points[i];
			PointPanel panel = new PointPanel(p.x, p.y);
			panel.addActionListener(this);
			this.pointPanels.add(panel);
		}
		
		this.pnlPreview = new PreviewPanel(new Dimension(150, 150), this.getPoints());
		this.pnlPreview.setBackground(Color.WHITE);
		this.pnlPreview.setBorder(BorderFactory.createEtchedBorder());
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(this.lblText)
				.addComponent(this.scrPane)
				.addComponent(this.pnlPreview));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(this.lblText)
				.addComponent(this.scrPane)
				.addComponent(this.pnlPreview));
		
		this.createLayout();
	}
	
	/**
	 * Creates the layout for the panel.
	 */
	private void createLayout() {
		// remove all the components
		this.pnlPanel.removeAll();
		
		// recreate the layout
		GroupLayout layout = new GroupLayout(this.pnlPanel);
		this.pnlPanel.setLayout(layout);
		
		// set all the flags
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		int size = this.pointPanels.size();
		
		// create the horizontal layout
		ParallelGroup hGroup = layout.createParallelGroup();
		for (int i = 0; i < size; i++) {
			PointPanel panel = this.pointPanels.get(i);
			hGroup.addComponent(panel);
			if (i < 3) {
				panel.btnRemove.setEnabled(false);
			} else {
				panel.btnRemove.setEnabled(true);
			}
		}
		// create the vertical layout
		SequentialGroup vGroup = layout.createSequentialGroup();
		for (int i = 0; i < size; i++) {
			PointPanel panel = this.pointPanels.get(i);
			vGroup.addComponent(panel);
		}
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		// find the point panel issuing the event
		int index = this.pointPanels.indexOf(event.getSource());
		// check if its found
		if (index >= 0) {
			// check the type of event
			if ("add".equals(event.getActionCommand())) {
				// insert a new point panel after this one
				PointPanel panel = new PointPanel();
				panel.addActionListener(this);
				this.pointPanels.add(index + 1, panel);
				// redo the layout
				this.createLayout();
				pnlPreview.setPoints(this.getPoints());
			} else if ("remove".equals(event.getActionCommand())) {
				// remove the point panel from the list
				this.pointPanels.remove(index);
				// redo the layout
				this.createLayout();
				pnlPreview.setPoints(this.getPoints());
			} else if ("changed".equals(event.getActionCommand())) {
				// a value has changed
				pnlPreview.setPoints(this.getPoints());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.sandbox.panels.ShapePanel#getDefaultShape()
	 */
	@Override
	public Convex getDefaultShape() {
		return DEFAULT_POLYGON;
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.sandbox.panels.ShapePanel#getShape()
	 */
	@Override
	public Convex getShape() {
		Vector2[] points = this.getPoints();
		Polygon polygon = new Polygon(points);
		return polygon;
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.sandbox.panels.InputPanel#isValidInput()
	 */
	@Override
	public boolean isValidInput() {
		Vector2[] points = this.getPoints();
		try {
			new Polygon(points);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.sandbox.panels.InputPanel#showInvalidInputMessage(java.awt.Window)
	 */
	@Override
	public void showInvalidInputMessage(Window owner) {
		Vector2[] points = this.getPoints();
		try {
			new Polygon(points);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(owner, e.getMessage(), "Notice", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Returns a list of points from the point panels.
	 * @return Vector2[]
	 */
	private Vector2[] getPoints() {
		int size = this.pointPanels.size();
		Vector2[] points = new Vector2[size];
		for (int i = 0; i < size; i++) {
			PointPanel panel = this.pointPanels.get(i);
			points[i] = new Vector2(panel.getValueX(), panel.getValueY());
		}
		return points;
	}
}
