package org.ihtsdo.rf2.compatibilitypkg.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.compatibilitypkg.impl.RF2RetiredIsaStatedRelationshipImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2StatedRelationshipFactory Description: Creating StatedRelationship Specific methods required by StatedRelationshipIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2RetiredIsaStatedRelationshipFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2RetiredIsaStatedRelationshipFactory.class);

	public RF2RetiredIsaStatedRelationshipFactory(Config config) {
		super(config);
	}

	public void export() throws IOException, Exception {

		logger.info("Started Stated Relationship Snapshot Export ...");

		try {

			RF2RetiredIsaStatedRelationshipImpl iterator = new RF2RetiredIsaStatedRelationshipImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			closeExportFileWriter();

			logger.info("Finished Stated Relationship Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
