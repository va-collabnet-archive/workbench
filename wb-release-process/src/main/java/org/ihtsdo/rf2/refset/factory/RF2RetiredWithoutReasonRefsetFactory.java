package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2ConceptInactivationImpl;
import org.ihtsdo.rf2.refset.impl.RF2ConceptRetiredWithoutReasonImpl;
import org.ihtsdo.rf2.refset.impl.RF2DescriptionInactivationImpl;
import org.ihtsdo.rf2.refset.impl.RF2DescriptionRetiredWithoutReasonImpl;
import org.ihtsdo.rf2.refset.impl.RF2RefinabilityImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2RetiredWithoutReasonRefsetFactory Concept: RF2 Release QA Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2RetiredWithoutReasonRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2RetiredWithoutReasonRefsetFactory.class);

	public RF2RetiredWithoutReasonRefsetFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Concept/Description Retired Without Reason Refset Export ...");

		try {

			RF2ConceptRetiredWithoutReasonImpl conceptInactivationIterator = new RF2ConceptRetiredWithoutReasonImpl(getConfig());
			Terms.get().iterateConcepts(conceptInactivationIterator);

			RF2DescriptionRetiredWithoutReasonImpl descInactivationIterator = new RF2DescriptionRetiredWithoutReasonImpl(getConfig());
			Terms.get().iterateConcepts(descInactivationIterator);

			
			closeExportFileWriter();

			logger.info("Finished Concept/Description Retired Without Reason Refset Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
