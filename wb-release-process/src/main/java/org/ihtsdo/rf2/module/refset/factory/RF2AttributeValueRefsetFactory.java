package org.ihtsdo.rf2.module.refset.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.refset.impl.RF2ConceptInactivationImpl;
import org.ihtsdo.rf2.module.refset.impl.RF2DescriptionInactivationImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2AttributeValueRefsetFactory Description: Creating Attribute Value Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2AttributeValueRefsetFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2AttributeValueRefsetFactory.class);

	/**
	 * Instantiates a new r f2 attribute value refset factory.
	 *
	 * @param config the config
	 */
	public RF2AttributeValueRefsetFactory(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
	public void export() {

		logger.info("Started Attribute Value Refset Export ...");

		try {

			RF2ConceptInactivationImpl conceptInactivationIterator = new RF2ConceptInactivationImpl(getConfig());
			Terms.get().iterateConcepts(conceptInactivationIterator);

			RF2DescriptionInactivationImpl descInactivationIterator = new RF2DescriptionInactivationImpl(getConfig());
			Terms.get().iterateConcepts(descInactivationIterator);

			closeExportFileWriter();

			logger.info("Finished Attribute Value Refset Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
