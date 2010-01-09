package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ConceptBdb extends ComponentBdb {

	public ConceptBdb(Bdb readOnlyBdbEnv, Bdb readWriteBdbEnv)
			throws IOException {
		super(readOnlyBdbEnv, readWriteBdbEnv);
	}

	@Override
	protected String getDbName() {
		return "conceptDb";
	}

	@Override
	protected void init() throws IOException {
		// TODO Auto-generated method stub
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

	public void writeConcept(Concept concept) {
		ConceptBinder binder = new ConceptBinder();
		DatabaseEntry key = new DatabaseEntry();
		IntegerBinding.intToEntry(concept.getNid(), key);
		DatabaseEntry value = new DatabaseEntry();
		binder.objectToEntry(concept, value);
		readWrite.put(null, key, value);
	}

	public long getCount() {
		long count = readOnly.count() + readWrite.count();
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ProcessAllConceptData#iterateConceptData(
	 * org.ihtsdo.db.bdb.concept.ConceptData)
	 */
	public void iterateConceptData(I_ProcessConceptData procesor)
			throws Exception {
		ArrayIntList conceptNids = getConceptNids();
		AceLog.getAppLog().info("Found: " + conceptNids.size());
		for (int cNid : conceptNids.toArray()) {
			procesor.processConceptData(getConcept(cNid));
		}
		AceLog.getAppLog().info("Iterated over read-only concept count: " + conceptNids.size());
	}

	public void iterateWritableConceptData(I_ProcessConceptData procesor)
			throws Exception {
		ArrayIntList conceptNids = getConceptNids();
		AceLog.getAppLog().info("Found: " + conceptNids.size());
		for (int cNid : conceptNids.toArray()) {
			procesor.processConceptData(getWritableConcept(cNid));
		}
		AceLog.getAppLog().info("Iterated over writable concept count: " + conceptNids.size());
	}

	public ArrayIntList getConceptNids() {
		ArrayIntList conceptNids = new ArrayIntList();
		CursorConfig cursorConfig = new CursorConfig();
		cursorConfig.setReadUncommitted(true);
		Cursor roCursor = readOnly.openCursor(null, cursorConfig);
		Cursor rwCursor = readWrite.openCursor(null, cursorConfig);
		try {
			DatabaseEntry roFoundKey = new DatabaseEntry();
			DatabaseEntry rwFoundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			foundData.setPartial(true);
			foundData.setPartial(0, 0, true);
			OperationStatus readOnlyStatus = roCursor.getNext(roFoundKey,
					foundData, LockMode.READ_UNCOMMITTED);
			OperationStatus readWriteStatus = rwCursor.getNext(rwFoundKey,
					foundData, LockMode.READ_UNCOMMITTED);
			int roKey = Integer.MAX_VALUE;
			int rwKey = Integer.MIN_VALUE;
			while (readOnlyStatus == OperationStatus.SUCCESS
					|| readWriteStatus == OperationStatus.SUCCESS) {
				if (readOnlyStatus == OperationStatus.SUCCESS && roKey < rwKey) {
					readOnlyStatus = roCursor.getNext(roFoundKey, foundData,
							LockMode.READ_UNCOMMITTED);
					roKey = IntegerBinding.entryToInt(roFoundKey);
				}
				if (readWriteStatus == OperationStatus.SUCCESS && rwKey < roKey) {
					rwKey = IntegerBinding.entryToInt(rwFoundKey);
					readWriteStatus = rwCursor.getNext(rwFoundKey, foundData,
							LockMode.READ_UNCOMMITTED);
				}
				if (roKey < rwKey && readOnlyStatus == OperationStatus.SUCCESS) {
					while (roKey < rwKey
							&& readOnlyStatus == OperationStatus.SUCCESS) {
						conceptNids.add(roKey);
						readOnlyStatus = roCursor.getNext(roFoundKey,
								foundData, LockMode.READ_UNCOMMITTED);
						roKey = IntegerBinding.entryToInt(roFoundKey);
					}
					if (roKey == rwKey) {
						conceptNids.add(roKey);
						readOnlyStatus = roCursor.getNext(roFoundKey,
								foundData, LockMode.READ_UNCOMMITTED);
						roKey = IntegerBinding.entryToInt(roFoundKey);
					} else {
						conceptNids.add(rwKey);
					}
				} else if (rwKey < roKey
						&& readWriteStatus == OperationStatus.SUCCESS) {
					while (rwKey < roKey
							&& readWriteStatus == OperationStatus.SUCCESS) {
						conceptNids.add(rwKey);
						readWriteStatus = rwCursor.getNext(rwFoundKey,
								foundData, LockMode.READ_UNCOMMITTED);
						roKey = IntegerBinding.entryToInt(rwFoundKey);
					}
					if (roKey == rwKey) {
						conceptNids.add(roKey);
						readOnlyStatus = roCursor.getNext(roFoundKey,
								foundData, LockMode.READ_UNCOMMITTED);
						roKey = IntegerBinding.entryToInt(roFoundKey);
					} else {
						conceptNids.add(roKey);
					}
				}
			}
		} finally {
			roCursor.close();
			rwCursor.close();
		}
		return conceptNids;
	}
}
