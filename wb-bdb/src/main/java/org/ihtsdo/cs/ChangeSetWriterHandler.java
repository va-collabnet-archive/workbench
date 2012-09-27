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
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.concept.component.refsetmember.array.bytearray.ArrayOfBytearrayMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.sap.STAMP;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.helper.version.RelativePositionComputer;
import org.ihtsdo.helper.version.RelativePositionComputerBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;

public class ChangeSetWriterHandler implements Runnable, I_ProcessUnfetchedConceptData, ActionListener {

    private static ConcurrentHashMap<String, ChangeSetGeneratorBI> writerMap = new ConcurrentHashMap<String, ChangeSetGeneratorBI>();
    public static AtomicInteger changeSetWriters = new AtomicInteger();
    public static boolean writeCommitRecord = false;
    public boolean writeAdjudicationRecord = false;
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
    private int commitRecordSapNid = 0;
    private int commitRecRefsetNid = 0;
    private int adjudicationRecordSapNid = 0;
    private int adjudicationRecRefsetNid = 0;

    public ChangeSetWriterHandler(I_RepresentIdSet cNidsToWrite,
            long commitTime, NidSetBI sapNidsFromCommit, ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading,
            Semaphore permit, boolean writeAdjudicationRecord) throws ValidationException, IOException {
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
        this.writeAdjudicationRecord = writeAdjudicationRecord;
        writerListForHandler = new ArrayList<ChangeSetGeneratorBI>(writerMap.values());
        int statusNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
        if (writeCommitRecord) {
            commitRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.COMMIT_RECORD.getUids());
            for (int sapNid : sapNidsFromCommit.getSetValues()) {
                if (Bdb.getSapDb().getAuthorNid(sapNid) != Integer.MIN_VALUE
                        && Bdb.getSapDb().getPathNid(sapNid) != Integer.MIN_VALUE) {
                    
                    this.commitRecordSapNid = Bdb.getSapNid(statusNid,
                            commitTime,
                            Bdb.getSapDb().getAuthorNid(sapNid),
                            Bdb.getSapDb().getModuleNid(sapNid),
                            Bdb.getSapDb().getPathNid(sapNid));
                    break;
                }
            }
            
            this.sapNidsFromCommit.add(this.commitRecordSapNid);
        }
        if (writeAdjudicationRecord) {
            adjudicationRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.ADJUDICATION_RECORD.getUids());
            for (int sapNid : sapNidsFromCommit.getSetValues()) {
                if (Bdb.getSapDb().getAuthorNid(sapNid) != Integer.MIN_VALUE
                        && Bdb.getSapDb().getPathNid(sapNid) != Integer.MIN_VALUE) {
                    int authorNid = Bdb.getSapDb().getAuthorNid(sapNid);
                    int pathNid = Bdb.getSapDb().getPathNid(sapNid);
                    int moduleNid = Bdb.getSapDb().getModuleNid(sapNid);
                    if (authorNid == 0 || pathNid == 0) {
                        System.out.println("Bad SAP: " + sapNid + " author:" + authorNid + " path: " + pathNid);
                    }
                    this.adjudicationRecordSapNid = Bdb.getSapNid(statusNid, commitTime, authorNid, moduleNid, pathNid);
                    break;
                }
            }
            this.sapNidsFromCommit.add(this.adjudicationRecordSapNid);
        }
    }

    @Override
    public void run() {
        try {
            conceptCount = Bdb.getConceptDb().getCount();

            activity = Terms.get().newActivityPanel(true, Terms.get().getActiveAceFrameConfig(),
                    "CS writer: " + commitTimeStr + "...",
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
            
            Set<Integer> editPathNids = new HashSet<Integer>(); //used by both commit and adj
            Set<UUID> authorTimeHashSet = new HashSet<UUID>(); // :WAS:byte[]
            if (writeCommitRecord && commitRecordSapNid != 0) {
                
                Set<Integer> allConceptStampNids = c.getAllStampNids();
                Set<Integer> conceptStampNids = new HashSet<Integer>();
                for(int stampNid : sapNidsFromCommit.getSetValues()){
                    if(allConceptStampNids.contains(stampNid)){
                        conceptStampNids.add(stampNid);
                    }
                }
                
                for(int stamp : conceptStampNids){
                    int editPathNid = Bdb.getSapDb().getPathNid(stamp);
                    editPathNids.add(editPathNid);
                }
                
                for(int editPathNid : editPathNids){
                    PositionBI viewPosition = Ts.get().newPosition(Ts.get().getPath(editPathNid),
                    Bdb.getSapDb().getTime(commitRecordSapNid));
                    RelativePositionComputerBI mapper = RelativePositionComputer.getComputer(viewPosition);
                    
                    for (Integer stamp : c.getAllStampNids()) {
                        if (stamp > Bdb.getSapDb().getReadOnlyMax() && Bdb.getSapDb().getTime(stamp) != Long.MAX_VALUE) {
                            if(mapper.onRoute(new STAMP(stamp))){
                                Concept authorConcept = Concept.get(Bdb.getSapDb().getAuthorNid(stamp));
                                if (authorConcept.getNid() != ReferenceConcepts.SNOROCKET.getNid()) {
                                    long time = Bdb.getSapDb().getTime(stamp);
                                    String stringToHash = authorConcept.getPrimUuid().toString()
                                            + Long.toString(time);
                                    UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID,
                                            stringToHash);
                                    authorTimeHashSet.add(type5Uuid);
                                }
                            }
                        }
                    }
                }
                
                if (!authorTimeHashSet.isEmpty()) {
                    byte[][] arrayOfAuthorTime = new byte[authorTimeHashSet.size()][];
                    UUID[] atUuidArray = authorTimeHashSet.toArray(new UUID[authorTimeHashSet.size()]);
                    for (int i = 0; i < arrayOfAuthorTime.length; i++) {
                        arrayOfAuthorTime[i] = Type5UuidFactory.getRawBytes(atUuidArray[i]);
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
            if (writeAdjudicationRecord && adjudicationRecordSapNid != 0) {
                boolean hasRecord = false;
                for(RefexChronicleBI refex : c.getRefexMembers(adjudicationRecRefsetNid)){
                    Set<Integer> allSapNids = refex.getAllStampNids();
                    for(int sapNid : allSapNids){
                        if(sapNid == adjudicationRecordSapNid){
                            hasRecord = true;
                            break;
                        }
                    }
                }
                if(!hasRecord){
                    for(int editPathNid : editPathNids){
                        PositionBI viewPosition = Ts.get().newPosition(Ts.get().getPath(editPathNid),
                        Bdb.getSapDb().getTime(commitRecordSapNid));
                        RelativePositionComputerBI mapper = RelativePositionComputer.getComputer(viewPosition);
                        for (Integer stamp : c.getAllStampNids()) {
                            if (stamp > Bdb.getSapDb().getReadOnlyMax()) {
                                if(mapper.onRoute(new STAMP(stamp))){
                                    Concept authorConcept = Concept.get(Bdb.getSapDb().getAuthorNid(stamp));
                                    if (authorConcept.getNid() != ReferenceConcepts.SNOROCKET.getNid()) {
                                        long time = Bdb.getSapDb().getTime(stamp);
                                        String stringToHash = authorConcept.getPrimUuid().toString()
                                            + Long.toString(time);
                                        UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID,
                                            stringToHash);
                                        authorTimeHashSet.add(type5Uuid);
                                    }
                                }
                            }
                        }
                    }
                    
                    if (!authorTimeHashSet.isEmpty()) {
                        byte[][] arrayOfAuthorTime = new byte[authorTimeHashSet.size()][];
                        UUID[] atUuidArray = authorTimeHashSet.toArray(new UUID[authorTimeHashSet.size()]);
                        for (int i = 0; i < arrayOfAuthorTime.length; i++) {
                            arrayOfAuthorTime[i] = Type5UuidFactory.getRawBytes(atUuidArray[i]);
                        }

                        ArrayOfBytearrayMember newAdjudicationRecord = new ArrayOfBytearrayMember();
                        UUID primoridalUuid = UUID.randomUUID();
                        newAdjudicationRecord.nid = Bdb.uuidToNid(primoridalUuid);
                        Bdb.getNidCNidMap().setCNidForNid(c.getConceptNid(), newAdjudicationRecord.nid);
                        newAdjudicationRecord.setPrimordialUuid(primoridalUuid);
                        newAdjudicationRecord.refsetNid = adjudicationRecRefsetNid;
                        newAdjudicationRecord.enclosingConceptNid = c.getConceptNid();
                        newAdjudicationRecord.referencedComponentNid = c.getConceptNid();
                        newAdjudicationRecord.setArrayOfByteArray(arrayOfAuthorTime);
                        newAdjudicationRecord.primordialSapNid = this.adjudicationRecordSapNid;

                        c.addAnnotation(newAdjudicationRecord);
                        Bdb.getConceptDb().writeConcept(c);
                    }
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
