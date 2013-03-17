package org.ihtsdo.rf2.test;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.core.factory.RF2StatedRelationshipFactory;
import org.ihtsdo.rf2.core.impl.RF2StatedRelationshipImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.Database;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.tk.Ts;
import java.io.File;
import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;




/**
 * Unit test for ChangeSet Generation.
 */
public class StatedRelationshipTest extends TestCase {

	private static I_TermFactory _termfactory;
	I_ConfigAceFrame _aceConfig;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *        name of the test case
	 */
	public StatedRelationshipTest(String testName) {
		super(testName);
		System.out.println("SctIDChangeSetCreation Junit Test Classes");
	}
	
	public void setUp() {
		try {
			// get and set the database location
			Database db = new Database();
			db.setLocation("E:\\Workbench_Bundle\\Prod\\PRODSERVER\\berkeley-db");
			File vodbDirectory = new File(db.getLocation());
			DatabaseSetupConfig dbSetupConfig = new DatabaseSetupConfig();
			Terms.createFactory(vodbDirectory, I_Constants.readOnly, I_Constants.cacheSize, dbSetupConfig);
			_termfactory = Terms.get();
		
		

		} catch (Exception e) {
			System.err.println("Unable to connect to the berkeley database" + e.getMessage());
		} 
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(StatedRelationshipTest.class);
	}

	/**
	 * testApp Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}
	
	
	

	//Adding newly created sctId for the new concept and committing concept 
	//New sctId is now present in the database but no change set gets created
	public void testCreateChangeset(){
		try {	
				System.out.println("===============StatedRelationshipTest  Started============");
				ExportUtil.init();
				Config config = null;
			
			    config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationship.xml");
				config.setOutputFolderName("target/export");
				config.setReleaseDate("20120131");
				config.setRf2Format("true");
				config.setFlushCount(10000);
				config.setInvokeDroolRules("false");
				config.setFileExtension("txt");
					
				
				//Below Parameters are necessary for ID-Generation
				config.setUpdateWbSctId("false");
				config.setNamespaceId("0");
				config.setPartitionId("00");
				config.setExecutionId("Daily Build Test");
				config.setModuleId("Core Component Test");
				config.setReleaseId("20120131");
				config.setComponentType("Concept Test");			
				config.setUsername("termmed");
				config.setPassword("termmed");
				config.setEndPoint("http://mgr.servers.aceworkspace.net:50002/axis2/services/id_generator");
			
				//Below Parameters are required for ID-Insertion
				config.setChangesetUserName("jmirza");
				config.setChangesetUserConcept("f7495b58-6630-3499-a44e-2052b5fcf06c");
				config.setChangesetRoot("E:/Workbench_Bundle/Prod/Test/profiles/jmirza");
				
				RF2StatedRelationshipImpl iterator = new RF2StatedRelationshipImpl(config);

				ExportUtil.getTermFactory().iterateConcepts(iterator);

				
		        System.out.println("===============StatedRelationshipTest  Finished============");
			} catch (Exception e) {
				System.out.println("Exception " +e.getMessage());
			}	
		
	}
	
	
	
}
