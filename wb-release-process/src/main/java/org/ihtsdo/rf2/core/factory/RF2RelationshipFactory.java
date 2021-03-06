package org.ihtsdo.rf2.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.core.impl.RF2RelationshipImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2RelationshipFactory Description: Creating Relationship Specific methods required by RelationshipIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2RelationshipFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2RelationshipFactory.class);

	public RF2RelationshipFactory(Config config) {
		super(config);
	}

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
