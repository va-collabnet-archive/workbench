package org.ihtsdo.db.bdb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_IterateIds;
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
import org.junit.Ignore;
import org.junit.Test;

public class CementLoadTest {

	String dbTarget;
	DataOutputStream eConceptDOS;

	@Test
	@Ignore
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
				EConcept eC = new EConcept(localConcept, mts);
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
			IdentifierSet cids = (IdentifierSet) Bdb.getConceptDb().getConceptIdSet();
			IdentifierSet roCids = Bdb.getConceptDb().getReadOnlyConceptIdSet();
			System.out.println("Differences: " + cids.getDifferences(roCids));
			System.out.println("Concept count: " + cids.cardinality());
			System.out.println("roCids count: " + roCids.cardinality());
			Assert.assertTrue(cids.equals(roCids));
			Assert.assertEquals(cids.cardinality(), roCids.cardinality());
			System.out.println("Concept count: " + cids.cardinality());
			
			AddStringIdProcessor addProcessor  = new AddStringIdProcessor(cids);
			Bdb.getConceptDb().iterateConceptDataInParallel(addProcessor);
			System.out.println("Unprocessed: " + addProcessor.getCids().cardinality());
			if (addProcessor.getCids().cardinality() != 0) {
				System.out.println("Unprocessed nids[0]: " + addProcessor.getCids());
			}
			System.out.println("Unprocessed conCids: " + addProcessor.getConCids().size());
			if (addProcessor.getCids().cardinality() != 0) {
				System.out.println("Unprocessed conCids[0]: " + addProcessor.getConCids().size());
			}
			//Assert.assertEquals(0, addProcessor.getCids().cardinality());
			Bdb.commit();
			
			CheckStringIdProcessor csidp = new CheckStringIdProcessor(cids);
			Bdb.getConceptDb().iterateConceptDataInParallel(csidp);
			System.out.println("Unmatched: " + csidp.getCids().cardinality());
			if (csidp.getCids().cardinality() != 0) {
				System.out.println("Unprocessed nids[1]: " + csidp.getCids());
			}
			System.out.println("Unmatched conCids: " + csidp.getConCids().size());
			if (addProcessor.getCids().cardinality() != 0) {
				System.out.println("Unprocessed conCids[1]: " + csidp.getConCids().size());
			}
			//Assert.assertEquals(0, csidp.getCids().cardinality());
			Bdb.close();
			
			Bdb.setup(dbTarget);
			csidp = new CheckStringIdProcessor(cids);
			Bdb.getConceptDb().iterateConceptDataInParallel(csidp);
			System.out.println("Unmatched: " + csidp.getCids().cardinality());
			if (csidp.getCids().cardinality() != 0) {
				System.out.println("Unprocessed nids[2]: " + csidp.getCids());
			}
			System.out.println("Unmatched conCids: " + csidp.getConCids().size());
			if (addProcessor.getCids().cardinality() != 0) {
				System.out.println("Unprocessed conCids[2]: " + csidp.getConCids().size());
			}
			Assert.assertEquals(0, csidp.getCids().cardinality());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		} finally {
			try {
				Bdb.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

	}
	
	private static class AddStringIdProcessor implements I_ProcessConceptData {

		I_RepresentIdSet cids;
		ConcurrentHashMap<Integer, Integer> conCids;
		
		public ConcurrentHashMap<Integer, Integer> getConCids() {
			return conCids;
		}

		public I_RepresentIdSet getCids() {
			return cids;
		}

		public AddStringIdProcessor(I_RepresentIdSet cids) throws IOException {
			super();
			this.cids = new IdentifierSet((IdentifierSet) cids);
			conCids = new ConcurrentHashMap<Integer, Integer>(cids.cardinality());
			I_IterateIds cidItr = cids.iterator();
			while (cidItr.next()) {
				conCids.put(cidItr.nid(), cidItr.nid());
			}
		}

		@Override
		public void processConceptData(Concept concept) throws Exception {
			cids.setNotMember(concept.getNid());
			conCids.remove(concept.getNid());
			String uuidOid = "2.25." + concept.getConceptAttributes().getPrimUuid();
			int authorNID = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.USER.getUids());
			int statusNid = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			int pathNid = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
			int authorityNID = Bdb.uuidsToNid(ArchitectonicAuxiliary.Concept.OID.getUids());
			IdentifierVersionString stringId = 
				new IdentifierVersionString(statusNid, authorNID, pathNid, 
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
		ConcurrentHashMap<Integer, Integer> conCids;
		
		public ConcurrentHashMap<Integer, Integer> getConCids() {
			return conCids;
		}
		
		public I_RepresentIdSet getCids() {
			return cids;
		}

		public CheckStringIdProcessor(I_RepresentIdSet cids) throws IOException {
			super();
			this.cids = new IdentifierSet((IdentifierSet) cids);
			conCids = new ConcurrentHashMap<Integer, Integer>(cids.cardinality());
			I_IterateIds cidItr = cids.iterator();
			while (cidItr.next()) {
				conCids.put(cidItr.nid(), cidItr.nid());
			}
		}

		@Override
		public void processConceptData(Concept concept) throws Exception {
			String uuidOid = "2.25." + concept.getConceptAttributes().getPrimUuid();
			ArrayList<IdentifierVersion> ids = concept.getConceptAttributes().getAdditionalIdentifierParts();
			if (ids != null) {
				for (IdentifierVersion id: ids) {
					if (id.getDenotation().equals(uuidOid)) {
						cids.setNotMember(concept.getNid());						
						conCids.remove(concept.getNid());
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
