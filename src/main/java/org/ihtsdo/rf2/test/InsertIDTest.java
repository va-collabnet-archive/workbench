package org.ihtsdo.rf2.test;

import java.io.IOException;
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
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
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

import org.ihtsdo.tk.api.KindOfCacheBI;


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
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
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
	public static KindOfCacheBI myStaticIsACache;

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
			db.setLocation("E:\\Workbench_Bundle\\Sync_Bundle\\03312011\\wb-bundle\\berkeley-db");
						
			ExportUtil.createTermFactory(db);
			ExportUtil.init();
			// ExportUtil.InitializeFileName();
			_termfactory = ExportUtil.getTermFactory();
			//_aceConfig = ExportUtil.createAceConfig();
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

	public void xtestInsertConceptId() throws IOException {
		
		try {
			I_GetConceptData testConcept = _termfactory.getConcept(UUID.fromString("cff53f1a-1d11-5ae7-801e-d3301cfdbea0"));
			System.out.println(testConcept.getInitialText());
			
			int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
			Long sctid = new Long("12345");
				
			I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = testConcept.getConceptAttributes();
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
		
						boolean flag = i_Identify.addLongId(sctid, arcAuxSnomedIntegerNid, statusNid, pathNid, effectiveDate);
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
		

		Collection<? extends I_DescriptionVersioned> descs1 = testConcept1.getDescriptions();
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
		
		Collection<? extends I_DescriptionVersioned> descs = testConcept1.getDescriptions();
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
		
		boolean flag = versioned.addLongId(sctid, arcAuxSnomedIntegerNid, _termfactory.uuidToNative(UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")), _termfactory.uuidToNative(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")), effectiveDate);
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
		
		boolean flag = versioned.addLongId(sctid, arcAuxSnomedIntegerNid, _termfactory.uuidToNative(UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")), ExportUtil.getNid(I_Constants.SNOMED_CORE_PATH_UID), effectiveDate);
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
