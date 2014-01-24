package org.ihtsdo.rf2.module.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.module.core.impl.RF2StatedRelationshipImpl;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;

/**
 * Title: RF2StatedRelationshipFactory Description: Creating StatedRelationship Specific methods required by StatedRelationshipIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2StatedRelationshipFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2StatedRelationshipFactory.class);

	public RF2StatedRelationshipFactory(Config config) {
		super(config);
	}

	public void export() throws IOException, Exception {

		logger.info("Started Stated Relationship Snapshot Export ...");

		try {

			RF2StatedRelationshipImpl iterator = new RF2StatedRelationshipImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			logger.info("==========Total number of metadata stated relationship records======" + iterator.getMetaDataCount());
			
			logger.info("==========Total number of duplicated records======" + iterator.getDupRecord());

			
			closeExportFileWriter();

			logger.info("Finished Stated Relationship Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
