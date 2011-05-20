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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
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
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;

public abstract class LuceneManager {

	public enum LuceneSearchType {
		DESCRIPTION, WORKFLOW_HISTORY;
	};

    public final static Version version = Version.LUCENE_30;
    
    protected static DescriptionIndexGenerator descIndexer = null;
    protected static WfHxIndexGenerator wfIndexer = null;
    protected static IndexWriter wfHxWriter;
    protected static IndexWriter descWriter;
    protected static ParallelMultiSearcher wfHxSearcher;
    protected static ParallelMultiSearcher descSearcher;

    public static Directory wfHxLuceneDir;
    public static Directory descLuceneMutableDir;
    public static Directory descLuceneReadOnlyDir;

    private static LuceneSearchType currentType = null;

    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public static void close(LuceneSearchType type) {
		ParallelMultiSearcher searcher;
		IndexWriter writer;
		
		if (type == LuceneSearchType.DESCRIPTION) {
			writer = descWriter;
    		searcher = descSearcher;
		} else {
			writer = wfHxWriter;
    		searcher = wfHxSearcher;
    	}

        // Only do if not first time called
    	if (currentType != null)
    	{
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
    }

    public static void init(LuceneSearchType type) throws IOException {
		currentType = type;

    	// Only do if not first time
    	if (type == LuceneSearchType.DESCRIPTION) {
	    	if (descLuceneReadOnlyDir == null) {
	    		descLuceneReadOnlyDir = initDirectory(DescriptionLuceneManager.descLuceneReadOnlyDirFile, false);
        	}
	
	    	if (descLuceneMutableDir == null) {
    	    	descLuceneMutableDir = initDirectory(DescriptionLuceneManager.descLuceneMutableDirFile, true);
	    	}
    	} else {
	    	if (wfHxLuceneDir == null) {
	    		wfHxLuceneDir = initDirectory(WfHxLuceneManager.wfHxLuceneDirFile, true);
	    	}
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

    protected static Directory setupWriter(File luceneDirFile, Directory luceneDir)
            throws IOException, CorruptIndexException,
            LockObtainFailedException {
        if (luceneDir == null) {
            luceneDir = new SimpleFSDirectory(luceneDirFile);
        }
        luceneDir.clearLock("write.lock");
        
        IndexWriter writer;
        if (new File(luceneDirFile, "segments.gen").exists()) {
            writer = new IndexWriter(luceneDir, new StandardAnalyzer(version), false,
                    MaxFieldLength.UNLIMITED);
        } else {
            writer = new IndexWriter(luceneDir, new StandardAnalyzer(version), true,
                    MaxFieldLength.UNLIMITED);
        }

        if (currentType == LuceneSearchType.DESCRIPTION) {
        	descWriter = writer;
        } else {
        	wfHxWriter = writer;
    }

        return luceneDir;
        }

    public static boolean indexExists(LuceneSearchType type) {
    	if (type == LuceneSearchType.DESCRIPTION) {
    		return DescriptionLuceneManager.descLuceneMutableDirFile.exists();
    	} else {
    		return WfHxLuceneManager.wfHxLuceneDirFile.exists();
        }
    }

    public static synchronized void writeToLucene(Collection items, LuceneSearchType type) throws IOException {
        init(type);
        try {
            rwl.writeLock().lock();
            if (type == LuceneSearchType.DESCRIPTION) {
            	DescriptionLuceneManager.writeToLuceneNoLock(items);
            } else {
            	WfHxLuceneManager.writeToLuceneNoLock(items);
            }
        } catch (CorruptIndexException e) {
            throw new IOException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public static SearchResult search(Query q, LuceneSearchType type) throws CorruptIndexException, IOException {
    	ParallelMultiSearcher searcherCopy; 
		ParallelMultiSearcher searcher;

    	init(type);
		int matchLimit = getMatchLimit(type);
        rwl.readLock().lock();

        try {
        	if (type == LuceneSearchType.DESCRIPTION) {
        		searcherCopy = descSearcher;
        		searcher = descSearcher;
        } else {
        		searcherCopy = wfHxSearcher;
        		searcher = wfHxSearcher;
    }

            if (searcherCopy == null) {
            	if (type == LuceneSearchType.DESCRIPTION) {
            		IndexSearcher readOnlySearcher = new IndexSearcher(descLuceneReadOnlyDir, true);
            		IndexSearcher mutableSearcher = new IndexSearcher(descLuceneMutableDir, true);
                searcherCopy = new ParallelMultiSearcher(readOnlySearcher, mutableSearcher);
                    descSearcher = searcherCopy;
                } else {
            		IndexSearcher allHxSearcher = new IndexSearcher(wfHxLuceneDir, true);
                	searcherCopy = new ParallelMultiSearcher(allHxSearcher);
                    wfHxSearcher = searcherCopy;
                }
            }
            TopDocs topDocs = searcherCopy.search(q, null, matchLimit);

            // Suppress duplicates in the read-only index 
            List<ScoreDoc> newDocs = new ArrayList<ScoreDoc>(topDocs.scoreDocs.length);
            HashSet<Integer> ids = new HashSet<Integer>(topDocs.scoreDocs.length);

            String searchTerm;
            if (type == LuceneSearchType.DESCRIPTION) {
            	searchTerm = "dnid"; 
            } else {
            	searchTerm = "memberId";
            }

            searcher = searcherCopy;
            for (ScoreDoc sd : topDocs.scoreDocs) {
                int index = searcherCopy.subSearcher(sd.doc);
                if (index == 1) {
                    newDocs.add(sd);
                    Document d = searcher.doc(sd.doc);

                    int nid = Integer.parseInt(d.get(searchTerm));
                    ids.add(nid);
                }
            }
            for (ScoreDoc sd : topDocs.scoreDocs) {
                int index = searcherCopy.subSearcher(sd.doc);
                if (index == 0) {
                    Document d = searcher.doc(sd.doc);
        
                    int nid = Integer.parseInt(d.get(searchTerm));
                    if (!ids.contains(nid)) {
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

    public static int getMatchLimit(LuceneSearchType type) {
    	if (type == LuceneSearchType.DESCRIPTION) {
    		return DescriptionLuceneManager.matchLimit;
    	} else {
			return WfHxLuceneManager.calculateMatchLimit();
    	}
    }

    public static void setMatchLimit(int limit, LuceneSearchType type) {
    	if (type == LuceneSearchType.DESCRIPTION) {
    		DescriptionLuceneManager.matchLimit = limit;
    	} else {

    	}
    }
    

	public static void createLuceneIndex(LuceneSearchType type) throws Exception {
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
        		descLuceneMutableDir = setupWriter(DescriptionLuceneManager.descLuceneMutableDirFile, descLuceneMutableDir);
        	} else {
        		WfHxLuceneManager.wfHxLuceneDirFile.mkdirs();
        		wfHxLuceneDir = setupWriter(WfHxLuceneManager.wfHxLuceneDirFile, wfHxLuceneDir);
        	}
        }

        writer.setUseCompoundFile(true);
        writer.setMergeFactor(15);
        writer.setMaxMergeDocs(Integer.MAX_VALUE);
        writer.setMaxBufferedDocs(1000);

        if (type == LuceneSearchType.DESCRIPTION) {
        	descIndexer = new DescriptionIndexGenerator(writer);
            AceLog.getAppLog().info("Starting index time: " + timer.getElapsedTime());
            Bdb.getConceptDb().iterateConceptDataInSequence(descIndexer);
        } else {
        	wfIndexer = new WfHxIndexGenerator(writer);
            AceLog.getAppLog().info("Starting index time: " + timer.getElapsedTime());
        	wfIndexer.initializeWfHxLucene();
    }


        AceLog.getAppLog().info("\nOptimizing index start time: " + timer.getElapsedTime());
        writer.optimize();
        writer.commit();
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            AceLog.getAppLog().info("Total index time: " + timer.getElapsedTime());
            timer.stop();
        }
	}
	

	public static void setDbRootDir(File root, LuceneSearchType type) {
		ParallelMultiSearcher searcher;
		IndexWriter writer;
		
		if (type == LuceneSearchType.DESCRIPTION) {
			DescriptionLuceneManager.descLuceneMutableDirFile = new File(root, DescriptionLuceneManager.descMutableDirectorySuffix);
	        DescriptionLuceneManager.descLuceneReadOnlyDirFile = new File(root, DescriptionLuceneManager.descReadOnlyDirectorySuffix);
			searcher = descSearcher;
			writer = descWriter;
		} else {
			WfHxLuceneManager.wfHxLuceneDirFile = new File(root, WfHxLuceneManager.wfHxDirectorySuffix);
			searcher = wfHxSearcher;
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
            writer = null;
        };

        if (searcher != null) {
            try {
                searcher.close();
            } catch (IOException ex) {
               throw new RuntimeException(ex);
             }
            searcher = null;
        }

        if (type == LuceneSearchType.DESCRIPTION) {
            descLuceneMutableDir = null;
            descLuceneReadOnlyDir = null;
    	} else {
            wfHxLuceneDir = null;
        }
    }
}
