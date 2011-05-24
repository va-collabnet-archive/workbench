package org.ihtsdo.lucene;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.ace.task.workflow.search.AbstractWorkflowHistorySearchTest;
import org.ihtsdo.ace.task.workflow.search.ActionLastWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.ActionWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.ModelerLastWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.ModelerWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.PathWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.SemanticTagWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.StateLastWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.StateWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.TimestampAfterWorkflowHistory;
import org.ihtsdo.ace.task.workflow.search.TimestampBeforeWorkflowHistory;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfHxQueryParser {
	
	private final String actionKey =		"action";
	private final String stateKey = 		"state";
	private final String pathKey = 			"path";
	private final String modelerKey = 		"modeler";
	private final String timeKey = 			"time";
	private final String semTagKey = 		"semTag";
	private final String lastActionKey =	"lastAction";
	private final String lastStateKey = 	"lastState";
	private final String lastModelerKey = 	"lastModeler";
	
	private boolean wfInProgress;
	private boolean completedWf;
	private BooleanQuery standardQuery = null;
	private List<I_TestSearchResults> checkList = null;
	
	public WfHxQueryParser(List<I_TestSearchResults> checkList, boolean wfInProgress, boolean completedWf) throws ParseException, TaskFailedException {
		this.wfInProgress = wfInProgress;
		this.completedWf = completedWf;
		this.checkList = checkList;
		
		standardQuery = createStandardQuery();
	}

	private BooleanQuery createStandardQuery() {
		BooleanQuery booleanQuery = new BooleanQuery();

		try {
			// Create time-based filter
			BooleanClause timeClause = generateTimeBasedClause();
			
			// Handle workflow-in-progress and completed-workflow filter 
	    	BooleanClause stateOfWorkflowClause= generateStateOfWorkflowClause();
	    	
			
			// Create Time-Based Filter
			if (timeClause != null) {
				booleanQuery.add(timeClause);
			}
	    	
			if (stateOfWorkflowClause != null) {
				booleanQuery.add(stateOfWorkflowClause);
			}
	    	
	    	for (I_TestSearchResults crit : checkList) {
	    		BooleanClause clause = processCriterion(crit);
	    		
	    		if (clause != null) {
	    			booleanQuery.add(clause);
	    		}
	    	}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable in creating Wf Lucene Query with error: " + e.getMessage());
		}
    	
    	return booleanQuery;
	}

	private BooleanClause generateStateOfWorkflowClause() throws ParseException {
		if (wfInProgress && completedWf) {
			// Return all as signified by not adding another clause
			return null;
		} else {
			// Add clause to examining if workflowId's whose final state is ACCEPT 
			Query q = new QueryParser(LuceneManager.version, "lastState", new StandardAnalyzer(LuceneManager.version)).parse(WorkflowHelper.getApprovedState().toString());

			// if wfInProgress is selected, add 'NOT' directive to clause
			if (completedWf) {
	    		return new BooleanClause(q, Occur.MUST);
			} else {
	    		return new BooleanClause(q, Occur.MUST_NOT);
			}
		}
	}

	private BooleanClause generateTimeBasedClause() {
		Long beforeTimestamp = null;
		Long afterTimestamp = null;

    	for (I_TestSearchResults crit : checkList) {
			AbstractWorkflowHistorySearchTest wfCrit = (AbstractWorkflowHistorySearchTest)crit;
			
			if (wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.timestampBefore) {
				beforeTimestamp = ((TimestampBeforeWorkflowHistory)wfCrit).getTimestampAsLong(); 
			}

			if (wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.timestampAfter) {
				afterTimestamp = ((TimestampAfterWorkflowHistory)wfCrit).getTimestampAsLong(); 
			}
    	}
    	
    	if (beforeTimestamp != null || afterTimestamp != null) {
			 Query q = NumericRangeQuery.newLongRange(timeKey, afterTimestamp, beforeTimestamp, true, true);

    		return new BooleanClause(q, Occur.MUST);
    	}
    	
    	return null;
 	}

	private BooleanClause processCriterion(I_TestSearchResults crit) throws ParseException, TaskFailedException {
		AbstractWorkflowHistorySearchTest wfCrit = (AbstractWorkflowHistorySearchTest)crit;
		
		// Already added
		if (wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.timestampBefore || 
			wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.timestampAfter) { 
			return null; 
		}
		
		if (wfInProgress && !completedWf &&
			wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.currentState &&
			identifyVal(wfCrit).equals(WorkflowHelper.getApprovedState().toString())) {
			// MUST-NOT on currentState == APPROVED -- Therefore request for last state must not be approve
			throw new ParseException("LastState requested and val != ACCEPT while wfInProgress && !completedWfs"); 
		}

		if (wfInProgress && !completedWf &&
			wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.currentAction &&
			identifyVal(wfCrit).equals(WorkflowHelper.getAcceptAction().toString())) {
			// MUST-NOT on currentAction == ACCEPT -- Therefore request for last action must not be accept
			throw new ParseException("LastAction requested and val = APPROVATED while wfInProgress && !completedWfs"); 
		}
	

		if (!wfInProgress && completedWf &&
			wfCrit.getTestType() == AbstractWorkflowHistorySearchTest.currentState &&
			!identifyVal(wfCrit).equals(WorkflowHelper.getAcceptAction().toString())) {
			// MUST on currentState -- Therefore request for last state must be other than approved 
			throw new ParseException("LastState requested and val != APPROVATED while !wfInProgress && completedWfs"); 
		}
		
		String key = identifyKey(wfCrit);
		String val = identifyVal(wfCrit);
		
		Query q = new QueryParser(LuceneManager.version, key, new StandardAnalyzer(LuceneManager.version)).parse(val);
		
		return new BooleanClause(q, Occur.MUST);
	}
	
	public Query searchByWfId(UUID workflowId) throws ParseException {
        return new QueryParser(LuceneManager.version, "workflowId", new StandardAnalyzer(LuceneManager.version)).parse(workflowId.toString());
	}

	private String identifyKey(AbstractWorkflowHistorySearchTest wfCrit) {
		switch (wfCrit.getTestType()) {
		case AbstractWorkflowHistorySearchTest.hasModeler:
			return modelerKey;
		case AbstractWorkflowHistorySearchTest.currentModeler:
			return lastModelerKey;
		case AbstractWorkflowHistorySearchTest.hasAction:
			return actionKey;
		case AbstractWorkflowHistorySearchTest.currentAction:
			return lastActionKey;
		case AbstractWorkflowHistorySearchTest.hasState:
			return stateKey;
		case AbstractWorkflowHistorySearchTest.currentState:
			return lastStateKey;
		case AbstractWorkflowHistorySearchTest.semTag:
			return semTagKey;
		case AbstractWorkflowHistorySearchTest.path:
			return pathKey;
	
		default:
			return null;
		}
	}

	private String identifyVal(AbstractWorkflowHistorySearchTest wfCrit) throws TaskFailedException {
		switch (wfCrit.getTestType()) {
		case AbstractWorkflowHistorySearchTest.hasModeler:
			return ((ModelerWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		case AbstractWorkflowHistorySearchTest.currentModeler:
			return ((ModelerLastWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		case AbstractWorkflowHistorySearchTest.hasAction:
			return ((ActionWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		case AbstractWorkflowHistorySearchTest.currentAction:
			return ((ActionLastWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		case AbstractWorkflowHistorySearchTest.hasState:
			return ((StateWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		case AbstractWorkflowHistorySearchTest.currentState:
			return ((StateLastWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		case AbstractWorkflowHistorySearchTest.semTag:
			return WorkflowHelper.parseSpaces(((SemanticTagWorkflowHistory)wfCrit).getTestHierarchy());
		case AbstractWorkflowHistorySearchTest.path:
			return ((PathWorkflowHistory)wfCrit).getCurrentTestUUID().toString();
		default:
			return null;

		}
	}

	public Query getStandardAnalyzerQuery() {
		return standardQuery;
	}
}
