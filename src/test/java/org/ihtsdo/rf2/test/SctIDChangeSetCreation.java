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
import org.ihtsdo.rf2.util.Database;
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
public class SctIDChangeSetCreation extends TestCase {

	private static I_TermFactory _termfactory;
	I_ConfigAceFrame _aceConfig;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *        name of the test case
	 */
	public SctIDChangeSetCreation(String testName) {
		super(testName);
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
		} 
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(SctIDChangeSetCreation.class);
	}

	/**
	 * testApp Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}
	
	
	private void addConceptId() {
        try {
            //I_GetConceptData igcd = _termfactory.getConcept(UUID.fromString("aa23ea32-37cc-6cc8-e044-0003ba13161a"));
            I_GetConceptData igcd = _termfactory.getConcept(UUID.fromString("a2993d32-bf7b-3305-e044-0003ba13161a"));
            I_Identify i_Identify = Terms.get().getId(igcd.getNid());
            I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = igcd.getConAttrs();
            i_Identify.addLongId(Long.parseLong("797977"), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), 
                    i_ConceptAttributeVersioned.getStatusNid(),
                    Long.MAX_VALUE,
                    _aceConfig.getEditCoordinate().getAuthorNid(),
                    _aceConfig.getEditCoordinate().getModuleNid(),
                    i_ConceptAttributeVersioned.getPathNid());
            Terms.get().addUncommitted(igcd);
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }      
    }
	
	private void addId() {
        try {
            boolean insertConceptId=false;
			boolean insertCtv3Id=false;
			boolean insertSnomedId=false;
			
            I_GetConceptData igcd = _termfactory.getConcept(UUID.fromString("122ccc03-c72f-4969-94d5-af5a7004905f"));
            I_Identify i_Identify = Terms.get().getId(igcd.getNid());
            I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = igcd.getConAttrs();
            
            /*i_Identify.addLongId(Long.parseLong("797977"), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), 
            		i_ConceptAttributeVersioned.getStatusNid(),
            		i_ConceptAttributeVersioned.getPathNid(),
            		Long.MAX_VALUE);
           */ 
    					
			//DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			long effectiveDate=df.parse("20120131").getTime();
					
			//get conceptId by calling webservice 
			String wsConceptId ="449804003";
			insertConceptId =  i_Identify.addLongId(Long.parseLong(wsConceptId), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), 
			      	i_ConceptAttributeVersioned.getStatusNid(),
			        Long.MAX_VALUE,
                                _aceConfig.getEditCoordinate().getAuthorNid(),
                                _aceConfig.getEditCoordinate().getModuleNid(),
                                i_ConceptAttributeVersioned.getPathNid());
			System.out.println("==SctId insertion finish==" + wsConceptId + "	" + insertConceptId);
			
			//get ctv3Id by calling webservice 
			String wsCtv3Id = "XUl7b";
			//insert ctv3id if conceptId inserted Successfully
			insertCtv3Id = i_Identify.addStringId(wsCtv3Id, ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid(), 
			      	i_ConceptAttributeVersioned.getStatusNid(),
                                Long.MAX_VALUE,
                                _aceConfig.getEditCoordinate().getAuthorNid(),
                                _aceConfig.getEditCoordinate().getModuleNid(),
			        i_ConceptAttributeVersioned.getPathNid());
			System.out.println("==Ctv3Id insertion finish==" + wsCtv3Id + "	" + insertCtv3Id);
				
			
			//get snomedId by calling webservice
			String wsSnomedId ="DF-00900";				
			//insert snomedid if conceptId & Ctv3Id inserted Successfully
			insertSnomedId = i_Identify.addStringId(wsSnomedId, ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid(), 
			      	i_ConceptAttributeVersioned.getStatusNid(),
                                Long.MAX_VALUE,
                                _aceConfig.getEditCoordinate().getAuthorNid(),
                                _aceConfig.getEditCoordinate().getModuleNid(),
			        i_ConceptAttributeVersioned.getPathNid());
			System.out.println("==SnomedId insertion finish==" + wsSnomedId + "	" + insertSnomedId);
			
			//Adding all the uncommited changes to Terms factory
			Terms.get().addUncommitted(igcd);
            
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }      
    }
	
	
	public void setupProfile() throws TerminologyException, IOException{
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
		
		ChangeSetWriterHandler.addWriter(newDbProfile.getUsername() + ".commitLog.xls",
				new CommitLog(new File(newDbProfile.getChangeSetRoot(),
				"commitLog.xls"), new File(newDbProfile.getChangeSetRoot(),
						"." + "commitLog.xls")));	
		
		
		 Ts.get().addChangeSetGenerator(tempKey, generator);
	}

	//Adding newly created sctId for the new concept and committing concept 
	//New sctId is now present in the database but no change set gets created
	public void testCreateChangeset(){
		try {	
				System.out.println("===============ChangeSet Creation Started============");
				addId();
				setupProfile();
		        Ts.get().commit();
		        //Ts.get().removeChangeSetGenerator(tempKey);
		        System.out.println("===============ChangeSet Creation Finished============");
			} catch (Exception e) {
				System.out.println("Exception " +e.getMessage());
			}	
		
	}
	
	
	
}
