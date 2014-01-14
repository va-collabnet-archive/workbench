package org.ihtsdo.rf2.test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.core.dao.ConceptDAO;
import org.ihtsdo.rf2.core.dao.DescriptionDAO;
import org.ihtsdo.rf2.util.Database;
import org.ihtsdo.rf2.util.ExportUtil;

import org.ihtsdo.tk.Ts;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;



/**
 * Unit test for simple App.
 */
public class InsertIDTest extends TestCase {

	private static I_TermFactory _termfactory;
	I_ConfigAceFrame _aceConfig;
	
	private Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
	private Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();

	private I_IntSet allowedDestRelTypes;
	private I_GetConceptData snomedRoot;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public InsertIDTest(String testName) {
		super(testName);
		System.out.println("ExportUtilTest Junit Test Classes");
	}

	public void setUp() {
		try {
			// get and set the database location
			Database db = new Database();
			db.setName("IHTSDO");
			db.setLocation("E:\\Workbench_Bundle\\UAT\\UAT-2011-30-10\\berkeley-db");
			
			ExportUtil.createTermFactory(db);
			ExportUtil.init();
			// ExportUtil.InitializeFileName();
			_termfactory = ExportUtil.getTermFactory();
			ExportUtil.createAceConfig();
			_aceConfig = ExportUtil.getAceConfig();
			
			System.out.println("=======1=======" + _aceConfig.getPrecedence().getDescription());
		} catch (Exception e) {
			System.err.println("Unable to connect to the berkeley database" + e.getMessage());
		} finally {
			/*try {
				ExportUtil.getTermFactory().close();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(InsertIDTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void xtestApp() {
		assertTrue(true);
	}

	
	public void testCreateChangeset(int componentNid){
		
		
		String wsSctId = "9999999";
		boolean flag = false;
		try {	
			
				DateFormat df = new SimpleDateFormat("yyyyMMdd");
				long effectiveDate=df.parse("20120131").getTime();
		
			
				I_GetConceptData testConcept = _termfactory.getConcept(UUID.fromString("5c37c3f6-22f4-4631-b0a1-95bee7f8b825"));
				System.out.println(testConcept.getInitialText());
		
				I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = testConcept.getConAttrs();
				List<?> conceptAttributeTupleList = i_ConceptAttributeVersioned.getTuples();
				
				I_Identify i_Identify = _termfactory.getId(componentNid);	
				
		            
				BdbTermFactory tfb = (BdbTermFactory) _termfactory;
				I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
				newDbProfile.setUsername("susan-test");
				newDbProfile.setUserConcept(_termfactory.getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
				newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
				newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
				newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
				newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
				File changeSetRoot = new File("E:\\Temp", "changesets");
				changeSetRoot.mkdirs();
				//File changeSetRoot = new File("profiles" + File.separator + "susan-test" + File.separator + "changesets");
				String changeSetWriterFileName = "susan-test" + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".eccs"; 
				newDbProfile.setChangeSetRoot(changeSetRoot);
				newDbProfile.setChangeSetWriterFileName(changeSetWriterFileName);
				String tempKey = UUID.randomUUID().toString();
				 ChangeSetGeneratorBI generator = Ts.get().createDtoChangeSetGenerator(
							new File(newDbProfile.getChangeSetRoot(),
									newDbProfile.getChangeSetWriterFileName()), 
									new File(newDbProfile.getChangeSetRoot(), "#0#"
											+ newDbProfile.getChangeSetWriterFileName()),
											ChangeSetGenerationPolicy.MUTABLE_ONLY);
				Ts.get().addChangeSetGenerator(tempKey, generator);
		        try {
		           	Terms.get().commit();
		        } catch (Exception e) {
		            throw new TaskFailedException();
		        } finally {
		             Ts.get().removeChangeSetGenerator(tempKey);
		        }				   
				  
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
						effectiveDate,
                                                _aceConfig.getEditCoordinate().getAuthorNid(),
                                                _aceConfig.getEditCoordinate().getModuleNid(),
                                                i_ConceptAttributeVersioned.getPathNid());
				I_GetConceptData commitedConcept = _termfactory.getConceptForNid(componentNid);
				System.out.println("==flag==" + flag);
				
				_termfactory.addUncommitted(commitedConcept);
				_termfactory.commit();
				
			} catch (NullPointerException ne) {
				ne.printStackTrace();
				System.out.println("NullPointerException " +ne.getMessage());
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println("NumberFormatException " +e.getMessage());
			} catch (TerminologyException e) {
				e.printStackTrace();
				System.out.println("TerminologyException " +e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IOException " +e.getMessage());;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception " +e.getMessage());
			}	
		
	}
	
	
	
	public void xtestInsertConceptId() throws IOException {
		
		try {
			I_GetConceptData testConcept = _termfactory.getConcept(UUID.fromString("cff53f1a-1d11-5ae7-801e-d3301cfdbea0"));
			System.out.println(testConcept.getInitialText());
			
			int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
			Long sctid = new Long("12345");
				
			I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = testConcept.getConAttrs();
			List<?> conceptAttributeTupleList = i_ConceptAttributeVersioned.getTuples();
				
				if (conceptAttributeTupleList.size() > 0 && conceptAttributeTupleList != null) {
					for (int i = 0; i < conceptAttributeTupleList.size(); i++) {
						I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributeTupleList.get(i);
						int pathNid = i_ConceptAttributeTuple.getPathNid();
						int statusNid =  i_ConceptAttributeTuple.getStatusNid();
						long effectiveDate = System.nanoTime();
						
						System.out.println("===========Initial=========");
						I_Identify i_Identify = testConcept.getIdentifier();
						List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();						
						if (i_IdentifyList.size() > 0) {
							for (int j = 0; j < i_IdentifyList.size(); j++) {
								I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(j);
								Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
								System.out.println(denotion.toString());
							}							
						}
		
						boolean flag = i_Identify.addLongId(sctid, arcAuxSnomedIntegerNid,
                                                        statusNid,
                                                        effectiveDate,
                                                        _aceConfig.getEditCoordinate().getAuthorNid(),
                                                        _aceConfig.getEditCoordinate().getModuleNid(),
                                                        pathNid);
						System.out.println("===insertion status=======" + flag);
			
						System.out.println("===========immediate=========");
						I_Identify i_Identify_after = testConcept.getIdentifier();
						List<? extends I_IdVersion> i_IdentifyAfterList = i_Identify_after.getIdVersions();
						
						if (i_IdentifyAfterList.size() > 0) {
							for (int j = 0; j < i_IdentifyAfterList.size(); j++) {
								I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList.get(j);
								Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
								System.out.println(denotion.toString());
							}							
						}	
					}
					
					_termfactory.addUncommitted(testConcept);
					_termfactory.commit();
					System.out.println("===========final=========");
					I_Identify i_Identify_after = testConcept.getIdentifier();
					List<? extends I_IdVersion> i_IdentifyAfterList = i_Identify_after.getIdVersions();
					
					if (i_IdentifyAfterList.size() > 0) {
						for (int j = 0; j < i_IdentifyAfterList.size(); j++) {
							I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList.get(j);
							Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
							System.out.println(denotion.toString());
						}							
					}						
					
				}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	public void xtestAddDescription() throws IOException, TerminologyException , Exception {
	
			    
	/*	
	  	I_ConfigAceFrame config = NewDefaultProfile.newProfile("", "", "", "", "");
		for (PositionBI pos: config.getViewPositionSet()) {
	        config.getEditingPathSet().add(pos.getPath());
		}
	    Concept testConcept = (Concept) Terms.get().newConcept(UUID.randomUUID(), false, _aceConfig);
	    Terms.get().newDescription(UUID.randomUUID(), testConcept, "en", "testing by VP", 
		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), _aceConfig);
		Terms.get().addUncommittedNoChecks(testConcept);
        Terms.get().commit();*/

        UUID primaryUuid = UUID.fromString("cff53f1a-1d11-5ae7-801e-d3301cfdbea0");
		I_GetConceptData testConcept1 = _termfactory.getConcept(primaryUuid);
		System.out.println(testConcept1.getInitialText());
		

		Collection<? extends I_DescriptionVersioned> descs1 = testConcept1.getDescs();
		if (!descs1.isEmpty() && descs1.size() > 0) {
			for (I_DescriptionVersioned<?> desc : descs1) {
				for (I_DescriptionPart<?> descPart : desc.getMutableParts()) {
						System.out.println(" Text : " + descPart.getText() + " Status : " + descPart.getStatusNid() + " InitialSignficant : " + descPart.isInitialCaseSignificant() );
					}
				}
			}
		
		Terms.get().newDescription(UUID.fromString("8eb80114-d648-51ae-aee2-2883389f4111"), testConcept1 , "en", "testing1111 by VP",
		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()),
		_aceConfig, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()),System.nanoTime());
		
