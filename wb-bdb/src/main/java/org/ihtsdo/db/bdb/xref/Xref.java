package org.ihtsdo.db.bdb.xref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRefset;
import org.ihtsdo.db.util.NidPairForRel;
import org.ihtsdo.tk.api.NidBitSetBI;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * All data for an individual component will be in the mutable, if modified
 * after the baseline (the baseline xref will be copied to mutable when a new
 * pair is added). Otherwise, all pairs (if any) will be in the read-only
 * database.
 * 
 * @author kec
 * 
 */
public class Xref extends ComponentBdb implements I_ProcessUnfetchedConceptData {

	// Find power-of-two sizes best matching arguments
	private static int concurrencyLevel = 128;
	private static int sshift = 0;
	private static int ssize = 1;
	static {
		while (ssize < concurrencyLevel) {
			++sshift;
			ssize <<= 1;
		}
	}
	private static int segmentShift = 32 - sshift;
	private static int segmentMask = ssize - 1;
	private static ReentrantLock[] locks = new ReentrantLock[concurrencyLevel];
	static {
		for (int i = 0; i < concurrencyLevel; i++) {
			locks[i] = new ReentrantLock();
		}
	}

	HashMap<Integer, long[]> readOnlyXref;
	AtomicReference<ConcurrentHashMap<Integer, long[]>> mutableXref;
	ReentrantReadWriteLock rwl;

	public Xref(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}

	@Override
	public void sync() throws IOException {
		ConcurrentHashMap<Integer, long[]> oldXref = null;
		rwl.writeLock().lock();
		try {
			if (!mutableXref.get().isEmpty()) {
				ConcurrentHashMap<Integer, long[]> newXref = new ConcurrentHashMap<Integer, long[]>();
				oldXref = mutableXref.getAndSet(newXref);
				readOnlyXref.putAll(oldXref);

			}
		} finally {
			rwl.writeLock().unlock();
		}
		if (oldXref != null) {
			long key = Bdb.gVersion.incrementAndGet();
			TupleOutput output = new TupleOutput();
			int size = oldXref.size();
			output.writeInt(size);
			for (Entry<Integer, long[]> entry : oldXref.entrySet()) {
				output.writeInt(entry.getKey());
				long[] refs = entry.getValue();
				int arrayLength = refs.length;
				output.writeInt(arrayLength);
				for (int i = 0; i < arrayLength; i++) {
					output.writeLong(refs[i]);
				}
			}

			DatabaseEntry keyEntry = new DatabaseEntry();
			LongBinding.longToEntry(key, keyEntry);
			DatabaseEntry valueEntry = new DatabaseEntry(output.toByteArray());
			OperationStatus status = mutable.put(null, keyEntry, valueEntry);
			if (status != OperationStatus.SUCCESS) {
				throw new IOException("Unsuccessful operation: " + status);
			}
		}
		super.sync();
	}

	@Override
	protected void init() throws IOException {
		Bdb.xref = this;
		readOnlyXref = new HashMap<Integer, long[]>();
		mutableXref = new AtomicReference<ConcurrentHashMap<Integer, long[]>>(
				new ConcurrentHashMap<Integer, long[]>());
		rwl = new ReentrantReadWriteLock();
		if (readOnly == null && mutable.count() == 0) {
			mutableXref.set(new ConcurrentHashMap<Integer, long[]>(Bdb
					.getConceptDb().getCount() * 2));
			try {
				Bdb.getConceptDb().iterateConceptDataInParallel(this);
			} catch (Exception e) {
				throw new IOException(e);
			}
		} else {
			readOnlyXref = new HashMap<Integer, long[]>(Bdb.getConceptDb()
					.getCount() * 2);
			readXref(readOnly);
			readXref(mutable);
		}
	}

