package org.ihtsdo.rf2.compatibilitypkg.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.compatibilitypkg.impl.RF2AssociationId_SCTIDMapImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;



//This refset contains Historical References and Other references Refset.

public class RF2AssociationId_SCTIDMapFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2AssociationId_SCTIDMapFactory.class);

	public RF2AssociationId_SCTIDMapFactory(Config config) {
		super(config);
	}

	public void export() {
		
		try {
			logger.info("Started Historical Association Identifier Export ...");

			RF2AssociationId_SCTIDMapImpl iterator = new RF2AssociationId_SCTIDMapImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			logger.info("Finished Historical Association Identifier Export.");

			closeExportFileWriter();

			
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}		
		
	}
}
