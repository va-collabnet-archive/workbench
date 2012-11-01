package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2Ctv3IdImpl;
import org.ihtsdo.rf2.refset.impl.RF2ICDOMapImpl;
import org.ihtsdo.rf2.refset.impl.RF2SnomedIdImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2SimpleMapRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

//This refset contains SnomedId and Ctv3Id and VTM-VMP and Non-Human and ICDO Refset.

public class RF2SimpleMapRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2SimpleMapRefsetFactory.class);

	public RF2SimpleMapRefsetFactory(Config config) {
		super(config);
	}

	public void export() {

		try {
			logger.info("Started Simple Map Snomed Id & CTV3 Id Refset Export ...");
			
			RF2SnomedIdImpl snomedIdIterator = new RF2SnomedIdImpl(getConfig());
			Terms.get().iterateConcepts(snomedIdIterator);

			RF2Ctv3IdImpl ctv3IdIterator = new RF2Ctv3IdImpl(getConfig());
			Terms.get().iterateConcepts(ctv3IdIterator);

			logger.info("Finished Simple Map SnomedId & CTV3Id  Refset Export.");
			
			logger.info("Started ICDO Map Refset Export ...");
			
			RF2ICDOMapImpl icdoMapIterator = new RF2ICDOMapImpl(getConfig());
			Terms.get().iterateConcepts(icdoMapIterator);

			closeExportFileWriter();

			logger.info("Finished ICDO Map Refset Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
}
