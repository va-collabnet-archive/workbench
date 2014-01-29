package org.ihtsdo.rf2.module.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.module.core.impl.RF2ConceptImpl;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2ConceptFactory Description: Creating Concept Specific methods required by ConceptIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2ConceptFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2ConceptFactory.class);

	/**
	 * Instantiates a new r f2 concept factory.
	 *
	 * @param config the config
	 */
	public RF2ConceptFactory(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
	public void export() {

		logger.info("Started Concept Snapshot Export...");

		try {

			RF2ConceptImpl iterator = new RF2ConceptImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			closeExportFileWriter();
			
			logger.info("==========Total number of metadata concept records======" + iterator.getMetaDataCount());

			logger.info("==========Total number of duplicated records======" + iterator.getDupRecord());

			logger.info("Finished Concept Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
