/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.I_StoreConceptAttributes;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;
import org.dwfa.vodb.types.ThinConVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.StatsConfig;

public class ConWithPartCoreBdb implements I_StoreConceptAttributes {

	public class ThinConCoreVersionedBinding extends TupleBinding {

		public ThinConVersioned entryToObject(TupleInput ti) {
			int size = ti.readShort();
			ThinConVersioned versioned = new ThinConVersioned(
					Integer.MIN_VALUE, size);
			for (int x = 0; x < size; x++) {
				I_ConceptAttributePart conAttrPart;
				try {
					conAttrPart = conPartBdb.getConPart(ti.readInt());
				} catch (IndexOutOfBoundsException e) {
					throw new RuntimeException(e);
				} catch (DatabaseException e) {
					throw new RuntimeException(e);
				}
				versioned.addVersion(conAttrPart);
			}
			return versioned;
		}

		public void objectToEntry(Object obj, TupleOutput to) {
			I_ConceptAttributeVersioned versioned = (I_ConceptAttributeVersioned) obj;
			to.writeShort(versioned.versionCount());
			for (I_ConceptAttributePart conAttrPart : versioned.getVersions()) {
				try {
					to.writeInt(conPartBdb.getConPartId(conAttrPart));
				} catch (DatabaseException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	private ThinConCoreVersionedBinding conBinding = new ThinConCoreVersionedBinding();

	private TupleBinding intBinder = TupleBinding
			.getPrimitiveBinding(Integer.class);

	private Database conCoreDb;

	private ConCoreBdb conPartBdb;

	public ConWithPartCoreBdb(Environment env, DatabaseConfig dbConfig)
			throws DatabaseException {
		super();
		conCoreDb = env.openDatabase(null, "conCore", dbConfig);
		conPartBdb = new ConCoreBdb(env, dbConfig);
		logStats();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#writeConceptAttributes(org.dwfa.ace.api.I_ConceptAttributeVersioned)
	 */
	public void writeConceptAttributes(I_ConceptAttributeVersioned concept)
			throws DatabaseException {

		int tupleCount = concept.getTuples().size();
		HashSet<I_ConceptAttributeTuple> tupleSet = new HashSet<I_ConceptAttributeTuple>(
				concept.getTuples());
		if (tupleCount != tupleSet.size()) {
			AceLog.getAppLog().severe("Tuples: " + concept.getTuples());
			AceLog.getAppLog().severe("Tuple set: " + tupleSet);
			throw new RuntimeException("Tuple set != tuple count...");
		}

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(concept.getConId(), key);
		conBinding.objectToEntry(concept, value);
		conCoreDb.put(null, key, value);
		// logStats();
	}

	public void logStats() throws DatabaseException {
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			StatsConfig config = new StatsConfig();
			config.setClear(true);
			config.setFast(false);
			DatabaseStats stats = conCoreDb.getStats(config);
			AceLog.getAppLog().fine("conceptDb stats: " + stats.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#hasConcept(int)
	 */
	public boolean hasConcept(int conceptId) throws DatabaseException {
		DatabaseEntry conceptKey = new DatabaseEntry();
		DatabaseEntry conceptValue = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, conceptKey);
		if (conCoreDb.get(null, conceptKey, conceptValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#getConceptAttributes(int)
	 */
	public I_ConceptAttributeVersioned getConceptAttributes(int conceptId)
			throws IOException {
		Stopwatch timer = null;
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Getting concept : " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry conceptKey = new DatabaseEntry();
		DatabaseEntry conceptValue = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, conceptKey);
		try {
			if (conCoreDb.get(null, conceptKey, conceptValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine(
							"Got concept: " + conceptId + " elapsed time: "
									+ timer.getElapsedTime() / 1000 + " secs");
				}
				ThinConVersioned conAttr = (ThinConVersioned) conBinding
						.entryToObject(conceptValue);
				conAttr.setConId(conceptId);
				return conAttr;
			}
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		throw new ToIoException(new DatabaseException(
				"Concept attributes for: " + ConceptBean.get(conceptId)
						+ " not found."));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#getConceptIterator()
	 */
	public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
		return new ConceptIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#iterateConceptAttributeEntries(org.dwfa.vodb.types.I_ProcessConceptAttributeEntries)
	 */
	public void iterateConceptAttributeEntries(
			I_ProcessConceptAttributeEntries processor) throws Exception {
		Cursor concCursor = conCoreDb.openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processConceptAttributeEntry(foundKey, foundData);
			} catch (Exception e) {
				concCursor.close();
				throw e;
			}
		}
		concCursor.close();
	}

	private class ConceptIterator implements Iterator<I_GetConceptData> {

		DatabaseEntry foundKey = new DatabaseEntry();

		DatabaseEntry foundData = new DatabaseEntry();

		boolean hasNext;

		private Integer conceptId;

		private Cursor concCursor;

		private ConceptIterator() throws IOException {
			super();
			try {
				concCursor = conCoreDb.openCursor(null, null);
				getNext();
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}

		private void getNext() {
			try {
				hasNext = (concCursor.getNext(foundKey, foundData,
						LockMode.DEFAULT) == OperationStatus.SUCCESS);
				if (hasNext) {
					conceptId = (Integer) intBinder.entryToObject(foundKey);
				} else {
					conceptId = null;
					concCursor.close();
				}
			} catch (Exception ex) {
				try {
					concCursor.close();
				} catch (DatabaseException e) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
				AceLog.getAppLog().alertAndLogException(ex);
				hasNext = false;
			}
		}

		public boolean hasNext() {
			return hasNext;
		}

		public I_GetConceptData next() {
			if (hasNext) {
				I_GetConceptData next = ConceptBean.get(conceptId);
				getNext();
				return next;
			}
			return null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		protected void finalize() throws Throwable {
			concCursor.close();
		}

	}

	public void close() throws DatabaseException {
		if (conCoreDb != null) {
			conCoreDb.close();
		}
		if (conPartBdb != null) {
			conPartBdb.close();
		}
	}

	public void sync() throws DatabaseException {
		if (conCoreDb != null) {
			if (!conCoreDb.getConfig().getReadOnly()) {
				conCoreDb.sync();
			}
		}
		if (conPartBdb != null) {
			conPartBdb.sync();
		}
	}

	public I_ConceptAttributeVersioned conAttrEntryToObject(DatabaseEntry key,
			DatabaseEntry value) {
		int conId = (Integer) intBinder.entryToObject(key);
		ThinConVersioned conAttr = (ThinConVersioned) conBinding
				.entryToObject(value);
		conAttr.setConId(conId);
		return conAttr;
	}

	public void commit(ConceptBean bean, int version, Set<TimePathId> values)
			throws DatabaseException {
		if (bean.conceptAttributes != null) {
			for (I_ConceptAttributePart p : bean.conceptAttributes
					.getVersions()) {
				boolean changed = false;
				if (p.getVersion() == Integer.MAX_VALUE) {
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
					changed = true;
					if (AceLog.getEditLog().isLoggable(Level.FINE)) {
						AceLog.getEditLog().fine("Committing: " + p);
					}
				}
				if (changed) {
					this.writeConceptAttributes(bean.conceptAttributes);
				}
			}
		}
		if (bean.uncommittedConceptAttributes != null) {
			for (I_ConceptAttributePart p : bean.uncommittedConceptAttributes
					.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
					if (AceLog.getEditLog().isLoggable(Level.FINE)) {
						AceLog.getEditLog().fine("Committing: " + p);
					}
				}
			}
			this.writeConceptAttributes(bean.uncommittedConceptAttributes);
			bean.conceptAttributes = bean.uncommittedConceptAttributes;
			bean.uncommittedConceptAttributes = null;
		}

	}

	public void setupBean(ConceptBean cb) throws IOException {
		// nothing to do
	}

}
