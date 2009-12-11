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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.I_StoreConceptAttributes;
import org.dwfa.vodb.I_StoreDescriptions;
import org.dwfa.vodb.I_StoreIdentifiers;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPartCore;
import org.dwfa.vodb.types.ThinDescPartWithCoreDelegate;
import org.dwfa.vodb.types.ThinDescVersioned;

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

public class ConDescBdb implements I_StoreConceptAttributes,
		I_StoreDescriptions {

	private static class CheckAndProcessLuceneMatch implements Runnable {

		Collection<LuceneMatch> matches;

		List<I_TestSearchResults> checkList;

		I_ConfigAceFrame config;

		Document doc;

		private float score;

		private CountDownLatch hitLatch;

		private I_StoreDescriptions descStore;

		public CheckAndProcessLuceneMatch(CountDownLatch hitLatch,
				LuceneProgressUpdator updater, Document doc, float score,
				Collection<LuceneMatch> matches,
				List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
				I_StoreDescriptions descStore) {
			super();
			this.doc = doc;
			this.score = score;
			this.matches = matches;
			this.checkList = checkList;
			this.config = config;
			this.hitLatch = hitLatch;
			this.descStore = descStore;
		}

		public void run() {
			if (hitLatch.getCount() > 0) {
				int nid = Integer.parseInt(doc.get("dnid"));
				int cnid = Integer.parseInt(doc.get("cnid"));
				try {
					ThinDescVersioned descV = (ThinDescVersioned) descStore
							.getDescription(nid, cnid);
					LuceneMatch match = new LuceneMatch(descV, score);
					if (checkList == null || checkList.size() == 0) {
						matches.add(match);
						if (AceLog.getAppLog().isLoggable(Level.FINE)) {
							AceLog.getAppLog().fine(
									"processing match: " + descV
											+ " new match size: "
											+ matches.size());
						}
					} else {
						try {
							boolean failed = false;
							for (I_TestSearchResults test : checkList) {
								if (test.test(descV, config) == false) {
									failed = true;
									break;
								}
							}

							if (failed == false) {
								matches.add(match);
							}
						} catch (TaskFailedException e) {
							AceLog.getAppLog().alertAndLogException(e);
						}
					}
				} catch (IOException e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				} catch (DatabaseException e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				}
				this.hitLatch.countDown();
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine(
							"Hit latch: " + this.hitLatch.getCount());
				}
			}
		}
	}

	private static class CheckAndProcessRegexMatch implements Runnable {
		Pattern p;

		Collection<I_DescriptionVersioned> matches;

		I_DescriptionVersioned descV;

		List<I_TestSearchResults> checkList;

		I_ConfigAceFrame config;

		public CheckAndProcessRegexMatch(Pattern p,
				Collection<I_DescriptionVersioned> matches,
				I_DescriptionVersioned descV,
				List<I_TestSearchResults> checkList, I_ConfigAceFrame config) {
			super();
			this.p = p;
			this.matches = matches;
			this.descV = descV;
			this.checkList = checkList;
			this.config = config;
		}

		public void run() {
			if ((p == null) || (descV.matches(p))) {
				if (checkList == null || checkList.size() == 0) {
					matches.add(descV);
				} else {
					try {
						boolean failed = false;
						for (I_TestSearchResults test : checkList) {
							if (test.test(descV, config) == false) {
								failed = true;
								break;
							}
						}

						if (failed == false) {
							matches.add(descV);
						}
					} catch (TaskFailedException e) {
						if (ACE.editMode) {
							AceLog.getAppLog().alertAndLogException(e);
						} else {
							AceLog.getAppLog().log(Level.SEVERE,
									e.getLocalizedMessage(), e);
						}
					}
				}
			}
		}

	}

	public class ConDescBinding extends TupleBinding {

		public ConceptBean entryToObject(TupleInput ti) {
			try {
				int conceptNid = ti.readInt();
				ConceptBean conceptBean = ConceptBean.get(conceptNid);
				int attributeParts = ti.readShort();
				ThinConVersioned conceptAttributes = new ThinConVersioned(
						conceptNid, attributeParts);
				for (int x = 0; x < attributeParts; x++) {
					I_ConceptAttributePart conAttrPart;
					try {
						conAttrPart = conPartBdb.getConPart(ti.readInt());
					} catch (IndexOutOfBoundsException e) {
						throw new RuntimeException(e);
					} catch (DatabaseException e) {
						throw new RuntimeException(e);
					}
					conceptAttributes.addVersion(conAttrPart);
				}
				conceptBean.conceptAttributes = conceptAttributes;

				int descCount = ti.readShort();
				conceptBean.descriptions = new ArrayList<I_DescriptionVersioned>(
						descCount);
				for (int x = 0; x < descCount; x++) {
					int descId = ti.readInt();
					int versionCount = ti.readShort();
					ThinDescVersioned descV = new ThinDescVersioned(descId,
							conceptNid, versionCount);
					conceptBean.descriptions.add(descV);
					String lastText = null;
					for (int y = 0; y < versionCount; y++) {
						ThinDescPartCore descCore = descCoreBdb
								.getDescPartCore(ti.readInt());
						lastText = ThinDescVersionedBinding.readDescText(lastText, ti);
						descV.addVersion(new ThinDescPartWithCoreDelegate(lastText,
								descCore));
					}
				}
				return conceptBean;
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			}
		}

		public void objectToEntry(Object obj, TupleOutput to) {
			try {
				ConceptBean conceptBean = (ConceptBean) obj;
				to.writeInt(conceptBean.getConceptId());
				to.writeShort(conceptBean.getConceptAttributes()
								.versionCount());
				for (I_ConceptAttributePart conAttrPart : conceptBean
						.getConceptAttributes().getVersions()) {
					to.writeInt(conPartBdb.getConPartId(conAttrPart));
				}
				if (conceptBean.descriptions == null) {
					to.writeShort(0);
				} else {
					to.writeShort(conceptBean.getDescriptions().size());
				}
				for (I_DescriptionVersioned desc : conceptBean
						.getDescriptions()) {
					to.writeInt(desc.getDescId());
					to.writeShort(desc.versionCount());
					byte[] lastText = new byte[0];
					for (I_DescriptionPart part : desc.getVersions()) {
						try {
							to.writeInt(descCoreBdb.getDescPartCoreId(part));
							lastText = ThinDescVersionedBinding.writeDescText(lastText, part, to);
						} catch (DatabaseException e) {
							throw new RuntimeException(e);
						}
					}
				}
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private ConDescBinding conDescBinding = new ConDescBinding();

	private TupleBinding intBinder = TupleBinding
			.getPrimitiveBinding(Integer.class);

	private Database conDescDb;

	private ConCoreBdb conPartBdb;

	private DescCoreBdb descCoreBdb;

	private File luceneDir;

	private I_StoreIdentifiers identifierDb;

	private IndexSearcher luceneSearcher = null;

	public ConDescBdb(Environment env, DatabaseConfig dbConfig, File luceneDir,
			I_StoreIdentifiers identifierDb) throws DatabaseException {
		super();
		this.luceneDir = luceneDir;
		conDescDb = env.openDatabase(null, "conDescDb", dbConfig);
		conPartBdb = new ConCoreBdb(env, dbConfig);
		descCoreBdb = new DescCoreBdb(env, dbConfig);
		this.identifierDb = identifierDb;
		logStats();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#writeConceptAttributes(org.dwfa.ace.api.I_ConceptAttributeVersioned)
	 */
	public void writeConceptAttributes(
			I_ConceptAttributeVersioned conceptAttributes)
			throws DatabaseException, IOException {

		ConceptBean bean = ConceptBean.get(conceptAttributes.getConId());
		if (bean.conceptAttributes == null) {
			bean = getConceptBeanFromBdb(conceptAttributes.getConId());
		}
		bean.conceptAttributes = conceptAttributes;

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(conceptAttributes.getConId(), key);
		conDescBinding.objectToEntry(bean, value);
		conDescDb.put(null, key, value);
		// logStats();
	}

	public void logStats() throws DatabaseException {
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			StatsConfig config = new StatsConfig();
			config.setClear(true);
			config.setFast(false);
			DatabaseStats stats = conDescDb.getStats(config);
			AceLog.getAppLog().fine("conDescDb stats: " + stats.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#hasConcept(int)
	 */
	public boolean hasConcept(int conceptId) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, key);
		if (conDescDb.get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
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
		ConceptBean bean = getConceptBeanFromBdb(conceptId);
		return bean.conceptAttributes;
	}

	private ConceptBean getConceptBeanFromBdb(int conceptId)
			throws IOException, ToIoException {
		ConceptBean bean = ConceptBean.get(conceptId);
		/*
		 * if (bean.conceptAttributes != null) { return bean; }
		 */
		Stopwatch timer = null;
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Getting concept : " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, key);
		try {
			if (conDescDb.get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine(
							"Got concept: " + conceptId + " elapsed time: "
									+ timer.getElapsedTime() / 1000 + " secs");
				}
				return (ConceptBean) conDescBinding.entryToObject(value);
			}
			bean.descriptions = new ArrayList<I_DescriptionVersioned>();
			return bean;
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
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
		Cursor concCursor = conDescDb.openCursor(null, null);
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
				concCursor = conDescDb.openCursor(null, null);
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

	private class DescriptionIterator implements
	Iterator<I_DescriptionVersioned> {

		boolean hasNext;

		private ConceptIterator conItr = new ConceptIterator();

		private Iterator<I_DescriptionVersioned> descItr;

		private DescriptionIterator() throws IOException {
			super();
		}

		public boolean hasNext() {
			if (descItr != null) {
				if (descItr.hasNext()) {
					return true;
				}
			}
			while (conItr.hasNext()) {
				try {
					descItr = conItr.next().getDescriptions().iterator();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if (descItr.hasNext()) {
					return true;
				}
			}
			return false;
		}

		public I_DescriptionVersioned next() {
			if (hasNext()) {
				return descItr.next();
			}
			return null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public void close() throws DatabaseException {
		if (conDescDb != null) {
			conDescDb.close();
			conDescDb = null;
		}
		if (conPartBdb != null) {
			conPartBdb.close();
			conPartBdb = null;
		}
		if (descCoreBdb != null) {
			descCoreBdb.close();
			descCoreBdb = null;
		}
	}

	public void sync() throws DatabaseException {
		if (conDescDb != null) {
			if (!conDescDb.getConfig().getReadOnly()) {
				conDescDb.sync();
			}
		}
		if (conPartBdb != null) {
			conPartBdb.sync();
		}
		if (descCoreBdb != null) {
			descCoreBdb.sync();
		}
	}

	public I_ConceptAttributeVersioned conAttrEntryToObject(DatabaseEntry key,
			DatabaseEntry value) {
		int conId = (Integer) intBinder.entryToObject(key);
		try {
			return getConceptAttributes(conId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void commit(ConceptBean bean, int version, Set<TimePathId> values)
			throws DatabaseException, IOException {
		boolean changed = false;
		if (bean.conceptAttributes != null) {
			for (I_ConceptAttributePart p : bean.conceptAttributes
					.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
					changed = true;
					if (AceLog.getEditLog().isLoggable(Level.FINE)) {
						AceLog.getEditLog().fine("Committing: " + p);
					}
				}
			}
		}
		if (bean.uncommittedConceptAttributes != null) {
			for (I_ConceptAttributePart p : bean.uncommittedConceptAttributes
					.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					changed = true;
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
					if (AceLog.getEditLog().isLoggable(Level.FINE)) {
						AceLog.getEditLog().fine("Committing: " + p);
					}
				}
			}
			bean.conceptAttributes = bean.uncommittedConceptAttributes;
			bean.uncommittedConceptAttributes = null;
		}
		if (bean.descriptions != null) {
			for (I_DescriptionVersioned desc : bean.descriptions) {
				for (I_DescriptionPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						writeToLucene(desc);
						values.add(new TimePathId(version, p.getPathId()));
						changed = true;
						if (AceLog.getEditLog().isLoggable(Level.FINE)) {
							AceLog.getEditLog().fine("Committing: " + p);
						}
					}
				}
			}
		}
		if (bean.uncommittedDescriptions != null) {
			for (I_DescriptionVersioned desc : bean.uncommittedDescriptions) {
				for (I_DescriptionPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						changed = true;
						p.setVersion(version);
						writeToLucene(desc);
						values.add(new TimePathId(version, p.getPathId()));
					}
				}
				if (AceLog.getEditLog().isLoggable(Level.FINE)) {
					AceLog.getEditLog().fine("Committing: " + desc);
				}
			}
			if (bean.descriptions == null) {
				bean.descriptions = new ArrayList<I_DescriptionVersioned>();
			}
			bean.descriptions.addAll(bean.uncommittedDescriptions);
			bean.uncommittedDescriptions = null;
		}
		if (changed) {
			writeConceptToBdb(bean);
		}
	}

	private void writeConceptToBdb(ConceptBean bean) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(bean.getConceptId(), key);
		conDescBinding.objectToEntry(bean, value);
		conDescDb.put(null, key, value);
	}

	private void writeToLucene(I_DescriptionVersioned desc)
			throws DatabaseException {
		try {
			IndexReader reader = IndexReader.open(luceneDir);
			reader.deleteDocuments(new Term("dnid", Integer.toString(desc
					.getDescId())));
			reader.close();
			IndexWriter writer = new IndexWriter(luceneDir,
					new StandardAnalyzer(), false);
			Document doc = new Document();
			doc.add(new Field("dnid", Integer.toString(desc.getDescId()),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			doc.add(new Field("cnid", Integer.toString(desc.getConceptId()),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			addIdsToIndex(doc, identifierDb.getId(desc.getDescId()));
			addIdsToIndex(doc, identifierDb.getId(desc.getConceptId()));

			String lastDesc = null;
			for (I_DescriptionTuple tuple : desc.getTuples()) {
				if (lastDesc == null
						|| lastDesc.equals(tuple.getText()) == false) {
					if (AceLog.getAppLog().isLoggable(Level.FINE)) {
						AceLog.getAppLog().fine(
								"Adding to index. dnid:  " + desc.getDescId()
										+ " desc: " + tuple.getText());
					}
					doc.add(new Field("desc", tuple.getText(), Field.Store.NO,
							Field.Index.TOKENIZED));
				}

			}
			writer.addDocument(doc);
			writer.close();
		} catch (CorruptIndexException e) {
			throw new DatabaseException(e);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	private void addIdsToIndex(Document doc, I_IdVersioned did) {
		for (I_IdPart p : did.getVersions()) {
			doc.add(new Field("desc", p.getSourceId().toString(),
					Field.Store.NO, Field.Index.UN_TOKENIZED));
		}
	}

	public Hits doLuceneSearch(String query) throws IOException, ParseException {
		if (luceneDir.exists() == false) {
			createLuceneDescriptionIndex();
		}
		if (luceneSearcher == null) {
			luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
		}
		Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
		return luceneSearcher.search(q);
	}

	public I_DescriptionVersioned descEntryToObject(DatabaseEntry key,
			DatabaseEntry value) {
		throw new UnsupportedOperationException(
				"Iterate over concepts instead...");
	}

	public List<I_DescriptionVersioned> getDescriptions(int conceptId)
			throws DatabaseException, IOException {
		ConceptBean bean = getConceptBeanFromBdb(conceptId);
		return bean.descriptions;
	}

	public boolean hasDescription(int descId, int conceptId)
			throws DatabaseException, IOException {
		for (I_DescriptionVersioned desc : getDescriptions(conceptId)) {
			if (desc.getDescId() == descId) {
				return true;
			}
		}
		return false;
	}

	public Iterator<I_DescriptionVersioned> getDescriptionIterator()
			throws IOException {
		return new DescriptionIterator();
	}

	public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor)
			throws Exception {
		throw new UnsupportedOperationException("Iterate concepts instead...");
	}

	public I_DescriptionVersioned getDescription(int descId, int concId)
			throws IOException, DatabaseException {
		for (I_DescriptionVersioned desc : getDescriptions(concId)) {
			if (desc.getDescId() == descId) {
				return desc;
			}
		}
		throw new IOException("No such description did: " + descId + " conid: "
				+ concId);
	}

	public int countDescriptions() throws DatabaseException, IOException {
		int count = 0;
		Iterator<I_DescriptionVersioned> descItr = getDescriptionIterator();
		while (descItr.hasNext()) {
			descItr.next();
			count++;
		}
		return count;
	}

	public CountDownLatch searchLucene(I_TrackContinuation tracker,
			String query, Collection<LuceneMatch> matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, LuceneProgressUpdator updater)
			throws DatabaseException, IOException, ParseException {
		Stopwatch timer = null;
		if (AceLog.getAppLog().isLoggable(Level.INFO)) {
			timer = new Stopwatch();
			timer.start();
		}
		if (luceneDir.exists() == false) {
			updater
					.setProgressInfo("Making lucene index -- this may take a while...");
			createLuceneDescriptionIndex();
		}
		updater.setIndeterminate(true);
		if (luceneSearcher == null) {
			updater.setProgressInfo("Opening search index...");
			luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
		}
		updater.setProgressInfo("Starting lucene query...");
		long startTime = System.currentTimeMillis();
		Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
		updater.setProgressInfo("Query complete in "
				+ Long.toString(System.currentTimeMillis() - startTime)
				+ " ms.");
		Hits hits = luceneSearcher.search(q);
		updater.setProgressInfo("Query complete in "
				+ Long.toString(System.currentTimeMillis() - startTime)
				+ " ms. Hits: " + hits.length());

		CountDownLatch hitLatch = new CountDownLatch(hits.length());
		updater.setHits(hits.length());
		updater.setIndeterminate(false);

		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			float score = hits.score(i);
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("Hit: " + doc + " Score: " + score);
			}

			ACE.threadPool.execute(new CheckAndProcessLuceneMatch(hitLatch,
					updater, doc, score, matches, checkList, config, this));
		}
		if (AceLog.getAppLog().isLoggable(Level.INFO)) {
			if (tracker.continueWork()) {
				AceLog.getAppLog().info(
						"Search time: " + timer.getElapsedTime());
			} else {
				AceLog.getAppLog().info(
						"Search Canceled. Elapsed time: "
								+ timer.getElapsedTime());
			}
			timer.stop();
		}
		return hitLatch;
	}

	public void searchRegex(I_TrackContinuation tracker, Pattern p,
			Collection<I_DescriptionVersioned> matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
			throws DatabaseException, IOException {
		Stopwatch timer = null;
		if (AceLog.getAppLog().isLoggable(Level.INFO)) {
			timer = new Stopwatch();
			timer.start();
		}
		Iterator<I_DescriptionVersioned> descItr = getDescriptionIterator();
		while (descItr.hasNext()) {
			if (tracker.continueWork()) {
				I_DescriptionVersioned descV = descItr.next();
				ACE.threadPool.execute(new CheckAndProcessRegexMatch(p,
						matches, descV, checkList, config));
			} else {
				while (latch.getCount() > 0) {
					latch.countDown();
				}
				break;
			}
			latch.countDown();
		}
		if (AceLog.getAppLog().isLoggable(Level.INFO)) {
			if (tracker.continueWork()) {
				AceLog.getAppLog().info(
						"Search time: " + timer.getElapsedTime());
			} else {
				AceLog.getAppLog().info(
						"Canceled. Elapsed time: " + timer.getElapsedTime());
			}
			timer.stop();
		}
	}

	public void createLuceneDescriptionIndex() throws IOException {
		Stopwatch timer = new Stopwatch();
		timer.start();
		luceneDir.mkdirs();
		IndexWriter writer = new IndexWriter(luceneDir, new StandardAnalyzer(),
				true);
		writer.setUseCompoundFile(true);
		writer.setMergeFactor(10000);
		writer.setMaxMergeDocs(Integer.MAX_VALUE);
		writer.setMaxBufferedDocs(1000);
		Iterator<I_DescriptionVersioned> descItr = getDescriptionIterator();
		int counter = 0;
		int optimizeInterval = 10000;
		while (descItr.hasNext()) {
			I_DescriptionVersioned descV = descItr.next();
			Document doc = new Document();
			doc.add(new Field("dnid", Integer.toString(descV.getDescId()),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			doc.add(new Field("cnid", Integer.toString(descV.getConceptId()),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			addIdsToIndex(doc, identifierDb.getId(descV.getDescId()));
			addIdsToIndex(doc, identifierDb.getId(descV.getConceptId()));

			String lastDesc = null;
			for (I_DescriptionTuple tuple : descV.getTuples()) {
				if (lastDesc == null
						|| lastDesc.equals(tuple.getText()) == false) {
					doc.add(new Field("desc", tuple.getText(), Field.Store.NO,
							Field.Index.TOKENIZED));
				}

			}
			writer.addDocument(doc);
			counter++;
			if (counter == optimizeInterval) {
				writer.optimize();
				counter = 0;
			}
		}
		AceLog.getAppLog().info(
				"Optimizing index time: " + timer.getElapsedTime());
		writer.optimize();
		writer.close();
		if (AceLog.getAppLog().isLoggable(Level.INFO)) {
			AceLog.getAppLog().info("Index time: " + timer.getElapsedTime());
			timer.stop();
		}
	}

	public void writeDescription(I_DescriptionVersioned desc)
			throws DatabaseException, IOException {
		writeToLucene(desc);
		writeDescriptionNoLuceneUpdate(desc);

	}

	public void writeDescriptionNoLuceneUpdate(I_DescriptionVersioned newDesc)
			throws DatabaseException, IOException {
		ConceptBean conceptBean = getConceptBeanFromBdb(newDesc.getConceptId());
		boolean newDescForConcept = true;
		for (I_DescriptionVersioned desc : conceptBean.getDescriptions()) {
			if (desc.getDescId() == newDesc.getDescId()) {
				if (desc.getVersions().size() == newDesc.getVersions().size()) {
					throw new DatabaseException(
							"Description versions are not the same size");
				}
				newDescForConcept = false;
				break;
			}
		}
		if (newDescForConcept) {
			conceptBean.getDescriptions().add(newDesc);
		}
		writeConceptToBdb(conceptBean);

		/*
		 * I_DescriptionVersioned retrievedDesc =
		 * getDescription(newDesc.getDescId(), newDesc.getConceptId());
		 * 
		 * if (newDesc.equals(retrievedDesc)) { //System.out.println("Retrieved
		 * Desc equal. "); } else { System.out.println("Retrieved Desc NOT
		 * equal. "); }
		 * 
		 * 
		 * DatabaseEntry key = new DatabaseEntry(); DatabaseEntry value = new
		 * DatabaseEntry(); intBinder.objectToEntry(newDesc.getConceptId(),
		 * key); if (conDescDb.get(null, key, value, LockMode.DEFAULT) ==
		 * OperationStatus.SUCCESS) { ArrayList<I_DescriptionVersioned>
		 * retrievedDescList = (ArrayList<I_DescriptionVersioned>)
		 * descListBinding.entryToObject(value); AceLog.getAppLog().info(
		 * "retrievedDescList: " + retrievedDescList ); }
		 * 
		 * intBinder.objectToEntry(newDesc.getConceptId(), key); if
		 * (conDescDb.get(null, key, value, LockMode.DEFAULT) ==
		 * OperationStatus.SUCCESS) { I_ConceptAttributeVersioned
		 * conceptAttributes = (I_ConceptAttributeVersioned)
		 * attributeBinding.entryToObject(value); AceLog.getAppLog().info(
		 * "conceptAttributes: " + conceptAttributes ); }
		 */

	}
	
	public void setupBean(ConceptBean cb) throws IOException {
		getConceptBeanFromBdb(cb.getConceptId());
	}

}
