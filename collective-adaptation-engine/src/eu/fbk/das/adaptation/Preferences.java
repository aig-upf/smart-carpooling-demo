package eu.fbk.das.adaptation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Preferences {
    /*
     * The total sum of the weights should be 1 ttweight + cweight + wdweight +
     * ncweight + rcweight + uspweight + wsdweight= 1
     */
    /* weight for travel time - Value range:[0,1] */
    private double ttweight;

    /* weight for cost - Value range:[0,1] */
    private double cweight;

    /* weight for walking distance - Value range:[0,1] */
    private double wdweight;

    /* weight for number of changes - Value range:[0,1] */
    private double ncweight;

    /* weight for reliability cost - Value range:[0,1] */
    private double rcweight;

    /* weight for unsatisfied safety preferences - Value range:[0,1] */
    private double uspweight;

    /* weight for willingness to share data - Value range:[0,1] */
    private double wsdweight;

    /* maximum of travel duration - in seconds */
    private long tmax;

    /* maximum of cost */
    private double cmax;

    /* maximum walking distance - in meters */
    private double wmax;

    /* maximum number of changes */
    private int nocmax;

    /*** Safety preferences ***/

    /* avoid unsafe areas */
    private boolean unsafear;

    /* avoid crowded areas */
    private boolean crowdedar;

    /* avoid polluted areas */
    private boolean pollutedar;

    /* avoid untrusted providers */
    private boolean untrustedprov;

    /*** Privacy preferences ***/

    /* sensitivity of name - Value range:[0,1] */
    private double namesens;

    /* sensitivity of email - Value range:[0,1] */
    private double emailsens;

    /* sensitivity of phone - Value range:[0,1] */
    private double phonesens;

    /* sensitivity of gps location - Value range:[0,1] */
    private double gpssens;

    /*** Transportation type ***/
    private boolean ttCar;
    private boolean ttFlexibus;
    private boolean ttWalk;
    private boolean ttCarpooling;

    /*** Payment method ***/
    private boolean pmCash;
    private boolean pmCreditCard;
    private boolean pmPaypal;
    private boolean pmServiceCard;

    public Preferences(double ttw, double cw, double wdw, double ncw, double rcw, double uspw, double wsdw, long tmax,
	    double cmax, double wmax, int nocmax, double namesens, double emailsens, double phonesens, double gpssens,
	    boolean unsafear, boolean crowdedar, boolean pollutedar, boolean untrustedprov) {
	ttweight = ttw;
	cweight = cw;
	wdweight = wdw;
	ncweight = ncw;
	rcweight = rcw;
	uspweight = uspw;
	wsdweight = wsdw;
	this.tmax = tmax;
	this.cmax = cmax;
	this.wmax = wmax;
	this.nocmax = nocmax;
	this.setNamesens(namesens);
	this.setEmailsens(emailsens);
	this.setPhonesens(phonesens);
	this.setGpssens(gpssens);
    }

    public Preferences() {
    }

    public double getTTweight() {
	return ttweight;
    }

    public double getCweight() {
	return cweight;
    }

    public double getRCweight() {
	return rcweight;
    }

    public double getWDweight() {
	return wdweight;
    }

    public double getNCweight() {
	return ncweight;
    }

    public double getUSPweight() {
	return uspweight;
    }

    public double getWSDweight() {
	return wsdweight;
    }

    public long getTmax() {
	return tmax;
    }

    public double getCmax() {
	return cmax;
    }

    public double getWmax() {
	return wmax;
    }

    public int getNoCmax() {
	return nocmax;
    }

    public double getNamesens() {
	return namesens;
    }

    public double getEmailsens() {
	return emailsens;
    }

    public double getPhonesens() {
	return phonesens;
    }

    public double getGpssens() {
	return gpssens;
    }

    public boolean isUnsafear() {
	return unsafear;
    }

    public boolean isCrowdedar() {
	return crowdedar;
    }

    public boolean isPollutedar() {
	return pollutedar;
    }

    public boolean isUntrustedprov() {
	return untrustedprov;
    }

    public void setTTweight(double tt) {
	this.ttweight = tt;
    }

    public void setCweight(double c) {
	this.cweight = c;
    }

    public void setWDweight(double w) {
	this.wdweight = w;
    }

    public void setNCweight(double nc) {
	this.ncweight = nc;
    }

    public void setRCweight(double rc) {
	this.rcweight = rc;
    }

    public void setUSPweight(double usp) {
	this.uspweight = usp;
    }

    public void setWSDweight(double wsd) {
	this.wsdweight = wsd;
    }

    public void setTmax(long t) {
	this.tmax = t;
    }

    public void setCmax(double c) {
	this.cmax = c;
    }

    public void setWmax(double w) {
	this.wmax = w;
    }

    public void setNoCmax(int noc) {
	this.nocmax = noc;
    }

    public void setNamesens(double namesens) {
	this.namesens = namesens;
    }

    public void setEmailsens(double emailsens) {
	this.emailsens = emailsens;
    }

    public void setPhonesens(double phonesens) {
	this.phonesens = phonesens;
    }

    public void setGpssens(double gpssens) {
	this.gpssens = gpssens;
    }

    public void setUnsafear(boolean unsafear) {
	this.unsafear = unsafear;
    }

    public void setCrowdedar(boolean crowdedar) {
	this.crowdedar = crowdedar;
    }

    public void setPollutedar(boolean pollutedar) {
	this.pollutedar = pollutedar;
    }

    public void setUntrustedprov(boolean untrustedprov) {
	this.untrustedprov = untrustedprov;
    }

    public boolean isTtCar() {
	return ttCar;
    }

    public void setTtCar(boolean ttCar) {
	this.ttCar = ttCar;
    }

    public boolean isTtFlexibus() {
	return ttFlexibus;
    }

    public void setTtFlexibus(boolean ttFlexibus) {
	this.ttFlexibus = ttFlexibus;
    }

    public boolean isTtWalk() {
	return ttWalk;
    }

    public void setTtWalk(boolean ttWalk) {
	this.ttWalk = ttWalk;
    }

    public boolean isTtCarpooling() {
	return ttCarpooling;
    }

    public void setTtCarpooling(boolean ttCarpooling) {
	this.ttCarpooling = ttCarpooling;
    }

    public boolean isPmCash() {
	return pmCash;
    }

    public void setPmCash(boolean pmCash) {
	this.pmCash = pmCash;
    }

    public boolean isPmCreditCard() {
	return pmCreditCard;
    }

    public void setPmCreditCard(boolean pmCreditCard) {
	this.pmCreditCard = pmCreditCard;
    }

    public boolean isPmPaypal() {
	return pmPaypal;
    }

    public void setPmPaypal(boolean pmPaypal) {
	this.pmPaypal = pmPaypal;
    }

    public boolean isPmServiceCard() {
	return pmServiceCard;
    }

    public void setPmServiceCard(boolean pmServiceCard) {
	this.pmServiceCard = pmServiceCard;
    }
}