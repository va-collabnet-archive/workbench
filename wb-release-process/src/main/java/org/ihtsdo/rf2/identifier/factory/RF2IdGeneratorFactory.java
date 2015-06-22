package org.ihtsdo.rf2.identifier.factory;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.factory.RF2IDFactory;
import org.ihtsdo.rf2.identifier.impl.RF2IdGeneratorImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2IdGeneratorFactory Description: Generating sct identifier by calling idgenerator webservice  Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2IdGeneratorFactory extends RF2IDFactory {

	private static Logger logger = Logger.getLogger(RF2IdGeneratorFactory.class);

	public RF2IdGeneratorFactory(Config config) {		
		super(config);
	}
	
	

	public void export() {

		logger.info("Started Id Generation Export...");

		try {		
	
		
			RF2IdGeneratorImpl iterator = new RF2IdGeneratorImpl(getConfig());		
			iterator.generateIdentifier();
	
//			closeExportFileWriter();			
			logger.info("Finished Id Generation Snapshot Export...");

		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
