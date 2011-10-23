package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.WorkflowHistoryRefsetSearcher;

public class WfHxLuceneManager extends LuceneManager {


    static final String wfLuceneFileSuffix = "lucene";
	protected static File wfHxLuceneDirFile = new File("target/workflow/lucene");
	protected static File runningLuceneDirFile = new File("workflow/lucene");
	private static UUID workflowIdToUpdate = null;
	public final static int matchLimit = 10000000;

	public static void writeToLuceneNoLock(Collection<WorkflowHistoryJavaBean> beans, ViewCoordinate viewCoord) throws CorruptIndexException, IOException {
        int idx = beans.size() - 1;

        if (workflowIdToUpdate == null) {
			throw new CorruptIndexException("Should have workflowIdToUpdate set, but is null");
		}
		if (wfHxWriter == null) {
		    wfHxLuceneDir = setupWriter(wfHxLuceneDirFile, wfHxLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);
		    wfHxWriter.setUseCompoundFile(true);
		    wfHxWriter.setMergeFactor(15);
		    wfHxWriter.setMaxMergeDocs(Integer.MAX_VALUE);
		    wfHxWriter.setMaxBufferedDocs(1000);
		}
		
		IndexWriter writerCopy = wfHxWriter;
		if (writerCopy != null) {
			writerCopy.deleteDocuments(new Term("workflowId", workflowIdToUpdate.toString()));
            workflowIdToUpdate = null;

			
			if (idx >= 0) {
	            WorkflowHistoryJavaBean lastBean = ((WorkflowHistoryJavaBean)beans.toArray()[idx]);
	            WorkflowLuceneSearchResult vals = new WorkflowLuceneSearchResult(lastBean);
	            WfHxIndexGenerator.initializeSemTags(viewCoord);

	            for (WorkflowHistoryJavaBean bean : beans) {
			        writerCopy.addDocument(WfHxIndexGenerator.createDoc(bean, vals));
			    }
			}

			writerCopy.commit();
		}
		
		if (wfHxSearcher != null) {
		    wfHxSearcher.close();
		    AceLog.getAppLog().info("Closing lucene wfHxSearcher");
		}
		wfHxSearcher = null;
    }

	public static boolean refsetMemberIdExists(UUID memberId) throws CorruptIndexException, IOException, ParseException {
        
		WfHxQueryParser wfHxParser = new WfHxQueryParser(true, true);
		Query wfQuery = wfHxParser.getRefsetMemberIdQuery(memberId.toString());
        
        SearchResult result = LuceneManager.search(wfQuery, LuceneSearchType.WORKFLOW_HISTORY);
		
		return result.topDocs.totalHits != 0;
	}
	
	public static SearchResult searchAllWorkflowCriterion(List<I_TestSearchResults> checkList, boolean wfInProgress, boolean completedWf) throws Exception {
        WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
        
		if (searcher.isInitialized()) {
	        WfHxQueryParser wfHxParser = new WfHxQueryParser(checkList, wfInProgress, completedWf);
	    	
	        Query wfQuery = wfHxParser.getStandardAnalyzerQuery();
	

	        SearchResult result = LuceneManager.search(wfQuery, LuceneSearchType.WORKFLOW_HISTORY);
	        if (result.topDocs.totalHits > 0) {
	            AceLog.getAppLog().info("StandardAnalyzer query returned " + result.topDocs.totalHits + " hits");
	        } else {
	            AceLog.getAppLog().info("StandardAnalyzer query returned empty results.");
	        }
	        
	        return result;
        } else {
            TopDocs emptyDocs = new TopDocs(0, new ScoreDoc[0], 0);

            return new SearchResult(emptyDocs, null);
        }
	}

	public static void setWorkflowId(UUID workflowId) {
		workflowIdToUpdate  = workflowId;
	}
}
