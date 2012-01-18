package org.ihtsdo.cs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.concept.component.refsetmember.array.bytearray.ArrayOfBytearrayMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;

public class ChangeSetWriterHandler implements Runnable, I_ProcessUnfetchedConceptData, ActionListener {

    private static ConcurrentHashMap<String, ChangeSetGeneratorBI> writerMap = new ConcurrentHashMap<String, ChangeSetGeneratorBI>();
    public static AtomicInteger changeSetWriters = new AtomicInteger();
    public static boolean writeCommitRecord = false;
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
    private int commitRecordSapNid;
    private int commitRecRefsetNid;

    public ChangeSetWriterHandler(I_RepresentIdSet cNidsToWrite,
            long commitTime, NidSetBI sapNidsFromCommit, ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading,
            Semaphore permit) throws ValidationException, IOException {
        super();
        assert commitTime != Long.MAX_VALUE;
        assert commitTime != Long.MIN_VALUE;
        this.permit = permit;
        this.cNidsToWrite = cNidsToWrite;
        changedCount = cNidsToWrite.cardinality();
        this.commitTime = commitTime;
        this.commitTimeStr = TimeHelper.formatDate(commitTime)
                + "; gVer: " + Bdb.gVersion.incrementAndGet()
                + " (" + cNidsToWrite.cardinality() + " concepts)";
        this.sapNidsFromCommit = sapNidsFromCommit;
        this.changeSetWriterThreading = changeSetWriterThreading;
        changeSetWriters.incrementAndGet();
        this.changeSetPolicy = changeSetPolicy;
        writerListForHandler = new ArrayList<ChangeSetGeneratorBI>(writerMap.values());
        if (writeCommitRecord) {
            commitRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.COMMIT_RECORD.getUids());
            for (int sapNid : sapNidsFromCommit.getSetValues()) {
                if (Bdb.getSapDb().getAuthorNid(sapNid) != Integer.MIN_VALUE
                        && Bdb.getSapDb().getPathNid(sapNid) != Integer.MIN_VALUE) {
                    int statusNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
                    this.commitRecordSapNid = Bdb.getSapNid(statusNid, Bdb.getSapDb().getAuthorNid(sapNid),
                            Bdb.getSapDb().getPathNid(sapNid),
                            commitTime);
                    break;
                }
            }
            
        }
    }

    @Override
    public void run() {
        try {
            conceptCount = Bdb.getConceptDb().getCount();

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
            String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

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

            Set<byte[]> authorTimeHash = new HashSet<byte[]>();
            if (writeCommitRecord) {
                for (Integer sap : c.getAllSapNids()) {
                    if (sap > Bdb.getSapDb().getReadOnlyMax()) {
                        Concept authorConcept = Concept.get(Bdb.getSapDb().getAuthorNid(sap));
                        long time = Bdb.getSapDb().getTime(sap);
                        String stringToHash = authorConcept.getPrimUuid().toString() + Long.toString(time);
                        UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID, stringToHash);
                        authorTimeHash.add(Type5UuidFactory.getRawBytes(type5Uuid));
                    }
                }
                if (!authorTimeHash.isEmpty()) {
                    byte[][] arrayOfAuthorTime = new byte[authorTimeHash.size()][];
                    Iterator<byte[]> authorTimeHashItr = authorTimeHash.iterator();
                    for (int i = 0; i < arrayOfAuthorTime.length; i++) {
                        arrayOfAuthorTime[i] = authorTimeHashItr.next();
                    }

                    ArrayOfBytearrayMember newCommitRecord = new ArrayOfBytearrayMember();
                    UUID primoridalUuid = UUID.randomUUID();
                    newCommitRecord.nid = Bdb.uuidToNid(primoridalUuid);
                    Bdb.getNidCNidMap().setCNidForNid(c.getConceptNid(), newCommitRecord.nid);
                    newCommitRecord.setPrimordialUuid(primoridalUuid);
                    newCommitRecord.refsetNid = commitRecRefsetNid;
                    newCommitRecord.enclosingConceptNid = c.getConceptNid();
                    newCommitRecord.referencedComponentNid = c.getConceptNid();
                    newCommitRecord.setArrayOfByteArray(arrayOfAuthorTime);
                    newCommitRecord.primordialSapNid = this.commitRecordSapNid;

                    c.addAnnotation(newCommitRecord);
                    Bdb.getConceptDb().writeConcept(c);
                }
            }
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
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

        String remainingStr = TimeHelper.getRemainingTimeString(completed, conceptCount, elapsed);

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
