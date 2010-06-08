package org.ihtsdo.db.bdb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessConceptData;
import org.ihtsdo.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.etypes.EConcept;
import org.junit.Assert;
import org.junit.Test;

public class CementLoadTest {

	String dbTarget;
	DataOutputStream eConceptDOS;

	@Test
	public void cementLoadTest() {
		try {
			dbTarget = "target/" + UUID.randomUUID();
			MemoryTermServer mts = new MemoryTermServer();
			LocalFixedTerminology.setStore(mts);
			mts.setGenerateIds(true);
			ArchitectonicAuxiliary aa = new ArchitectonicAuxiliary();
			aa.addToMemoryTermServer(mts);
			File directory = new File(dbTarget);
			directory.mkdirs();


			File eConceptsFile = new File(dbTarget, "eConcepts.jbin");
			eConceptsFile.getParentFile().mkdirs();
			BufferedOutputStream eConceptsBos = new BufferedOutputStream(
					new FileOutputStream(eConceptsFile));
			eConceptDOS = new DataOutputStream(eConceptsBos);
			for (I_ConceptualizeLocally localConcept: mts.getConcepts()) {
				EConcept eC = new EConcept(localConcept.universalize());
				eC.writeExternal(eConceptDOS);
			}
			eConceptDOS.close();

			Bdb.setup(dbTarget);
			FileInputStream fis = new FileInputStream(eConceptsFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream in = new DataInputStream(bis);

			try {
				while (true) {
					EConcept eConcept = new EConcept(in);
					Concept newConcept = Concept.get(eConcept);
					Bdb.getConceptDb().writeConcept(newConcept);
				}
			} catch (EOFException e) {
				in.close();
			}
			System.out.println("finished load, start sync");
			Bdb.sync();
			System.out.println("Finished db sync.");
			Bdb.close();
			
			Bdb.setup(dbTarget);
			I_RepresentIdSet cids = Bdb.getConceptDb().getConceptIdSet();
			I_RepresentIdSet roCids = Bdb.getConceptDb().getReadOnlyConceptIdSet();
			Assert.assertEquals(cids, roCids);
			System.out.println("Concept count: " + cids.cardinality());
			
			AddStringIdProcessor addProcessor  = new AddStringIdProcessor(cids);
			Bdb.getConceptDb().iterateConceptDataInParallel(addProcessor);
			System.out.println("Unprocessed: " + addProcessor.getCids().cardinality());
			if (addProcessor.getCids().cardinality() != 0) {
				System.out.println("Unprocessed nids[0]: " + addProcessor.getCids());
			}
			Assert.assertEquals(0, addProcessor.getCids().cardinality());
			Bdb.commit();
			
			CheckStringIdProcessor csidp = new CheckStringIdProcessor(cids);
			Bdb.getConceptDb().iterateConceptDataInParallel(csidp);
			System.out.println("Unmatched: " + csidp.getCids().cardinality());
			if (csidp.getCids().cardinality() != 0) {
				System.out.println("Unprocessed nids[1]: " + csidp.getCids());
			}
			Assert.assertEquals(0, csidp.getCids().cardinality());
			Bdb.close();
			
			Bdb.setup(dbTarget);
			csidp = new CheckStringIdProcessor(cids);
			Bdb.getConceptDb().iterateConceptDataInParallel(csidp);
			Bdb.close();
			System.out.println("Unmatched: " + csidp.getCids().cardinality());
			if (csidp.getCids().cardinality() != 0) {
				System.out.println("Unprocessed nids[2]: " + csidp.getCids());
			}
			Assert.assertEquals(0, csidp.getCids().cardinality());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}

	}
	
	private static class AddStringIdProcessor implements I_ProcessConceptData {

		I_RepresentIdSet cids;
		
		public I_RepresentIdSet getCids() {
			return cids;
		}

		public AddStringIdProcessor(I_RepresentIdSet cids) {
			super();
			this.cids = new IdentifierSet((IdentifierSet) cids);
		}

		@Override
		public void processConceptData(Concept concept) throws Exception {
			cids.setNotMember(concept.getNid());
			String uuidOid = "2.25." + concept.getConceptAttributes().getPrimUuid();
			int statusNid = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			int pathNid = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
			int authorityNID = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.OID.getUids());
			IdentifierVersionString stringId = 
				new IdentifierVersionString(statusNid, pathNid, 
						Long.MAX_VALUE, uuidOid, authorityNID);
			concept.getConceptAttributes().addIdVersion(stringId);
			Terms.get().addUncommitted(concept);
			
		}

		@Override
		public boolean continueWork() {
			return true;
		}
		
	}
	
	private static class CheckStringIdProcessor implements I_ProcessConceptData {
		I_RepresentIdSet cids;
		
		public I_RepresentIdSet getCids() {
			return cids;
		}

		public CheckStringIdProcessor(I_RepresentIdSet cids) {
			super();
			this.cids = new IdentifierSet((IdentifierSet) cids);
		}

		@Override
		public void processConceptData(Concept concept) throws Exception {
			String uuidOid = "2.25." + concept.getConceptAttributes().getPrimUuid();
			ArrayList<IdentifierVersion> ids = concept.getConceptAttributes().getAdditionalIdentifierParts();
			if (ids != null) {
				for (IdentifierVersion id: ids) {
					if (id.getDenotation().equals(uuidOid)) {
						cids.setNotMember(concept.getNid());						
					}
				}
			} else {
				System.out.println("Null ids for: " + concept);
			}
		}

		@Override
		public boolean continueWork() {
			return true;
		}
	}

}
