package org.ihtsdo.rf2.workflow.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.core.impl.RF2ConceptImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.workflow.impl.RF2WorkflowHistoryImpl;

/**
 * Title: RF2ConceptFactory Description: Creating Concept Specific methods required by ConceptIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 * 
 * Modified by Alejandro Rodriguez
 * Date 20150311
 */

public class RF2WorkflowHistoryFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2WorkflowHistoryFactory.class);

	public RF2WorkflowHistoryFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Workflow History Export...");

		try {

			RF2WorkflowHistoryImpl iterator = new RF2WorkflowHistoryImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			closeExportFileWriter();
			
		
			logger.info("Finished Workflow History Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
