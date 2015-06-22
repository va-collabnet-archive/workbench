package org.ihtsdo.rf2.compatibilitypkg.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.compatibilitypkg.impl.RF2HistoricalAssociationIdentImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2HistoricalAssociationRefsetFactory Description: Creating Historical Association Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */


//This refset contains Historical References and Other references Refset.

public class RF2HistoricalAssociationIdentFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2HistoricalAssociationIdentFactory.class);

	public RF2HistoricalAssociationIdentFactory(Config config) {
		super(config);
	}

	public void export() {
		
		try {
			logger.info("Started Historical Association Identifier Export ...");

			RF2HistoricalAssociationIdentImpl iterator = new RF2HistoricalAssociationIdentImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			logger.info("Finished Historical Association Identifier Export.");

			closeExportFileWriter();

			
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}		
		
	}
}
