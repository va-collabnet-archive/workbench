package org.ihtsdo.rf2.identifier.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.identifier.impl.RF2IdInsertionImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2InsertIdentifierFactory Description: Inserting Identifier in the workbench methods required by ConceptIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2IdInsertionFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2IdInsertionFactory.class);

	public RF2IdInsertionFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Identifier Insertion ...");

		try {

			RF2IdInsertionImpl iterator = new RF2IdInsertionImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			closeExportFileWriter();
			
			
			logger.info("Finished Identifier Insertion ...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
