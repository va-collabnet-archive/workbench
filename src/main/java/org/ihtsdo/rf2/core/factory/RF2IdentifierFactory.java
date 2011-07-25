package org.ihtsdo.rf2.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2IdentifierFactory Description: Creating Identifier File with the Header Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2IdentifierFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2IdentifierFactory.class);

	public RF2IdentifierFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Identifier Export ...");

		try {
			closeExportFileWriter();

			logger.info("Finished Identifier Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
