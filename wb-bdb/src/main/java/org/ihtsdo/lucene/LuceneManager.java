package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
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
	
	public static File luceneDirFile = new File("target/berkeley-db/lucene");
	public static File getLuceneDirFile() {
        return luceneDirFile;
    }
    public static void setLuceneDirFile(File luceneDirFile) {
        LuceneManager.luceneDirFile = luceneDirFile;
    }
    public static Directory luceneDir;

    private static IndexWriter writer;

	private static IndexSearcher searcher;
	
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
		if (luceneDir == null) {
			if (luceneDirFile.exists()) {
				luceneDir = new SimpleFSDirectory(luceneDirFile);
				luceneDir.clearLock("write.lock");
				if (new File(luceneDirFile, "segments.gen").exists()) {
		            writer = new IndexWriter(luceneDir, new StandardAnalyzer(Version.LUCENE_29), false, 
		            		MaxFieldLength.UNLIMITED);
				} else {
		            writer = new IndexWriter(luceneDir, new StandardAnalyzer(Version.LUCENE_29), true, 
		            		MaxFieldLength.UNLIMITED);
				}
			} else {
				luceneDirFile.mkdirs();
				luceneDir = new SimpleFSDirectory(luceneDirFile);
				luceneDir.clearLock("write.lock");
	            writer = new IndexWriter(luceneDir, new StandardAnalyzer(Version.LUCENE_29), true, 
	            		MaxFieldLength.UNLIMITED);
			}
		}
	}
	public static boolean indexExists() {
		return luceneDirFile.exists();
	}

    public static void createLuceneDescriptionIndex() throws Exception {
    	init();
        Stopwatch timer = new Stopwatch();
        timer.start();
        luceneDirFile.mkdirs();
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
        	rwl.writeLock().unlock();
        } catch (CorruptIndexException e) {
        	rwl.writeLock().unlock();
            throw new IOException(e);
        } catch (IOException e) {
        	rwl.writeLock().unlock();
            throw new IOException(e);
        }
    }
	public static Document createDoc(Description desc)
			throws IOException {
        Document doc = new Document();
		doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("cnid", Integer.toString(desc.getConceptId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
		addIdsToIndex(doc, desc);
		addIdsToIndex(doc, Concept.get(desc.getConceptId()).getConceptAttributes());

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
		return doc;
	}


    private static  void addIdsToIndex(Document doc, I_Identify did) {
    	if (did != null) {
    		for (I_IdPart p : did.getMutableIdParts()) {
    			doc.add(new Field("desc", p.getDenotation().toString(), Field.Store.NO, 
    					Field.Index.UN_TOKENIZED));
    		}
    	} else {
    		AceLog.getAppLog().alertAndLogException(new Exception("Identifier is null"));
    	}
	}

	public static Hits search(Query q) throws CorruptIndexException, IOException {
    	init();
		rwl.readLock().lock();
		try {
			IndexSearcher searcherCopy = searcher;
			if (searcherCopy == null) {
				searcherCopy = new IndexSearcher(luceneDir, true);
				searcher = searcherCopy;
			}
			return searcherCopy.search(q);
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

}
