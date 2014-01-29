package org.ihtsdo.rf2.module.refset.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.refset.impl.RF2NonHumanImpl;
import org.ihtsdo.rf2.module.refset.impl.RF2VMPImpl;
import org.ihtsdo.rf2.module.refset.impl.RF2VTMImpl;
import org.ihtsdo.rf2.module.util.Config;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2LanguageRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2SimpleFullRefsetFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2SimpleFullRefsetFactory.class);

	/**
	 * Instantiates a new r f2 simple full refset factory.
	 *
	 * @param config the config
	 */
	public RF2SimpleFullRefsetFactory(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
	public void export() {

	

		try {
			logger.info("Started VTM-VMP Refset Export ...");
			
			RF2VTMImpl vtmUSIterator = new RF2VTMImpl(getConfig());
			Terms.get().iterateConcepts(vtmUSIterator);

			RF2VMPImpl vmpIterator = new RF2VMPImpl(getConfig());
			Terms.get().iterateConcepts(vmpIterator);

			logger.info("Finished VTM-VMP Refset Export.");
			
			logger.info("Started Non Human Refset Export ...");
			
			RF2NonHumanImpl nonHumanIterator = new RF2NonHumanImpl(getConfig());
			Terms.get().iterateConcepts(nonHumanIterator);
			
			logger.info("Finished Non Human Refset Export.");
			
			closeExportFileWriter();

			logger.info("Finished Non Human Refset Export.");
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
}
