package org.ihtsdo.rf2.core.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.core.impl.RF2ConceptImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2ConceptFactory Description: Creating Concept Specific methods required by ConceptIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2ConceptFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2ConceptFactory.class);

	public RF2ConceptFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Concept Export (new process)...");

		try {

			RF2ConceptImpl iterator = new RF2ConceptImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			closeExportFileWriter();
			
			logger.info("==========Total number of metadata concept records======" + iterator.getMetaDataCount());

			logger.info("==========Total number of duplicated records======" + iterator.getDupRecord());

			logger.info("Finished Concept Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
