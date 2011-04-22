package org.ihtsdo.db.bdb;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
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
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;

public class BdbCommitManager {

    private static Semaphore dbWriterPermit = new Semaphore(50);
    private static Semaphore luceneWriterPermit = new Semaphore(50);
    private static ThreadGroup commitManagerThreadGroup =
            new ThreadGroup("commit manager threads");

    private static class ConceptWriter implements Runnable {

        private Concept c;

        public ConceptWriter(Concept c) {
            super();
            this.c = c;
        }

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
            }
        }
    }

    private static class LuceneWriter implements Runnable {

        private int batchSize = 200;
        private IdentifierSet descNidsToWrite;

        public LuceneWriter(IdentifierSet descNidsToCommit) {
            super();
            this.descNidsToWrite = descNidsToCommit;
        }

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
                        LuceneManager.writeToLucene(toIndex);
                        toIndex = new ArrayList<Description>(batchSize + 1);
                    }
                }
                LuceneManager.writeToLucene(toIndex);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            luceneWriterPermit.release();
        }
    }
    private static I_RepresentIdSet uncommittedCNids = new IdentifierSet();
    private static I_RepresentIdSet uncommittedCNidsNoChecks = new IdentifierSet();
    private static I_RepresentIdSet uncommittedDescNids = new IdentifierSet();
    private static ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap = new ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();
    private static long lastCommit = Bdb.gVersion.incrementAndGet();
    private static long lastCancel = Integer.MIN_VALUE;

    public static long getLastCancel() {
        return lastCancel;
    }

    public static void addUncommittedNoChecks(I_ExtendByRef extension) {
        RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
        addUncommittedNoChecks(member.getEnclosingConcept());
    }
    private static List<I_TestDataConstraints> commitTests = new ArrayList<I_TestDataConstraints>();
    private static List<I_TestDataConstraints> creationTests = new ArrayList<I_TestDataConstraints>();
    public static String pluginRoot = "plugins";
    private static ExecutorService dbWriterService;
    private static ExecutorService changeSetWriterService;
    private static ExecutorService luceneWriterService;
    private static boolean performCreationTests = true;
    private static boolean performCommitTests = true;
    private static boolean writeChangeSets = true;

    static {
        reset();
    }

    public static void reset() {
        changeSetWriterService = Executors.newFixedThreadPool(1, new NamedThreadFactory(commitManagerThreadGroup,
                "Change set writer"));
        dbWriterService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new NamedThreadFactory(commitManagerThreadGroup, "Db writer"));
        luceneWriterService = Executors.newFixedThreadPool(1, new NamedThreadFactory(commitManagerThreadGroup,
                "Lucene writer"));
        loadTests("commit", commitTests);
        loadTests("precommit", creationTests);
    }
    private static AtomicReference<Concept> lastUncommitted = new AtomicReference<Concept>();

    public static void addUncommittedNoChecks(I_GetConceptData concept) {
        ((Concept) concept).modified();
        if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info(
                    "---@@@ Adding uncommitted NO checks: "
                    + concept.getNid() + " ---@@@ ");
        }
        Concept c = null;
        uncommittedCNidsNoChecks.setMember(concept.getNid());
        c = lastUncommitted.getAndSet((Concept) concept);
        if (c == concept) {
            c = null;
        }
        try {
            writeUncommitted(c);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void flushUncommitted() throws InterruptedException {
        Concept c = lastUncommitted.getAndSet(null);
        if (c != null) {
            writeUncommitted(c);
        }
    }

    private static void writeUncommitted(Concept c) throws InterruptedException {
        if (c != null) {
            if (Bdb.watchList.containsKey(c.getNid())) {
                AceLog.getAppLog().info(
                        "---@@@ writeUncommitted checks: "
                        + c.getNid() + " ---@@@ ");
            }
            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(c));
            dbWriterService.execute(new ConceptWriter(c));
        }
    }

    public static void addUncommitted(I_ExtendByRef extension) {
        RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
        addUncommitted(member.getEnclosingConcept());
    }

    private static class SetNidsForCid implements Runnable {

        Concept concept;

        public SetNidsForCid(Concept concept) {
            super();
            this.concept = concept;
        }

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

    public static void addUncommitted(ConceptChronicleBI igcd) {
        if (igcd == null) {
            return;
        }
        Concept concept = (Concept) igcd;
        dataCheckMap.remove(concept);
        if (concept.isUncommitted() == false) {
            removeUncommitted(concept);
            if (Bdb.watchList.containsKey(concept.getNid())) {
                AceLog.getAppLog().info(
                        "--- Removing uncommitted concept: "
                        + concept.getNid() + " --- ");
            }
            return;
        }
        concept.modified();
        if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info(
                    "---@@@ Adding uncommitted concept: " + concept.getNid()
                    + " ---@@@ ");
        }
        try {

            if (performCreationTests) {
                Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();
                dataCheckMap.put(concept, warningsAndErrors);

                DataCheckRunner.runDataChecks(concept, creationTests);
            }
            uncommittedCNids.setMember(concept.getNid());
            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(concept));
            dbWriterService.execute(new ConceptWriter(concept));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(new UpdateFrames(concept));
    }

    private static class UpdateFrames implements Runnable {

        Concept c;

        public UpdateFrames(Concept c) {
            super();
            this.c = c;
        }

        @Override
        public void run() {
            if (getActiveFrame() != null) {
                I_ConfigAceFrame activeFrame = getActiveFrame();
                for (I_ConfigAceFrame frameConfig : activeFrame.getDbConfig().getAceFrames()) {
                    frameConfig.setCommitEnabled(true);
                    updateAlerts();
                    if (c.isUncommitted()) {
                        frameConfig.addUncommitted(c);
                    } else {
                        frameConfig.removeUncommitted(c);
                    }
                }
            }
        }
    }

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

    public static void commit(Concept c, ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        if (uncommittedCNids.cardinality() == 1
                && uncommittedCNidsNoChecks.cardinality() == 1
                && uncommittedCNids.isMember(c.getNid())
                && uncommittedCNidsNoChecks.isMember(c.getNid())) {
            commit(changeSetPolicy, changeSetWriterThreading);
            return;

        } else if (uncommittedCNids.cardinality() == 1
                && uncommittedCNidsNoChecks.cardinality() == 0
                && uncommittedCNids.isMember(c.getNid())) {
            commit(changeSetPolicy, changeSetWriterThreading);
            return;

        } else if (uncommittedCNids.cardinality() == 0
                && uncommittedCNidsNoChecks.cardinality() == 1
                && uncommittedCNidsNoChecks.isMember(c.getNid())) {
            commit(changeSetPolicy, changeSetWriterThreading);
            return;

        }

        Svn.rwl.acquireUninterruptibly();
        try {
            int errorCount = 0;
            int warningCount = 0;
            List<AlertToDataConstraintFailure> warningsAndErrors =
                    new ArrayList<AlertToDataConstraintFailure>();
            dataCheckMap.put(c, warningsAndErrors);
            
            DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(c, commitTests);
            CountDownLatch latch = checkRunner.latch;
            latch.await();
            warningsAndErrors.addAll(checkRunner.get());

            for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                    errorCount++;
                } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                    warningCount++;
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
                                    "Data errors exist",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    if (SwingUtilities.isEventDispatchThread()) {
                        int selection = JOptionPane.showConfirmDialog(
                                new JFrame(),
                                "Do you want to continue with commit?",
                                "Warnings Detected",
                                JOptionPane.YES_NO_OPTION);
                        performCommit = selection == JOptionPane.YES_OPTION;
                    } else {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {

                                @Override
                                public void run() {
                                    int selection = JOptionPane.showConfirmDialog(
                                            new JFrame(),
                                            "Do you want to continue with commit?",
                                            "Warnings Detected",
                                            JOptionPane.YES_NO_OPTION);
                                    performCommit = selection == JOptionPane.YES_OPTION;
                                }
                            });
                        } catch (InvocationTargetException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                            performCommit = false;
                        }
                    }
                }
            }

            if (performCommit) {
                KindOfComputer.reset();
                KindOfComputer.updateIsaCache(getActiveFrame().getViewCoordinate().getIsaCoordinate(), c.getNid());
                long commitTime = System.currentTimeMillis();
                NidSetBI sapNidsFromCommit = c.setCommitTime(commitTime);
                Bdb.getConceptDb().writeConcept(c);


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
                            uncommittedCNidsNoChecks.or(uncommittedCNids);
                            if (uncommittedCNidsNoChecks.cardinality() > 0) {
                                ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
                                        uncommittedCNidsNoChecks, commitTime,
                                        sapNidsFromCommit, changeSetPolicy.convert(),
                                        changeSetWriterThreading,
                                        Svn.rwl);
                                changeSetWriterService.execute(handler);
                            }
                            break;
                        case OFF:

                            break;

                        default:
                            throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                    }
                }
                uncommittedCNids.setNotMember(c.getNid());
                uncommittedCNidsNoChecks.setNotMember(c.getNid());

                WorkflowHistoryRefsetWriter.unLockMutex();

                luceneWriterPermit.acquire();
                IdentifierSet descNidsToCommit = new IdentifierSet();
                for (int dnid : c.getData().getDescNids()) {
                    descNidsToCommit.setMember(dnid);
                    uncommittedDescNids.setNotMember(dnid);
                }
                luceneWriterService.execute(new LuceneWriter(descNidsToCommit));
                dataCheckMap.remove(c);
            }


        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } finally {
            Svn.rwl.release();
        }
        fireCommit();
    }
    private static boolean performCommit = false;

    public static void commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        lastCommit = Bdb.gVersion.incrementAndGet();
        Svn.rwl.acquireUninterruptibly();
        boolean passedRelease = false;
        try {
            synchronized (uncommittedCNids) {
                synchronized (uncommittedCNidsNoChecks) {
                    flushUncommitted();
                    performCommit = true;
                    int errorCount = 0;
                    int warningCount = 0;
                    if (performCreationTests) {
                        NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();
                        DataCheckRunner.cancelAll();
                        dataCheckMap.clear();
                        while (uncommittedCNidItr.next()) {
                            List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
                            Concept concept = Concept.get(uncommittedCNidItr.nid());
                            dataCheckMap.put(concept, warningsAndErrors);

                            DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(concept, commitTests);
                            checkRunner.latch.await();
                            warningsAndErrors.addAll(checkRunner.get());
                            for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                                if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                                    errorCount++;
                                } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                                    warningCount++;
                                }
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
                                            "Data errors exist",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } else {
                            if (SwingUtilities.isEventDispatchThread()) {
                                int selection = JOptionPane.showConfirmDialog(
                                        new JFrame(),
                                        "Do you want to continue with commit?",
                                        "Warnings Detected",
                                        JOptionPane.YES_NO_OPTION);
                                performCommit = selection == JOptionPane.YES_OPTION;
                            } else {
                                try {
                                    SwingUtilities.invokeAndWait(new Runnable() {

                                        @Override
                                        public void run() {
                                            int selection = JOptionPane.showConfirmDialog(
                                                    new JFrame(),
                                                    "Do you want to continue with commit?",
                                                    "Warnings Detected",
                                                    JOptionPane.YES_NO_OPTION);
                                            performCommit = selection == JOptionPane.YES_OPTION;
                                        }
                                    });
                                } catch (InvocationTargetException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                    performCommit = false;
                                }
                            }
                        }
                    }

                    if (performCommit) {
                        KindOfComputer.reset();
                        NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();
                        while (uncommittedCNidItr.next()) {
							if (getActiveFrame() != null) {
                           		 KindOfComputer.updateIsaCache(getActiveFrame().getViewCoordinate().getIsaCoordinate(), uncommittedCNidItr.nid());
							}
                        }
                        NidBitSetItrBI uncommittedCNidItrNoChecks = uncommittedCNidsNoChecks.iterator();
                        while (uncommittedCNidItrNoChecks.next()) {
                        	if (getActiveFrame() != null) {
                        		KindOfComputer.updateIsaCache(getActiveFrame().getViewCoordinate().getIsaCoordinate(), uncommittedCNidItrNoChecks.nid());
                        	}
                        }
                        long commitTime = System.currentTimeMillis();
                        IntSet sapNidsFromCommit = Bdb.getSapDb().commit(
                                commitTime);

                        if (writeChangeSets && sapNidsFromCommit.size() > 0) {
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
                                        ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
                                                uncommittedCNidsNoChecks, commitTime,
                                                sapNidsFromCommit, changeSetPolicy.convert(),
                                                changeSetWriterThreading,
                                                Svn.rwl);
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
                        uncommittedCNids.clear();
                        uncommittedCNidsNoChecks = Terms.get().getEmptyIdSet();

                        WorkflowHistoryRefsetWriter.unLockMutex();

                        luceneWriterPermit.acquire();
                        IdentifierSet descNidsToCommit = new IdentifierSet((IdentifierSet) uncommittedDescNids);
                        uncommittedDescNids.clear();
                        luceneWriterService.execute(new LuceneWriter(descNidsToCommit));
                        dataCheckMap.clear();
                    }
                }
            }
            if (performCommit) {
                Bdb.sync();
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InterruptedException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ExecutionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (Exception e1) {
        	AceLog.getAppLog().alertAndLogException(e1);
		} finally {
            if (!passedRelease) {
                Svn.rwl.release();
            }
        }
        fireCommit();
    }

    public static void commit() {
        commit(ChangeSetPolicy.MUTABLE_ONLY,
                ChangeSetWriterThreading.SINGLE_THREAD);
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
                        updateAlerts();
                    }
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
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

    public static void cancel() {
        lastCancel = Bdb.gVersion.incrementAndGet();
        synchronized (uncommittedCNids) {
            synchronized (uncommittedCNidsNoChecks) {
                try {
                    KindOfComputer.reset();
                    handleCanceledConcepts(uncommittedCNids);
                    handleCanceledConcepts(uncommittedCNidsNoChecks);
                    uncommittedCNidsNoChecks.clear();
                    uncommittedCNids.clear();
                    Bdb.getSapDb().commit(Long.MIN_VALUE);
                    DataCheckRunner.cancelAll();
                    dataCheckMap.clear();
                } catch (IOException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
        }
        fireCancel();
        updateFrames();
    }

    private static void handleCanceledConcepts(I_RepresentIdSet uncommittedCNids2)
            throws IOException {
        NidBitSetItrBI idItr = uncommittedCNids2.iterator();
        while (idItr.next()) {
            Concept c = Concept.get(idItr.nid());
            if (c.isCanceled()) {
                Terms.get().forget(c);
            }
        }
    }

    public static void forget(I_ConceptAttributeVersioned attr) throws IOException {
        Concept c = Bdb.getConcept(attr.getConId());
        ConceptAttributes a = (ConceptAttributes) attr;
        if (a.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (a.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
            } else {
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
        } else {
            // have to forget "all" references to component...
            c.abort();
        }
        c.modified();
        Terms.get().addUncommittedNoChecks(c);
    }

    public static void forget(I_RelVersioned rel) throws IOException {
        Concept c = Bdb.getConcept(rel.getC1Id());
        Relationship r = (Relationship) rel;
        if (r.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (r.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
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
            c.getSourceRels().remove(rel);
            c.getData().getSrcRelNids().remove(rel.getNid());
            r.primordialSapNid = -1;
        }
        c.modified();
        Terms.get().addUncommittedNoChecks(c);
    }

    public static void forget(I_DescriptionVersioned desc) throws IOException {
        Description d = (Description) desc;
        Concept c = Bdb.getConcept(d.getConceptNid());
        if (d.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (d.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
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

            c.getDescriptions().remove(d);
            c.getData().getDescNids().remove(d.getNid());
            d.primordialSapNid = -1;
        }
        c.modified();
        Terms.get().addUncommittedNoChecks(c);
    }

    public static void forget(I_GetConceptData concept) throws IOException {
        Concept c = (Concept) concept;
        Bdb.getConceptDb().forget(c);
    }

    @SuppressWarnings("unchecked")
    public static void forget(I_ExtendByRef extension) throws IOException {
        RefsetMember m = (RefsetMember) extension;
        Concept c = Bdb.getConcept(m.getRefsetId());
        ComponentBI component = Bdb.getComponent(m.getComponentNid());
        ConceptComponent comp = (ConceptComponent) component;
        if (m.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (m.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
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
        c.modified();
        Terms.get().addUncommittedNoChecks(c);
    }

    private static void loadTests(String directory,
            List<I_TestDataConstraints> list) {
        File componentPluginDir = new File(pluginRoot + File.separator
                + directory);
        File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".task");
            }
        });

        if (plugins != null) {
            for (File f : plugins) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    I_TestDataConstraints test = (I_TestDataConstraints) ois.readObject();
                    ois.close();
                    list.add(test);
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLog(Level.WARNING,
                            "Processing: " + f.getAbsolutePath(), e);
                }
            }
        }
    }

    private static void doUpdate() {
        try {
            for (Frame f : OpenFrames.getFrames()) {
                if (AceFrame.class.isAssignableFrom(f.getClass())) {
                    AceFrame af = (AceFrame) f;
                    ACE aceInstance = af.getCdePanel();
                    aceInstance.getDataCheckListScroller();
                    aceInstance.getUncommittedListModel().clear();

                    for (Collection<AlertToDataConstraintFailure> alerts : dataCheckMap.values()) {
                        aceInstance.getUncommittedListModel().addAll(alerts);
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
                }
            }
        } catch (Exception e) {
            AceLog.getAppLog().warning(e.toString());
        }
    }

    public static void updateFrames() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                doUpdate();
            }
        });
    }

    public static void updateAlerts() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                doUpdate();
            }
        });
    }

    public static void removeUncommitted(final Concept concept) {
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

    public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
        List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
        try {
            NidBitSetItrBI cNidItr = uncommittedCNids.iterator();
            while (cNidItr.next()) {
                try {
                    Concept toTest = Concept.get(cNidItr.nid());
                    DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(toTest, commitTests);
                    checkRunner.latch.await();
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return warningsAndErrors;
    }

    public static void addUncommittedDescNid(int dNid) {
        uncommittedDescNids.setMember(dNid);
    }

    public static long getLastCommit() {
        return lastCommit;
    }

    public static void suspendChangeSetWriters() {
        writeChangeSets = false;
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
        AceLog.getAppLog().info("Shutting down luceneWriterService.");
        luceneWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of luceneWriterService.");
        luceneWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("Shutting down changeSetWriterService.");
        changeSetWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of changeSetWriterService.");
        changeSetWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("BdbCommitManager is shutdown.");
    }

    public static void writeImmediate(Concept concept) {
        new ConceptWriter(concept).run();
    }

    public static boolean isCheckCreationDataEnabled() {
        return performCreationTests;
    }

    public static boolean isCheckCommitDataEnabled() {
        return performCommitTests;
    }

    public static void setCheckCreationDataEnabled(boolean enabled) {
        performCreationTests = enabled;
    }

    public static void setCheckCommitDataEnabled(boolean enabled) {
        performCommitTests = enabled;
    }

    public static class DataCheckRunner 
        extends SwingWorker<Collection<AlertToDataConstraintFailure>, 
            Collection<AlertToDataConstraintFailure>> {
        
        private static ConcurrentHashMap<Concept, DataCheckRunner> runners 
                = new ConcurrentHashMap<Concept, DataCheckRunner>();

        private CountDownLatch latch;
        private List<I_TestDataConstraints> tests;
        private Concept c;
        private boolean canceled = false;

        private DataCheckRunner(Concept c, List<I_TestDataConstraints> tests) {
           this.c = c;
           this.tests = tests;
           latch = new CountDownLatch(tests.size());
           DataCheckRunner oldRunner = runners.put(c, this);
           if (oldRunner != null) {
               oldRunner.cancel();
           }
        }

        public static DataCheckRunner runDataChecks(Concept c, 
                List<I_TestDataConstraints> tests) {
            DataCheckRunner runner = new DataCheckRunner(c, tests);
            runner.execute();
            return runner;
        }

        public static void cancelAll() {
            for (DataCheckRunner runner: runners.values()) {
                runner.cancel();
            }
            runners.clear();
        }
                
        @Override
        protected void done() {
            super.done();
            if (!canceled) {
                runners.remove(c);
            }
        }

        
        @Override
        protected Collection<AlertToDataConstraintFailure> doInBackground() 
                throws Exception {
            List<AlertToDataConstraintFailure> runnerAlerts = 
                    new ArrayList<AlertToDataConstraintFailure>();
            if (canceled) return runnerAlerts;
            if (c != null && tests != null) {
                for (I_TestDataConstraints test : tests) {
                    if (canceled) return runnerAlerts;
                    try {
                        Collection<AlertToDataConstraintFailure> result = 
                                test.test(c, true);
                        runnerAlerts.addAll(result);
                        if (canceled) return runnerAlerts;
                        publish(result);
                        Collection<RefsetMember<?, ?>> extensions = c.getExtensions();
                        for (RefsetMember<?, ?> extension : extensions) {
                            if (canceled) return runnerAlerts;
                            if (extension.isUncommitted()) {
                                result = test.test(extension, true);
                                runnerAlerts.addAll(result);
                                publish(result);
                                if (canceled) return runnerAlerts;
                            }
                        }
                    } catch (Throwable e) {
                        AceLog.getEditLog().alertAndLogException(e);
                    }
                    latch.countDown();
                }
            }
            return runnerAlerts;
        }

        public CountDownLatch getLatch() {
            return latch;
        }
        
        
        public void cancel() {
            while (latch.getCount() > 0) {
                latch.countDown();
            }
            this.canceled = true;
        }

        public boolean isCanceled() {
            return canceled;
        }


        @Override
        protected void process(List<Collection<AlertToDataConstraintFailure>> chunks) {
            for (Collection<AlertToDataConstraintFailure> results : chunks) {
                Collection<AlertToDataConstraintFailure> currentAlerts = dataCheckMap.get(c);
                if (currentAlerts == null) {
                    currentAlerts = new HashSet<AlertToDataConstraintFailure>();
                }
                currentAlerts.addAll(results);
                dataCheckMap.put(c, currentAlerts);
            }
            if (canceled) return;
            doUpdate();
        }
    }
}
