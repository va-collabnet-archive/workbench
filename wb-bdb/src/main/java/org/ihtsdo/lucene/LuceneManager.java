package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public abstract class LuceneManager {

    public enum LuceneSearchType {

        DESCRIPTION, WORKFLOW_HISTORY;
    };
    protected static final Logger logger = Logger.getLogger(LuceneManager.class.getName());
    private static ExecutorService luceneWriterService =
            Executors.newFixedThreadPool(1, new NamedThreadFactory(new ThreadGroup("Lucene group"), "Lucene writer"));
    public final static Version version = Version.LUCENE_43;
    protected static DescriptionIndexGenerator descIndexer = null;
    protected static WfHxIndexGenerator wfIndexer = null;
    public static Directory descLuceneMutableDir;
    public static Directory descLuceneReadOnlyDir;
    public static Directory wfHxLuceneDir;
    protected static IndexReader descReadOnlyReader;
    protected static IndexWriter descWriter;
    protected static DirectoryReader descMutableSearcher;
    protected static IndexReader wfReadOnlyReader;
    protected static IndexWriter wfHxWriter;
    protected static DirectoryReader wfMutableSearcher;
    private static Semaphore initSemaphore = new Semaphore(1);

    public static void commit() throws IOException {
        if (descWriter != null) {
            descWriter.commit();
        }
        if (wfHxWriter != null) {
            wfHxWriter.commit();
        }
        
    }

    public static void close(LuceneSearchType type) {
        IndexWriter writer;

        if (type == LuceneSearchType.DESCRIPTION) {
            writer = descWriter;

            if (writer != null) {
                try {
                    writer.commit();
                    writer.close(true);
                    writer = null;
                } catch (Throwable e) {
                    logger.log(Level.SEVERE, "Exception during lucene writer close", e);
                }
            }

            descWriter = writer;
            logger.info("Shutting down luceneWriterService.");
            luceneWriterService.shutdown();
            logger.info("Awaiting termination of luceneWriterService.");

            try {
                luceneWriterService.awaitTermination(90, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } else {
            writer = wfHxWriter;
            if (writer != null) {
                try {
                    writer.commit();
                    writer.close(true);
                    writer = null;
                } catch (Throwable e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }


            wfHxWriter = writer;
        }
    }

    public static void init(LuceneSearchType type) throws IOException {
        // Only do if not first time
        if (type == LuceneSearchType.DESCRIPTION) {
            if (descLuceneReadOnlyDir == null) {
                initSemaphore.acquireUninterruptibly();
                try {
                    if (descLuceneReadOnlyDir == null) {
                        descLuceneReadOnlyDir = initDirectory(DescriptionLuceneManager.descLuceneReadOnlyDirFile, false, LuceneSearchType.DESCRIPTION);
                        if (DirectoryReader.indexExists(descLuceneReadOnlyDir)) {
                            descReadOnlyReader = DirectoryReader.open(descLuceneReadOnlyDir);
                        }
                    }
                } finally {
                    initSemaphore.release();
                }
            }

            if (descLuceneMutableDir == null) {
                initSemaphore.acquireUninterruptibly();
                try {
                    if (descLuceneMutableDir == null) {
                        descLuceneMutableDir = initDirectory(DescriptionLuceneManager.descLuceneMutableDirFile, true, LuceneSearchType.DESCRIPTION);
                    }
                } finally {
                    initSemaphore.release();
                }
            }
        } else {
            if (wfHxLuceneDir == null) {
                initSemaphore.acquireUninterruptibly();
                try {
                    if (wfHxLuceneDir == null) {
                        if (WfHxLuceneManager.runningLuceneDirFile.exists()) {
                            // For execution in application
                            setLuceneRootDir(WfHxLuceneManager.runningLuceneDirFile, LuceneSearchType.WORKFLOW_HISTORY);
                            wfHxLuceneDir = initDirectory(WfHxLuceneManager.runningLuceneDirFile, true, LuceneSearchType.WORKFLOW_HISTORY);
                            if (DirectoryReader.indexExists(wfHxLuceneDir)) {
                                wfReadOnlyReader = DirectoryReader.open(wfHxLuceneDir);
                            }
                         } else {
                            // For creating index via LoadWfToBdb mojo for which setLuceneRootDir has already been called
                            wfHxLuceneDir = initDirectory(WfHxLuceneManager.wfHxLuceneDirFile, true, LuceneSearchType.WORKFLOW_HISTORY);
                        }
                    }
                } finally {
                    initSemaphore.release();
                }
            }
        }
    }

    private static Directory initDirectory(File luceneDirFile, boolean mutable, LuceneSearchType type)
            throws IOException, CorruptIndexException,
            LockObtainFailedException {
        Directory luceneDir;
        if (luceneDirFile.exists()) {
            luceneDir = new SimpleFSDirectory(luceneDirFile);
            if (mutable) {
                setupWriter(luceneDirFile, luceneDir, type);
            }
        } else {
            luceneDirFile.mkdirs();
            luceneDir = new SimpleFSDirectory(luceneDirFile);
            if (mutable) {
                setupWriter(luceneDirFile, luceneDir, type);
            }
        }
        return luceneDir;
    }

    protected static Directory setupWriter(File luceneDirFile, Directory luceneDir, LuceneSearchType type)
            throws IOException, CorruptIndexException,
            LockObtainFailedException {
        if (luceneDir == null) {
            luceneDir = new SimpleFSDirectory(luceneDirFile);
        }
        luceneDir.clearLock("write.lock");

        IndexWriterConfig config = new IndexWriterConfig(version, new StandardAnalyzer(version));
        MergePolicy mergePolicy = new LogByteSizeMergePolicy();

        config.setMergePolicy(mergePolicy);
        config.setSimilarity(new ShortTextSimilarity());
        IndexWriter writer = new IndexWriter(luceneDir, config);

        if (type == LuceneSearchType.DESCRIPTION) {
            descWriter = writer;
            descMutableSearcher = DirectoryReader.open(writer, true);
        } else {
            wfHxWriter = writer;
            wfMutableSearcher = DirectoryReader.open(writer, true);
        }

        return luceneDir;
    }

    public static class ShortTextSimilarity extends DefaultSimilarity {

        public ShortTextSimilarity() {
        }

        public float idf(int docFreq, int numDocs) {
            return (float) 1.0;
        }

        @Override
        public float coord(int overlap, int maxOverlap) {
            return 1.0f;
        }

        @Override
        public float tf(float freq) {
            return 1.0f;
        }

        @Override
        public float tf(int freq) {
            return 1.0f;
        }

        public float computeNorm(String field, FieldInvertState state) {
            return (float) (1.0 / (state.getLength() * 5));
        }
    }

    public static boolean indexExists(LuceneSearchType type) {
        if (type == LuceneSearchType.DESCRIPTION) {
            return DescriptionLuceneManager.descLuceneMutableDirFile.exists();
        } else {
            return WfHxLuceneManager.wfHxLuceneDirFile.exists();
        }
    }

    public static void writeToLucene(Collection items, LuceneSearchType type) throws IOException {
        if (type == LuceneSearchType.WORKFLOW_HISTORY) {
            throw new IOException("Cannot call this for WfHxLuceneManager-based functionality");
        }

        writeToLucene(items, type, null);
    }

    public static synchronized void writeToLucene(Collection items, LuceneSearchType type, ViewCoordinate viewCoord) throws IOException {
        init(type);
        try {
            if (type == LuceneSearchType.DESCRIPTION) {
                DescriptionLuceneManager.writeToLuceneNoLock(items);
            } else {
                WfHxLuceneManager.writeToLuceneNoLock(items, null, viewCoord);
            }
        } catch (IOException e) {
            throw new IOException(e);
        } catch (TerminologyException e) {
            throw new IOException(e);
        }
    }

    public static SearchResult search(Query q, LuceneSearchType type) throws CorruptIndexException, IOException {
        IndexSearcher searcher;

        init(type);
        int matchLimit = getMatchLimit(type);

        TtkMultiReader mr = null;


        IndexReader readOnlyReader;
        DirectoryReader mutableReader;
        IndexWriter writer;
        if (type == LuceneSearchType.DESCRIPTION) {
            readOnlyReader = descReadOnlyReader;
            mutableReader = descMutableSearcher;
            writer = descWriter;

        } else {
            readOnlyReader = wfReadOnlyReader;
            mutableReader = wfMutableSearcher;
            writer = wfHxWriter;
        }


        if (readOnlyReader != null) {
            DirectoryReader newMutableSearcher =
                    DirectoryReader.openIfChanged(mutableReader, writer, true);
            if (newMutableSearcher != null) {
//                mutableReader.close();
                mutableReader = newMutableSearcher;
            }
            mr = new TtkMultiReader(readOnlyReader, mutableReader);
            searcher = new IndexSearcher(mr);
            searcher.setSimilarity(new ShortTextSimilarity());
        } else {
            DirectoryReader newMutableSearcher =
                    DirectoryReader.openIfChanged(mutableReader, writer, true);
            if (newMutableSearcher != null) {
//                mutableReader.close();
                mutableReader = newMutableSearcher;
            }
            searcher = new IndexSearcher(mutableReader);
            searcher.setSimilarity(new ShortTextSimilarity());
        }




        TopDocs topDocs = searcher.search(q, null, matchLimit);

        // Suppress duplicates in the read-only index 
        List<ScoreDoc> newDocs = new ArrayList<ScoreDoc>(topDocs.scoreDocs.length);
        HashSet<Integer> ids = new HashSet<Integer>(topDocs.scoreDocs.length);

        String searchTerm;
        if (type == LuceneSearchType.DESCRIPTION) {
            searchTerm = "dnid";
        } else {
            searchTerm = "memberId";
        }

        if (mr != null) {
            for (ScoreDoc sd : topDocs.scoreDocs) {
                if (!mr.isFirstIndex(sd.doc)) {
                    newDocs.add(sd);
                    Document d = searcher.doc(sd.doc);

                    int nid = Integer.parseInt(d.get(searchTerm));
                    ids.add(nid);
                }
            }
        }
        for (ScoreDoc sd : topDocs.scoreDocs) {
            if ((mr == null) || mr.isFirstIndex(sd.doc)) {
                Document d = searcher.doc(sd.doc);
                int nid = Integer.parseInt(d.get(searchTerm));

                if (!ids.contains(nid)) {
                    newDocs.add(sd);
                }
            }
        }

        topDocs.scoreDocs = newDocs.toArray(new ScoreDoc[newDocs.size()]);
        topDocs.totalHits = topDocs.scoreDocs.length;
        return new SearchResult(topDocs, searcher);

    }

    public static int getMatchLimit(LuceneSearchType type) {
        if (type == LuceneSearchType.DESCRIPTION) {
            return DescriptionLuceneManager.matchLimit;
        } else {
            return WfHxLuceneManager.matchLimit;
        }
    }

    public static void setMatchLimit(int limit, LuceneSearchType type) {
        if (type == LuceneSearchType.DESCRIPTION) {
            DescriptionLuceneManager.matchLimit = limit;
        } else {
        }
    }

    public static void createLuceneIndex(LuceneSearchType type) throws Exception {
        createLuceneIndex(type, null);
    }

    public static void createLuceneIndex(LuceneSearchType type, ViewCoordinate viewCoord) throws Exception {
        IndexWriter writer;
        init(type);
        Stopwatch timer = new Stopwatch();
        timer.start();

        if (type == LuceneSearchType.DESCRIPTION) {
            writer = descWriter;
        } else {
            writer = wfHxWriter;
        }

        if (writer == null) {
            if (type == LuceneSearchType.DESCRIPTION) {
                DescriptionLuceneManager.descLuceneMutableDirFile.mkdirs();
                descLuceneMutableDir = setupWriter(DescriptionLuceneManager.descLuceneMutableDirFile, descLuceneMutableDir, type);
            } else {
                WfHxLuceneManager.wfHxLuceneDirFile.mkdirs();
                wfHxLuceneDir = setupWriter(WfHxLuceneManager.wfHxLuceneDirFile, wfHxLuceneDir, type);
            }
        }

        if (type == LuceneSearchType.DESCRIPTION) {
            descIndexer = new DescriptionIndexGenerator(writer);
            AceLog.getAppLog().info("Starting index time: " + timer.getElapsedTime());
            Bdb.getConceptDb().iterateConceptDataInSequence(descIndexer);
        } else {
            wfIndexer = new WfHxIndexGenerator(writer, viewCoord);
            AceLog.getAppLog().info("All concepts initialized.  Write to Lucene Index with starting time: " + timer.getElapsedTime());
            wfIndexer.initializeExistingWorkflow();
            AceLog.getAppLog().info("Starting index time: " + timer.getElapsedTime());
        }


        AceLog.getAppLog().info("\nOptimizing index start time: " + timer.getElapsedTime());
        writer.commit();
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            AceLog.getAppLog().info("Total index time: " + timer.getElapsedTime());
            timer.stop();
        }
    }

    public static void setLuceneRootDir(File root, LuceneSearchType type) {
        IndexWriter writer;

        if (type == LuceneSearchType.DESCRIPTION) {
            DescriptionLuceneManager.descLuceneMutableDirFile = new File(root, DescriptionLuceneManager.descMutableDirectorySuffix);
            DescriptionLuceneManager.descLuceneReadOnlyDirFile = new File(root, DescriptionLuceneManager.descReadOnlyDirectorySuffix);
            writer = descWriter;
        } else {
            WfHxLuceneManager.wfHxLuceneDirFile = root;
            writer = wfHxWriter;
        }

        if (writer != null) {
            try {
                writer.close(true);
            } catch (CorruptIndexException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (type == LuceneSearchType.DESCRIPTION) {
            descLuceneMutableDir = null;
            descLuceneReadOnlyDir = null;
        } else {
            wfHxLuceneDir = null;
        }
    }
}
