package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.db.bdb.Bdb;

public class LuceneManager {
	
	public static Version version = Version.LUCENE_30;
	
	public static File luceneMutableDirFile = new File("target/berkeley-db/mutable/lucene");
	public static File luceneReadOnlyDirFile = new File("target/berkeley-db/read-only/lucene");
    public static Directory luceneMutableDir;
    public static Directory luceneReadOnlyDir;

    private static IndexWriter writer;

	private static ParallelMultiSearcher searcher;
	
	private static int matchLimit = 10000;
	
	private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	
	public static void close() {
        if (searcher != null) {
            try {
                searcher.close();
                searcher = null;
            } catch (Throwable e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
	    if (writer != null) {
	        try {
                writer.commit();
                writer.close(true);
                writer = null;
            } catch (Throwable e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
	    }
	}
	public static void init() throws IOException {
		if (luceneReadOnlyDir == null) {
			luceneReadOnlyDir = initDirectory(luceneReadOnlyDirFile, false);
		}
		if (luceneMutableDir == null) {
			luceneMutableDir = initDirectory(luceneMutableDirFile, true);
		}
	}

	private static Directory initDirectory(File luceneDirFile, boolean mutable)
			throws IOException, CorruptIndexException,
			LockObtainFailedException {
		Directory luceneDir = null;
		if (luceneDirFile.exists()) {
			luceneDir = new SimpleFSDirectory(luceneDirFile);
			if (mutable) {
				setupWriter(luceneDirFile, luceneDir);
			}
		} else {
			luceneDirFile.mkdirs();
			luceneDir = new SimpleFSDirectory(luceneDirFile);
			if (mutable) {
				setupWriter(luceneDirFile, luceneDir);
			}
		}
		return luceneDir;
	}
	private static Directory setupWriter(File luceneDirFile, Directory luceneDir)
			throws IOException, CorruptIndexException,
			LockObtainFailedException {
		if (luceneDir == null) {
			luceneDir = new SimpleFSDirectory(luceneDirFile);
		}
		luceneDir.clearLock("write.lock");
		if (new File(luceneDirFile, "segments.gen").exists()) {
			writer = new IndexWriter(luceneDir, new StandardAnalyzer(version), false, 
					MaxFieldLength.UNLIMITED);
		} else {
			writer = new IndexWriter(luceneDir, new StandardAnalyzer(version), true, 
					MaxFieldLength.UNLIMITED);
		}
		return luceneDir;
	}
	public static boolean indexExists() {
		return luceneMutableDirFile.exists();
	}

    public static void createLuceneDescriptionIndex() throws Exception {
    	init();
        Stopwatch timer = new Stopwatch();
        timer.start();
        luceneMutableDirFile.mkdirs();
        if (writer == null) {
			luceneMutableDir = setupWriter(luceneMutableDirFile, luceneMutableDir);
        }
        writer.setUseCompoundFile(true);
        writer.setMergeFactor(15);
        writer.setMaxMergeDocs(Integer.MAX_VALUE);
        writer.setMaxBufferedDocs(1000);
 
        IndexGenerator indexer = new IndexGenerator(writer);
        Bdb.getConceptDb().iterateConceptDataInSequence(indexer);
        
        AceLog.getAppLog().info("Optimizing index time: " + timer.getElapsedTime());
        writer.optimize();
        writer.commit();
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            AceLog.getAppLog().info("Index time: " + timer.getElapsedTime());
            timer.stop();
        }
    }


    public static synchronized void writeToLucene(Collection<Description> descriptions) throws IOException {
    	init();
        try {
        	rwl.writeLock().lock();
        	writeToLuceneNoLock(descriptions);
        } catch (CorruptIndexException e) {
            throw new IOException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
        	rwl.writeLock().unlock();
        }
    }
	private static void writeToLuceneNoLock(Collection<Description> descriptions)
			throws CorruptIndexException, IOException {
		IndexWriter writerCopy = writer;
		if (writerCopy != null) {
		    for (Description desc: descriptions) {
		        if (desc != null) {
		            writerCopy.deleteDocuments(new Term("dnid", Integer.toString(desc.getDescId())));
		            writerCopy.addDocument(createDoc(desc));
		        }
		    }
		    writerCopy.commit();
		}
		
		if (searcher != null) {
			searcher.close();
		    AceLog.getAppLog().info("Closing lucene searcher");
		}
		searcher = null;
	}
	public static Document createDoc(Description desc)
			throws IOException {
        Document doc = new Document();
		doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("cnid", Integer.toString(desc.getConceptNid()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addIdsToIndex(doc, desc);
		addIdsToIndex(doc, Concept.get(desc.getConceptNid()).getConceptAttributes());

		String lastDesc = null;
		for (I_DescriptionTuple tuple : desc.getTuples()) {
		    if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
		        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
		            AceLog.getAppLog().fine(
		                "Adding to index. dnid:  " + desc.getDescId() + " desc: " + tuple.getText());
		        }
		        doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.ANALYZED));
		    }
		}
		return doc;
	}

