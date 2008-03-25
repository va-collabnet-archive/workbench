package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinIdPartCore;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class IdPartCoreBdb implements I_StoreInBdb {

	private class PartIdGenerator {
		private int lastId = Integer.MIN_VALUE;

		private PartIdGenerator() throws DatabaseException {
			Cursor idCursor = idPartDb.openCursor(null, null);
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			lastId = Integer.MIN_VALUE;
			if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				lastId = (Integer) intBinder.entryToObject(foundKey);
			}
			idCursor.close();
		}

		public synchronized int nextId() {
			lastId++;
			return lastId;
		}
	}

	public static class ThinIdCoreBinding extends TupleBinding {

		public ThinIdPartCore entryToObject(TupleInput ti) {
			ThinIdPartCore id = new ThinIdPartCore();
			id.setPathId(ti.readInt());
			id.setVersion(ti.readInt());
			id.setIdStatus(ti.readInt());
			id.setSource(ti.readInt());
			return id;
		}

		public void objectToEntry(Object obj, TupleOutput to) {
			ThinIdPartCore idPartCore = (ThinIdPartCore) obj;
			to.writeInt(idPartCore.getPathId());
			to.writeInt(idPartCore.getVersion());
			to.writeInt(idPartCore.getIdStatus());
			to.writeInt(idPartCore.getSource());
		}
	}

	private ThinIdCoreBinding idCoreBinding = new ThinIdCoreBinding();
	private TupleBinding intBinder = TupleBinding
			.getPrimitiveBinding(Integer.class);

	private Database idPartDb;
	private PartIdGenerator partIdGenerator;
	private HashMap<ThinIdPartCore, Integer> partIdMap = new HashMap<ThinIdPartCore, Integer>();
	private HashMap<Integer, ThinIdPartCore> idPartMap = new HashMap<Integer, ThinIdPartCore>();

	public IdPartCoreBdb(Environment env, DatabaseConfig dbConfig)
			throws DatabaseException {
		super();
		idPartDb = env.openDatabase(null, "idPartDb", dbConfig);

		Cursor partCursor = idPartDb.openCursor(null, null);
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();
		while (partCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			int conAttrPartId = (Integer) intBinder.entryToObject(foundKey);
			ThinIdPartCore conAttrPart = (ThinIdPartCore) idCoreBinding
					.entryToObject(foundData);
			partIdMap.put(conAttrPart, conAttrPartId);
			idPartMap.put(conAttrPartId, conAttrPart);
		}
		partCursor.close();
		AceLog.getAppLog().info("id part map size: " + partIdMap.size());
		partIdGenerator = new PartIdGenerator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPartId(org.dwfa.ace.api.I_RelPart)
	 */
	public int getIdPartCoreId(ThinIdPartCore idPartCore)
			throws DatabaseException {
		if (partIdMap.containsKey(idPartCore)) {
			return partIdMap.get(idPartCore);
		}
		int conAttrPartId = partIdGenerator.nextId();
		DatabaseEntry partKey = new DatabaseEntry();
		DatabaseEntry partValue = new DatabaseEntry();
		intBinder.objectToEntry((Integer) conAttrPartId, partKey);
		idCoreBinding.objectToEntry(idPartCore, partValue);
		idPartDb.put(null, partKey, partValue);
		partIdMap.put(idPartCore, conAttrPartId);
		idPartMap.put(conAttrPartId, idPartCore);
		if (partIdMap.size() % 100 == 0) {
			AceLog.getAppLog().info(
					"id part core map size now: " + partIdMap.size());
		}
		// AceLog.getAppLog().info("Writing part id: " + newPartId + " " +
		// part);
		return conAttrPartId;
	}

	public int getIdPartCoreId(I_IdPart idPart) throws DatabaseException {
		ThinIdPartCore idPartCore = new ThinIdPartCore();
		idPartCore.setIdStatus(idPart.getIdStatus());
		idPartCore.setPathId(idPart.getPathId());
		idPartCore.setSource(idPart.getSource());
		idPartCore.setVersion(idPart.getVersion());
		return getIdPartCoreId(idPartCore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPart(int)
	 */
	public ThinIdPartCore getIdPartCore(int partId)
			throws DatabaseException {
		if (idPartMap.containsKey(partId)) {
			return idPartMap.get(partId);
		}
		throw new DatabaseException("Id part: " + partId + " not found.");
	}

	public void close() throws DatabaseException {
		idPartDb.close();
	}

	public void sync() throws DatabaseException {
		idPartDb.sync();
	}

	public void commit(ConceptBean bean, int version, Set<TimePathId> values)
			throws DatabaseException {
		// nothing to do...
		
	}

	public void setupBean(ConceptBean cb) throws IOException {
		// nothing to do
	}

}
