package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2ConceptInactivationImpl;
import org.ihtsdo.rf2.refset.impl.RF2DescriptionInactivationImpl;
import org.ihtsdo.rf2.refset.impl.RF2RefinabilityImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2AttributeValueRefsetFactory Description: Creating Attribute Value Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2AttributeValueRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2AttributeValueRefsetFactory.class);

	public RF2AttributeValueRefsetFactory(Config config) {
		super(config);
	}

	public void export() {

		logger.info("Started Attribute Value Refset Export ...");

		try {

			RF2ConceptInactivationImpl conceptInactivationIterator = new RF2ConceptInactivationImpl(getConfig());
			Terms.get().iterateConcepts(conceptInactivationIterator);

			RF2DescriptionInactivationImpl descInactivationIterator = new RF2DescriptionInactivationImpl(getConfig());
			Terms.get().iterateConcepts(descInactivationIterator);

			//RF2RefinabilityImpl refinabilityIterator = new RF2RefinabilityImpl(getConfig());
			//ExportUtil.getTermFactory().iterateConcepts(refinabilityIterator);

			closeExportFileWriter();

			logger.info("Finished Attribute Value Refset Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
