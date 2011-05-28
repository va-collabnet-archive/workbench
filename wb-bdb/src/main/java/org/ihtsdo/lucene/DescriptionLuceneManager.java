package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.description.Description;

public class DescriptionLuceneManager extends LuceneManager {

	protected static File descLuceneMutableDirFile = new File("target/berkeley-db/mutable/lucene"); 
    protected static File descLuceneReadOnlyDirFile = new File("target/berkeley-db/read-only/lucene");
    protected static String descMutableDirectorySuffix = "mutable/lucene";
    protected static String descReadOnlyDirectorySuffix = "read-only/lucene";
	public static int matchLimit = 10000;

    protected static void writeToLuceneNoLock(Collection<Description> descriptions) throws CorruptIndexException, IOException {
		if (descWriter == null) {
		    descLuceneMutableDir = setupWriter(descLuceneMutableDirFile, descLuceneMutableDir, LuceneSearchType.DESCRIPTION);
		    descWriter.setUseCompoundFile(true);
		    descWriter.setMergeFactor(15);
		    descWriter.setMaxMergeDocs(Integer.MAX_VALUE);
		    descWriter.setMaxBufferedDocs(1000);
		}
		
		IndexWriter writerCopy = descWriter;
		if (writerCopy != null) {
		    for (Description desc : descriptions) {
		        if (desc != null) {
		            writerCopy.deleteDocuments(new Term("dnid", Integer.toString(desc.getDescId())));
		            writerCopy.addDocument(DescriptionIndexGenerator.createDoc(desc));
		        }
		    }
		    writerCopy.commit();
		}
		
		if (descSearcher != null) {
		    descSearcher.close();
		    AceLog.getAppLog().info("Closing lucene desc Searcher");
		}
		descSearcher = null;
    }
}
