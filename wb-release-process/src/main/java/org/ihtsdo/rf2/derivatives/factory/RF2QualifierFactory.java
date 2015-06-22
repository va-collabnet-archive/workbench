package org.ihtsdo.rf2.derivatives.factory;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.derivatives.impl.RF2QualifierImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2QualifierFactory Description: Creating Qualifiers Specific methods required by Concept Iterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2QualifierFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2QualifierFactory.class);

	public RF2QualifierFactory(Config config) {
		super(config);
		try {
			ExportUtil.loadCurrentInferRels();
			ExportUtil.loadPreviousQualIds();
			ExportUtil.loadConceptForQualStartStop();
			ExportUtil.loadSCTID_map();
			ExportUtil.loadQualStartStop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void export() throws IOException, Exception {

		try {

			logger.info("Start qualifiers Snapshot Export...");
			RF2QualifierImpl iterator = new RF2QualifierImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			closeExportFileWriter();
			logger.info("Finished qualifiers Snapshot Export...");
		} catch (MalformedURLException e) {
			logger.error("Message :", e);
		} catch (IOException e) {
			logger.error("Message :", e);
		} catch (Exception e) {
			logger.error("Message :", e);
		}
	}
}
