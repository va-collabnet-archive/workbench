package org.ihtsdo.cs;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.time.TimeUtil;

public class ChangeSetWriterHandler implements Runnable, I_ProcessUnfetchedConceptData {

	private static CopyOnWriteArraySet<I_WriteChangeSet> writers = new CopyOnWriteArraySet<I_WriteChangeSet>();
	public static AtomicInteger changeSetWriters = new AtomicInteger();

	private I_RepresentIdSet cNidsToWrite;
    private long commitTime;
    private String commitTimeStr;
	private IntSet sapNidsFromCommit;
    private int conceptCount;
    private ActivityPanel activity;
    private long startTime = System.currentTimeMillis();
    private AtomicInteger processedCount = new AtomicInteger();

	public ChangeSetWriterHandler(I_RepresentIdSet cNidsToWrite,
			long commitTime, IntSet sapNidsFromCommit) {
		super();
		assert commitTime != Long.MAX_VALUE;
		assert commitTime != Long.MIN_VALUE;
		this.cNidsToWrite = cNidsToWrite;
		this.commitTime = commitTime;
		this.commitTimeStr = TimeUtil.formatDate(commitTime);
		this.sapNidsFromCommit = sapNidsFromCommit;
		changeSetWriters.incrementAndGet();
	}

	@Override
	public void run() {
		try {
	        conceptCount = Bdb.getConceptDb().getCount();

	        activity = new ActivityPanel(true, null, null);
	        activity.setIndeterminate(true);
	        activity.setProgressInfoUpper("Writing change sets for: " + commitTimeStr + "...");
	        activity.setProgressInfoLower("Opening change set writers...");
	        activity.getStopButton().setVisible(false);
	        ActivityViewer.addActivity(activity);
			for (I_WriteChangeSet writer : writers) {
				writer.open(sapNidsFromCommit);
			}
            activity.setValue(0);
            activity.setMaximum(conceptCount);
            activity.setIndeterminate(false);

            activity.setProgressInfoLower("Iterating over concepts...");
            Bdb.getConceptDb().iterateConceptDataInParallel(this);

            activity.setProgressInfoLower("Committing change set writers...");
            for (I_WriteChangeSet writer : writers) {
                writer.commit();
            }
            long endTime = System.currentTimeMillis();

            long elapsed = endTime - startTime;
            String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);

            activity.setProgressInfoUpper("Change sets written for: " + commitTimeStr);
            activity.setProgressInfoLower("Elapsed: " + elapsedStr);
            activity.complete();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public static void addWriter(I_WriteChangeSet writer) {
		writers.add(writer);
	}

	public static void removeWriter(I_WriteChangeSet writer) {
		writers.remove(writer);
	}

    @Override
    public void processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc) throws Exception {
        if (cNidsToWrite.isMember(cNid)) {
            Concept c = fcfc.fetch();
            for (I_WriteChangeSet writer: writers) {
                writer.writeChanges(c, commitTime);
            }
        }
        int completed = processedCount.incrementAndGet();
        if (completed % 5000 == 0) {
            activity.setValue(completed);
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);

            String remainingStr = TimeUtil.getRemainingTimeString(completed, conceptCount, elapsed);

            activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Remaining: " + remainingStr);
        }
    }

    @Override
    public boolean continueWork() {
        // user cannot cancel operation
        return true;
    }

}
