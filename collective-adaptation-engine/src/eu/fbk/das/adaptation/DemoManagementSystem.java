package eu.fbk.das.adaptation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import eu.fbk.das.adaptation.api.CollectiveAdaptationProblem;
import eu.fbk.das.adaptation.ensemble.Ensemble;

/**
 * This is the class that handles everything happening in the demo, including
 * instance control and storage and message exchange
 * 
 * @author Antonio
 * 
 */
public class DemoManagementSystem {

    /**
     * path to the repository of definitions
     */
    private static String REPO_PATH = null;
    // private CollectiveAdaptationManager cam;

    private static int idEnsembles = 0;
    private static int idRoles = 0;

    private static DemoManagementSystem ONLY_INSTANCE = null;

    /**
     * private constructor to prevent uncontrolled instantiation
     */
    private DemoManagementSystem() {
	// cam = new CollectiveAdaptationManager();

    }

    public static DemoManagementSystem initializeSystem(String repoPath) throws FileNotFoundException {
	if (ONLY_INSTANCE != null) {
	    throw new IllegalStateException("Only one instance of DemoManagementSystem can be initialized");
	}
	ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	// File f = new File(classloader.getResource(repoPath).getFile());

	// if (!Files.exists(Paths.get(repoPath))) {
	// if (!f.exists()) {
	// throw new FileNotFoundException("Repository path is not a valid
	// path");
	// }
	DemoManagementSystem.REPO_PATH = repoPath;
	DemoManagementSystem dms = new DemoManagementSystem();

	return dms;
    }

    public Ensemble getEnsemble(String type) {
	Ensemble ei = null;

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();

	    if (type == null) {
		throw new NullPointerException("Ensemble Type is null");
	    }
	    File dir = new File(REPO_PATH);
	    if (!dir.isDirectory()) {
		throw new NullPointerException("Impossibile to load the ensemble type, mainDir not found " + dir);
	    }
	    File f = new File(REPO_PATH + File.separator + type + ".xml");

	    // retrieve the type from file
	    EnsembleParser parser = new EnsembleParser();
	    ei = parser.parseEnsemble(f);
	    ensembleInstances.add(ei);

	} else {

	    File f = new File(REPO_PATH + File.separator + type + ".xml");

	    EnsembleParser parser = new EnsembleParser();
	    ei = parser.parseEnsemble(f);

	    ensembleInstances.add(ei);
	}
	// System.out.println("Ensemble " + type + " created");

	return ei;
    }

    private ArrayList<Ensemble> ensembleInstances;

    public ArrayList<Ensemble> getEnsembleInstances() {

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();
	    return ensembleInstances;
	} else {
	    return ensembleInstances;
	}
    }

    public void setEnsembleInstances(ArrayList<Ensemble> ensembleInstances) {
	this.ensembleInstances = ensembleInstances;
    }

    public Ensemble getEnsembleInstance(String type, CollectiveAdaptationProblem cap) {
	Ensemble en = null;

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();
	    // System.out.println("Create a new Ensemble Instance of type: " +
	    // type + " with ID: " + idEnsembles);

	    en = this.getEnsemble(type, cap);

	    idEnsembles++;
	    ensembleInstances.add(en);

	} else {
	    // System.out.println("Create a new Ensemble Instance of type: " +
	    // type + " with ID: " + idEnsembles);

	    en = this.getEnsemble(type, cap);

	    idEnsembles++;
	    ensembleInstances.add(en);
	}

	return en;
    }

    public Ensemble getEnsemble(String type, CollectiveAdaptationProblem cap) {
	Ensemble ei = null;
	ClassLoader classloader = Thread.currentThread().getContextClassLoader();

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();
	}

	File dir = new File(REPO_PATH);
	if (!dir.isDirectory()) {
	    throw new NullPointerException("Impossibile to load the ensemble type, mainDir not found " + dir);
	}
	File f = new File(REPO_PATH + type + ".xml");

	// retrieve the type from file
	EnsembleParser parser = new EnsembleParser();
	ei = parser.parseEnsemble(f);

	/*
	 * for (int i = 0; i < cap.getEnsembles().size(); i++) {
	 * CollectiveAdaptationEnsemble cae = cap.getEnsembles().get(i); if
	 * (cae.getEnsembleName().equals(ei.getName()) && cae.getRoles() != null
	 * && !cae.getRoles().isEmpty()) { // ensemble found for (int j = 0; j <
	 * cae.getRoles().size(); j++) {
	 * 
	 * String roleName = cae.getRoles().get(j).getRole(); if
	 * (roleName.equals(cap.getTarget())) { // target found //
	 * System.out.println(roleName); // find the right role in the ensemble
	 * parsed from file for (int k = 0; k < ei.getRole().size(); k++) { Role
	 * rm = ei.getRole().get(k); if (rm.getSolver().size() > 1 &&
	 * (rm.getType().contains("RoutePassenger")))
	 * 
	 * rm.setType(cap.getTarget()); } } else if
	 * (roleName.equals(cap.getStartingRole())) { for (int k = 0; k <
	 * ei.getRole().size(); k++) { Role rm = ei.getRole().get(k); if
	 * ((rm.getType().contains("FlexibusDriver")))
	 * 
	 * rm.setType(cap.getStartingRole()); }
	 * 
	 * }
	 * 
	 * } break;
	 * 
	 * } }
	 */
	ensembleInstances.add(ei);
	// System.out.println("Ensemble " + type + " created");
	return ei;
    }
    /*
     * public Role getRole(String type) { Role r = null;
     * 
     * if (roleInstances == null) { roleInstances = new ArrayList<Role>();
     * //System.out.println("Create anew!"); // building path String separator =
     * System.getProperty("file.separator"); String filePath = REPO_PATH +
     * separator + "types" + separator + "roles" + separator + type + ".xml";
     * ObjectMapper mapper = new ObjectMapper(); try { r = mapper.readValue(new
     * File(filePath), Role.class); } catch (IOException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } UUID id =
     * UUID.randomUUID(); // r.setId(id); roleInstances.add(r); } else {
     * //System.out.println("Create anew!"); // building path String separator =
     * System.getProperty("file.separator"); String filePath = REPO_PATH +
     * separator + "types" + separator + "roles" + separator + type + ".xml";
     * ObjectMapper mapper = new ObjectMapper(); try { r = mapper.readValue(new
     * File(filePath), Role.class); } catch (IOException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } UUID id =
     * UUID.randomUUID(); r.setId(id); roleInstances.add(r); }
     * 
     * return r; }
     */
}
