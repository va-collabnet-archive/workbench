package org.ihtsdo.cs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;

public class ChangeSetWriterHandler implements Runnable, I_ProcessUnfetchedConceptData, ActionListener {

   private static ConcurrentHashMap<String, ChangeSetGeneratorBI> writerMap = new ConcurrentHashMap<String, ChangeSetGeneratorBI>();
   public static AtomicInteger changeSetWriters = new AtomicInteger();
   private I_RepresentIdSet cNidsToWrite;
   private long commitTime;
   private String commitTimeStr;
   private NidSetBI sapNidsFromCommit;
   private int conceptCount;
   private I_ShowActivity activity;
   private long startTime = System.currentTimeMillis();
   private AtomicInteger processedCount = new AtomicInteger();
   private AtomicInteger processedChangedCount = new AtomicInteger();
   private int changedCount = Integer.MIN_VALUE;
   private ChangeSetWriterThreading changeSetWriterThreading;
   private ChangeSetGenerationPolicy changeSetPolicy;
   private Timer timer;
   private Semaphore permit;
   private List<ChangeSetGeneratorBI> writerListForHandler;
   
   public ChangeSetWriterHandler(I_RepresentIdSet cNidsToWrite,
           long commitTime, NidSetBI sapNidsFromCommit, ChangeSetGenerationPolicy changeSetPolicy,
           ChangeSetWriterThreading changeSetWriterThreading) {	
		
		super();
		Semaphore permit1 = new Semaphore(1, true);
		init(cNidsToWrite,commitTime,sapNidsFromCommit,changeSetPolicy,changeSetWriterThreading,permit1);
	}

   public ChangeSetWriterHandler(I_RepresentIdSet cNidsToWrite,
           long commitTime, NidSetBI sapNidsFromCommit, ChangeSetGenerationPolicy changeSetPolicy,
           ChangeSetWriterThreading changeSetWriterThreading,
           Semaphore permitIn) {
      super();
      init(cNidsToWrite,commitTime,sapNidsFromCommit,changeSetPolicy,changeSetWriterThreading,permitIn);
   }
   
   
private void init(I_RepresentIdSet cNidsToWrite,
           long commitTime, NidSetBI sapNidsFromCommit, ChangeSetGenerationPolicy changeSetPolicy,
           ChangeSetWriterThreading changeSetWriterThreading,
           Semaphore permitIn){
	
	assert commitTime != Long.MAX_VALUE;
    assert commitTime != Long.MIN_VALUE;
    this.permit = permitIn;
    this.cNidsToWrite = cNidsToWrite;
    changedCount = cNidsToWrite.cardinality();
    this.commitTime = commitTime;
    this.commitTimeStr = TimeUtil.formatDate(commitTime)
            + "; gVer: " + Bdb.gVersion.incrementAndGet()
            + " (" + cNidsToWrite.cardinality() + " concepts)";
    this.sapNidsFromCommit = sapNidsFromCommit;
    this.changeSetWriterThreading = changeSetWriterThreading;
    changeSetWriters.incrementAndGet();
    this.changeSetPolicy = changeSetPolicy;
    writerListForHandler = new ArrayList<ChangeSetGeneratorBI>(writerMap.values());	
}

   @Override
   public void run() {
      try {
         conceptCount = Bdb.getConceptDb().getCount();
         if (permit != null) {
 			AceLog.getAppLog().info("ChangeSetWriterHandler run permit != null avail = "+permit.availablePermits());
 			try {
 				permit.acquire();
 			} catch (InterruptedException e) {
 				AceLog.getAppLog().severe("ChangeSetWriterHandler.run InterruptedException thrown", e);
 				e.printStackTrace();
 				AceLog.getAppLog().alertAndLogException(e);
 			}
 		}
         else{
        	 AceLog.getAppLog().info("ChangeSetWriterHandler run permit = null");
         }

         activity = Terms.get().newActivityPanel(true, Terms.get().getActiveAceFrameConfig(), "CS writer: " + commitTimeStr + "...",
                 false);
         activity.setIndeterminate(true);
         activity.setProgressInfoUpper("CS writer: " + commitTimeStr + "...");
         activity.setProgressInfoLower("Opening change set writers...");
         timer = new Timer(2000, this);
         timer.start();
         activity.setStopButtonVisible(false);
         for (ChangeSetGeneratorBI writer : writerListForHandler) {
            writer.open(sapNidsFromCommit);
         }
         activity.setValue(0);
         activity.setMaximum(conceptCount);
         activity.setIndeterminate(false);

         activity.setProgressInfoLower("Iterating over concepts...");
         switch (changeSetWriterThreading) {
            case MULTI_THREAD:
               Bdb.getConceptDb().iterateConceptDataInParallel(this);
               break;
            case SINGLE_THREAD:
               Bdb.getConceptDb().iterateConceptDataInSequence(this);
               break;
            default:
               throw new RuntimeException("Can't handle threading: " + changeSetWriterThreading);
         }

         activity.setProgressInfoLower("Committing change set writers...");
         for (ChangeSetGeneratorBI writer : writerListForHandler) {
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
      } finally {
         if (permit != null) {
            permit.release();
         }
      }
   }

   public static void addWriter(String key, ChangeSetGeneratorBI writer) {
      writerMap.put(key, writer);
   }

   public static void removeWriter(String key) {
      writerMap.remove(key);
   }

   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
      if (cNidsToWrite.isMember(cNid)) {
         processedChangedCount.incrementAndGet();
         Concept c = (Concept) fcfc.fetch();
         for (ChangeSetGeneratorBI writer : writerListForHandler) {
            writer.setPolicy(changeSetPolicy);
            writer.writeChanges(c, commitTime);
         }
      }
   }

   @Override
   public boolean continueWork() {
      // user cannot cancel operation
      return true;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      int completed = processedCount.incrementAndGet();
      activity.setValue(completed);
      long endTime = System.currentTimeMillis();
      long elapsed = endTime - startTime;
      String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);

      String remainingStr = TimeUtil.getRemainingTimeString(completed, conceptCount, elapsed);

      activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Remaining: " + remainingStr
              + " processed: " + processedChangedCount + "/" + changedCount);
      if (activity.isCompleteForComparison()) {
         timer.stop();
      }
   }

   @Override
   public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
      // TODO Auto-generated method stub
   }

   @Override
   public NidBitSetBI getNidSet() {
      return cNidsToWrite;
   }
}
