package eu.fbk.das.adaptation;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.adaptation.ensemble.Ensemble;

/**
 * Parser utility class for {@link EnsembleType}
 * 
 */
public class EnsembleParser {

    private static final Logger logger = LogManager.getLogger(EnsembleParser.class);

    public EnsembleParser() {
    }

    public Ensemble parseEnsemble(File f) {
	Ensemble en = null;
	try {
	    JAXBContext jaxbContext = JAXBContext.newInstance(Ensemble.class);
	    en = (Ensemble) jaxbContext.createUnmarshaller().unmarshal(f);
	} catch (JAXBException e) {
	    logger.error(e);
	}
	return en;
    }

}
