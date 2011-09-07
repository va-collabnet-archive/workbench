package org.ihtsdo.rf2.identifier.factory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.core.impl.RF2ConceptImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.identifier.impl.RF2IdGeneratorImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2IdGeneratorFactory Description: Generating sct identifier by calling idgenerator webservice  Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2IdGeneratorFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2IdGeneratorFactory.class);

	public RF2IdGeneratorFactory(Config config) {		
		super(config);
		System.out.println("==================");
	}
	
	public static void main(String[] args) {
		System.out.println("==============statrted======");
		RF2IdGeneratorFactory idFactory = new RF2IdGeneratorFactory(getConfig());
		idFactory.export();
		System.out.println("==============Finished======");
	}

	public void export() {

		logger.info("Started Id Generation Export...");

		try {
			RF2IdGeneratorImpl iterator = new RF2IdGeneratorImpl(getConfig());
			iterator.generateIdentifier();
			//Terms.get().iterateConcepts(iterator);
			closeExportFileWriter();			
			logger.info("Finished Id Generation Snapshot Export...");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());			
		}
	}
}
