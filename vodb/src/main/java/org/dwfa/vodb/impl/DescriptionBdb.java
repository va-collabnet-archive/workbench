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
import java.util.concurrent.Semaphore;
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
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.CheckAndProcessLuceneMatch;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ConceptIdKeyForDescCreator;
import org.dwfa.vodb.I_StoreDescriptions;
import org.dwfa.vodb.I_StoreIdentifiers;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.impl.ConDescRelBdb.LuceneDeescriptionProcessor;
import org.dwfa.vodb.process.ProcessQueue;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;
import org.dwfa.vodb.types.ThinDescVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.PreloadConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.StatsConfig;

public class DescriptionBdb implements I_StoreInBdb, I_StoreDescriptions {
    private static final int THREAD_COUNT = 3;
    private Database descDb;
    private SecondaryDatabase conceptDescMap;
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private ThinDescVersionedBinding descBinding = new ThinDescVersionedBinding();
    private ConceptIdKeyForDescCreator descForConceptKeyCreator = new ConceptIdKeyForDescCreator(descBinding);
    private File luceneDir;
    private I_StoreIdentifiers identifierDb;
    private IndexSearcher luceneSearcher = null;

    public DescriptionBdb(Environment env, DatabaseConfig dbConfig, File luceneDir, I_StoreIdentifiers identifierDb)
            throws DatabaseException {
        super();
        this.luceneDir = luceneDir;
        this.identifierDb = identifierDb;
        descDb = env.openDatabase(null, "desc", dbConfig);
        PreloadConfig preloadConfig = new PreloadConfig();
        preloadConfig.setLoadLNs(false);
        descDb.preload(preloadConfig);

        ConceptIdKeyForDescCreator descConceptKeyCreator = new ConceptIdKeyForDescCreator(descBinding);

        SecondaryConfig descByConceptIdConfig = new SecondaryConfig();
        descByConceptIdConfig.setReadOnly(VodbEnv.isReadOnly());
        descByConceptIdConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        descByConceptIdConfig.setAllowCreate(!VodbEnv.isReadOnly());
        descByConceptIdConfig.setSortedDuplicates(false);
        descByConceptIdConfig.setKeyCreator(descConceptKeyCreator);
        descByConceptIdConfig.setAllowPopulate(true);
        descByConceptIdConfig.setTransactional(VodbEnv.isTransactional());

        conceptDescMap = env.openSecondaryDatabase(null, "conceptDescMap", descDb, descByConceptIdConfig);
        conceptDescMap.preload(preloadConfig);
        // logStats();
    }

