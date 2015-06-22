package org.ihtsdo.rf2.compatibilitypkg.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.compatibilitypkg.impl.RF2RetiredIsaRelationshipImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2RelationshipFactory Description: Creating Relationship Specific methods required by RelationshipIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2RetiredIsaRelationshipFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2RetiredIsaRelationshipFactory.class);

	public RF2RetiredIsaRelationshipFactory(Config config) {
		super(config);
	}

	public void export() throws IOException, Exception {

		logger.info("Started Relationship Snapshot Export...");

		try {

			RF2RetiredIsaRelationshipImpl iterator = new RF2RetiredIsaRelationshipImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			closeExportFileWriter();

			logger.info("Finished Relationship Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}// end Program
