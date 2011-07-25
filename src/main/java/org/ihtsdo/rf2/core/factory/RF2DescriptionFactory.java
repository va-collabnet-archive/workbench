package org.ihtsdo.rf2.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.core.impl.RF2DescriptionImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2DescriptionFactory Description: Creating Description Specific methods required by DescriptionIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2DescriptionFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2DescriptionFactory.class);

	public RF2DescriptionFactory(Config config) {
		super(config);
	}

	public void export() throws IOException, Exception {

		logger.info("Started Description Export ...");

		try {

			RF2DescriptionImpl iterator = new RF2DescriptionImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);
			
			logger.info("==========Total number of metadata description records======" + iterator.getMetaDataCount());
			
			logger.info("==========Total number of duplicated records======" + iterator.getDupRecord());

			closeExportFileWriter();

			logger.info("Finished Description Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
