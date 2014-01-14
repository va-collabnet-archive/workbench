package org.ihtsdo.rf2.test;

import java.sql.Timestamp;
import java.util.Date;

import junit.framework.TestCase;

import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;

public class ConceptExporterMojoTest extends TestCase {

	public void setup() {
		System.out.println("Inside setup");
	}

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 * @return
	 */
	public void testApp() {
		System.out.println("**************Starting Data Extraction Program************" + (new Timestamp(new Date().getTime())));
		System.out.println("ConceptExporterMojoTest Test Cases");
		try {

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/concept.xml");

			config.setReleaseDate("20020731");
			config.setFileExtension("txt");

			config.setOutputFolderName("export");
			config.setDefaults();
			
			config.setDroolsDrlFile("org/ihtsdo/rf2/core/drools/rules/ConceptReleaseRules.drl");

			RF2ConceptFactory factory = new RF2ConceptFactory(config);
			factory.export();

			/*
			 * DescriptionExport descriptionExport = new DescriptionExport(); I_TermFactory descTermFactory = descriptionExport.getTermFactory(); descriptionExport.descriptionExport();
			 * descTermFactory.close();
			 * 
			 * RelationshipExport relationshipExport = new RelationshipExport(); I_TermFactory relTermFactory = relationshipExport.getTermFactory(); relationshipExport.relationshipExport();
			 * relTermFactory.close();
			 * 
			 * StatedRelationshipExport statedRelationshipExport = new StatedRelationshipExport(); I_TermFactory statedRelTermFactory = statedRelationshipExport.getTermFactory();
			 * statedRelationshipExport.statedRelationshipExport(); statedRelTermFactory.close();
			 */

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("**************Ending Data Extraction Program************ " + (new Timestamp(new Date().getTime())));
		System.exit(0);
	}

}
