package org.ihtsdo.lucene;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.workflow.LuceneWfHxMatch;
import org.dwfa.ace.search.workflow.SearchWfHxWorker.LuceneWfHxProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;

public class WfHxCheckAndProcessLuceneMatch extends CheckAndProcessLuceneMatch {
	
	public WfHxCheckAndProcessLuceneMatch(CountDownLatch hitLatch,
			LuceneWfHxProgressUpdator updater, Document doc, float score,
			Collection<LuceneMatch> matches,
			List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config) {
		super(hitLatch, updater, doc, score, matches, checkList, config);
	}

	public void run() {
		LuceneMatch match = null;
	
		if (hitLatch.getCount() > 0) {
			String displayState = doc.get("lastState");
			String displayAction = doc.get("lastAction");
			String displayModeler = doc.get("lastModeler");
			Long displayTime = Long.parseLong(doc.get("lastTime"));
			String conceptId = doc.get("conceptId");
			String fsn = doc.get("fsn");

			WorkflowLuceneSearchResult val = new WorkflowLuceneSearchResult(displayAction, displayState, displayModeler, displayTime, conceptId, fsn);
			
            match = new LuceneWfHxMatch(val, score);
            matches.add(match);
		} 

		this.hitLatch.countDown();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Hit latch: " + this.hitLatch.getCount());
        }
    }
}