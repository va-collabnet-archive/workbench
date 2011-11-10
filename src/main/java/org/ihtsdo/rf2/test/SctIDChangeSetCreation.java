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
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.util.Database;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.KindOfCacheBI;
import java.io.File;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;




/**
 * Unit test for simple App.
 */
public class SctIDChangeSetCreation extends TestCase {

	private static I_TermFactory _termfactory;
	I_ConfigAceFrame _aceConfig;
	public static KindOfCacheBI myStaticIsACache;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *        name of the test case
	 */
	public SctIDChangeSetCreation(String testName) {		
		System.out.println("SctIDChangeSetCreation Junit Test Classes");
	}

	public void setUp() {
		try {
			// get and set the database location
			Database db = new Database();
			db.setLocation("E:\\Workbench_Bundle\\UAT\\UAT-2011-30-10\\berkeley-db");
			File vodbDirectory = new File(db.getLocation());
			DatabaseSetupConfig dbSetupConfig = new DatabaseSetupConfig();
			Terms.createFactory(vodbDirectory, I_Constants.readOnly, I_Constants.cacheSize, dbSetupConfig);
			_termfactory = Terms.get();
		} catch (Exception e) {
			System.err.println("Unable to connect to the berkeley database" + e.getMessage());
		} finally {
		
		}
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(InsertIDTest.class);
	}

	/**
	 * testApp Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	//Adding newly created sctId for the new concept and committing concept 
	//New sctId is now present in the database but no change set gets created
	//Recurrent varicose vein of lower leg (disorder)
	public void testCreateChangeset(){
		String wsSctId = "797979"; // Dummy ids
		boolean flag = false;
		try {	
				System.out.println("===============ChangeSet Creation Started============");
				DateFormat df = new SimpleDateFormat("yyyyMMdd");
				long effectiveDate=df.parse("20120131").getTime(); //Putting hardcoded values
			
				I_GetConceptData testConcept = _termfactory.getConcept(UUID.fromString("00119899-e520-5acd-ae88-86f7eb109cc9"));
				I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = testConcept.getConceptAttributes();
				I_Identify i_Identify = _termfactory.getId(testConcept.getNid());	
				BdbTermFactory tfb = (BdbTermFactory) _termfactory;
				I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
				newDbProfile.setUsername("testvp");
				newDbProfile.setUserConcept(_termfactory.getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
				newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
				newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
				newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.MUTABLE_ONLY);
				newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
				//File changeSetRoot = new File("profiles" + File.separator + newDbProfile.getUsername() + File.separator + "changesets");				
				File changeSetRoot = new File("E:\\Workbench_Bundle\\UAT\\UAT-2011-30-10\\profiles\\testvp", "changesets");
				changeSetRoot.mkdirs();
				String changeSetWriterFileName = "testvp" + "." + "#" + 1 + "#" + UUID.randomUUID().toString() + ".eccs"; 
				newDbProfile.setChangeSetRoot(changeSetRoot);
				newDbProfile.setChangeSetWriterFileName(changeSetWriterFileName);
				String tempKey = UUID.randomUUID().toString();
				
				ChangeSetGeneratorBI generator = Ts.get().createDtoChangeSetGenerator(
							new File(newDbProfile.getChangeSetRoot(),
									newDbProfile.getChangeSetWriterFileName()), 
									new File(newDbProfile.getChangeSetRoot(), "#1#"
											+ newDbProfile.getChangeSetWriterFileName()),
											ChangeSetGenerationPolicy.MUTABLE_ONLY);			
				 Ts.get().addChangeSetGenerator(tempKey, generator);		        			   
				  
		        /*		   
				ChangeSetWriterHandler.addWriter(newDbProfile.getUsername()
						+ ".eccs", new EConceptChangeSetWriter(new File(newDbProfile.getChangeSetRoot(), newDbProfile.getChangeSetWriterFileName()), 
								new File(newDbProfile.getChangeSetRoot(), "."
										+ newDbProfile.getChangeSetWriterFileName()), 
										ChangeSetGenerationPolicy.INCREMENTAL, true));
				
				ChangeSetWriterHandler.addWriter(newDbProfile.getUsername() + ".commitLog.xls",
						new CommitLog(new File(newDbProfile.getChangeSetRoot(),
						"commitLog.xls"), new File(newDbProfile.getChangeSetRoot(),
								"." + "commitLog.xls")));	
				*/
		        
				flag = i_Identify.addLongId(Long.parseLong(wsSctId), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), 
						i_ConceptAttributeVersioned.getStatusNid(),
						i_ConceptAttributeVersioned.getPathNid(),
						effectiveDate);
				
				I_GetConceptData commitedConcept = _termfactory.getConceptForNid(testConcept.getNid());
				_termfactory.addUncommitted(commitedConcept);
		        Ts.get().commit();
		        
		     
		        //Ts.get().removeChangeSetGenerator(tempKey);
		        System.out.println("===============ChangeSet Creation Finished============");
			} catch (Exception e) {
				System.out.println("Exception " +e.getMessage());
			}	
		
	}
	
	
	
}