    public void logStats() throws DatabaseException {
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            StatsConfig config = new StatsConfig();
            config.setClear(true);
            config.setFast(false);
            DatabaseStats stats = descDb.getStats(config);
            AceLog.getAppLog().fine("descDb stats: " + stats.toString());
        }
    }

    private class DescriptionIterator implements Iterator<I_DescriptionVersioned> {

        DatabaseEntry foundKey = new DatabaseEntry();

        DatabaseEntry foundData = new DatabaseEntry();

        boolean hasNext;

        private I_DescriptionVersioned desc;

        private Cursor descCursor;

        private DescriptionIterator() throws IOException {
            super();
            try {
                descCursor = descDb.openCursor(null, null);
                getNext();
            } catch (DatabaseException e) {
                throw new ToIoException(e);
            }
        }

        private void getNext() {
            try {
                hasNext = (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
                if (hasNext) {
                    desc = (I_DescriptionVersioned) descBinding.entryToObject(foundData);
                } else {
                    desc = null;
                    descCursor.close();
                }
            } catch (Exception ex) {
                try {
                    descCursor.close();
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

        public I_DescriptionVersioned next() {
            if (hasNext) {
                I_DescriptionVersioned next = desc;
                getNext();
                return next;
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void closeCursor() throws DatabaseException {
            descCursor.close();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreDescriptions#getDescriptionIterator()
     */
    public Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException {
        return new DescriptionIterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreDescriptions#writeDescription(org.dwfa.ace.
     * api.I_DescriptionVersioned)
     */
    public void writeDescription(I_DescriptionVersioned desc) throws DatabaseException {
        writeToLucene(desc);
        writeDescriptionNoLuceneUpdate(desc);
    }

    public void writeDescriptionNoLuceneUpdate(I_DescriptionVersioned desc) throws DatabaseException {
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(desc.getDescId(), key);
        descBinding.objectToEntry(desc, value);
        descDb.put(BdbEnv.transaction, key, value);
    }

    private void writeToLucene(I_DescriptionVersioned desc) throws DatabaseException {
        try {
            IndexReader reader = IndexReader.open(luceneDir);
            reader.deleteDocuments(new Term("dnid", Integer.toString(desc.getDescId())));
            reader.close();
            IndexWriter writer = new IndexWriter(luceneDir, new StandardAnalyzer(), false);
            Document doc = new Document();
            doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("cnid", Integer.toString(desc.getConceptId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            addIdsToIndex(doc, identifierDb.getId(desc.getDescId()));
            addIdsToIndex(doc, identifierDb.getId(desc.getConceptId()));

            String lastDesc = null;
            for (I_DescriptionTuple tuple : desc.getTuples()) {
                if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine(
                            "Adding to index. dnid:  " + desc.getDescId() + " desc: " + tuple.getText());
                    }
                    doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.TOKENIZED));
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreDescriptions#hasDescription(int)
     */
    public boolean hasDescription(int descId, int conceptId) throws DatabaseException {
        DatabaseEntry descKey = new DatabaseEntry();
        DatabaseEntry descValue = new DatabaseEntry();
        intBinder.objectToEntry(descId, descKey);
        if (descDb.get(BdbEnv.transaction, descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreDescriptions#getDescription(int)
     */
    public I_DescriptionVersioned getDescription(int descId, int conId) throws IOException {
        DatabaseEntry descKey = new DatabaseEntry();
        DatabaseEntry descValue = new DatabaseEntry();
        intBinder.objectToEntry(descId, descKey);
        try {
            if (descDb.get(BdbEnv.transaction, descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                return (I_DescriptionVersioned) descBinding.entryToObject(descValue);
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }

        try {
            throw new IOException("Description: " + descId + " " + identifierDb.getUids(descId) + " not found.");
        } catch (TerminologyException e) {
            throw new ToIoException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreDescriptions#getDescriptions(int)
     */
    public List<I_DescriptionVersioned> getDescriptions(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting descriptions for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        descForConceptKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = conceptDescMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        List<I_DescriptionVersioned> matches = new ArrayList<I_DescriptionVersioned>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinDescVersioned descFromConceptId = (ThinDescVersioned) descBinding.entryToObject(foundData);
            if (descFromConceptId.getConceptId() == conceptId) {
                matches.add(descFromConceptId);
            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "Descriptions fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return matches;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreDescriptions#searchRegex(org.dwfa.ace.search
     * .I_TrackContinuation,
     * java.util.regex.Pattern, java.util.Collection,
     * java.util.concurrent.CountDownLatch, java.util.List,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public void searchRegex(I_TrackContinuation tracker, Pattern p, Collection<I_DescriptionVersioned> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
            throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            timer = new Stopwatch();
            timer.start();
        }
        Cursor descCursor = descDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        Semaphore checkSemaphore = new Semaphore(15);
        while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                checkSemaphore.acquire();
            } catch (InterruptedException e) {
                AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
            }
            if (tracker.continueWork()) {
                I_DescriptionVersioned descV = (I_DescriptionVersioned) descBinding.entryToObject(foundData);
                ACE.threadPool.execute(new CheckAndProcessRegexMatch(latch, checkSemaphore, p, matches, descV,
                    checkList, config));
            } else {
                while (latch.getCount() > 0) {
                    latch.countDown();
                }
                break;
            }
        }
        descCursor.close();
        try {
            latch.await();
        } catch (InterruptedException e) {
            AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
        }
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            if (tracker.continueWork()) {
                AceLog.getAppLog().info("Search 3 time: " + timer.getElapsedTime());
            } else {
                AceLog.getAppLog().info("Canceled. Elapsed time: " + timer.getElapsedTime());
            }
            timer.stop();
        }
    }

    /*
     * For issues upgrading to lucene 2.x, see this link:
     * 
     * http://www.nabble.com/Lucene-in-Action-examples-complie-problem-tf2418478.
     * html#a6743189
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreDescriptions#searchLucene(org.dwfa.ace.search
     * .I_TrackContinuation,
     * java.lang.String, java.util.Collection,
     * java.util.concurrent.CountDownLatch, java.util.List,
     * org.dwfa.ace.api.I_ConfigAceFrame,
     * org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator)
     */
    public CountDownLatch searchLucene(I_TrackContinuation tracker, String query, Collection<LuceneMatch> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
            LuceneProgressUpdator updater) throws DatabaseException, IOException, ParseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            timer = new Stopwatch();
            timer.start();
        }
        if (luceneDir.exists() == false) {
            updater.setProgressInfo("Making lucene index -- this may take a while...");
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
        updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime) + " ms.");
        Hits hits = luceneSearcher.search(q);
        updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
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

            ACE.threadPool.execute(new CheckAndProcessLuceneMatch(hitLatch, updater, doc, score, matches, checkList,
                config, this));
        }
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            if (tracker.continueWork()) {
                AceLog.getAppLog().info("Search 4 time: " + timer.getElapsedTime());
            } else {
                AceLog.getAppLog().info("Search Canceled. Elapsed time: " + timer.getElapsedTime());
            }
            timer.stop();
        }
        return hitLatch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreDescriptions#createLuceneDescriptionIndex()
     */
    public void createLuceneDescriptionIndex() throws IOException {
        try {
            Stopwatch timer = new Stopwatch();
            timer.start();
            luceneDir.mkdirs();
            IndexWriter writer = new IndexWriter(luceneDir, new StandardAnalyzer(), true);
            writer.setUseCompoundFile(true);
            writer.setMergeFactor(10000);
            writer.setMaxMergeDocs(Integer.MAX_VALUE);
            writer.setMaxBufferedDocs(1000);
            Cursor descCursor = descDb.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            int counter = 0;
            int optimizeInterval = 10000;
            ProcessQueue processQueue = new ProcessQueue(THREAD_COUNT);
            LuceneDeescriptionProcessor processor = new LuceneDeescriptionProcessor(writer);
            while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                ThinDescVersioned descV = (ThinDescVersioned) descBinding.entryToObject(foundData);
                processor.add(descV);
                counter++;
                if (counter % optimizeInterval == 0) {
                    AceLog.getAppLog().info("Lucene description index creation, " + counter + " queued");
                    processQueue.execute(processor);
                    processor = new LuceneDeescriptionProcessor(writer);
                    synchronized (writer) {
                        writer.optimize();
                    }
                }
            }
            if (processor.getBatchSize() > 0) {
                processQueue.execute(processor);
            }
            processQueue.awaitCompletion();
            descCursor.close();
            AceLog.getAppLog().info("Optimizing index time: " + timer.getElapsedTime());
            writer.optimize();
            writer.close();
            if (AceLog.getAppLog().isLoggable(Level.INFO)) {
                AceLog.getAppLog().info("Index time: " + timer.getElapsedTime());
                timer.stop();
            }
        } catch (DatabaseException ex) {
            throw new ToIoException(ex);
        }
    }

    private void indexDescription(IndexWriter writer, ThinDescVersioned descV) throws IOException,
            CorruptIndexException {
        Document doc = new Document();
        doc.add(new Field("dnid", Integer.toString(descV.getDescId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("cnid", Integer.toString(descV.getConceptId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        addIdsToIndex(doc, identifierDb.getId(descV.getDescId()));
        addIdsToIndex(doc, identifierDb.getId(descV.getConceptId()));

        String lastDesc = null;
        for (I_DescriptionTuple tuple : descV.getTuples()) {
            if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
                doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.TOKENIZED));
            }

        }
        writer.addDocument(doc);
    }

    class LuceneDeescriptionProcessor implements Runnable {
        List<ThinDescVersioned> batch = new ArrayList<ThinDescVersioned>();
        IndexWriter writer;

        public LuceneDeescriptionProcessor(IndexWriter writer) {
            this.writer = writer;
        }

        public int getBatchSize() {
            return batch.size();
        }

        public void add(ThinDescVersioned descV) {
            batch.add(descV);
        }

        @Override
        public void run() {
            try {
                for (ThinDescVersioned description : batch) {
                    indexDescription(writer, description);
                }
                synchronized (writer) {
                    writer.optimize();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void addIdsToIndex(Document doc, I_IdVersioned did) {
        for (I_IdPart p : did.getVersions()) {
            doc.add(new Field("desc", p.getSourceId().toString(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreDescriptions#iterateDescriptionEntries(org.
     * dwfa.vodb.types.I_ProcessDescriptionEntries)
     */
    public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor) throws Exception {
        Cursor descCursor = descDb.openCursor(null, null);
        DatabaseEntry foundKey = processor.getKeyEntry();
        DatabaseEntry foundData = processor.getDataEntry();
        while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                processor.processDesc(foundKey, foundData);
            } catch (Exception e) {
                descCursor.close();
                throw e;
            }
        }
        descCursor.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreDescriptions#doLuceneSearch(java.lang.String)
     */
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

    public void close() throws DatabaseException {
        if (conceptDescMap != null) {
            conceptDescMap.close();
        }
        if (descDb != null) {
            descDb.close();
        }
    }

    public void sync() throws DatabaseException {
        if (descDb != null) {
            if (!descDb.getConfig().getReadOnly()) {
                descDb.sync();
            }
        }
        if (conceptDescMap != null) {
            if (!conceptDescMap.getConfig().getReadOnly()) {
                conceptDescMap.sync();
            }
        }

    }

    public I_DescriptionVersioned descEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return (I_DescriptionVersioned) descBinding.entryToObject(value);
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException {
        if (bean.descriptions != null) {
            for (I_DescriptionVersioned desc : bean.descriptions) {
                boolean changed = false;
                for (I_DescriptionPart p : desc.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                        changed = true;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Committing: " + p);
                        }
                    }
                }
                if (changed) {
                    this.writeDescription(desc);
                }
            }
        }
        if (bean.uncommittedDescriptions != null) {
            for (I_DescriptionVersioned desc : bean.uncommittedDescriptions) {
                for (I_DescriptionPart p : desc.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                this.writeDescription(desc);
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
    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
