package org.ihtsdo.db.bdb;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.svn.Svn;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.arena.contradiction.ContradictionEditorFrame;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.db.change.BdbCommitSequence;
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.lucene.WfHxLuceneWriterAccessor;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Frame;
import java.beans.PropertyVetoException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.lang.reflect.InvocationTargetException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.lucene.index.IndexNotFoundException;
import org.ihtsdo.arena.conceptview.ConceptTemplates;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;

public class BdbCommitManager {

    private static final int PERMIT_COUNT = 50;
    private static final int DATACHECK_PERMIT_COUNT = 300;
    public static String pluginRoot = "plugins";
    private static final AtomicInteger writerCount = new AtomicInteger(0);
    private static boolean writeChangeSets = true;
    private static Set<I_ExtendByRef> uncommittedWfMemberIds = new HashSet<I_ExtendByRef>();
    private static I_RepresentIdSet uncommittedDescNids = new IdentifierSet();
    private static I_RepresentIdSet uncommittedCNidsNoChecks = new IdentifierSet();
    private static I_RepresentIdSet uncommittedCNids = new IdentifierSet();
    private static boolean performCreationTests = true;
    private static boolean performCommitTests = true;
    private static Semaphore luceneWriterPermit = new Semaphore(PERMIT_COUNT);
    private static long lastDoUpdate = Long.MIN_VALUE;
    private static long lastCommit = Bdb.gVersion.incrementAndGet();
    private static long lastCancel = Integer.MIN_VALUE;
    private static Semaphore dbWriterPermit = new Semaphore(PERMIT_COUNT);
    private static Semaphore dbCheckerPermit = new Semaphore(DATACHECK_PERMIT_COUNT);
    private static Semaphore dbCheckerPermit2 = new Semaphore(DATACHECK_PERMIT_COUNT);
    private static ReentrantReadWriteLock dataCheckLock = new ReentrantReadWriteLock();
    private static List<I_TestDataConstraints> creationTests =
            new ArrayList<I_TestDataConstraints>();
    private static List<I_TestDataConstraints> commitTests =
            new ArrayList<I_TestDataConstraints>();
    private static ThreadGroup commitManagerThreadGroup =
            new ThreadGroup("commit manager threads");
    private static ExecutorService changeSetWriterService;
    private static ExecutorService dbWriterService;
    private static ExecutorService dbCheckerService;
    private static ExecutorService luceneWriterService;
    /**
     * <p> listeners </p>
     */
    private static ICommitListener[] listeners = new ICommitListener[0];
    //J-
    private static ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap =
            new ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();
    //J+

    //~--- static initializers -------------------------------------------------
    static {
        reset();
    }

    //~--- methods -------------------------------------------------------------
    public static void addUncommitted(ConceptChronicleBI igcd) {
        if (igcd == null) {
            return;
        }
        
        Concept concept = (Concept) igcd;

        ChangeNotifier.touch(concept);
        dataCheckMap.remove(concept);
        GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.ADD_UNCOMMITTED, null, concept);
        
