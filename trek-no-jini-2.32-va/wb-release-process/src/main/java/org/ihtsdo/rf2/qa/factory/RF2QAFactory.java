package org.ihtsdo.rf2.qa.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.core.impl.RF2ConceptImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.qa.impl.RF2QAImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2ConceptFactory Description: Creating Concept Specific methods required by ConceptIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2QAFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2QAFactory.class);

	public RF2QAFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Snapshot QA Export...");

		try {

			RF2QAImpl iterator = new RF2QAImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			closeExportFileWriter();
			
			
			logger.info("Finished Snapshot QA Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
