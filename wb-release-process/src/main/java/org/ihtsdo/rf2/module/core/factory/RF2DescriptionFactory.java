package org.ihtsdo.rf2.module.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.module.core.impl.RF2DescriptionImpl;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2DescriptionFactory Description: Creating Description Specific methods required by DescriptionIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2DescriptionFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2DescriptionFactory.class);

	/**
	 * Instantiates a new r f2 description factory.
	 *
	 * @param config the config
	 */
	public RF2DescriptionFactory(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
	public void export() throws IOException, Exception {

		logger.info("Started Description Snapshot Export...");

		try {

			RF2DescriptionImpl iterator = new RF2DescriptionImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);
			
			logger.info("==========Total number of metadata description records======" + iterator.getMetaDataCount());
			
			logger.info("==========Total number of duplicated records======" + iterator.getDupRecord());

			closeExportFileWriter();

			logger.info("Finished Description Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