        if (concept.isUncommitted() == false) {
            if (Bdb.watchList.containsKey(concept.getNid())) {
                AceLog.getAppLog().info("--- Removing uncommitted concept: " + concept.getNid() + " --- ");
            }
            
            ConceptTemplates.dataChecks.put(concept.getNid(), false);
            Ts.get().touchComponentAlert(concept.getNid());
            
            removeUncommitted(concept);

            try {
                dbWriterPermit.acquire();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            dbWriterService.execute(new SetNidsForCid(concept));
            dbWriterService.execute(new ConceptWriter(concept));

            return;
        }

        concept.modified();

        if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info("---@@@ Adding uncommitted concept: " + concept.getNid() + " ---@@@ ");
        }
       ReadLock readLock = dataCheckLock.readLock();
        try {
            readLock.lock();
            uncommittedCNids.setMember(concept.getNid());
            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(concept));
            dbWriterService.execute(new ConceptWriter(concept));
            dbCheckerPermit.acquire();
            dbCheckerPermit2.acquire();
            dbCheckerService.execute(new DataChecker(concept));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally{
            dbCheckerPermit.acquireUninterruptibly(DATACHECK_PERMIT_COUNT);
            readLock.unlock();
            dbCheckerPermit.release(DATACHECK_PERMIT_COUNT);
        }
    }
    
    private static class DataChecker extends SwingWorker<Boolean, Object>{
       Concept concept;
       
       public DataChecker(Concept concept){
           this.concept = concept;
       }
       
       @Override
       public Boolean doInBackground() {
           Boolean testsDone = false;
           if (performCreationTests) {
                waitTillWritesFinished();
                Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();
                if(!warningsAndErrors.isEmpty()){
                    dataCheckMap.put(concept, warningsAndErrors);
                }else{
                    if(dataCheckMap.containsKey(concept)){
                        dataCheckMap.remove(concept);
                    }
                }
                DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(concept, creationTests);
                
                try {
                    checkRunner.latch.await();
                    int errorCount = 0;
                    int warningCount = 0;
                    warningsAndErrors.addAll(checkRunner.get());
                    for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                        if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                            errorCount++;
                        } else if (alert.getAlertType().equals(ALERT_TYPE.OMG)) {
                            errorCount++;
                        } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                            warningCount++;
                        }
                    }
                
                    if(checkRunner.get().isEmpty() || errorCount == 0){
                        ConceptTemplates.dataChecks.put(concept.getNid(), false);
                        Ts.get().touchComponentAlert(concept.getNid());
                    }else{
                        ConceptTemplates.dataChecks.put(concept.getNid(), true);
                        Ts.get().touchComponentAlert(concept.getNid());
                    }
                    testsDone = true;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
           dbCheckerPermit.release();
           return testsDone;
       }

       @Override
       protected void done() {
           UpdateFrames updateFrames = new UpdateFrames(concept);
           updateFrames.run();
           dbCheckerPermit2.release();
       }
        
    }

    public static void addUncommitted(I_ExtendByRef extension) {
        RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;

        addUncommitted(member.getEnclosingConcept());

        if (WorkflowHelper.isWorkflowCapabilityAvailable() && extension.getRefsetId() == WorkflowHelper.getWorkflowRefsetNid()) {
            handleWorkflowHistoryExtensions(extension);
        }
    }

    public static void addUncommittedDescNid(int dNid) {
        uncommittedDescNids.setMember(dNid);
    }

    public static void addUncommittedNoChecks(I_ExtendByRef extension) {
        RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;

        addUncommittedNoChecks(member.getEnclosingConcept());

        if (WorkflowHelper.isWorkflowCapabilityAvailable() && extension.getRefsetId() == WorkflowHelper.getWorkflowRefsetNid()) {
            HashSet<I_ExtendByRef> singleMemberSet = new HashSet<I_ExtendByRef>();
            singleMemberSet.add(extension);

            try {
                Runnable luceneWriter = WfHxLuceneWriterAccessor.addWfHxLuceneMembersFromExtensions(singleMemberSet);

				if (luceneWriter != null) {
	               luceneWriterService.execute(luceneWriter);
	            }
			} catch (InterruptedException e) {
	            AceLog.getAppLog().info("Adding uncommitted NO checks on extension: " + extension.toString());
			}
       }
   }

    public static void writeDirect(ConceptChronicleBI concept) {
        Concept c = (Concept) concept;

        c.modified();
        ChangeNotifier.touch(c, true);
        try {
            writeUncommitted(c);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addUncommittedNoChecks(ConceptChronicleBI concept) {
        Concept c = (Concept) concept;

        c.modified();

        ChangeNotifier.touch(c);

        if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info("---@@@ Adding uncommitted NO checks: " + concept.getNid() + " ---@@@ ");
        }

        if (concept.isUncommitted()) {
            uncommittedCNidsNoChecks.setMember(concept.getNid());
        } else {
            if (Bdb.watchList.containsKey(concept.getNid())) {
                AceLog.getAppLog().info("--- Removing uncommitted concept: " + concept.getNid() + " --- ");
            }
            removeUncommitted(c);
        }

        try {
            writeUncommitted(c);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cancel() {
        lastCancel = Bdb.gVersion.incrementAndGet();

        synchronized (uncommittedCNids) {
            synchronized (uncommittedCNidsNoChecks) {
                synchronized (uncommittedWfMemberIds) {
                    try {
                        NidBitSetItrBI uncommittedCNidsItr = uncommittedCNids.iterator();
                        NidBitSetItrBI uncommittedCNidsNoChecksItr = uncommittedCNidsNoChecks.iterator();
                        Set<Integer> cNidSet = new HashSet<Integer>();

                        while (uncommittedCNidsItr.next()) {
                            cNidSet.addAll(Concept.get(uncommittedCNidsItr.nid()).getConceptNidsAffectedByCommit());

                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine(
                                        "Canceling on concept: "
                                        + Ts.get().getComponent(uncommittedCNidsItr.nid()).toUserString() + " UUID: "
                                        + Ts.get().getUuidsForNid(uncommittedCNidsItr.nid()).toString());
                            }
                            
                            ConceptTemplates.dataChecks.remove(uncommittedCNidsItr.nid());
                            ConceptTemplates.templates.remove(uncommittedCNidsItr.nid());
                            Ts.get().touchComponentAlert(uncommittedCNidsItr.nid());
                            Ts.get().touchComponentTemplate(uncommittedCNidsItr.nid());
                        }

                        while (uncommittedCNidsNoChecksItr.next()) {
                            cNidSet.addAll(
                                    Concept.get(uncommittedCNidsNoChecksItr.nid()).getConceptNidsAffectedByCommit());

                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine(
                                        "Canceling on concept: "
                                        + Ts.get().getComponent(uncommittedCNidsNoChecksItr.nid()).toUserString()
                                        + " UUID: "
                                        + Ts.get().getUuidsForNid(uncommittedCNidsNoChecksItr.nid()).toString());
                            }
                            
                            ConceptTemplates.dataChecks.remove(uncommittedCNidsNoChecksItr.nid());
                            ConceptTemplates.templates.remove(uncommittedCNidsItr.nid());
                            Ts.get().touchComponentAlert(uncommittedCNidsNoChecksItr.nid());
                            Ts.get().touchComponentTemplate(uncommittedCNidsItr.nid());
                        }

                        ChangeNotifier.touchComponents(cNidSet);
                        Bdb.getSapDb().commit(Long.MIN_VALUE);
                        Bdb.getSapDb().commit(Long.MIN_VALUE);
                        handleCanceledConcepts(uncommittedCNids);
                        handleCanceledConcepts(uncommittedCNidsNoChecks);
                        uncommittedCNidsNoChecks.clear();
                        uncommittedCNids.clear();
                        DataCheckRunner.cancelAll();
                        dataCheckMap.clear();
                    } catch (IOException e1) {
                        AceLog.getAppLog().alertAndLogException(e1);
                    }
                }
            }
        }

        fireCancel();
        updateFrames();
    }

    public static boolean commit() {
        try {
            I_ConfigAceFrame frameConfig = Terms.get().getActiveAceFrameConfig();

            if (frameConfig != null) {
                return commit(frameConfig.getDbConfig().getUserChangesChangeSetPolicy(),
                        frameConfig.getDbConfig().getChangeSetWriterThreading());
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        return commit(ChangeSetPolicy.MUTABLE_ONLY, ChangeSetWriterThreading.SINGLE_THREAD);
    }
    
    public static boolean commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        return commit(changeSetPolicy, changeSetWriterThreading, false);
    }

    public static boolean commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading, boolean writeAdjudication) {
        Svn.rwl.acquireUninterruptibly();

        boolean passedRelease = false;
        boolean performCommit = true;
        WriteLock datacheckWriteLock = dataCheckLock.writeLock();
        I_RepresentIdSet allUncommitted = new IdentifierSet();
        try {
            
            synchronized (uncommittedCNids) {
                synchronized (uncommittedCNidsNoChecks) {
                    synchronized (uncommittedWfMemberIds) {
                        allUncommitted.or(uncommittedCNids);
                        allUncommitted.or(uncommittedCNidsNoChecks);
                        for (I_ExtendByRef ref : uncommittedWfMemberIds) {
                            allUncommitted.setMember(ref.getMemberId());
                        }
                        try {
                            GlobalPropertyChange.fireVetoableChange(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, null, allUncommitted);
                        } catch (PropertyVetoException ex) {
                            return false;
                        }

                        int errorCount = 0;
                        int warningCount = 0;
                        
                        if (performCreationTests) {
                            datacheckWriteLock.lock();
                            NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();

                            DataCheckRunner.cancelAll();
                            dataCheckMap.clear();
                            while (uncommittedCNidItr.next()) {
                                Set<AlertToDataConstraintFailure> warningsAndErrors =
                                        new HashSet<AlertToDataConstraintFailure>();
                                Concept concept = Concept.get(uncommittedCNidItr.nid());
                                    if (!warningsAndErrors.isEmpty()) {
                                        dataCheckMap.put(concept, warningsAndErrors);
                                    } else {
                                        if (dataCheckMap.containsKey(concept)) {
                                            dataCheckMap.remove(concept);
                                        }
                                    }
                                    DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(concept, commitTests);
                                    checkRunner.latch.await();

                                    warningsAndErrors.addAll(checkRunner.get());
                                    for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                                        if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                                            errorCount++;
                                        } else if (alert.getAlertType().equals(ALERT_TYPE.OMG)) {
                                            errorCount++;
                                        } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                                            warningCount++;
                                        }
                                    }
                                    
                                    if (checkRunner.get().isEmpty() || errorCount == 0) {
                                        ConceptTemplates.dataChecks.put(uncommittedCNidItr.nid(), false);
                                        Ts.get().touchComponentAlert(uncommittedCNidItr.nid());
                                    } else {
                                        ConceptTemplates.dataChecks.put(uncommittedCNidItr.nid(), true);
                                        Ts.get().touchComponentAlert(uncommittedCNidItr.nid());
                                    }
                                    
                            }
                        }
                        if (errorCount + warningCount != 0) {
                            if (errorCount > 0) {
                                performCommit = false;
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(new JFrame(),
                                                "Please fix data errors prior to commit.",
                                                "Data errors exist", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } else {
                                if (SwingUtilities.isEventDispatchThread()) {
                                    int selection = JOptionPane.showConfirmDialog(new JFrame(),
                                            "Do you want to continue with commit?", "Warnings Detected",
                                            JOptionPane.YES_NO_OPTION);
                                    if(selection == JOptionPane.YES_OPTION){
                                    }
                                    performCommit = selection == JOptionPane.YES_OPTION;
                                } else {
                                    try {
                                        AskToContinue asker = new AskToContinue();

                                        SwingUtilities.invokeAndWait(asker);
                                        performCommit = asker.continueWithCommit;
                                    } catch (InvocationTargetException e) {
                                        AceLog.getAppLog().alertAndLogException(e);
                                        performCommit = false;
                                    }
                                }
                            }
                        }

                        if (performCommit) {
                            lastCommit = Bdb.gVersion.incrementAndGet();
                            if (Bdb.annotationConcepts != null) {
                                for (Concept annotationConcept : Bdb.annotationConcepts) {
                                    dbWriterService.execute(new ConceptWriter(annotationConcept));
                                }
                                Bdb.annotationConcepts.clear();
                            }

                            NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();

                            while (uncommittedCNidItr.next()) {
                                if (getActiveFrame() != null) {
                                    int cnid = uncommittedCNidItr.nid();

                                    Concept c = Concept.get(cnid);

                                    c.modified(lastCommit);
                                }
                            }

                            NidBitSetItrBI uncommittedCNidItrNoChecks = uncommittedCNidsNoChecks.iterator();

                            long commitTime = System.currentTimeMillis();
                            IntSet sapNidsFromCommit = Bdb.getSapDb().commit(commitTime);

                            if (writeChangeSets && (sapNidsFromCommit.size() > 0)) {
                                if (changeSetPolicy == null) {
                                    changeSetPolicy = ChangeSetPolicy.OFF;
                                }

                                if (changeSetWriterThreading == null) {
                                    changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
                                }

                                switch (changeSetPolicy) {
                                    case COMPREHENSIVE:
                                    case INCREMENTAL:
                                    case MUTABLE_ONLY:
                                        uncommittedCNidsNoChecks.or(uncommittedCNids);

                                        if (uncommittedCNidsNoChecks.cardinality() > 0) {
                                            ChangeSetWriterHandler handler =
                                                    new ChangeSetWriterHandler(uncommittedCNidsNoChecks, commitTime,
                                                    sapNidsFromCommit, changeSetPolicy.convert(),
                                                    changeSetWriterThreading, Svn.rwl, writeAdjudication);

                                            changeSetWriterService.execute(handler);
                                            passedRelease = true;
                                        }

                                        break;

                                    case OFF:
                                        break;

                                    default:
                                        throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                                }
                            }

                            notifyCommit();
                            uncommittedCNids.clear();
                            uncommittedCNidsNoChecks = Terms.get().getEmptyIdSet();
                            luceneWriterPermit.acquire();

                            IdentifierSet descNidsToCommit = new IdentifierSet((IdentifierSet) uncommittedDescNids);

                            uncommittedDescNids.clear();
                            luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));

                            if (uncommittedWfMemberIds.size() > 0) {
                                Set<I_ExtendByRef> wfMembersToCommit =
                                        uncommittedWfMemberIds.getClass().newInstance();

                                wfMembersToCommit.addAll(uncommittedWfMemberIds);

                                Runnable luceneWriter = WfHxLuceneWriterAccessor.addWfHxLuceneMembersFromExtensions(wfMembersToCommit);

                                if (luceneWriter != null) {
                                    luceneWriterService.execute(luceneWriter);
                                }

                                uncommittedWfMemberIds.clear();
                            }

                            dataCheckMap.clear();
                        }
                        GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_COMMIT, null, allUncommitted);
                    }
                }
            }

            if (performCommit) {
                Bdb.sync();
                BdbCommitSequence.nextSequence();
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } finally {
            datacheckWriteLock.unlock();
            if (!passedRelease) {
                Svn.rwl.release();
            }
        }

        fireCommit();

        if (performCommit) {
            GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_SUCESSFUL_COMMIT, null, allUncommitted);
            return true;
        }else{
            updateAlerts();
        }

        return false;
    }
    
    public static boolean commit(Concept c, ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        return commit(c, changeSetPolicy, changeSetWriterThreading, false);
    }
    public static boolean commit(Concept c, ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading, boolean writeAdjudication) {
        if ((uncommittedCNids.cardinality() == 1) && (uncommittedCNidsNoChecks.cardinality() == 1)
                && uncommittedCNids.isMember(c.getNid()) && uncommittedCNidsNoChecks.isMember(c.getNid())) {
            return commit(changeSetPolicy, changeSetWriterThreading, writeAdjudication);
        } else if ((uncommittedCNids.cardinality() == 1) && (uncommittedCNidsNoChecks.cardinality() == 0)
                && uncommittedCNids.isMember(c.getNid())) {
            return commit(changeSetPolicy, changeSetWriterThreading, writeAdjudication);
        } else if ((uncommittedCNids.cardinality() == 0) && (uncommittedCNidsNoChecks.cardinality() == 1)
                && uncommittedCNidsNoChecks.isMember(c.getNid())) {
            return commit(changeSetPolicy, changeSetWriterThreading, writeAdjudication);
        }

        Svn.rwl.acquireUninterruptibly();
        I_RepresentIdSet allUncommitted = new IdentifierSet();
        allUncommitted.setMember(c.getConceptNid());
        try {
            GlobalPropertyChange.fireVetoableChange(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, null, allUncommitted);
        } catch (PropertyVetoException ex) {
            return false;
        }

        boolean performCommit = true;
        WriteLock datacheckWriteLock = dataCheckLock.writeLock();
        try {
            AceLog.getAppLog().info("Committing concept: " + c.toUserString() + " UUID: "
                    + c.getPrimUuid().toString());

            int errorCount = 0;
            int warningCount = 0;
            Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();
            if(!warningsAndErrors.isEmpty()){
                dataCheckMap.put(c, warningsAndErrors);
            }else{
                if(dataCheckMap.containsKey(c)){
                    dataCheckMap.remove(c);
                }
            }
            datacheckWriteLock.lock();
            DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(c, commitTests);
            CountDownLatch latch = checkRunner.latch;

            latch.await();
            warningsAndErrors.addAll(checkRunner.get());
            
            for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                    errorCount++;
                } else if (alert.getAlertType().equals(ALERT_TYPE.OMG)) {
                    errorCount++;
                }else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                    warningCount++;
                }
            }
            
            if(checkRunner.get().isEmpty() || errorCount == 0){
                 ConceptTemplates.dataChecks.put(c.getNid(), false);
                 Ts.get().touchComponentAlert(c.getNid());
            }else{
                ConceptTemplates.dataChecks.put(c.getNid(), true);
                Ts.get().touchComponentAlert(c.getNid());
            }

            
            if (errorCount + warningCount != 0) {
                if (errorCount > 0) {
                    performCommit = false;
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(new JFrame(), "Please fix data errors prior to commit.",
                                    "Data errors exist", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    if (SwingUtilities.isEventDispatchThread()) {
                        int selection = JOptionPane.showConfirmDialog(new JFrame(),
                                "Do you want to continue with commit?", "Warnings Detected",
                                JOptionPane.YES_NO_OPTION);

                        performCommit = selection == JOptionPane.YES_OPTION;
                    } else {
                        try {
                            AskToContinue asker = new AskToContinue();

                            SwingUtilities.invokeAndWait(asker);
                            performCommit = asker.continueWithCommit;
                        } catch (InvocationTargetException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                            performCommit = false;
                        }
                    }
                }
            }

            if (performCommit) {
                BdbCommitSequence.nextSequence();

                for (Concept annotationConcept : Bdb.annotationConcepts) {
                    dbWriterService.execute(new ConceptWriter(annotationConcept));
                }

                Bdb.annotationConcepts.clear();

                long commitTime = System.currentTimeMillis();
                NidSetBI sapNidsFromCommit = c.setCommitTime(commitTime);
                IdentifierSet commitSet = new IdentifierSet();

                commitSet.setMember(c.getNid());
                c.modified();
                Bdb.getConceptDb().writeConcept(c);

                if (uncommittedWfMemberIds.size() > 0) {
                    commitSet.setMember(WorkflowHelper.getWorkflowRefsetNid());

                    Concept wfRefset = (Concept) Ts.get().getConcept(WorkflowHelper.getWorkflowRefsetNid());

                    sapNidsFromCommit.addAll(wfRefset.setCommitTime(commitTime).getSetValues());
                    wfRefset.modified();
                    Bdb.getConceptDb().writeConcept(wfRefset);
                }

                if (writeChangeSets) {
                    if (changeSetPolicy == null) {
                        changeSetPolicy = ChangeSetPolicy.OFF;
                    }

                    if (changeSetWriterThreading == null) {
                        changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
                    }

                    switch (changeSetPolicy) {
                        case COMPREHENSIVE:
                        case INCREMENTAL:
                        case MUTABLE_ONLY:
                            ChangeSetWriterHandler handler = new ChangeSetWriterHandler(commitSet, commitTime,
                                    sapNidsFromCommit, changeSetPolicy.convert(),
                                    changeSetWriterThreading, Svn.rwl, writeAdjudication);

                            changeSetWriterService.execute(handler);

                            break;

                        case OFF:
                            break;

                        default:
                            throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                    }
                }

                uncommittedCNids.andNot(commitSet);
                uncommittedCNidsNoChecks.andNot(commitSet);
                luceneWriterPermit.acquire();

                IdentifierSet descNidsToCommit = new IdentifierSet();

                for (int dnid : c.getData().getDescNids()) {
                    descNidsToCommit.setMember(dnid);
                    uncommittedDescNids.setNotMember(dnid);
                }

                luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));

                if (uncommittedWfMemberIds.size() > 0) {
                    Set<I_ExtendByRef> wfMembersToCommit = uncommittedWfMemberIds.getClass().newInstance();

                    wfMembersToCommit.addAll(uncommittedWfMemberIds);

               		Runnable luceneWriter = WfHxLuceneWriterAccessor.addWfHxLuceneMembersFromExtensions(wfMembersToCommit);

                    if (luceneWriter != null) {
                        luceneWriterService.execute(luceneWriter);
                    }

                    uncommittedWfMemberIds.clear();
                }

                dataCheckMap.remove(c);
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } finally {
            Svn.rwl.release();
            datacheckWriteLock.unlock();
        }

        GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_COMMIT, null, allUncommitted);
        fireCommit();

        if (performCommit) {
            GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_SUCESSFUL_COMMIT, null, allUncommitted);
            return true;
        }else{
            updateAlerts();
        }

        return false;
    }

    private static void doUpdate() {
        if (lastDoUpdate < Bdb.gVersion.get()) {
            lastDoUpdate = Bdb.gVersion.get();

            try {
                for (Frame f : OpenFrames.getFrames()) {
                    if (AceFrame.class.isAssignableFrom(f.getClass())) {
                        AceFrame af = (AceFrame) f;
                        ACE aceInstance = af.getCdePanel();

                        aceInstance.getDataCheckListScroller();
                        aceInstance.getUncommittedListModel().clear();
                        if(uncommittedCNids.cardinality() > 0){
                            for (I_GetConceptData key : dataCheckMap.keySet()) {
                                if(key.isUncommitted()){
                                    aceInstance.getUncommittedListModel().addAll(dataCheckMap.get(key));
                                }
                            }
                        }
                        
                        if (aceInstance.getUncommittedListModel().size() > 0) {
                            for (int i = 0; i < aceInstance.getLeftTabs().getTabCount(); i++) {
                                if (aceInstance.getLeftTabs().getTitleAt(i).equals(ACE.DATA_CHECK_TAB_LABEL)) {
                                    aceInstance.getLeftTabs().setSelectedIndex(i);

                                    break;
                                }
                            }

                            // show data checks tab...
                        } else {
                            for (TermComponentDataCheckSelectionListener l : aceInstance.getDataCheckListeners()) {
                                l.setSelection(null);
                            }

                            // hide data checks tab...
                        }

                        if (uncommittedCNids.cardinality() == 0) {
                            aceInstance.aceFrameConfig.setCommitEnabled(false);
                            aceInstance.aceFrameConfig.fireCommit();
                        } else {
                            aceInstance.aceFrameConfig.setCommitEnabled(true);
                        }
                    } else if (ContradictionEditorFrame.class.isAssignableFrom(f.getClass())) {
                        ContradictionEditorFrame cef = (ContradictionEditorFrame) f;

                        if (uncommittedCNids.cardinality() == 0) {
                            cef.getActiveFrameConfig().setCommitEnabled(false);
                            cef.getActiveFrameConfig().fireCommit();
                        } else {
                            cef.getActiveFrameConfig().setCommitEnabled(true);
                        }
                    }
                }
            } catch (Exception e) {
                AceLog.getAppLog().warning(e.toString());
            }
        }
    }

    public static void fireCancel() {
        if (DwfaEnv.isHeadless()) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    if (Terms.get().getActiveAceFrameConfig() != null) {
                        for (I_ConfigAceFrame frameConfig :
                                Terms.get().getActiveAceFrameConfig().getDbConfig().getAceFrames()) {
                            frameConfig.fireCommit();
                            frameConfig.setCommitEnabled(false);
                        }
                    }

                    updateAlerts();
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
    }

    private static void fireCommit() {
        if (DwfaEnv.isHeadless()) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    if (Terms.get().getActiveAceFrameConfig() != null) {
                        for (I_ConfigAceFrame frameConfig :
                                Terms.get().getActiveAceFrameConfig().getDbConfig().getAceFrames()) {
                            frameConfig.fireCommit();
                            frameConfig.setCommitEnabled(false);
                        }

//                        updateAlerts();
                    }
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
    }

    public static boolean forget(ConceptAttributeVersionBI attr) throws IOException {
        Concept c = Bdb.getConcept(attr.getConceptNid());
      ConceptAttributes a = (ConceptAttributes) attr;
      boolean returnValue;

        if ((a.getTime() != Long.MAX_VALUE) && (a.getTime() != Long.MIN_VALUE)) {

            // Only need to forget additional versions;
            if (a.revisions != null) {
                synchronized (a.revisions) {
                    List<ConceptAttributesRevision> toRemove = new ArrayList<ConceptAttributesRevision>();
                    Iterator<ConceptAttributesRevision> ri = a.revisions.iterator();

                    while (ri.hasNext()) {
                        ConceptAttributesRevision ar = ri.next();

                        if (ar.getTime() == Long.MAX_VALUE) {
                            toRemove.add(ar);
                        }
                    }

                    for (ConceptAttributesRevision r : toRemove) {
                        a.removeRevision(r);
                        r.sapNid = -1;
                    }
                }
            }
            returnValue = false;
        } else {
            a.primordialSapNid = -1;
            returnValue = true;
        }
        c.modified();
        Terms.get().addUncommitted(c);
        return returnValue;
    }


   public static void forget(DescriptionVersionBI desc) throws IOException {
      Description d = (Description) desc;
        Concept c = Bdb.getConcept(d.getConceptNid());

        if (d.getTime() != Long.MAX_VALUE) {

            // Only need to forget additional versions;
            if (d.revisions == null) {
                throw new UnsupportedOperationException("Cannot forget a committed component.");
            } else {
                synchronized (d.revisions) {
                    List<DescriptionRevision> toRemove = new ArrayList<DescriptionRevision>();
                    Iterator<DescriptionRevision> di = d.revisions.iterator();

                    while (di.hasNext()) {
                        DescriptionRevision dr = di.next();

                        if (dr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(dr);
                        }
                    }

                    for (DescriptionRevision tr : toRemove) {
                        d.removeRevision(tr);
                        tr.sapNid = -1;
                    }
                }
            }
        } else {

            // have to forget "all" references to component...
            c.getDescs().remove(d);
            c.getData().getDescNids().remove(d.getNid());
            d.primordialSapNid = -1;
        }

        c.modified();
        Terms.get().addUncommitted(c);
    }

   @SuppressWarnings("unchecked")
   public static void forget(RefexChronicleBI extension) throws IOException {
      RefsetMember m         = (RefsetMember) extension;
      Concept      c         = Bdb.getConcept(m.getRefsetId());
      ComponentBI  component = Bdb.getComponent(m.getComponentNid());

        if (component instanceof Concept) {
            component = ((Concept) component).getConceptAttributes();
        }

        ConceptComponent comp = (ConceptComponent) component;

        if (m.getTime() != Long.MAX_VALUE) {

            // Only need to forget additional versions;
            if (m.revisions == null) {
                throw new UnsupportedOperationException("Cannot forget a committed component.");
            } else {
                synchronized (m.revisions) {
                    List<RefsetRevision<?, ?>> toRemove = new ArrayList<RefsetRevision<?, ?>>();
                    Iterator<?> mi = m.revisions.iterator();

                    while (mi.hasNext()) {
                        RefsetRevision<?, ?> mr = (RefsetRevision<?, ?>) mi.next();

                        if (mr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(mr);
                        }
                    }

                    for (RefsetRevision tr : toRemove) {
                        m.removeRevision(tr);
                        tr.sapNid = -1;
                    }
                }
            }
        } else {

            // have to forget "all" references to component...
            if (c.isAnnotationStyleRefex()) {
                comp.getAnnotationsMod().remove(m);
            } else {
                c.getRefsetMembers().remove(m);
                c.getData().getMemberNids().remove(m.getMemberId());
            }

            m.setStatusAtPositionNid(-1);
        }

        if (WorkflowHelper.isWorkflowCapabilityAvailable() && (WorkflowHelper.getWorkflowRefsetNid() == extension.getRefexNid())) {
            uncommittedWfMemberIds.remove(extension);
        }

        c.modified();
        Terms.get().addUncommitted(c);
    }


   public static void forget(ConceptChronicleBI concept) throws IOException {
      Concept c = (Concept) concept;

        c.cancel();
    }


   public static void forget(RelationshipVersionBI rel) throws IOException {
      Concept      c = Bdb.getConcept(rel.getSourceNid());
      Relationship r = (Relationship) rel;

        if (r.getTime() != Long.MAX_VALUE) {

            // Only need to forget additional versions;
            if (r.revisions == null) {
                throw new UnsupportedOperationException("Cannot forget a committed component.");
            } else {
                synchronized (r.revisions) {
                    List<RelationshipRevision> toRemove = new ArrayList<RelationshipRevision>();
                    Iterator<RelationshipRevision> ri = r.revisions.iterator();

                    while (ri.hasNext()) {
                        RelationshipRevision rr = ri.next();

                        if (rr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(rr);
                        }
                    }

                    for (RelationshipRevision tr : toRemove) {
                        r.removeRevision(tr);
                    }
                }
            }
        } else {

            // have to forget "all" references to component...
            c.getSourceRels().remove((Relationship) rel);
            c.getData().getSrcRelNids().remove(rel.getNid());
            r.primordialSapNid = -1;
        }

        c.modified();

        Terms.get().addUncommitted(c);
    }

    private static void handleCanceledConcepts(I_RepresentIdSet uncommittedCNids2) throws IOException {
        NidBitSetItrBI idItr = uncommittedCNids2.iterator();

        while (idItr.next()) {
            try {
                Concept c = Concept.get(idItr.nid());

                if (c.isCanceled()) {
                    Terms.get().forget(c);
                }

                c.flushVersions();
                c.modified();
                c.setLastWrite(Bdb.gVersion.incrementAndGet());

            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private static void handleWorkflowHistoryExtensions(I_ExtendByRef extension) {
        if ((WorkflowHelper.getWorkflowRefsetNid() == extension.getRefsetId())) {
            uncommittedWfMemberIds.add(extension);
        }
    }

    private static void loadTests(String directory, List<I_TestDataConstraints> list) {
        File componentPluginDir = new File(pluginRoot + File.separator + directory);
        File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".task");
            }
        });
        
        File omgDir = new File(componentPluginDir + File.separator + "priority");
        if(omgDir.exists()){
            File[] omg = omgDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File arg0, String fileName) {
                    return fileName.toLowerCase().endsWith(".task");
                }
            });
            if(omg != null){
                for (File f : omg) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    I_TestDataConstraints test = (I_TestDataConstraints) ois.readObject();

                    ois.close();
                    boolean alreadyInList = false;
                    for (I_TestDataConstraints looptest : list) {
                    	if (test.toString().equals(looptest.toString())) {
                    		alreadyInList = true;
                    	}
                    }
                    if (!alreadyInList) {
                    	list.add(test);	
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLog(Level.WARNING, "Processing: " + f.getAbsolutePath(), e);
                }
            }
            }
        }

        if (plugins != null) {
            for (File f : plugins) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    I_TestDataConstraints test = (I_TestDataConstraints) ois.readObject();

                    ois.close();
                    boolean alreadyInList = false;
                    for (I_TestDataConstraints looptest : list) {
                    	if (test.toString().equals(looptest.toString())) {
                    		alreadyInList = true;
                    	}
                    }
                    if (!alreadyInList) {
                    	list.add(test);	
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLog(Level.WARNING, "Processing: " + f.getAbsolutePath(), e);
                }
            }
        }
    }

    private static void notifyCommit() {
        if ((listeners != null) && (listeners.length > 0)) {
            final CommitEvent event;

            event = new CommitEvent(uncommittedCNidsNoChecks);

            for (final ICommitListener listener : listeners) {
                try {
                    listener.afterCommit(event);
                } catch (final Exception exception) {

                    // @todo handle exception
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * <p> notify the commit event </p>
     */
    private static void notifyShutdown() {
        if ((listeners != null) && (listeners.length > 0)) {
            for (final ICommitListener listener : listeners) {
                try {
                    listener.shutdown();
                } catch (final Exception exception) {

                    // @todo handle exception
                    exception.printStackTrace();
                }
            }
        }
    }

    public static void removeUncommitted(final Concept concept) {
        if (uncommittedCNids.isMember(concept.getNid())) {
            uncommittedCNids.setNotMember(concept.getNid());

            if (uncommittedCNids.cardinality() == 0) {
                dataCheckMap.clear();
            } else {
                dataCheckMap.remove(concept);
            }

            if (getActiveFrame() != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        removeUncommittedUpdateFrame(concept);
                    }
                });
            }
        }
        if (uncommittedCNidsNoChecks.isMember(concept.getNid())) {
            uncommittedCNidsNoChecks.setNotMember(concept.getNid());
        }
    }

    private static void removeUncommittedUpdateFrame(Concept concept) {
        for (I_ConfigAceFrame frameConfig : getActiveFrame().getDbConfig().getAceFrames()) {
            try {
                frameConfig.removeUncommitted(concept);
                updateAlerts();

                if (uncommittedCNids.cardinality() == 0) {
                    frameConfig.setCommitEnabled(false);
                }
            } catch (Exception e) {
                AceLog.getAppLog().warning(e.toString());
            }
        }
    }

    public static void reset() {
        changeSetWriterService = Executors.newFixedThreadPool(1,
                new NamedThreadFactory(commitManagerThreadGroup, "Change set writer"));
        dbWriterService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new NamedThreadFactory(commitManagerThreadGroup, "Db writer"));
        dbCheckerService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new NamedThreadFactory(commitManagerThreadGroup, "Data checker"));
        luceneWriterService = Executors.newFixedThreadPool(1,
                new NamedThreadFactory(commitManagerThreadGroup, "Lucene writer"));
        loadTests("commit", commitTests);
        loadTests("precommit", creationTests);
    }

    public static void resumeChangeSetWriters() {
        writeChangeSets = true;
    }

    public static void shutdown() throws InterruptedException {
        cancel();
        AceLog.getAppLog().info("Shutting down dbWriterService.");
        dbWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of dbWriterService.");
        dbWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("Shutting down dbCheckerService.");
        dbCheckerService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of dbCheckerService.");
        dbCheckerService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("Shutting down luceneWriterService.");
        luceneWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of luceneWriterService.");
        luceneWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("Shutting down changeSetWriterService.");
        changeSetWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of changeSetWriterService.");
        changeSetWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("BdbCommitManager is shutdown.");
        notifyShutdown();
    }

    public static void suspendChangeSetWriters() {
        writeChangeSets = false;
    }

    public static void updateAlerts() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                    doUpdate();
                }
        });
    }

    public static void updateFrames() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                    doUpdate();
                }
        });
    }

    public static void waitTillWritesFinished() {
        if (writerCount.get() > 0) {
            try {
                dbWriterPermit.acquireUninterruptibly(PERMIT_COUNT);
            } finally {
                dbWriterPermit.release(PERMIT_COUNT);
            }
        }
    }
    
    public static void waitTillDatachecksFinished() {
        dbCheckerPermit.acquireUninterruptibly(DATACHECK_PERMIT_COUNT);
        dbCheckerPermit2.acquireUninterruptibly(DATACHECK_PERMIT_COUNT);
        dbCheckerPermit.release(DATACHECK_PERMIT_COUNT);
        dbCheckerPermit2.release(DATACHECK_PERMIT_COUNT);
    }

    public static void writeImmediate(Concept concept) {
        new ConceptWriter(concept).run();
    }

    private static void writeUncommitted(Concept c) throws InterruptedException {
        if (c != null) {
            if (Bdb.watchList.containsKey(c.getNid())) {
                AceLog.getAppLog().info("---@@@ writeUncommitted checks: " + c.getNid() + " ---@@@ ");
            }

            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(c));
            dbWriterService.execute(new ConceptWriter(c));
        }
    }

    //~--- get methods ---------------------------------------------------------
    private static I_ConfigAceFrame getActiveFrame() {
        try {
            return Terms.get().getActiveAceFrameConfig();
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        return null;
    }

    public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
        Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();
        WriteLock datacheckWriteLock = dataCheckLock.writeLock();
        try {
            NidBitSetItrBI cNidItr = uncommittedCNids.iterator();

            while (cNidItr.next()) {
                try {
                    Concept toTest = Concept.get(cNidItr.nid());
                    datacheckWriteLock.lock();
                    DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(toTest, commitTests);

                    checkRunner.latch.await();
                    int errorCount = 0;
                    int warningCount = 0;
                    warningsAndErrors.addAll(checkRunner.get());
                    for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                        if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                            errorCount++;
                        } else if (alert.getAlertType().equals(ALERT_TYPE.OMG)) {
                            errorCount++;
                        } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                            warningCount++;
                        }
                    }
                    if(checkRunner.get().isEmpty() || errorCount == 0){
                        ConceptTemplates.dataChecks.put(cNidItr.nid(), false);
                        Ts.get().touchComponentAlert(cNidItr.nid());
                    }else{
                        ConceptTemplates.dataChecks.put(cNidItr.nid(), true);
                        Ts.get().touchComponentAlert(cNidItr.nid());
                    }
                } catch (InterruptedException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (ExecutionException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }finally{
            datacheckWriteLock.unlock();
        }

        List<AlertToDataConstraintFailure> warningsAndErrorsList =
                new ArrayList<AlertToDataConstraintFailure>();

        warningsAndErrorsList.addAll(warningsAndErrors);

        return warningsAndErrorsList;
    }

    public static ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>> getDatacheckMap() {
        return dataCheckMap;
    }

    public static long getLastCancel() {
        return lastCancel;
    }

    public static long getLastCommit() {
        return lastCommit;
    }

    public static Set<Concept> getUncommitted() {
        try {
            Set<Concept> returnSet = new HashSet<Concept>();
            NidBitSetItrBI cNidItr = uncommittedCNids.iterator();

            while (cNidItr.next()) {
                returnSet.add(Concept.get(cNidItr.nid()));
            }

            cNidItr = uncommittedCNidsNoChecks.iterator();

            while (cNidItr.next()) {
                returnSet.add(Concept.get(cNidItr.nid()));
            }

            return returnSet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isCheckCommitDataEnabled() {
        return performCommitTests;
    }

    public static boolean isCheckCreationDataEnabled() {
        return performCreationTests;
    }

    //~--- set methods ---------------------------------------------------------
    public static void setCheckCommitDataEnabled(boolean enabled) {
        performCommitTests = enabled;
    }

    public static void setCheckCreationDataEnabled(boolean enabled) {
        performCreationTests = enabled;
    }

    public static void addUncommittedNid(int cNid) {
        uncommittedCNidsNoChecks.setMember(cNid);
    }

    //~--- inner classes -------------------------------------------------------
    public static class AskToContinue implements Runnable {

        private boolean continueWithCommit;

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            int selection = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to continue with commit?",
                    "Warnings Detected", JOptionPane.YES_NO_OPTION);

            continueWithCommit = selection == JOptionPane.YES_OPTION;
        }
    }

    private static class ConceptWriter implements Runnable {

        private Concept c;

        //~--- constructors -----------------------------------------------------
        public ConceptWriter(Concept c) {
            super();
            assert c.readyToWrite();
            this.c = c;
            writerCount.incrementAndGet();
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            try {
                while (c.isUnwritten() && !c.isCanceled()) {
                    Bdb.getConceptDb().writeConcept(c);
                }
            } catch (Throwable e) {
                String exceptionStr = "Exception Writing: " + c.toLongString();
                Exception newEx = new Exception(exceptionStr, e);

                System.out.println(exceptionStr + "\n\n" + e.toString());
                AceLog.getAppLog().alertAndLogException(newEx);
            } finally {
                dbWriterPermit.release();
                writerCount.decrementAndGet();
            }
        }
    }

    public static class DataCheckRunner
            extends SwingWorker<Collection<AlertToDataConstraintFailure>, Collection<AlertToDataConstraintFailure>> {

        private static ConcurrentHashMap<Concept, DataCheckRunner> runners = new ConcurrentHashMap<Concept, DataCheckRunner>();
        //~--- fields -----------------------------------------------------------
        private boolean canceled = false;
        private Concept c;
        private CountDownLatch latch;
        private List<I_TestDataConstraints> tests;

        //~--- constructors -----------------------------------------------------
        private DataCheckRunner(Concept c, List<I_TestDataConstraints> tests) {
            this.c = c;
            this.tests = tests;
            latch = new CountDownLatch(tests.size());
            
            DataCheckRunner oldRunner = runners.put(c, this);

            if (oldRunner != null) {
                oldRunner.cancel();
            }
        }

        //~--- methods ----------------------------------------------------------
        public void cancel() {
            while (latch.getCount() > 0) {
                latch.countDown();
            }

            this.canceled = true;
        }

        public static void cancelAll() {
            for (DataCheckRunner runner : runners.values()) {
                runner.cancel();
            }

            runners.clear();
        }

        @Override
        protected Collection<AlertToDataConstraintFailure> doInBackground() throws Exception {
            try {
            List<AlertToDataConstraintFailure> runnerAlerts = new ArrayList<AlertToDataConstraintFailure>();

            if (canceled) {
                return runnerAlerts;
            }
            
//       System.out.println(">>>>>>>>>>>>> Doing in background: " + latch.getCount());
            if ((c != null) && (tests != null)) {
                for (I_TestDataConstraints test : tests) {

//             System.out.println(">>>>>>>>>>>>> Running test: " + test.getClass().getName());
                    if (canceled) {
                        return runnerAlerts;
                    }

                    try {
                        Collection<AlertToDataConstraintFailure> result = test.test(c, true);
                        runnerAlerts.addAll(result);
                        if (canceled) {
                            return runnerAlerts;
                        }

                        publish(result);
                        
                        for(AlertToDataConstraintFailure fail : result){
                            if(fail.getAlertType().equals(AlertToDataConstraintFailure.ALERT_TYPE.OMG)){
                                long remaining = latch.getCount();
                                for (long i = 0; i < remaining; i++) {
//                                    System.out.println(">>>>>>>>>>>>> Latch cancel: " + latch.getCount());
                                    latch.countDown();
                                }
                                return runnerAlerts;
                            }
                        }

                        Collection<RefsetMember<?, ?>> extensions = c.getExtensions();

                        for (RefsetMember<?, ?> extension : extensions) {
                            if (canceled) {
                                return runnerAlerts;
                            }

                            if (extension.isUncommitted()) {
                                result = test.test(extension, true);
                                runnerAlerts.addAll(result);
                                publish(result);

                                if (canceled) {
                                    return runnerAlerts;
                                }
                            }
                        }
                    } catch (Throwable e) {
                        AceLog.getEditLog().alertAndLogException(e);
                    }

//             System.out.println(">>>>>>>>>>>>> Finished Test: " + test.getClass().getName());
                    latch.countDown();

//             System.out.println(">>>>>>>>>>>>> Latch count: " + latch.getCount());
                }
            }

            return runnerAlerts;
            
            } finally {
                long remaining = latch.getCount();

                for (long i = 0; i < remaining; i++) {

//          System.out.println(">>>>>>>>>>>>> Latch cancel: " + latch.getCount());
                    latch.countDown();
                }

                if (!canceled) {
                    runners.remove(c);
                }
                dbCheckerPermit2.release();
            }
        }

        @Override
        protected void process(List<Collection<AlertToDataConstraintFailure>> chunks) {
            for (Collection<AlertToDataConstraintFailure> results : chunks) {
                Collection<AlertToDataConstraintFailure> currentAlerts = dataCheckMap.get(c);

                if (currentAlerts == null) {
                    currentAlerts = new HashSet<AlertToDataConstraintFailure>();
                }

                currentAlerts.addAll(results);
                if(!currentAlerts.isEmpty()){
                    dataCheckMap.put(c, currentAlerts);
                }else{
                    if(dataCheckMap.containsKey(c)){
                        dataCheckMap.remove(c);
                    }
                }
            }

            if (canceled) {
                return;
            }
            doUpdate();
        }

        public static DataCheckRunner runDataChecks(Concept c, List<I_TestDataConstraints> tests) {
            DataCheckRunner runner = new DataCheckRunner(c, tests);
            try {
                dbCheckerPermit2.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(BdbCommitManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            runner.execute();
            return runner;
        }

        //~--- get methods ------------------------------------------------------
        public CountDownLatch getLatch() {
            return latch;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }

    private static class DescLuceneWriter implements Runnable {

        private int batchSize = 200;
        private IdentifierSet descNidsToWrite;

        //~--- constructors -----------------------------------------------------
        public DescLuceneWriter(IdentifierSet descNidsToCommit) {
            super();
            this.descNidsToWrite = descNidsToCommit;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            try {
                ArrayList<Description> toIndex = new ArrayList<Description>(batchSize + 1);
                I_IterateIds idItr = descNidsToWrite.iterator();
                int count = 0;

                while (idItr.next()) {
                    count++;

                    Description d = (Description) Bdb.getComponent(idItr.nid());

                    toIndex.add(d);

                    if (count > batchSize) {
                        count = 0;
                        LuceneManager.writeToLucene(toIndex, LuceneSearchType.DESCRIPTION);
                        toIndex = new ArrayList<Description>(batchSize + 1);
                    }
                }

                LuceneManager.writeToLucene(toIndex, LuceneSearchType.DESCRIPTION);
            } catch (IndexNotFoundException e) {
                AceLog.getAppLog().warning("Index not yet created: " + e.getLocalizedMessage());
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }

            luceneWriterPermit.release();
        }
    }

    private static class SetNidsForCid implements Runnable {

        Concept concept;

        //~--- constructors -----------------------------------------------------
        public SetNidsForCid(Concept concept) {
            super();
            this.concept = concept;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            try {
                Collection<Integer> nids = concept.getAllNids();
                NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();

                for (int nid : nids) {
                    nidCidMap.setCNidForNid(concept.getNid(), nid);
                }
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private static class UpdateFrames implements Runnable {

        Concept c;

        //~--- constructors -----------------------------------------------------
        public UpdateFrames(Concept c) {
            super();
            this.c = c;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            if (getActiveFrame() != null) {
                if(ACE.datachecksRunning()){
                    updateAlerts();
                }
                I_ConfigAceFrame activeFrame = getActiveFrame();

                for (Frame f : OpenFrames.getFrames()) {
                    I_ConfigAceFrame frameConfig = null;

                    if (f instanceof ContradictionEditorFrame) {
                        frameConfig = ((ContradictionEditorFrame) f).getActiveFrameConfig();
                    } else if (f instanceof AceFrame) {
                        frameConfig = ((AceFrame) f).getCdePanel().getAceFrameConfig();
                    }

                    if (frameConfig != null) {
                        frameConfig.setCommitEnabled(true);

                        if (c.isUncommitted()) {
                            frameConfig.addUncommitted(c);
                        } else {
                            frameConfig.removeUncommitted(c);
                        }
                    }
                }
            }
        }
    }
}