	private void readXref(Database db) {
		if (db == null) {
			return;
		}
		CursorConfig cursorConfig = new CursorConfig();
		cursorConfig.setReadUncommitted(true);
		Cursor cursor = db.openCursor(null, cursorConfig);
		try {
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			while (cursor.getNext(foundKey, foundData,
					LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				@SuppressWarnings("unused")
				long index = LongBinding.entryToLong(foundKey);
				TupleInput ti = new TupleInput(foundData.getData());
				int records = ti.readInt();
				for (int i = 0; i < records; i++) {
					int nid = ti.readInt();
					int xrefSize = ti.readInt();
					long[] xrefArray = new long[xrefSize];
					for (int j = 0; j < xrefSize; j++) {
						xrefArray[j] = ti.readLong();
					}
					readOnlyXref.put(nid, xrefArray);
				}
			}
		} finally {
			cursor.close();
		}
	}

	public List<NidPairForRel> getDestRelPairs(int cNid) {
		// only need a read lock for sync,
		// since underlying structure is concurrent...
		rwl.readLock().lock();

		try {
			HashSet<NidPairForRel> result = new HashSet<NidPairForRel>();
			long[] allPairs = mutableXref.get().get(cNid);
			if (allPairs == null) {
				allPairs = readOnlyXref.get(cNid);
			}
			if (allPairs != null && allPairs.length != 0) {
				for (long nids : allPairs) {
					NidPair aPair = NidPair.getNidPair(nids);
					if (aPair.isRelPair()) {
						result.add((NidPairForRel) aPair);
					}
				}
			}
			return new ArrayList<NidPairForRel>(result);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public List<NidPairForRefset> getRefsetPairs(int nid) {
		// only need a read lock for sync,
		// since underlying structure is concurrent...
		rwl.readLock().lock();
		try {
			HashSet<NidPairForRefset> result = new HashSet<NidPairForRefset>();
			long[] allPairs = mutableXref.get().get(nid);
			if (allPairs == null) {
				allPairs = readOnlyXref.get(nid);
			}
			if (allPairs != null && allPairs.length != 0) {
				for (long nids : allPairs) {
					NidPair aPair = NidPair.getNidPair(nids);
					if (aPair.isRefsetPair()) {
						result.add((NidPairForRefset) aPair);
					}
				}
			}
			return new ArrayList<NidPairForRefset>(result);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public void addPair(int nid, NidPair pair) {
		// only need a read lock for sync,
		// since underlying structure is concurrent...
		rwl.readLock().lock();
		
		int word = (nid >>> segmentShift) & segmentMask;
		locks[word].lock();
		try {
			long pairAsLong = pair.asLong();
			assert pairAsLong != 0;
			long[] currentPairs = mutableXref.get().get(nid);
			if (currentPairs == null) {
				currentPairs = readOnlyXref.get(nid);
			}

			long[] newPairs;
			if (currentPairs == null) {
				newPairs = new long[] { pairAsLong };
				currentPairs = mutableXref.get().putIfAbsent(nid, newPairs);
				if (currentPairs == null) {
					return;
				}
			}

			if (mutableXref.get().get(nid) == null) {
				// copy readOnlyXref record over to mutable.
				mutableXref.get().putIfAbsent(nid, currentPairs);
			}
			while (true) {
				int index = Arrays.binarySearch(currentPairs, pairAsLong);
				if (index >= 0) {
					return; // it's there already.
				}
				int insertionPoint = -index - 1;
				newPairs = new long[currentPairs.length + 1];
				if (insertionPoint > 0) {
					System.arraycopy(currentPairs, 0, newPairs, 0,
							insertionPoint);
				}
				newPairs[insertionPoint] = pairAsLong;
				if (insertionPoint < currentPairs.length) {
					System.arraycopy(currentPairs, insertionPoint, newPairs,
							insertionPoint + 1, currentPairs.length
									- insertionPoint);
				}
				assert Arrays.binarySearch(newPairs, 0) < 0 : "currentPairs: "
						+ Arrays.toString(currentPairs) + "\nnewPairs"
						+ Arrays.toString(newPairs) + "\npairAsLong: "
						+ pairAsLong + "\nindex: " + index
						+ "\ninsertionPoint: " + insertionPoint;
				if (mutableXref.get().replace(nid, currentPairs, newPairs)) {
					return;
				}
				currentPairs = mutableXref.get().get(nid);
			}
		} finally {
			locks[word].unlock();
			rwl.readLock().unlock();
		}
	}

	public void forgetPair(int nid, NidPair pair) {
		// only need a read lock for sync,
		// since underlying structure is concurrent...
		rwl.readLock().lock(); 
		int word = (nid >>> segmentShift) & segmentMask;
		locks[word].lock();
		try {
			long pairAsLong = pair.asLong();
			long[] currentPairs = mutableXref.get().get(nid);
			if (currentPairs == null) {
				currentPairs = readOnlyXref.get(nid);
			}

			long[] newPairs;
			if (currentPairs == null) {
				// nothing to forget
				return;
			}

			if (mutableXref.get().get(nid) == null) {
				// copy readOnlyXref record over to mutable.
				mutableXref.get().putIfAbsent(nid, currentPairs);
			}
			/*
			 * public static void arraycopy(Object src, int srcPos, Object dest,
			 * int destPos, int length)
			 */
			while (true) {
				int index = Arrays.binarySearch(currentPairs, pairAsLong);
				if (index < 0) {
					return; // it's removed already.
				}
				newPairs = new long[currentPairs.length - 1];
				if (index == 0) {
					System.arraycopy(currentPairs, 1, newPairs, 0,
							currentPairs.length - 1);
				} else {
					System.arraycopy(currentPairs, 0, newPairs, 0, index - 1);
					if (index < currentPairs.length) {
						System.arraycopy(currentPairs, index + 1, newPairs,
								index, currentPairs.length - index);
					}
				}
				if (mutableXref.get().replace(nid, currentPairs, newPairs)) {
					return;
				}
				currentPairs = mutableXref.get().get(nid);
			}
		} finally {
			locks[word].unlock();
			rwl.readLock().unlock();
		}
	}

	@Override
	public void processUnfetchedConceptData(int cNid,
			I_FetchConceptFromCursor fcfc) throws Exception {
		fcfc.fetch().updateXrefs();
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return Bdb.getConceptDb().getConceptNidSet();
	}

	@Override
	public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
		// Nothing to do...

	}

	@Override
	public boolean continueWork() {
		return true;
	}

	@Override
	protected String getDbName() {
		return "xref";
	}

}
