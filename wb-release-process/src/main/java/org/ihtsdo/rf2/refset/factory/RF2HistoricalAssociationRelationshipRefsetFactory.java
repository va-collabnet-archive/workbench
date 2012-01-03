package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2HistoricalAssociationRelationshipImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2HistoricalAssociationRelationshipImpl Description: Creating Historical relationship file Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */



public class RF2HistoricalAssociationRelationshipRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2HistoricalAssociationRelationshipRefsetFactory.class);

	public RF2HistoricalAssociationRelationshipRefsetFactory(Config config) {
		super(config);
	}
	

	public void export() {

		try {
			logger.info("Started Historical Association Relationship Export ...");
			
			RF2HistoricalAssociationRelationshipImpl historicalIterator = new RF2HistoricalAssociationRelationshipImpl(getConfig());
			Terms.get().iterateConcepts(historicalIterator);

			logger.info("Finished Simple Map SnomedId & CTV3Id  Refset Export.");

			closeExportFileWriter();

			logger.info("Finished Historical Association Relationship Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
}
