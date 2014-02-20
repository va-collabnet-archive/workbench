package org.ihtsdo.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.lucene.index.Term;
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
	public final static int matchLimit = 10000000;
    static final String wfLuceneFileSuffix = "lucene";
	public static File wfHxLuceneDirFile = new File("target/workflow/lucene");
	protected static File runningLuceneDirFile = new File("workflow/lucene");

	private static HashSet<WorkflowHistoryJavaBean> beansToAdd;

	public static int writeToLuceneNoLock(Collection<WorkflowHistoryJavaBean> beans, Map<UUID, WorkflowLuceneSearchResult> lastBeanInWfMap, ViewCoordinate viewCoord) throws IOException, TerminologyException {
		int recordsImported = 0;
		Set<UUID> processedIds = new HashSet<UUID>();
        WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
        WorkflowHistoryJavaBean currentBean = null;

        if (wfHxWriter == null) {
        	if (runningLuceneDirFile.exists()) {
        		wfHxLuceneDir = setupWriter(runningLuceneDirFile, wfHxLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);
        	} else {
        		wfHxLuceneDir = setupWriter(wfHxLuceneDirFile, wfHxLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);
        	}
	}
        
        WfHxIndexGenerator.initializeSemTags(viewCoord);

        try {
            if (searcher.isInitialized()) {
				for (WorkflowHistoryJavaBean bean : beans) {
					currentBean = bean;
					
					if (!processedIds.contains(bean.getWorkflowId())) {
						processedIds.add(bean.getWorkflowId());
						
						// Delete existing records for workflowId and regenerate
						wfHxWriter.deleteDocuments(new Term("workflowId", bean.getWorkflowId().toString()));
	
						// Get all WfHx for WfId
				    	I_GetConceptData con = Terms.get().getConcept(bean.getConcept());
						Set<WorkflowHistoryJavaBean> wfIdBeans = searcher.getAllHistoryForWorkflowId(con, bean.getWorkflowId());
						
						// Get the latest Wf entry for Wf
						WorkflowLuceneSearchResult lastBeanVals = null;
						if (lastBeanInWfMap != null) {
							lastBeanVals = lastBeanInWfMap.get(bean.getWorkflowId());
						} else {
							WorkflowHistoryJavaBean lastBean = WorkflowHelper.getLatestWfHxJavaBeanForWorkflowId(con, bean.getWorkflowId());
							if (lastBean == null) {
								lastBean = bean;
							} 

							// Add all workflow Id beans as lucene document
							lastBeanVals = new WorkflowLuceneSearchResult(lastBean);
						}
							
						// Add all workflow Id beans as lucene document
			            if (lastBeanVals != null) {
				            for (WorkflowHistoryJavaBean beanToIndex : wfIdBeans) {
				            	recordsImported++;
				            	wfHxWriter.addDocument(WfHxIndexGenerator.createDoc(beanToIndex, lastBeanVals));
				            }
			            } 
					}
				}
	        }
        } catch (Exception e) {
			AceLog.getAppLog().warning("Failed on bean: " + currentBean);
        }

		AceLog.getAppLog().log(Level.INFO, "Have written " + recordsImported + " workflow history lucene records to " + wfHxWriter.getDirectory());
		beans.clear();
                return recordsImported;
    }

	public static SearchResult searchAllWorkflowCriterion(List<I_TestSearchResults> checkList, boolean wfInProgress, boolean completedWf) throws Exception {
        WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
        
		if (searcher.isInitialized()) {
	        WfHxQueryParser wfHxParser = new WfHxQueryParser(checkList, wfInProgress, completedWf);
	        
	        // Create Query
	        Query wfQuery = wfHxParser.getStandardAnalyzerQuery();

	        if (wfQuery != null) {
		        // Search WfHx
		        SearchResult result = LuceneManager.search(wfQuery, LuceneSearchType.WORKFLOW_HISTORY);
	
		        if (result.topDocs.totalHits > 0) {
		            AceLog.getAppLog().info("StandardAnalyzer query returned " + result.topDocs.totalHits + " hits");
		        } else {
		            AceLog.getAppLog().info("StandardAnalyzer query returned empty results.");
		        }
		        
		        return result;
	        }
		}

		// If searcher not initialize or Query is empty, return 0 results
        TopDocs emptyDocs = new TopDocs(0, new ScoreDoc[0], 0);
        return new SearchResult(emptyDocs, null);
	}
	
	public static void addToLuceneNoWrite(WorkflowHistoryJavaBean latestWorkflow) {
		if (beansToAdd == null) {
			beansToAdd = new HashSet<WorkflowHistoryJavaBean>();
		}
		
		beansToAdd.add(latestWorkflow);
	}

	public static int writeUnwrittenWorkflows() throws IOException, TerminologyException {
		init(LuceneSearchType.WORKFLOW_HISTORY);

	    if (LuceneManager.indexExists(LuceneSearchType.WORKFLOW_HISTORY) != false) {
		int records = writeToLuceneNoLock(beansToAdd, null, null);
		beansToAdd.clear();
                return records;
	    }
            return 0;
	} 
}
