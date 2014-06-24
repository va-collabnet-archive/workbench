package org.ihtsdo.rf2.identifier.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.factory.RF2IDFactory;
import org.ihtsdo.rf2.identifier.impl.RF2IdListGeneratorImpl;
import org.ihtsdo.rf2.identifier.impl.RF2RelationshipIdListGeneratorImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2RelationshipIdListGeneratorFactory Relationship: Generating relationshipId by calling idgenerator webservice using specific combination Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2RelationshipIdListGeneratorFactory extends RF2IDFactory {

	private static Logger logger = Logger.getLogger(RF2RelationshipIdListGeneratorFactory.class);

	public RF2RelationshipIdListGeneratorFactory(Config config) {		
		super(config);
	}
	
	

	public void export() {

		logger.info("Started RelationshipId Generation Snapshot Export...");

		try {		
			
			RF2RelationshipIdListGeneratorImpl iterator = new RF2RelationshipIdListGeneratorImpl(getConfig());		
			iterator.generateIdentifier();
	
//			closeExportFileWriter();			
			logger.info("Finished RelationshipId Generation Snapshot Export...");

		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
