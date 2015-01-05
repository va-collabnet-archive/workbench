package org.ihtsdo.rf2.identifier.factory;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.factory.RF2IDFactory;
import org.ihtsdo.rf2.identifier.impl.RF2IdListGeneratorImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2IdGeneratorFactory Description: Generating sct identifier by calling idgenerator webservice  Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2IdListGeneratorFactory extends RF2IDFactory {

	private static Logger logger = Logger.getLogger(RF2IdListGeneratorFactory.class);

	public RF2IdListGeneratorFactory(Config config) {		
		super(config);
	}
	
	

	public void export() {

		logger.info("Started Id Generation Export...");

		try {		
	
		
			RF2IdListGeneratorImpl iterator = new RF2IdListGeneratorImpl(getConfig());		
			iterator.generateIdentifier();
	
//			closeExportFileWriter();			
			logger.info("Finished Id Generation Snapshot Export...");

		
		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
