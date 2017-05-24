package eu.fbk.das.adaptation.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import eu.fbk.das.adaptation.EnsembleManager;
import eu.fbk.das.adaptation.RoleManager;
import eu.fbk.das.adaptation.api.CollectiveAdaptationCommandExecution;
import eu.fbk.das.adaptation.api.CollectiveAdaptationProblem;
import eu.fbk.das.adaptation.api.CollectiveAdaptationSolution;
import eu.fbk.das.adaptation.api.RoleCommand;
import eu.fbk.das.adaptation.ensemble.Ensemble;
import eu.fbk.das.adaptation.ensemble.Issue;
import eu.fbk.das.adaptation.model.IssueCommunication;
import eu.fbk.das.adaptation.model.IssueResolution;
import eu.fbk.das.adaptation.presentation.action.IssueTableSelectionListener;
import eu.fbk.das.adaptation.presentation.action.MouseTreeNodeListener;

public class CAWindow extends JFrame {

	private static final Logger logger = LogManager.getLogger(CAWindow.class);
	private static final long serialVersionUID = -2707712944901661771L;

	private static final String STYLE_ROLE = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;align=center;fontSize=9;";
	private static final String STYLE_ISSUE_EDGE = "fontColor=#FF0000;fontSize=8;endArrow=classic;html=1;fontFamily=Helvetica;align=left;";
	private static final String STYLE_ROLE1 = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;align=center;fontSize=9;fillColor=90EE90";

	private final static String PROP_PATH = "adaptation.properties";
	private static final String STYLE_INIT = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;fillColor=white;size=2";

	public int counter = 0;

	// main frame
	public JFrame frame;

	public JPanel treePanel;

	// main window components

	private JPanel mainPanel;

	private Label label;

	// private JList<String> IssueResolutionsList;
	private JTable IssueResolutionsList;

	private JTable PossibleSolutionList;

	private JTable analyzerLog;
	private JTable monitorList;
	private JTable planningList;
	private JTable executeList;

	private List<EnsembleManager> ensm;

	private mxGraphLayout layout;
	private mxGraphLayout layout1;
	private JButton btnStep;

	private JScrollPane activeIssuesScrollPane;
	private JScrollPane PossibleSolutionScrollPane;

	private JScrollPane CATreeScrollPane;
	private JScrollPane activeMonitorScrollPane;
	private JScrollPane analyzerLogScrollPane;
	private JScrollPane plannerLogScrollPane;
	private JScrollPane executeLogScrollPane;
	private mxGraphComponent graphComponent;

	private mxGraphComponent graphComponentHierarchy;

	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	public void setGraphComponent(mxGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
	}

	private JPanel southPanel;
	private JTabbedPane tabbedPane_1;
	private JScrollPane hierarchyPanel;
	private JPanel buttonPanel;
	private JPanel spacerPanel;
	private JPanel panel_1;
	private JButton btnRunCollectiveAdaptation;
	private JButton btnApplySolution;
	private CollectiveAdaptationSolution solution;
	private CollectiveAdaptationCommandExecution executor;

