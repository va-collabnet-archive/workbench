package org.ihtsdo.rf2.identifier.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.identifier.impl.RF2ConceptWOmapImpl;
import org.ihtsdo.rf2.util.Config;

public class RF2ConceptWOmapFactory extends RF2AbstractFactory {
		private static Logger logger = Logger.getLogger(RF2ConceptWOmapFactory.class);

		public RF2ConceptWOmapFactory(Config config) {
			super(config);
		}

		public void export() {

			logger.info("Started Concept without map export...");

			try {

				RF2ConceptWOmapImpl iterator = new RF2ConceptWOmapImpl(getConfig());

				Terms.get().iterateConcepts(iterator);

				closeExportFileWriter();
				

				logger.info("Finished Concept without map export...");

			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage());			
			}
		}
}

