package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfHxLuceneManager extends LuceneManager {


    static final String wfLuceneFileSuffix = "lucene";
	protected static File wfHxLuceneDirFile = new File("target/workflow/lucene");
	protected static File runningLuceneDirFile = new File("workflow/lucene");
	private static HashSet<WorkflowHistoryJavaBean> beansToAdd;
	public final static int matchLimit = 10000000;

	public static void writeToLuceneNoLock(Collection<WorkflowHistoryJavaBean> beans, ViewCoordinate viewCoord) throws IOException, TerminologyException {
        if (wfHxWriter == null) {
		    wfHxLuceneDir = setupWriter(wfHxLuceneDirFile, wfHxLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);
		    wfHxWriter.setUseCompoundFile(true);
		    wfHxWriter.setMergeFactor(15);
		    wfHxWriter.setMaxMergeDocs(Integer.MAX_VALUE);
		    wfHxWriter.setMaxBufferedDocs(1000);
		}
		
		if (wfHxWriter != null) {
			Set<UUID> processedIds = new HashSet<UUID>();
            WfHxIndexGenerator.initializeSemTags(viewCoord);
            WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();

            if (searcher.isInitialized()) {
				for (WorkflowHistoryJavaBean bean : beans) {
					if (!processedIds.contains(bean.getWorkflowId())) {
						wfHxWriter.deleteDocuments(new Term("workflowId", bean.getWorkflowId().toString()));
						processedIds.add(bean.getWorkflowId());
						I_GetConceptData con = Terms.get().getConcept(bean.getConcept());
						
						Set<WorkflowHistoryJavaBean> wfIdBeans = searcher.getAllHistoryForWorkflowId(con, bean.getWorkflowId());
						WorkflowHistoryJavaBean lastBean;
						try {
							lastBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(con, bean.getWorkflowId());
				            WorkflowLuceneSearchResult vals = new WorkflowLuceneSearchResult(lastBean);
	
				            wfHxWriter.addDocument(WfHxIndexGenerator.createDoc(bean, vals));
						} catch (TerminologyException e) {
						    AceLog.getAppLog().log(Level.WARNING, "Failed adding WfId: " + bean.getWorkflowId() + " for concept: " + bean.getFSN() + " to lucene index");
						}
					
					}
				}
            }
            
			wfHxWriter.commit();
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

	public static void addToLuceneNoWrite(SortedSet<WorkflowHistoryJavaBean> latestWorkflow) {
		if (beansToAdd == null) {
			beansToAdd = new HashSet<WorkflowHistoryJavaBean>();
		}
		
		beansToAdd.addAll(latestWorkflow);
	}
	
	public static void writeUnwrittenWorkflows(ViewCoordinate vc) throws IOException, TerminologyException {
		writeToLuceneNoLock(beansToAdd, vc);
	}
}
