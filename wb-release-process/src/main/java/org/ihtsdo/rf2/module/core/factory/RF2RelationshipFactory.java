package org.ihtsdo.rf2.module.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.module.core.impl.RF2RelationshipImpl;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2RelationshipFactory Description: Creating Relationship Specific methods required by RelationshipIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2RelationshipFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2RelationshipFactory.class);

	/**
	 * Instantiates a new r f2 relationship factory.
	 *
	 * @param config the config
	 */
	public RF2RelationshipFactory(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
	public void export() throws IOException, Exception {

		logger.info("Started Inferred Relationship Snapshot Export...");

		try {

			RF2RelationshipImpl iterator = new RF2RelationshipImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			logger.info("==========Total number of metadata inferred relationship records======" + iterator.getMetaDataCount());
			
			logger.info("==========Total number of duplicated records======" + iterator.getDupRecord());

			
			closeExportFileWriter();

			logger.info("Finished Inferred Relationship Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}// end Program