    private static  void addIdsToIndex(Document doc, I_Identify did) {
    	if (did != null) {
    		for (I_IdPart p : did.getMutableIdParts()) {
    			doc.add(new Field("desc", p.getDenotation().toString(), Field.Store.NO, 
    					Field.Index.NOT_ANALYZED));
    		}
    	} else {
    		AceLog.getAppLog().alertAndLogException(new Exception("Identifier is null"));
    	}
	}

	public static SearchResult search(Query q) throws CorruptIndexException, IOException {
    	init();
		rwl.readLock().lock();
		try {
			ParallelMultiSearcher searcherCopy = searcher;
			if (searcherCopy == null) {
				IndexSearcher readOnlySearcher = new IndexSearcher(luceneReadOnlyDir, true);
				IndexSearcher mutableSearcher  = new IndexSearcher(luceneMutableDir, true);
				searcherCopy = new ParallelMultiSearcher(readOnlySearcher, mutableSearcher);
				searcher = searcherCopy;
			}
			TopDocs topDocs = searcherCopy.search(q, null, matchLimit);
			
			// Suppress duplicates in the read-only index 
			List<ScoreDoc> newDocs = new ArrayList<ScoreDoc>(topDocs.scoreDocs.length);
			HashSet<Integer> dids = new HashSet<Integer>(topDocs.scoreDocs.length);
			for (ScoreDoc sd: topDocs.scoreDocs) {
				int index = searcherCopy.subSearcher(sd.doc);
				if (index == 1) {
					newDocs.add(sd);
					Document d = searcher.doc(sd.doc);
		            int nid = Integer.parseInt(d.get("dnid"));
		            dids.add(nid);
				}
			}
			for (ScoreDoc sd: topDocs.scoreDocs) {
				int index = searcherCopy.subSearcher(sd.doc);
				if (index == 0) {
					Document d = searcher.doc(sd.doc);
		            int nid = Integer.parseInt(d.get("dnid"));
		            if (!dids.contains(nid)) {
						newDocs.add(sd);
		            }
				}
			}
			topDocs.scoreDocs = newDocs.toArray(new ScoreDoc[newDocs.size()]);
			topDocs.totalHits = topDocs.scoreDocs.length;
			return new SearchResult(topDocs, searcherCopy);
		} finally {
			rwl.readLock().unlock();
		}
	}
	public static int getMatchLimit() {
		return matchLimit;
	}
	public static void setMatchLimit(int matchLimit) {
		LuceneManager.matchLimit = matchLimit;
	}
	
	public static void setDbRootDir(File root) {
		luceneMutableDirFile = new File(root, "mutable/lucene");
		luceneReadOnlyDirFile = new File(root, "read-only/lucene");
	}

}
