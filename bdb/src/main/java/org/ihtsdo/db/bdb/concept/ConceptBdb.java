package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.IdentifierSetReadOnly;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.bdb.NidCNidMapBdb;

import cern.colt.map.OpenIntIntHashMap;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ConceptBdb extends ComponentBdb {

	private IdentifierSet readOnlyConceptIdSet;

	public ConceptBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}

	@Override
	protected String getDbName() {
		return "conceptDb";
	}

	@Override
	protected void init() throws IOException {
		// Nothing to do...
	}

	public List<UUID> getUuidsForConcept(int cNid) throws IOException {
		return getConcept(cNid).getUids();
	}

	public Concept getConcept(int cNid) throws IOException {
		return Concept.get(cNid, false);
	}

	public Concept getWritableConcept(int cNid) throws IOException {
		return Concept.get(cNid, true);
	}

	public void writeConcept(Concept concept) throws IOException {
		ConceptBinder binder = new ConceptBinder();
		DatabaseEntry key = new DatabaseEntry();
		int cNid = concept.getNid();
		IntegerBinding.intToEntry(cNid, key);
		DatabaseEntry value = new DatabaseEntry();
		binder.objectToEntry(concept, value);
		mutable.put(null, key, value);
		readOnlyConceptIdSet = null;
		int[] nids = concept.getAllNids();
		NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
		for (int nid : nids) {
			nidCidMap.setCidForNid(cNid, nid);
		}
	}

	public long getCount() {
		long count = readOnly.count() + mutable.count();
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ProcessAllConceptData#iterateConceptData(
	 * org.ihtsdo.db.bdb.concept.ConceptData)
	 */
	public void iterateConceptDataInSequence(I_ProcessConceptData procesor)
			throws Exception {
		IdentifierSet conceptNids = getConceptNids();
		I_IterateIds nidItr = conceptNids.iterator();
		while (nidItr.next()) {
			procesor.processConceptData(getConcept(nidItr.nid()));
		}
	}

	public void iterateConceptDataInParallel(I_ProcessConceptData processor)
			throws Exception {
		IdentifierSet ids = (IdentifierSet) getReadOnlyConceptIdSet();
		List<IdentifierSet> idPools = new ArrayList<IdentifierSet>();
		for (int i = 0; i < Bdb.getExecutorPoolSize(); i++) {
			idPools.add(new IdentifierSet(ids.totalBits()));
		}
		I_IterateIds idsItr = ids.iterator();
		boolean next = idsItr.next();
		while (next) {
			for (IdentifierSet set: idPools) {
				set.setMember(idsItr.nid());
				next = idsItr.next();
				if (!next) {
					break;
				}
			}
			if (!next) {
				break;
			}
		}
		List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(idPools.size());
		for (IdentifierSet set: idPools) {
			ParallelConceptIterator pci = new ParallelConceptIterator(set, processor);
			Future<Boolean> f = Bdb.getExecutorPool().submit(pci);
			futures.add(f);
		}

		for (Future<Boolean> f: futures) {
			f.get();
		}
	}

	public class ParallelConceptIterator implements Callable<Boolean> {
		private IdentifierSet iterationSet;
		private I_ProcessConceptData processor;
		
		@Override
		public Boolean call() throws Exception {
			I_IterateIds idsItr = iterationSet.iterator();
			while (idsItr.next()) {
				processor.processConceptData(getConcept(idsItr.nid()));
			}
			return true;
		}

		public ParallelConceptIterator(IdentifierSet iterationSet,
				I_ProcessConceptData processor) {
			super();
			this.iterationSet = iterationSet;
			this.processor = processor;
		}
		
	}
	
	public void iterateWritableConceptDataInSequence(I_ProcessConceptData procesor)
			throws Exception {
		IdentifierSet conceptNids = getConceptNids();
		I_IterateIds nidItr = conceptNids.iterator();
		while (nidItr.next()) {
			procesor.processConceptData(getWritableConcept(nidItr.nid()));
		}
	}

	private static class GetCNids implements Callable<OpenIntIntHashMap> {
		private Database db;

		public GetCNids(Database db) {
			super();
			this.db = db;
		}

		@Override
		public OpenIntIntHashMap call() throws Exception {
			int size = (int) db.count();
			OpenIntIntHashMap nidMap = new OpenIntIntHashMap(size + 2);
			CursorConfig cursorConfig = new CursorConfig();
			cursorConfig.setReadUncommitted(true);
			Cursor cursor = db.openCursor(null, cursorConfig);
			try {
				DatabaseEntry foundKey = new DatabaseEntry();
				DatabaseEntry foundData = new DatabaseEntry();
				foundData.setPartial(true);
				foundData.setPartial(0, 0, true);
				int max = Integer.MIN_VALUE;
				while (cursor.getNext(foundKey, foundData,
						LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
					int cNid = IntegerBinding.entryToInt(foundKey);
					nidMap.put(cNid, cNid);
					max = Math.max(max, cNid);
				}
				cursor.close();
				nidMap.put(Integer.MAX_VALUE, max);
				return nidMap;
			} finally {
				cursor.close();
			}
		}
	}

	/**
	 * 
	 * @return a read-only bit set, with all concept identifiers set to true.
	 * @throws IOException
	 */
	public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException {
		if (readOnlyConceptIdSet == null) {
			IdentifierSet set = getConceptNids();
			readOnlyConceptIdSet = new IdentifierSetReadOnly(set);
		}
		return readOnlyConceptIdSet;
	}

	/**
	 * 
	 * @return a mutable bit set, with all concept identifiers set to true.
	 * @throws IOException
	 */
	public I_RepresentIdSet getConceptIdSet() throws IOException {
		if (readOnlyConceptIdSet == null) {
			IdentifierSet set = getConceptNids();
			readOnlyConceptIdSet = new IdentifierSetReadOnly(set);
			return set;
		}
		return new IdentifierSet(readOnlyConceptIdSet);

	}

	private IdentifierSet getConceptNids() throws IOException {
		Future<OpenIntIntHashMap> readOnlyFuture = Bdb.getExecutorPool()
				.submit(new GetCNids(readOnly));
		Future<OpenIntIntHashMap> mutableFuture = Bdb.getExecutorPool().submit(
				new GetCNids(mutable));
		try {
			OpenIntIntHashMap readOnlyMap = readOnlyFuture.get();
			OpenIntIntHashMap mutableMap = mutableFuture.get();
			return mergeIntoIdSet(readOnlyMap, mutableMap);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private IdentifierSet mergeIntoIdSet(OpenIntIntHashMap map1,
			OpenIntIntHashMap map2) {
		if (map1.size() < map2.size()) {
			return mergeIntoIdSet(map2, map1);
		}
		int max1 = map1.get(Integer.MAX_VALUE);
		int max2 = map1.get(Integer.MAX_VALUE);
		map1.removeKey(Integer.MAX_VALUE);
		map2.removeKey(Integer.MAX_VALUE);
		int max = Math.max(max1, max2);
		map1.ensureCapacity(map1.size() + map2.size());
		for (int key : map2.keys().elements()) {
			map1.put(key, key);
		}
		map2 = null;
		IdentifierSet returnList = new IdentifierSet(max - Integer.MAX_VALUE);
		for (int key : map1.keys().elements()) {
			returnList.setMember(key);
		}
		return returnList;
	}
}
