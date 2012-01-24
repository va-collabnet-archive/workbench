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
	protected static File wfHxLuceneDirFile = new File("target" + File.separatorChar + "workflow" + File.separatorChar + "lucene");
	protected static File runningLuceneDirFile = new File("workflow" + File.separatorChar + "lucene");

	private static HashSet<WorkflowHistoryJavaBean> beansToAdd;

	public static void writeToLuceneNoLock(Collection<WorkflowHistoryJavaBean> beans, Map<UUID, WorkflowLuceneSearchResult> lastBeanInWfMap, ViewCoordinate viewCoord) throws IOException, TerminologyException {
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
        	
        	wfHxWriter.setUseCompoundFile(true);
		    wfHxWriter.setMergeFactor(15);
		    wfHxWriter.setMaxMergeDocs(Integer.MAX_VALUE);
		    wfHxWriter.setMaxBufferedDocs(1000);
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

		AceLog.getAppLog().log(Level.INFO, "Have written " + recordsImported + " workflow history lucene records");
		wfHxWriter.commit();

		if (wfHxSearcher != null) {
		    wfHxSearcher.close();
		}

		wfHxSearcher = null;
		beans.clear();
    }

	public static SearchResult searchAllWorkflowCriterion(List<I_TestSearchResults> checkList, boolean wfInProgress, boolean completedWf) throws Exception {
        WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
        
		if (searcher.isInitialized()) {
	        WfHxQueryParser wfHxParser = new WfHxQueryParser(checkList, wfInProgress, completedWf);
	        
	        // Create Query
	        Query wfQuery = wfHxParser.getStandardAnalyzerQuery();

	        // Search WfHx
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
	
	public static void addToLuceneNoWrite(WorkflowHistoryJavaBean latestWorkflow) {
		if (beansToAdd == null) {
			beansToAdd = new HashSet<WorkflowHistoryJavaBean>();
		}
		
		beansToAdd.add(latestWorkflow);
	}

	public static void writeUnwrittenWorkflows() throws IOException, TerminologyException {
		init(LuceneSearchType.WORKFLOW_HISTORY);

	    if (LuceneManager.indexExists(LuceneSearchType.WORKFLOW_HISTORY) != false) {
	    	if (runningLuceneDirFile.exists()) {
				writeToLuceneNoLock(beansToAdd, null, null);
				beansToAdd.clear();
	    	} else {
	            AceLog.getAppLog().severe("Cannot be here unless bundle is executed");
	    	}
	    }
	} 
}
