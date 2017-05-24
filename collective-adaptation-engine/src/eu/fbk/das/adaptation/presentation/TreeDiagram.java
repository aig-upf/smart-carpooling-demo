package eu.fbk.das.adaptation.presentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeDiagram {

    // private static final Logger logger = LogManager
    // .getLogger(TreeDiagram.class);

    private String name;

    private Set<Integer> states;

    private int initialState;

    private Set<Integer> finalStates;

    private boolean isRunning = false;

    private boolean isEnded = false;

    public void setEnded(boolean isEnded) {
	this.isEnded = isEnded;
    }

    public boolean getEnded() {
	return isEnded;
    }

    public Set<Integer> getFinalStates() {
	return finalStates;
    }

    public void setFinalStates(Set<Integer> finalStates) {
	this.finalStates = finalStates;
    }

    private List<TreeNode> nodes;

    public boolean isRunning() {
	return isRunning;
    }

    public void setRunning(boolean isRunning) {
	this.isRunning = isRunning;
    }

    private int currentState;

    private TreeNode currentNode;

    /** default constructor */
    public TreeDiagram() {
	if (nodes == null) {
	    nodes = new ArrayList<TreeNode>();
	}
    }

    public TreeDiagram(int tid, Set<Integer> states, int initialState, List<TreeNode> all) {

	for (int i = 0; i < all.size(); i++) {
	    states.add(i);
	}
	this.states = new HashSet<Integer>(states);
	if (this.states.contains(initialState))
	    this.initialState = initialState;
	else
	    // throw new InvalidFlowInitialStateException();
	    this.nodes = new ArrayList<TreeNode>(all);

	for (TreeNode node : all) {

	    if (this.states.contains(node.getSource()) && this.states.contains(node.getTarget())) {

		// //activity duplication control
		boolean isAdded = false;
		for (Object activ : this.nodes)
		    if (activ.equals(node)) {
			isAdded = true;
			break;
		    }
		if (!isAdded)
		    this.nodes.add(node);
	    } else {
		// throw new InvalidFlowActivityException();
	    }
	}

    }

    /**
     * Create a TreeDiagram with a given list of nodes
     * 
     * @param all
     *            , list of nodes
     * 
     *            Note: processDigram in order to be run by
     *            {@link ProcessEngine} need a processId and a proper set of
     *            states
     */
    public TreeDiagram(List<TreeNode> all) {
	this.nodes = all;
    }

    public TreeDiagram(String string, List<TreeNode> all, Set<Integer> states, int treeId) {
	this.states = new HashSet<Integer>(states);

	this.nodes = new ArrayList<TreeNode>(all);

	for (TreeNode node : all) {

	    if (this.states.contains(node.getSource()) && this.states.contains(node.getTarget())) {

		// //activity duplication control
		boolean isAdded = false;
		for (Object activ : this.nodes)
		    if (activ.equals(node)) {
			isAdded = true;
			break;
		    }
		if (!isAdded)
		    this.nodes.add(node);
	    } else {
		// throw new InvalidFlowActivityException();
	    }
	}

    }

    public void addState(int i) {
	states.add(i);
    }

    public TreeNode getCurrentNode() {
	return currentNode;
    }

    public void setcurrentNode(TreeNode currentNode) {
	this.currentNode = currentNode;
    }

    public Set<Integer> getStates() {
	return states;
    }

    public int getInitialState() {
	return initialState;
    }

    public List<TreeNode> getnodes() {
	return nodes;
    }

    /*
     * public boolean FinalState(int state) { int finale = 0; boolean result =
     * false;
     * 
     * for (TreeNode act : this.nodes) {
     * 
     * if (act.getSource() != state) { // //System.out.println(state+
     * " is not final"); } else { // //System.out.println(state+" is final");
     * finale++; } }
     * 
     * if (finale == 0) { result = true; } else { result = false; } return
     * result; }
     */
    public int getCurrentState() {
	return currentState;
    }

    public String delete_prefix(String string) {
	String[] arr = string.split("\\.|_");
	String result = arr[arr.length - 1];
	return result;
    }

    public void setCurrentState(int currentState) {
	if (!states.contains(currentState))
	    // throw new InvalidFlowCurrentStateException();
	    this.currentState = currentState;
    }

    /*
     * public TreeNode getFirstActivity() { TreeNode result = null;
     * 
     * for (int i = 0; i < this.nodes.size(); i++) { boolean first = true; for
     * (TreeNode act : this.nodes) { if (this.getnodes().get(i).getSource() ==
     * act.getTarget()) { first = false; } } if (first) { result =
     * this.getnodes().get(i); } } return result; }
     */

    /**
     * simulates the execution of a flow activity
     * 
     * @param activity
     *            to be executed
     * @return true if execution is successful, false if execution is not
     *         possible due to the current state of a flow
     * @throws InvalidServiceActionException
     * @throws InvalidServiceCurrentStateException
     *
     */

    /*
     * public boolean executeActivity(String activity) throws
     * InvalidFlowActivityException, InvalidFlowCurrentStateException { boolean
     * isExecuted = false; Set<TreeNode> nodes = new HashSet<TreeNode>();
     * 
     * if (!nodes.contains(activity)) throw new InvalidFlowActivityException();
     * for (TreeNode act : nodes) if ((act.getSource() == currentState) &&
     * act.getName().equals(activity)) { setCurrentState(act.getTarget());
     * isExecuted = true; break; } return isExecuted; }
     * 
     * public void refine(TreeNode activity, List<TreeNode> refinement) {
     * 
     * this.getnodes().remove(activity); for (int i = 0; i < refinement.size();
     * i++) { this.getnodes().add(refinement.get(i)); }
     * 
     * for (Object act : this.getnodes()) { //System.out.println(((TreeNode)
     * act).getName()); }
     * 
     * }
     */
    @Override
    public String toString() {
	String str = "";
	str += "states:" + states + "\n";
	str += "initial state:" + initialState + "\n";
	str += "current state:" + currentState + "\n";
	str += "nodes:" + nodes + "\n";

	return str;
    }

    /*
     * public List<TreeNode> getNextActivity() {
     * 
     * List<TreeNode> actList = new ArrayList<TreeNode>();
     * 
     * // case in which we start a new flow if (this.currentNode == null) {
     * 
     * int initialState = this.getInitialState();
     * 
     * for (TreeNode act : this.getnodes()) { if (act.getSource() ==
     * initialState) { actList.add(act); } }
     * 
     * } // we are in the middle of the flow execution else { int target =
     * this.getTarget(); for (TreeNode act : this.getnodes()) { if
     * (act.getSource() == target) { actList.add(act); }
     * 
     * } } if (actList.isEmpty()) { this.terminated = true; } return actList;
     * 
     * }
     */

    /*
     * public static TreeDiagram
     * AdaptationToFlowModel(List<ServiceTransitionGlobal> adaptationProcess,
     * String initialState, int pid, List<String> abstracts) throws
     * InvalidFlowInitialStateException, InvalidFlowActivityException {
     * 
     * Set<Integer> states = new HashSet<Integer>(); int order = 0;
     * List<TreeNode> actList = new ArrayList<TreeNode>();
     * constructSequenceFromState(initialState, adaptationProcess, actList,
     * order, states, pid, abstracts); TreeDiagram model = new
     * TreeDiagram("refinement", actList, states, pid); return model; }
     * 
     * private static void constructSequenceFromState(String pointer,
     * List<ServiceTransitionGlobal> adaptationProcess, List<TreeNode> actList,
     * int order, Set<Integer> states, int pid, List<String> abstracts) {
     * 
     * List<ServiceTransitionGlobal> currentTransitions =
     * getTransitionsFromState(pointer, adaptationProcess); if
     * (currentTransitions.size() == 1) { if
     * (multipleTransitionsToState(pointer, adaptationProcess)) { // end of pick
     * branch return; } ServiceTransitionGlobal t =
     * currentTransitions.iterator().next(); addActivityInSequence(t, actList,
     * order, abstracts); Integer from = Integer.parseInt(t.getFrom()); Integer
     * to = Integer.parseInt(t.getTo()); states.add(from); states.add(to);
     * order++; constructSequenceFromState(t.getTo(), adaptationProcess,
     * actList, order, states, pid, abstracts); }
     * 
     * if (currentTransitions.size() > 1) { // //System.out.println(
     * "Two or more transitions..."); // handle pick List<TreeNode> nodes = new
     * ArrayList<TreeNode>();
     * 
     * //System.out.println("Creating a pick..."); List<OnMessageActivity>
     * onMessages = new ArrayList<OnMessageActivity>(); for
     * (ServiceTransitionGlobal tr : currentTransitions) { OnMessageActivity
     * onMsg = new OnMessageActivity(); onMsg.setOnMessage(true);
     * onMsg.setName(tr.getAction());
     * 
     * try { onMsg.setBranch(AdaptationToFlowModel(adaptationProcess,
     * tr.getTo(), pid, abstracts)); } catch (InvalidFlowInitialStateException
     * e) { logger.error(e.getMessage(), e); } catch
     * (InvalidFlowActivityException e) { logger.error(e.getMessage(), e); }
     * 
     * onMessages.add(onMsg); }
     * 
     * Integer pickTo = getPickToState(onMessages.get(0).getBranch()); if
     * (pickTo == -1 && onMessages.get(0).getBranch().getnodes().size() == 0) {
     * pickTo = Integer.parseInt(currentTransitions.get(0).getTo()); }
     * 
     * Integer pickfrom = Integer.parseInt(currentTransitions.get(0).getFrom());
     * 
     * PickActivity nextAct = new PickActivity(pickfrom, pickTo, "PICK",
     * onMessages); nextAct.setPick(true); actList.add(nextAct);
     * states.add(pickfrom); states.add(pickTo); order++;
     * constructSequenceFromState(pickTo.toString(), adaptationProcess, actList,
     * order, states, pid, abstracts);
     * 
     * }
     * 
     * }
     * 
     * private static int getPickToState(TreeDiagram branch) {
     * 
     * int finalstate = -1; Set<Integer> states = branch.getStates(); int
     * nstates = states.size(); for (int i = 0; i < nstates; i++) { boolean
     * isfinal = true; int current = states.iterator().next(); for (TreeNode act
     * : branch.getnodes()) { if (act.getSource() == current) { isfinal = false;
     * } } if (isfinal) { finalstate = current; break; } } return finalstate;
     * 
     * }
     */
    /*
     * private static List<ServiceTransitionGlobal>
     * getTransitionsFromState(String pointer, List<ServiceTransitionGlobal>
     * adaptationProcess) { int range = numOFSucc(pointer, adaptationProcess);
     * List<ServiceTransitionGlobal> result = new
     * ArrayList<ServiceTransitionGlobal>(range);
     * 
     * for (int i = 0; i < adaptationProcess.size(); i++) {
     * 
     * if (adaptationProcess.get(i).getFrom().equals(pointer)) {
     * result.add(adaptationProcess.get(i)); } } return result; }
     * 
     * private static boolean multipleTransitionsToState(String pointer,
     * List<ServiceTransitionGlobal> adaptationProcess) { int range =
     * numOFPrec(pointer, adaptationProcess);
     * 
     * boolean result = false; if (range > 1) { result = true; }
     * 
     * return result; }
     * 
     * public static int numOFSucc(String pointer, List<ServiceTransitionGlobal>
     * transitions) { int result = 0; for (int i = 0; i < transitions.size();
     * i++) {
     * 
     * if (transitions.get(i).getFrom().equals(pointer)) { result++; }
     * 
     * } return result; }
     * 
     * public static int numOFPrec(String pointer, List<ServiceTransitionGlobal>
     * transitions) { int result = 0; for (int i = 0; i < transitions.size();
     * i++) {
     * 
     * if (transitions.get(i).getTo().equals(pointer)) { result++; }
     * 
     * } return result; }
     * 
     * private static void addActivityInSequence(ServiceTransitionGlobal t,
     * List<TreeNode> activityList, int order, List<String> abstracts) {
     * 
     * TreeNode act = null;
     * 
     * act = createActivity(t, abstracts);
     * 
     * act.setSid(t.getSid()); act.setServiceType(t.getServiceType());
     * act.setOrder(order); act.setSource(Integer.parseInt(t.getFrom()));
     * act.setTarget(Integer.parseInt(t.getTo())); activityList.add(act); }
     * 
     * 
     * public static TreeNode createNode(ServiceTransition transition,
     * List<String> abstracts) {
     * 
     * TreeNode act = new TreeNode();
     * 
     * String type = transition.getType().toString();
     * 
     * boolean isAbstractAction = abstracts.contains(transition.getAction());
     * 
     * // activity type if (type.equals("IN") && !isAbstractAction) { act = new
     * InvokeActivty(); act.setSend(true); act.setType(TreeNodeType.INVOKE);
     * act.setName(transition.getAction()); } else if (type.equals("IN") &&
     * isAbstractAction) { act = new AbstractActivity(); act.setAbstract(true);
     * act.setType(TreeNodeType.ABSTRACT); act.setName(transition.getAction());
     * } if (type.equals("OUT")) { act = new ReplyActivity();
     * act.setReceive(true); act.setType(TreeNodeType.REPLY);
     * act.setName(transition.getAction()); } return act;
     * 
     * }
     */

    public void setnodes(List<TreeNode> nodes) {
	this.nodes = nodes;
    }

    public String getName() {
	return name;
    }

    public void addNode(TreeNode node) {
	this.nodes.add(node);
    }

    public void addAllNode(List<TreeNode> nodes) {
	if (nodes == null) {
	    this.nodes = new ArrayList<TreeNode>();
	}
	this.nodes.addAll(nodes);
    }

    public void setName(String name) {
	this.name = name;

    }
    /*
     * public TreeNode findTreeNode(
     * eu.fbk.das.process.engine.api.jaxb.scenario.Scenario.DomainObject.
     * DomainObjectInstance d) { for (TreeNode act : getnodes()) { if
     * (act.getName().equals(d.getProcess().getCurentActivity())) { return act;
     * } } return null; }
     */
}