	public CAWindow(List<EnsembleManager> ems, CollectiveAdaptationProblem cap,
			int IssueIndex, CollectiveAdaptationCommandExecution executor) {

		super("Collective Adaptation Viewer");
		this.executor = executor;
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(10);
		borderLayout.setHgap(10);

		this.ensm = ems;

		mainPanel = new JPanel();
		mainPanel.setVisible(true);
		// mettere solo preferredSize per far comparire le barre di scorrimento
		// verticali
		mainPanel.setPreferredSize(new Dimension(1124, 1300));
		mainPanel.setLayout(new BorderLayout(0, 0));

		// Log Text ARea
		// Label lblLog = new Label("Execution Log");
		// lblLog.setBounds(10, 550, 223, 22);
		// mainPanel.add(lblLog);

		// log inside a scrollpane logTextArea = new JTextArea("");
		// logTextArea = new JTextArea();
		// logTextArea.setBounds(10, 575, 982, 120);
		// logTextArea.setEditable(false);
		// JScrollPane logScrollPane = new JScrollPane(logTextArea);
		// logScrollPane.setBounds(10, 580, 982, 120);
		// mainPanel.add(logScrollPane);

		getContentPane().add(mainPanel);

		// window to show issue tables and trees

		Vector<String> columnNames = new Vector<String>();
		columnNames.add("N");
		columnNames.add("CAP ID");
		columnNames.add("Role");
		columnNames.add("Issue");
		columnNames.add("Issue Status");
		columnNames.add("Ensemble");

		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(1000, 50));
		buttonPanel.setBounds(new Rectangle(0, 0, 0, 80));
		mainPanel.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new BorderLayout(0, 0));

		label = new Label(
				"Select an active Issue Resolution to see its collective adaptation resolution tree");
		buttonPanel.add(label, BorderLayout.WEST);

		spacerPanel = new JPanel();
		buttonPanel.add(spacerPanel, BorderLayout.CENTER);

		panel_1 = new JPanel();
		buttonPanel.add(panel_1, BorderLayout.EAST);

		btnRunCollectiveAdaptation = new JButton("Run collective adaptation");
		btnRunCollectiveAdaptation.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO: run collective adaptation, enable apply solution button

				run(cap, ems, cap.getIssue(), cap.getCapID(),
						cap.getStartingRole(), IssueIndex);
				btnRunCollectiveAdaptation.setEnabled(false);
				btnApplySolution.setEnabled(true);

			}
		});
		btnRunCollectiveAdaptation
				.setToolTipText("run collective adaptation algorithm");
		panel_1.add(btnRunCollectiveAdaptation);

		btnApplySolution = new JButton("Apply solution");
		btnApplySolution.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO: send one command at time
				applySolution(solution);
				executor.endCommand();

			}
		});
		btnApplySolution.setEnabled(false);
		btnApplySolution
				.setToolTipText("apply collective adaptation solution, one command at time");
		panel_1.add(btnApplySolution);

		IssueResolutionsList = new JTable(null, columnNames);

		// TEST PRO-EDITING CON WINDOW BUILDER
		// {
		// @Override
		// public boolean isCellEditable(int r, int c) {
		// return false; // Disallow the editing of any cell
		// }
		//
		// };

		IssueResolutionsList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		IssueResolutionsList.setBounds(5, 125, 600, 400);
		IssueResolutionsList.setFillsViewportHeight(true);

		IssueResolutionsList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// IssueResolutionsList.getSelectionModel()
		// .addListSelectionListener(new
		// IssueTableSelectionListener(IssueResolutionsList, em, this));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setPreferredSize(new Dimension(600, 800));
		tabbedPane.setMinimumSize(new Dimension(400, 400));
		mainPanel.add(tabbedPane, BorderLayout.WEST);

		activeIssuesScrollPane = new JScrollPane(IssueResolutionsList);
		tabbedPane.addTab("Active Issue Resolutions", null,
				activeIssuesScrollPane, null);
		tabbedPane.setEnabledAt(0, true);

		// Load Trees Frame
		CATree cat = new CATree();
		// frame to see the issue resolution tree mxGraphComponent
		graphComponent = new mxGraphComponent(cat);

		CATree hierarchy = new CATree();
		graphComponentHierarchy = new mxGraphComponent(hierarchy);

		// layout = new mxParallelEdgeLayout(graphComponent.getGraph());

		// layout = new mxHierarchicalLayout(graphComponent.getGraph());
		// layout = new mxCompactTreeLayout(graphComponent.getGraph());

		graphComponent.setEnabled(false);

		graphComponent.getGraphControl().addMouseListener(
				new MouseTreeNodeListener(graphComponent, this.ensm, this));

		graphComponent.setBounds(600, 125, 650, 600);
		graphComponent.setBorder(javax.swing.BorderFactory
				.createLineBorder(new Color(0, 0, 0)));

		// GRAPH HIERARCHY

		graphComponentHierarchy.setGraph(hierarchy);
		graphComponentHierarchy.setEnabled(false);
		layout1 = new mxHierarchicalLayout(graphComponentHierarchy.getGraph());

		// layout1.execute(graphComponentHierarchy.getGraph().getDefaultParent());

		graphComponentHierarchy.getGraphControl().addMouseListener(
				new MouseTreeNodeListener(graphComponentHierarchy, this.ensm,
						this));

		graphComponentHierarchy.setBounds(600, 125, 650, 600);
		graphComponentHierarchy.setBorder(javax.swing.BorderFactory
				.createLineBorder(new Color(0, 0, 0)));

		layout1.execute(graphComponentHierarchy.getGraph().getDefaultParent());

		// Object v3 = hierarchy.insertNode(hierarchy.getDefaultParent(), null,
		// "RM_1", STYLE_ROLE);
		// Object v4 = hierarchy.insertNode(hierarchy.getDefaultParent(), null,
		// "RM_2", STYLE_ROLE);
		// hierarchy.insertEdge(hierarchy.getDefaultParent(), "", "", v2, v3,
		// STYLE_ISSUE_EDGE);
		// hierarchy.insertEdge(hierarchy.getDefaultParent(), "", "", v2, v4,
		// STYLE_ISSUE_EDGE);

		// layout.execute(graphComponentHierarchy.getGraph().getDefaultParent());

		JTabbedPane tabbedPaneTree = new JTabbedPane(JTabbedPane.TOP);
		tabbedPaneTree.setPreferredSize(new Dimension(800, 800));
		tabbedPaneTree.setMinimumSize(new Dimension(5, 800));
		mainPanel.add(tabbedPaneTree, BorderLayout.CENTER);

		CATreeScrollPane = new JScrollPane(graphComponent);
		tabbedPaneTree.addTab("Issue Resolution Tree", null, CATreeScrollPane,
				null);
		tabbedPaneTree.setEnabledAt(0, true);

		// Evoknowledge
		Vector<String> SolutionsColumnNames = new Vector<String>();
		SolutionsColumnNames.add("IssueType");
		SolutionsColumnNames.add("Predicted Solution");
		SolutionsColumnNames.add("Success Rate");
		SolutionsColumnNames.add("Feasibility");
		SolutionsColumnNames.add("Ranking");

		southPanel = new JPanel();
		southPanel.setPreferredSize(new Dimension(10, 400));
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout(0, 0));

		tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_1.setMaximumSize(new Dimension(32767, 400));
		tabbedPane_1.setPreferredSize(new Dimension(600, 300));
		southPanel.add(tabbedPane_1, BorderLayout.WEST);

		hierarchyPanel = new JScrollPane(graphComponentHierarchy);
		// CATreeScrollPane = new JScrollPane(graphComponent);

		tabbedPane_1.addTab("Ensembles Hierarchy", null, hierarchyPanel, null);

		PossibleSolutionList = new JTable(null, SolutionsColumnNames) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false; // Disallow the editing of any cell
			}

		};

		PossibleSolutionList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		PossibleSolutionList.setBounds(10, 520, 40, 22);
		PossibleSolutionList.setFillsViewportHeight(true);
		PossibleSolutionList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// TAB FOR SOLUTION PRUNING AND RANKING

		JTabbedPane tabbedPaneNew = new JTabbedPane(JTabbedPane.TOP);
		southPanel.add(tabbedPaneNew);
		tabbedPaneNew.setPreferredSize(new Dimension(5, 300));
		tabbedPaneNew.setMaximumSize(new Dimension(32767, 400));

		PossibleSolutionScrollPane = new JScrollPane(PossibleSolutionList);
		tabbedPaneNew.addTab("PREDICTION, FEASIBILITY, RANKING", null,
				PossibleSolutionScrollPane, null);
		tabbedPaneNew.setEnabledAt(0, true);

		// /////////////////
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1024, 768);

		this.setVisible(true);

	}

	public CAWindow() {
		// TODO Auto-generated constructor stub
	}

	private Vector<String> convertAndFilterForJtable(String counter,
			String capID, IssueResolution ir, EnsembleManager em) {
		// Vector<String> response = new Vector<String>();

		Vector<String> v = new Vector<String>();
		v.add(counter);
		v.add(capID);
		v.add(ir.getRoleCurrent().getRole().getType());
		v.add(ir.getIssueInstance().getIssueType());
		v.add(ir.getStatus());
		v.add(em.getEnsemble().getName());

		return v;
	}

	private Vector<Vector<String>> convertPlannerForJtable(List<String> plans) {
		Vector<Vector<String>> response = new Vector<Vector<String>>();
		/*
		 * for (IssueResolution ir : issueResolutions) { Vector<String> v = new
		 * Vector<String>(); v.add(ir.getRoleCurrent().getType());
		 * v.add(ir.getIssueInstance().getType());
		 * 
		 * response.add(v);
		 * 
		 * }
		 */
		return response;
	}

	private Vector<Vector<String>> convertExecuteForJtable(List<String> executes) {
		Vector<Vector<String>> response = new Vector<Vector<String>>();
		/*
		 * for (IssueResolution ir : issueResolutions) { Vector<String> v = new
		 * Vector<String>(); v.add(ir.getRoleCurrent().getType());
		 * v.add(ir.getIssueInstance().getType());
		 * 
		 * response.add(v);
		 * 
		 * }
		 */
		return response;
	}

	private Vector<Vector<String>> convertAnalyzerForJtable(List<String> logs) {
		Vector<Vector<String>> response = new Vector<Vector<String>>();
		/*
		 * for (IssueResolution ir : issueResolutions) { Vector<String> v = new
		 * Vector<String>(); v.add(ir.getRoleCurrent().getType());
		 * v.add(ir.getIssueInstance().getType());
		 * 
		 * response.add(v);
		 * 
		 * }
		 */
		return response;
	}

	private Vector<Vector<String>> convertMonitorForJtable(List<String> monitors) {
		Vector<Vector<String>> response = new Vector<Vector<String>>();
		/*
		 * for (IssueResolution ir : issueResolutions) { Vector<String> v = new
		 * Vector<String>(); v.add(ir.getRoleCurrent().getType());
		 * v.add(ir.getIssueInstance().getType());
		 * 
		 * response.add(v);
		 * 
		 * }
		 */
		return response;
	}

	/**
	 * Clear graph
	 * 
	 * @see {@link mxGraphModel#clear}
	 */
	public void clearTree() {
		mxGraph current = graphComponent.getGraph();
		((mxGraphModel) current.getModel()).clear();

	}

	public void updateHierarchy(CATree tree) {

		graphComponentHierarchy.setGraph(tree);
		layout1 = new mxHierarchicalLayout(graphComponentHierarchy.getGraph());

		layout1.execute(graphComponentHierarchy.getGraph().getDefaultParent());

		graphComponentHierarchy.repaint();
	}

	public void updateNodeColor(CATree tree, String nodeName) {
		for (int i = 0; i < tree.getNodesHierarchy().size(); i++) {
			mxCell node = (mxCell) tree.getNodesHierarchy().get(i);
			node.setStyle(STYLE_ROLE);
			// tree.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#FF000");

		}

	}

	public void updateTree(CATree tree) {

		graphComponent.setGraph(tree);
		layout = new mxHierarchicalLayout(graphComponent.getGraph());

		layout.execute(graphComponent.getGraph().getDefaultParent());
		graphComponent.repaint();

	}

	public void applySolution(CollectiveAdaptationSolution cas) {
		logger.info("Applying solution for collective problem with id "
				+ cas.getCapID());
		// for each ensemble
		for (String ensemble : cas.getEnsembleCommands().keySet()) {
			// get commands
			List<RoleCommand> commands = cas.getEnsembleCommands()
					.get(ensemble);
			for (int i = 0; i < commands.size(); i++) {
				executor.applyCommand(ensemble, commands.get(i));
			}

		}
		logger.info("Solution applied correctly");
	}

	public void loadTreeFrame() {

		CATree cat = new CATree();
		// frame to see the issue resolution tree mxGraphComponent
		graphComponent = new mxGraphComponent(cat);
		// layout = new mxParallelEdgeLayout(graphComponent.getGraph());

		// layout = new mxHierarchicalLayout(graphComponent.getGraph());

		graphComponent.getGraphControl().addMouseListener(
				new MouseTreeNodeListener(graphComponent, this.ensm, this));

		graphComponent.setBounds(420, 125, 400, 400);
		graphComponent.setBorder(javax.swing.BorderFactory
				.createLineBorder(new Color(0, 0, 0)));

		graphComponent.setEnabled(true);
		// layout.execute(graphComponent.getGraph().getDefaultParent());

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(420, 125, 400, 400);
		mainPanel.add(tabbedPane);

		CATreeScrollPane = new JScrollPane(graphComponent);
		tabbedPane
				.addTab("Issue Resolution Tree", null, CATreeScrollPane, null);
		tabbedPane.setEnabledAt(0, true);
	}

	public void loadAnalyzerFrame(List<String> logs, Ensemble e) {
		Vector<Vector<String>> data = convertAnalyzerForJtable(logs);
		// List of Active Issue Resolutions
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("State");

		analyzerLog = new JTable(data, columnNames) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false; // Disallow the editing of any cell
			}

		};
		analyzerLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		analyzerLog.setBounds(640, 310, 200, 130);
		analyzerLog.setFillsViewportHeight(true);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(640, 310, 200, 130);
		mainPanel.add(tabbedPane);

		analyzerLogScrollPane = new JScrollPane(analyzerLog);
		tabbedPane.addTab("Analyzing", null, analyzerLogScrollPane, null);
		tabbedPane.setEnabledAt(0, true);

		refreshWindow();
	}

	public void loadMonitoringTable(List<String> activeMonitors, Ensemble e) {
		Vector<Vector<String>> data = convertMonitorForJtable(activeMonitors);
		// List of Active Issue Resolutions
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("Monitor");
		columnNames.add("State");

		monitorList = new JTable(data, columnNames) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false; // Disallow the editing of any cell
			}

		};
		monitorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		monitorList.setBounds(640, 125, 200, 130);
		monitorList.setFillsViewportHeight(true);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(640, 125, 200, 130);
		mainPanel.add(tabbedPane);

		activeMonitorScrollPane = new JScrollPane(monitorList);
		tabbedPane.addTab("Monitoring", null, activeMonitorScrollPane, null);
		tabbedPane.setEnabledAt(0, true);

		refreshWindow();
	}

	public void updateResolutions(String capID, IssueResolution issue,
			EnsembleManager em) {

		this.counter = this.counter + 1;
		String counter = Integer.toString(this.counter);
		EnsembleManager e = issue.getRoleCurrent().getEnsemble();
		Vector<String> row = convertAndFilterForJtable(counter, capID, issue, e);
		// //System.out.println(row);
		// for (Ensemble ensemble : ensembles) {
		// Vector<String> v = new Vector<String>();
		// v.add(ensemble.getName());
		// data.add(v);
		// }

		Vector<String> columnNames = new Vector<String>();
		columnNames.add("N");
		columnNames.add("CAP ID");
		columnNames.add("Role");
		columnNames.add("Issue");
		columnNames.add("Issue Status");
		columnNames.add("Ensemble");

		((DefaultTableModel) IssueResolutionsList.getModel()).addRow(row);
		IssueResolutionsList.getSelectionModel().addListSelectionListener(
				new IssueTableSelectionListener(IssueResolutionsList, e, this));
		// IssueResolutionsList.setModel(new DefaultTableModel(data,
		// columnNames));

		IssueResolutionsList.getColumnModel().getColumn(0)
				.setPreferredWidth(20);
		IssueResolutionsList.getColumnModel().getColumn(1)
				.setPreferredWidth(20);
	}

	/*
	 * public void loadActiveIssueResolutionsTable(List<IssueResolution>
	 * activeIssueResolutions, EnsembleManager em) { Vector<Vector<String>> data
	 * = convertAndFilterForJtable(activeIssueResolutions, em); // List of
	 * Active Issue Resolutions Vector<String> columnNames = new
	 * Vector<String>(); columnNames.add("Role Type"); columnNames.add("Role ID"
	 * ); columnNames.add("Issue Type"); columnNames.add("Ensemble");
	 * 
	 * IssueResolutionsList = new JTable(data, columnNames) {
	 * 
	 * @Override public boolean isCellEditable(int r, int c) { return false; //
	 * Disallow the editing of any cell }
	 * 
	 * }; IssueResolutionsList.setSelectionMode(ListSelectionModel.
	 * SINGLE_INTERVAL_SELECTION); IssueResolutionsList.setBounds(5, 125, 400,
	 * 400); IssueResolutionsList.setFillsViewportHeight(true);
	 * IssueResolutionsList.getSelectionModel() .addListSelectionListener(new
	 * IssueTableSelectionListener(IssueResolutionsList, em, this));
	 * 
	 * JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	 * tabbedPane.setBounds(5, 125, 400, 400); mainPanel.add(tabbedPane);
	 * 
	 * activeIssuesScrollPane = new JScrollPane(IssueResolutionsList);
	 * tabbedPane.addTab("Active Issue Resolutions", null,
	 * activeIssuesScrollPane, null); tabbedPane.setEnabledAt(0, true);
	 * 
	 * refreshWindow(); }
	 */
	public void loadPlannerFrame(List<String> plans, Ensemble e) {
		Vector<Vector<String>> data = convertPlannerForJtable(plans);
		// List of Active Issue Resolutions
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("State");

		planningList = new JTable(data, columnNames) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false; // Disallow the editing of any cell
			}

		};
		planningList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		planningList.setBounds(850, 310, 200, 130);
		planningList.setFillsViewportHeight(true);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(850, 310, 200, 130);
		mainPanel.add(tabbedPane);

		plannerLogScrollPane = new JScrollPane(planningList);
		tabbedPane.addTab("Planning", null, plannerLogScrollPane, null);
		tabbedPane.setEnabledAt(0, true);

		refreshWindow();
	}

	public void loadExecuteFrame(List<String> exec, Ensemble e) {
		Vector<Vector<String>> data = convertExecuteForJtable(exec);
		// List of Active Issue Resolutions
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("State");

		executeList = new JTable(data, columnNames) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false; // Disallow the editing of any cell
			}

		};
		executeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		executeList.setBounds(850, 125, 200, 130);
		executeList.setFillsViewportHeight(true);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(850, 125, 200, 130);
		mainPanel.add(tabbedPane);

		executeLogScrollPane = new JScrollPane(executeList);
		tabbedPane.addTab("Executing", null, executeLogScrollPane, null);
		tabbedPane.setEnabledAt(0, true);

		refreshWindow();
	}

	public void updateActiveIssueResolutionTable(String capID,
			IssueResolution ir, EnsembleManager em) {

		String counter = Integer.toString(this.counter);
		Vector<String> row = convertAndFilterForJtable(counter, capID, ir, em);

		Vector<String> columnNames = new Vector<String>();
		columnNames.add("N");
		columnNames.add("Role Type");
		columnNames.add("Role ID");
		columnNames.add("Issue Type");
		columnNames.add("Ensemble");
		// IssueResolutionsList.setModel(new DefaultTableModel(row,
		// columnNames));

		refreshWindow();
	}

	public String getSelectedIssueInTable() {
		int sr = IssueResolutionsList.getSelectedRow();
		if (sr == -1 || sr >= IssueResolutionsList.getModel().getRowCount()) {
			return "";
		}
		return (String) IssueResolutionsList.getModel().getValueAt(sr, 0);
	}

	public void resetIssueInstances() {
		IssueResolutionsList.setModel(null);

	}

	public void showTree(CATree cat) {

		// frame to see the issue resolution tree mxGraphComponent

		// cat.insertVertex(cat.getDefaultParent(), null, "ee", 0, 0, 80, 30,
		// null);
		// frame to see the issue resolution tree mxGraphComponent

		graphComponent.setGraph(cat);

		// layout = new mxParallelEdgeLayout(graphComponent.getGraph());

		// layout = new mxHierarchicalLayout(graphComponent.getGraph());

		graphComponent.setBounds(280, 125, 250, 400);
		graphComponent.setBorder(javax.swing.BorderFactory
				.createLineBorder(new Color(0, 0, 0)));

		graphComponent.setEnabled(true);
		layout.execute(graphComponent.getGraph().getDefaultParent());

	}

	public void refreshWindow() {
		mainPanel.validate();
		mainPanel.repaint();
	}

	public void resetTreeFrame() {
		if (graphComponent != null) {
			graphComponent.removeAll();
		}
		refreshWindow();

	}

	public void run(CollectiveAdaptationProblem cap,
			List<EnsembleManager> ensembles, String issueName, String capID,
			String startingRole, int issueIndex) {

		solution = new CollectiveAdaptationSolution(capID, null);
		Issue issue = new Issue();
		issue.setIssueType(issueName);

		// search the role that can trigger the specific issue
		EnsembleManager en = ensembles
				.stream()
				.filter(e -> e.getEnsemble().getName()
						.equals(cap.getStartingRoleEnsemble())).findFirst()
				.get();

		RoleManager r = en.getRolebyType(startingRole);
		System.out.println("ISSUE TRIGGERED: " + issue.getIssueType());

		IssueResolution resolution1 = new IssueResolution(1, "ISSUE_TRIGGERED",
				r, r, issue, null);
		resolution1.setRoot(true);
		r.addIssueResolution(resolution1);

		EnsembleManager em = null;

		// add the issueresolution to the right Ensemble
		for (int i = 0; i < ensembles.size(); i++) {
			for (int j = 0; j < ensembles.get(i).getRolesManagers().size(); j++) {
				RoleManager currentManager = ensembles.get(i)
						.getRolesManagers().get(j);
				if (currentManager.getRole().getType()
						.equalsIgnoreCase(r.getRole().getType())) {
					ArrayList<IssueResolution> resolutions = new ArrayList<IssueResolution>();

					em = ensembles.get(i);
					if (em.getIssueCommunications() != null) {
						em.getIssueCommunications().clear();
					}

					solution.setCapID(capID);
					HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
					List<RoleCommand> commands = new ArrayList<RoleCommand>();
					ensembleCommands.put(em.getEnsemble().getName(), commands);

					solution.setEnsembleCommands(ensembleCommands);

					// update id of the issue resolution
					em.setIssueResolutionCount(1);
					resolution1.setIssueResolutionID(em
							.getIssueResolutionCount());

					resolutions.add(resolution1);
					em.setActiveIssueResolutions(resolutions);

					List<IssueCommunication> relatedComs = new ArrayList<IssueCommunication>();

					em.setCommunicationsRelations(resolution1, relatedComs);

					CATree hierarchyTree = createHierarchyTree(ensembles);

					this.updateHierarchy(hierarchyTree);

					em.checkIssues(cap, capID, this, ensembles, solution,
							issueIndex, hierarchyTree, null, null, null, null);

					break;
				}
			}
		}
		// retrieve the final solution for the ensemble
		List<RoleCommand> roleCommands = new ArrayList<RoleCommand>();
		solution.setCapID(capID);
		for (int i = 0; i < em.getRolesManagers().size(); i++) {
			RoleManager rm = em.getRolesManagers().get(i);
			// System.out.println("ROLE: " + rm.getRole().getType());
			RoleCommand command = rm.getRoleCommands();

			roleCommands.add(command);

		}

		HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
		ensembleCommands.put(em.getEnsemble().getName(), roleCommands);
		solution.setEnsembleCommands(ensembleCommands);
		// return solution;

	}

	private CATree createHierarchyTree(List<EnsembleManager> ensembles) {
		CATree hierarchyTree = new CATree();

		// CREATE FIRST PART OF THE HIERARCHY TREE

		Object root1 = hierarchyTree.insertNodeHierarchy(
				hierarchyTree.getDefaultParent(), null, "UMS", STYLE_INIT);

		Object v1 = hierarchyTree.insertNodeHierarchy(
				hierarchyTree.getDefaultParent(), null, "FBC", STYLE_INIT);
		hierarchyTree.insertEdge(hierarchyTree.getDefaultParent(), "", "",
				root1, v1, STYLE_ISSUE_EDGE);

		for (int k = 0; k < ensembles.size(); k++) {
			EnsembleManager e = ensembles.get(k);
			if (!(e.getEnsemble().getName().contains("Flexi"))) {
				List<RoleManager> roles = e.getRolesManagers();
				for (int m = 0; m < roles.size(); m++) {
					RoleManager role = roles.get(m);
					if (role.getRole().getType().contains("RouteManagement")) {
						Object v = hierarchyTree.insertNodeHierarchy(
								hierarchyTree.getDefaultParent(), null, role
										.getRole().getType(), STYLE_INIT);
						hierarchyTree.insertEdge(
								hierarchyTree.getDefaultParent(), "", "", v1,
								v, STYLE_ISSUE_EDGE);
						for (int n = 0; n < roles.size(); n++) {
							RoleManager role1 = roles.get(n);
							if (role1.getRole().getType()
									.contains("RoutePassenger")) {
								Object v2 = hierarchyTree.insertNodeHierarchy(
										hierarchyTree.getDefaultParent(), null,
										role1.getRole().getType(), STYLE_INIT);
								hierarchyTree.insertEdge(
										hierarchyTree.getDefaultParent(), "",
										"", v, v2, STYLE_ISSUE_EDGE);
							} else if (role1.getRole().getType()
									.contains("FlexibusDriver")) {
								Object v2 = hierarchyTree.insertNodeHierarchy(
										hierarchyTree.getDefaultParent(), null,
										role1.getRole().getType(), STYLE_INIT);
								hierarchyTree.insertEdge(
										hierarchyTree.getDefaultParent(), "",
										"", v, v2, STYLE_ISSUE_EDGE);
							}

						}
					}

				}

			}

		}
		return hierarchyTree;
	}
}