	/*		t = Terms.get().newDescription(newDescriptionId, testConcept1 , "en", "testing2222 by VP",
			Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()),
			_aceConfig);*/

		_termfactory.addUncommitted(testConcept1);
		_termfactory.commit();
		
		if (_termfactory.hasConcept(testConcept1.getNid())) {
            I_GetConceptData concept = _termfactory.getConcept(testConcept1.getNid());
            _termfactory.addUncommittedNoChecks(concept);
            _termfactory.commit();
        }
		
		Collection<? extends I_DescriptionVersioned> descs = testConcept1.getDescs();
		if (!descs.isEmpty() && descs.size() > 0) {
			for (I_DescriptionVersioned<?> desc : descs) {
				for (I_DescriptionPart<?> descPart : desc.getMutableParts()) {
						System.out.println(" Text : " + descPart.getText() + " Status : " + descPart.getStatusNid() + " InitialSignficant : " + descPart.isInitialCaseSignificant() );
					}
				}
			}
		}
	

	public void xtestInsertId() throws IOException, TerminologyException , Exception {
		
		UUID primaryUuid = UUID.fromString("cff53f1a-1d11-5ae7-801e-d3301cfdbea0");
		I_Identify versioned = _termfactory.getId(primaryUuid);
		I_GetConceptData testConcept = _termfactory.getConcept(primaryUuid);
		System.out.println(testConcept.getInitialText());
		
		int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
		long effectiveDate = System.currentTimeMillis();
		Long sctid = new Long("77777");
		
		System.out.println("===========initial=========");
		I_Identify i_Identify = _termfactory.getId(primaryUuid);
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();						
		if (i_IdentifyList.size() > 0) {
			for (int j = 0; j < i_IdentifyList.size(); j++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(j);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				System.out.println(denotion.toString());
			}							
		}
		
		boolean flag = versioned.addLongId(sctid, arcAuxSnomedIntegerNid,
                        _termfactory.uuidToNative(UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")),
                        effectiveDate,
                        _aceConfig.getEditCoordinate().getAuthorNid(),
                        _aceConfig.getEditCoordinate().getModuleNid(),
                        _termfactory.uuidToNative(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")));
		System.out.println("===insertion status=======" + flag);
		
		System.out.println("===========immediate=========");
		I_Identify i_Identify_after = _termfactory.getId(primaryUuid);
		List<? extends I_IdVersion> i_IdentifyAfterList = i_Identify_after.getIdVersions();
		
		if (i_IdentifyAfterList.size() > 0) {
			for (int j = 0; j < i_IdentifyAfterList.size(); j++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList.get(j);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				System.out.println(denotion.toString());
			}							
		}
		
        if (_termfactory.hasConcept(versioned.getNid())) {
            I_GetConceptData concept = _termfactory.getConcept(versioned.getNid());
            _termfactory.addUncommittedNoChecks(concept);
        }
        
        
        
		System.out.println("===========final=========");
		I_Identify i_Identify_final = _termfactory.getId(primaryUuid);
		List<? extends I_IdVersion> i_IdentifyAfterList_final = i_Identify_final.getIdVersions();
		
		if (i_IdentifyAfterList_final.size() > 0) {
			for (int j = 0; j < i_IdentifyAfterList_final.size(); j++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList_final.get(j);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				System.out.println(denotion.toString());
			}							
		}
	}
	
	
	public void xtestInsertDescriptionId() throws IOException, TerminologyException , Exception {
		
		_termfactory.getPaths();
		
		UUID primaryUuid = UUID.fromString("8eb80114-d648-51ae-aee2-2883389f4111");
		I_Identify versioned = _termfactory.getId(primaryUuid);
		int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
		long effectiveDate = System.nanoTime();
		//long effectiveDate = Long.parseLong("20110406");
		Long sctid = new Long("111111");
		
		System.out.println("===========before=========");
		I_Identify i_Identify = _termfactory.getId(primaryUuid);
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();						
		if (i_IdentifyList.size() > 0) {
			for (int j = 0; j < i_IdentifyList.size(); j++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(j);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				System.out.println(denotion.toString());
			}							
		}
		
		boolean flag = versioned.addLongId(sctid, arcAuxSnomedIntegerNid,
                        _termfactory.uuidToNative(UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")),
                        effectiveDate,
                        _aceConfig.getEditCoordinate().getAuthorNid(),
                        _aceConfig.getEditCoordinate().getModuleNid(),
                        ExportUtil.getNid(I_Constants.SNOMED_CORE_PATH_UID));
		System.out.println("===insertion value=======" + flag);
		
		System.out.println("===========after=========");
		I_Identify i_Identify_after = _termfactory.getId(primaryUuid);
		List<? extends I_IdVersion> i_IdentifyAfterList = i_Identify_after.getIdVersions();
		
		if (i_IdentifyAfterList.size() > 0) {
			for (int j = 0; j < i_IdentifyAfterList.size(); j++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList.get(j);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				System.out.println(denotion.toString());
			}							
		}
		
        if (_termfactory.hasConcept(versioned.getNid())) {
            I_GetConceptData concept = _termfactory.getConcept(versioned.getNid());
            _termfactory.addUncommittedNoChecks(concept);
            _termfactory.commit();
        }
        
        
        
		System.out.println("===========final=========");
		I_Identify i_Identify_final = _termfactory.getId(primaryUuid);
		List<? extends I_IdVersion> i_IdentifyAfterList_final = i_Identify_final.getIdVersions();
		
		if (i_IdentifyAfterList_final.size() > 0) {
			for (int j = 0; j < i_IdentifyAfterList_final.size(); j++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList_final.get(j);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				System.out.println(denotion.toString());
			}							
		}
	}
	
}
