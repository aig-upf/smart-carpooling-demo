package eu.fbk.das.adaptation.utils;

import java.io.File;

import javax.xml.bind.JAXBContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.adaptation.Preferences;

public final class ParserUtil {

    private static final Logger logger = LogManager.getLogger(ParserUtil.class);

    public static Preferences parse(String mainDir, String ref) {
	if (ref == null) {
	    throw new NullPointerException("Preference file is null");
	}
	try {
	    File dir = new File(mainDir);
	    if (!dir.isDirectory()) {
		throw new NullPointerException("Impossibile to load scenario, mainDir not found " + dir);
	    }
	    File f = new File(mainDir + File.separator + ref);
	    JAXBContext context = JAXBContext.newInstance(Preferences.class);
	    return (Preferences) context.createUnmarshaller().unmarshal(f);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}
	return null;

    }
}
