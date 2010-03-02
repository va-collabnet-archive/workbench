package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.IdentifierSetReadOnly;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.thread.NamedThreadFactory;

import cern.colt.map.OpenIntIntHashMap;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ConceptBdb extends ComponentBdb {

	private IdentifierSetReadOnly readOnlyConceptIdSet;
	
	private static ThreadGroup conDbThreadGroup  = 
		new ThreadGroup("concept db threads");

	private static ExecutorService iteratorService = Executors.newCachedThreadPool(new NamedThreadFactory(conDbThreadGroup,
	"parallel iterator service"));
	private static ExecutorService idFinderService = Executors.newCachedThreadPool(new NamedThreadFactory(conDbThreadGroup,
	"id finder service"));

	
	private int processors = Runtime.getRuntime().availableProcessors();

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
		assert cNid != Integer.MAX_VALUE;
		return Concept.get(cNid);
	}

	public Concept getWritableConcept(int cNid) throws IOException {
		return Concept.get(cNid);
	}

	public void writeConcept(Concept concept) throws IOException {
		if (Bdb.watchList.containsKey(concept.getNid())) {
			AceLog.getAppLog().info(
					"---!!! Writing to conceptBdb: " + concept.toLongString()
							+ "\n---!!! ");
		}
		ConceptBinder binder = new ConceptBinder();
		DatabaseEntry key = new DatabaseEntry();
		int cNid = concept.getNid();
		IntegerBinding.intToEntry(cNid, key);
		DatabaseEntry value = new DatabaseEntry();
		synchronized (concept) {
			long writeVersion = Bdb.gVersion.incrementAndGet();
			if (writeVersion < concept.getDataVersion()) {
				Bdb.gVersion.set(concept.getDataVersion() + 1);
				writeVersion = Bdb.gVersion.incrementAndGet();
			}
			binder.objectToEntry(concept, value);
			mutable.put(null, key, value);
			concept.setLastWrite(writeVersion);
		}
		readOnlyConceptIdSet = null;
		Collection<Integer> nids = concept.getAllNids();
		NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
		for (int nid : nids) {
			assert nid != 0 : "nid is 0: " + nids;
			nidCidMap.setCidForNid(cNid, nid);
		}
	}

	public int getCount() throws IOException {
		return (int) getReadOnlyConceptIdSet().cardinality();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ProcessAllConceptData#iterateConceptData(
	 * org.ihtsdo.db.bdb.concept.ConceptData)
	 */
	public void iterateConceptDataInSequence(I_ProcessConceptData processor)
			throws Exception {
		iterateConceptData(processor, 1);
	}

	public void iterateConceptDataInParallel(I_ProcessConceptData processor)
			throws Exception {
		iterateConceptData(processor, processors * 2);
	}

	private void iterateConceptData(I_ProcessConceptData processor,
			int executors) throws IOException, InterruptedException,
			ExecutionException {
		IdentifierSet ids = (IdentifierSet) getReadOnlyConceptIdSet();
		int cardinality = ids.cardinality();
		int idsPerParallelConceptIterator = cardinality / executors;
		I_IterateIds idsItr = ids.iterator();
		List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(
				executors + 1);
		int sum = 0;
		while (idsItr.next()) {
			int first = idsItr.nid();
			int last = first;
			int count = 1;
			while (idsItr.next()) {
				last = idsItr.nid();
				count++;
				if (count == idsPerParallelConceptIterator) {
					break;
				}
			}
			sum = sum + count;
			ParallelConceptIterator pci = new ParallelConceptIterator(first,
					last, count, processor);
			Future<Boolean> f = iteratorService.submit(pci);
			futures.add(f);
		}
		for (Future<Boolean> f : futures) {
			f.get();
		}
	}

	public class ParallelConceptIterator implements Callable<Boolean> {
		private I_ProcessConceptData processor;
		private int first;
		private int last;
		private int countToProcess;
		private int processedCount = 0;

		public ParallelConceptIterator(int first, int last, int count,
				I_ProcessConceptData processor) {
			super();
			this.first = first;
			this.last = last;
			this.countToProcess = count;
			this.processor = processor;
		}

		@Override
		public Boolean call() throws Exception {
			CursorConfig cursorConfig = new CursorConfig();
			cursorConfig.setReadUncommitted(true);
			Cursor roCursor = readOnly.openCursor(null, cursorConfig);
			Cursor mutableCursor = mutable.openCursor(null, cursorConfig);
			int roKey = first;
			int mutableKey = first;
			try {
				DatabaseEntry roFoundKey = new DatabaseEntry();
				IntegerBinding.intToEntry(roKey, roFoundKey);
				DatabaseEntry roFoundData = new DatabaseEntry();
				DatabaseEntry mutableFoundKey = new DatabaseEntry();
				IntegerBinding.intToEntry(mutableKey, mutableFoundKey);
				DatabaseEntry mutableFoundData = new DatabaseEntry();

				roKey = setupCursor(roCursor, roFoundKey, roFoundData);
				mutableKey = setupCursor(mutableCursor, mutableFoundKey,
						mutableFoundData);

				while ((roKey <= last || mutableKey <= last)
						&& processor.continueWork()) {
					if (roKey == mutableKey) {
						processor.processConceptData(Concept.get(roKey,
								roFoundData.getData(), mutableFoundData
										.getData()));
						processedCount++;
						if (roKey < last) {
							roKey = advanceCursor(roCursor, roFoundKey,
									roFoundData);
							mutableKey = advanceCursor(mutableCursor,
									mutableFoundKey, mutableFoundData);
						} else {
							roKey = Integer.MAX_VALUE;
							mutableKey = Integer.MAX_VALUE;
						}
					} else if (roKey < mutableKey) {
						processor.processConceptData(Concept.get(roKey,
								roFoundData.getData(), new byte[0]));
						processedCount++;
						if (roKey < last) {
							roKey = advanceCursor(roCursor, roFoundKey,
									roFoundData);
						} else {
							roKey = Integer.MAX_VALUE;
						}
					} else {
						processor.processConceptData(Concept.get(mutableKey,
								new byte[0], mutableFoundData.getData()));
						processedCount++;
						if (mutableKey < last) {
							mutableKey = advanceCursor(mutableCursor,
									mutableFoundKey, mutableFoundData);
						} else {
							mutableKey = Integer.MAX_VALUE;
						}
					}
				}
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine(
							"Parallel concept iterator finished.\n"
									+ " First: " + first + " last: " + last
									+ " roKey: " + roKey + " mutableKey: "
									+ mutableKey + " processedCount: "
									+ processedCount + " countToProcess: "
									+ countToProcess);
				}
				return true;
			} finally {
				roCursor.close();
				mutableCursor.close();
			}
		}

		private int advanceCursor(Cursor mutableCursor,
				DatabaseEntry mutableFoundKey, DatabaseEntry mutableFoundData) {
			int mutableKey;
			if (mutableCursor.getNext(mutableFoundKey, mutableFoundData,
					LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				mutableKey = IntegerBinding.entryToInt(mutableFoundKey);
			} else {
				mutableKey = Integer.MAX_VALUE;
			}
			return mutableKey;
		}

		private int setupCursor(Cursor cursor, DatabaseEntry foundKey,
				DatabaseEntry foundData) {
			int cNid;
			if (cursor.getSearchKeyRange(foundKey, foundData,
					LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				cNid = IntegerBinding.entryToInt(foundKey);
			} else {
				cNid = Integer.MAX_VALUE;
			}
			return cNid;
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
	public IdentifierSetReadOnly getReadOnlyConceptIdSet() throws IOException {
		if (readOnlyConceptIdSet == null) {
			Future<OpenIntIntHashMap> readOnlyFuture = idFinderService
					.submit(new GetCNids(readOnly));
			Future<OpenIntIntHashMap> mutableFuture = idFinderService
					.submit(new GetCNids(mutable));
			try {
				OpenIntIntHashMap readOnlyMap = readOnlyFuture.get();
				OpenIntIntHashMap mutableMap = mutableFuture.get();
				readOnlyConceptIdSet = new IdentifierSetReadOnly(
						mergeIntoIdSet(readOnlyMap, mutableMap));
			} catch (InterruptedException e) {
				throw new IOException(e);
			} catch (ExecutionException e) {
				throw new IOException(e);
			}
		}
		return readOnlyConceptIdSet;
	}

	/**
	 * 
	 * @return a mutable bit set, with all concept identifiers set to true.
	 * @throws IOException
	 */
	public I_RepresentIdSet getConceptIdSet() throws IOException {
		return new IdentifierSet(getReadOnlyConceptIdSet());
	}

	public I_RepresentIdSet getEmptyIdSet() throws IOException {
		return new IdentifierSet(getReadOnlyConceptIdSet().totalBits());
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

	public class ReferenceFinder implements I_ProcessConceptData {
		int memberNid;
		StringBuffer buff = new StringBuffer();

		public ReferenceFinder(int memberNid) {
			super();
			this.memberNid = memberNid;
		}

		public void processConceptData(Concept concept) throws Exception {
			if (concept.getAllNids().contains(memberNid)) {
				buff.append(concept.toLongString());
				return;
			}
			for (RefsetMember<?, ?> member : concept.getExtensions()) {
				if (member.getNid() == memberNid) {
					buff.append(concept.toLongString());
					return;
				}
			}
		}

		@Override
		public String toString() {
			if (buff.length() == 0) {
				buff.append("[no references found]");
			}
			return buff.toString();
		}

		@Override
		public boolean continueWork() throws Exception {
			return true;
		}

	}

	public String findReferences(int memberNid) throws Exception {
		ReferenceFinder finder = new ReferenceFinder(memberNid);
		iterateConceptDataInParallel(finder);
		return finder.toString();
	}

	public void forget(Concept c) throws IOException {
		try {
			DatabaseEntry key = new DatabaseEntry();
			int cNid = c.getNid();
			IntegerBinding.intToEntry(cNid, key);
			// Perform the deletion. All records that use this key are
			// deleted.
			mutable.delete(null, key);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
