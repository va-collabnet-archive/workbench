package org.ihtsdo.db.bdb;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

import org.apache.commons.lang.ArrayUtils;
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
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.svn.Svn;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
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
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class BdbCommitManager {

    /**
     * <p>
     * listeners
     * </p>
     */
    private static ICommitListener[] listeners = new ICommitListener[0];
    private static Semaphore dbWriterPermit = new Semaphore(50);
    private static Semaphore luceneWriterPermit = new Semaphore(50);
    private static ThreadGroup commitManagerThreadGroup =
            new ThreadGroup("commit manager threads");

    private static class ConceptWriter implements Runnable {

        private final Concept c;

        public ConceptWriter(final Concept c) {
            super();
            this.c = c;
        }

        @Override
        public void run() {
            try {
                while (c.isUnwritten() && !c.isCanceled()) {
                    Bdb.getConceptDb().writeConcept(c);
                }
            } catch (final Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            dbWriterPermit.release();
        }
    }

    private static class LuceneWriter implements Runnable {

        private final int batchSize = 200;
        private final IdentifierSet descNidsToWrite;

        public LuceneWriter(final IdentifierSet descNidsToCommit) {
            super();
            this.descNidsToWrite = descNidsToCommit;
        }

        @Override
        public void run() {
            try {
                ArrayList<Description> toIndex = new ArrayList<Description>(this.batchSize + 1);
                final I_IterateIds idItr = this.descNidsToWrite.iterator();
                int count = 0;
                while (idItr.next()) {
                    count++;
                    final Description d = (Description) Bdb.getComponent(idItr.nid());
                    toIndex.add(d);
                    if (count > batchSize) {
                        count = 0;
                        LuceneManager.writeToLucene(toIndex);
                        toIndex = new ArrayList<Description>(batchSize + 1);
                    }
                }
                LuceneManager.writeToLucene(toIndex);
            } catch (final Exception e) {
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

    public static void addUncommittedNoChecks(final I_ExtendByRef extension) {
        final RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
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

	public static void addUncommittedNoChecks(final I_GetConceptData concept) {
       ((Concept)concept).modified();
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
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void flushUncommitted() throws InterruptedException {
        final Concept c = lastUncommitted.getAndSet(null);
        if (c != null) {
            writeUncommitted(c);
        }
    }

    private static void writeUncommitted(final Concept c) throws InterruptedException {
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

    public static void addUncommitted(final I_ExtendByRef extension) {
        final RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
        addUncommitted(member.getEnclosingConcept());
    }

    private static class SetNidsForCid implements Runnable {

        Concept concept;

        public SetNidsForCid(final Concept concept) {
            super();
            this.concept = concept;
        }

        @Override
        public void run() {
            try {
                final Collection<Integer> nids = this.concept.getAllNids();
                final NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
                for (final int nid : nids) {
                    nidCidMap.setCNidForNid(this.concept.getNid(), nid);
                }
            } catch (final IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    public static void addUncommitted(ConceptChronicleBI igcd) {
        if (igcd == null) {
            return;
        }
        final Concept concept = (Concept) igcd;
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
                final Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();
                dataCheckMap.put(concept, warningsAndErrors);
                for (final I_TestDataConstraints test : creationTests) {
                    try {
                        warningsAndErrors.addAll(test.test(concept, false));
                        final Collection<RefsetMember<?, ?>> extensions = concept.getExtensions();
                        for (final RefsetMember<?, ?> extension : extensions) {
                            if (extension.isUncommitted()) {
                                warningsAndErrors.addAll(test.test(extension, false));
                            }
                        }
                    } catch (final Exception e) {
                        AceLog.getEditLog().alertAndLogException(e);
                    }
                }
            }

            uncommittedCNids.setMember(concept.getNid());
            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(concept));
            dbWriterService.execute(new ConceptWriter(concept));
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(new UpdateFrames(concept));
    }

    private static class UpdateFrames implements Runnable {

        Concept c;

        public UpdateFrames(final Concept c) {
            super();
            this.c = c;
        }

        @Override
        public void run() {
            if (getActiveFrame() != null) {
                final I_ConfigAceFrame activeFrame = getActiveFrame();
                for (final I_ConfigAceFrame frameConfig : activeFrame.getDbConfig()
                        .getAceFrames()) {
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
        } catch (final TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (final IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }
    private static boolean performCommit = false;

    public static void commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        lastCommit = Bdb.gVersion.incrementAndGet();
        Svn.rwl.acquireUninterruptibly();
        boolean passedRelease = false;
		
		AceLog.getAppLog().info("BDBCommitManager commit called");
        try {
            synchronized (uncommittedCNids) {
                synchronized (uncommittedCNidsNoChecks) {
                    flushUncommitted();
                    performCommit = true;
                    int errorCount = 0;
                    int warningCount = 0;
                    if (performCreationTests) {
                        final NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();
                        dataCheckMap.clear();
                        while (uncommittedCNidItr.next()) {
                            final List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
                            final Concept concept = Concept.get(uncommittedCNidItr.nid());
                            dataCheckMap.put(concept, warningsAndErrors);
                            for (final I_TestDataConstraints test : commitTests) {
                                try {
                                    warningsAndErrors.addAll(test.test(concept, true));
                                    final Collection<RefsetMember<?, ?>> extensions = concept.getExtensions();
                                    for (final RefsetMember<?, ?> extension : extensions) {
                                        if (extension.isUncommitted()) {
                                            warningsAndErrors.addAll(test.test(extension, true));
                                        }
                                    }
                                    for (final AlertToDataConstraintFailure alert : warningsAndErrors) {
                                        if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                                            errorCount++;
                                        } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                                            warningCount++;
                                        }
                                    }
                                } catch (final Exception e) {
                                    AceLog.getEditLog().alertAndLogException(e);
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
                                final int selection = JOptionPane.showConfirmDialog(
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
                                            final int selection = JOptionPane.showConfirmDialog(
                                                    new JFrame(),
                                                    "Do you want to continue with commit?",
                                                    "Warnings Detected",
                                                    JOptionPane.YES_NO_OPTION);
                                            performCommit = selection == JOptionPane.YES_OPTION;
                                        }
                                    });
                                } catch (final InvocationTargetException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                    performCommit = false;
                                }
                            }
                        }
                    }

                    if (performCommit) {
                        KindOfComputer.reset();
                        final long commitTime = System.currentTimeMillis();
                        final IntSet sapNidsFromCommit = Bdb.getSapDb().commit(
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
                                    final ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
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
                        notifyCommit();
                        uncommittedCNids.clear();
                        uncommittedCNidsNoChecks = Terms.get().getEmptyIdSet();

                        luceneWriterPermit.acquire();
                        final IdentifierSet descNidsToCommit = new IdentifierSet((IdentifierSet) uncommittedDescNids);
                        uncommittedDescNids.clear();
                        luceneWriterService.execute(new LuceneWriter(descNidsToCommit));
                        dataCheckMap.clear();
                    }
                }
            }
            if (performCommit) {
                Bdb.sync();
            }
        } catch (final IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (final InterruptedException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (final ExecutionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (final TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } finally {
            if (!passedRelease) {
                Svn.rwl.release();
            }
        }
        fireCommit();
        updateFrames();
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

            public void run() {
                try {
                    if (Terms.get().getActiveAceFrameConfig() != null) {
                        for (final I_ConfigAceFrame frameConfig : Terms.get().getActiveAceFrameConfig().getDbConfig()
                                .getAceFrames()) {
                            frameConfig.fireCommit();
                            frameConfig.setCommitEnabled(false);
                        }
                        updateAlerts();
                    }
                } catch (final TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (final IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
    }

    private static void fireCancel() {
        if (DwfaEnv.isHeadless()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    if (Terms.get().getActiveAceFrameConfig() != null) {
                        for (final I_ConfigAceFrame frameConfig : Terms.get().getActiveAceFrameConfig().getDbConfig()
                                .getAceFrames()) {
                            frameConfig.fireCommit();
                            frameConfig.setCommitEnabled(false);
                        }
                    }
                    updateAlerts();
                } catch (final TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (final IOException e) {
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
                    dataCheckMap.clear();
                } catch (final IOException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
        }
        fireCancel();
        updateFrames();
    }

    private static void handleCanceledConcepts(final I_RepresentIdSet uncommittedCNids2) throws IOException {
        final NidBitSetItrBI idItr = uncommittedCNids2.iterator();
        while (idItr.next()) {
            final Concept c = Concept.get(idItr.nid());
            if (c.isCanceled()) {
                Terms.get().forget(c);
            }
        }
    }

    public static void forget(final I_ConceptAttributeVersioned attr) throws IOException {
        final Concept c = Bdb.getConcept(attr.getConId());
        final ConceptAttributes a = (ConceptAttributes) attr;
        if (a.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (a.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
            } else {
                synchronized (a.revisions) {
                    final List<ConceptAttributesRevision> toRemove = new ArrayList<ConceptAttributesRevision>();
                    final Iterator<ConceptAttributesRevision> ri = a.revisions.iterator();
                    while (ri.hasNext()) {
                        final ConceptAttributesRevision ar = ri.next();
                        if (ar.getTime() == Long.MAX_VALUE) {
                            toRemove.add(ar);
                        }
                    }
                    for (final ConceptAttributesRevision r : toRemove) {
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

    public static void forget(final I_RelVersioned rel) throws IOException {
        final Concept c = Bdb.getConcept(rel.getC1Id());
        final Relationship r = (Relationship) rel;
        if (r.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (r.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
            } else {
                synchronized (r.revisions) {
                    final List<RelationshipRevision> toRemove = new ArrayList<RelationshipRevision>();
                    final Iterator<RelationshipRevision> ri = r.revisions.iterator();
                    while (ri.hasNext()) {
                        final RelationshipRevision rr = ri.next();
                        if (rr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(rr);
                        }
                    }
                    for (final RelationshipRevision tr : toRemove) {
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

    public static void forget(final I_DescriptionVersioned desc) throws IOException {
        final Description d = (Description) desc;
        final Concept c = Bdb.getConcept(d.getConceptNid());
        if (d.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (d.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
            } else {
                synchronized (d.revisions) {
                    final List<DescriptionRevision> toRemove = new ArrayList<DescriptionRevision>();
                    final Iterator<DescriptionRevision> di = d.revisions.iterator();
                    while (di.hasNext()) {
                        final DescriptionRevision dr = di.next();
                        if (dr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(dr);
                        }
                    }
                    for (final DescriptionRevision tr : toRemove) {
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

    public static void forget(final I_GetConceptData concept) throws IOException {
        final Concept c = (Concept) concept;
        Bdb.getConceptDb().forget(c);
    }

    @SuppressWarnings("unchecked")
    public static void forget(final I_ExtendByRef extension) throws IOException {
        final RefsetMember m = (RefsetMember) extension;
        final Concept c = Bdb.getConcept(m.getRefsetId());
        if (m.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (m.revisions == null) {
                throw new UnsupportedOperationException(
                        "Cannot forget a committed component.");
            } else {
                synchronized (m.revisions) {
                    final List<RefsetRevision<?, ?>> toRemove = new ArrayList<RefsetRevision<?, ?>>();
                    final Iterator<?> mi = m.revisions.iterator();
                    while (mi.hasNext()) {
                        final RefsetRevision<?, ?> mr = (RefsetRevision<?, ?>) mi.next();
                        if (mr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(mr);
                        }
                    }
                    for (final RefsetRevision tr : toRemove) {
                        m.removeRevision(tr);
                        tr.sapNid = -1;
                    }
                }
            }
        } else {
            // have to forget "all" references to component...
            c.getRefsetMembers().remove(m);
            c.getData().getMemberNids().remove(m.getMemberId());
            m.setStatusAtPositionNid(-1);
        }
        c.modified();
        Terms.get().addUncommittedNoChecks(c);
    }

	private static void loadTests(final String directory,
			final List<I_TestDataConstraints> list) {
		final File componentPluginDir = new File(pluginRoot + File.separator
                + directory);
        final File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {

            public boolean accept(final File arg0, final String fileName) {
                return fileName.toLowerCase().endsWith(".task");
            }
        });

        if (plugins != null) {
            for (final File f : plugins) {
                try {
                    final FileInputStream fis = new FileInputStream(f);
                    final BufferedInputStream bis = new BufferedInputStream(fis);
                    final ObjectInputStream ois = new ObjectInputStream(bis);
					final I_TestDataConstraints test = (I_TestDataConstraints) ois
							.readObject();
                    ois.close();
                    list.add(test);
                } catch (final Exception e) {
                    AceLog.getAppLog().alertAndLog(Level.WARNING,
                            "Processing: " + f.getAbsolutePath(), e);
                }
            }
        }
    }

    private static void doUpdate() {
        try {
            for (final Frame f : OpenFrames.getFrames()) {
                if (AceFrame.class.isAssignableFrom(f.getClass())) {
                    final AceFrame af = (AceFrame) f;
                    final ACE aceInstance = af.getCdePanel();
                    aceInstance.getDataCheckListScroller();
                    aceInstance.getUncommittedListModel().clear();

                    for (final Collection<AlertToDataConstraintFailure> alerts : dataCheckMap.values()) {
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
                        for (final TermComponentDataCheckSelectionListener l : aceInstance.getDataCheckListeners()) {
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
        } catch (final Exception e) {
            AceLog.getAppLog().warning(e.toString());
        }
    }

    public static void updateFrames() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                doUpdate();
            }
        });
    }

    public static void updateAlerts() {
        SwingUtilities.invokeLater(new Runnable() {

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

                public void run() {
                    removeUncommittedUpdateFrame(concept);
                }
            });
        }
    }

	private static void removeUncommittedUpdateFrame(final Concept concept) {
		for (final I_ConfigAceFrame frameConfig : getActiveFrame().getDbConfig()
				.getAceFrames()) {
            try {
                frameConfig.removeUncommitted(concept);
                updateAlerts();
                if (uncommittedCNids.cardinality() == 0) {
                    frameConfig.setCommitEnabled(false);
                }
            } catch (final Exception e) {
                AceLog.getAppLog().warning(e.toString());
            }
        }
    }

    public static Set<Concept> getUncommitted() {
        try {
            final Set<Concept> returnSet = new HashSet<Concept>();
            NidBitSetItrBI cNidItr = uncommittedCNids.iterator();
            while (cNidItr.next()) {
                returnSet.add(Concept.get(cNidItr.nid()));
            }
            cNidItr = uncommittedCNidsNoChecks.iterator();
            while (cNidItr.next()) {
                returnSet.add(Concept.get(cNidItr.nid()));
            }
            return returnSet;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
        final List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
        try {
            final NidBitSetItrBI cNidItr = uncommittedCNids.iterator();
            while (cNidItr.next()) {
                try {
                    final Concept toTest = Concept.get(cNidItr.nid());
                    for (final I_TestDataConstraints test : commitTests) {
                        try {
                            for (final AlertToDataConstraintFailure failure : test.test(toTest, true)) {
                                warningsAndErrors.add(failure);
                            }
                        } catch (final Exception e) {
                            AceLog.getEditLog().alertAndLogException(e);
                        }
                    }
                } catch (final IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        } catch (final IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return warningsAndErrors;
    }

    public static void addUncommittedDescNid(final int dNid) {
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
        notifyShutdown();
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

    public static void setCheckCreationDataEnabled(final boolean enabled) {
        performCreationTests = enabled;
    }

    public static void setCheckCommitDataEnabled(final boolean enabled) {
        performCommitTests = enabled;
    }

    /**
     * <p>
     * Register a commit listener
     * </p>
     * @param listener
     */
    public static void addCommitListener(final ICommitListener listener) {
        if (listener != null) {
            listeners = (ICommitListener[]) ArrayUtils.add(listeners, listener);
        }
    }

    /**
     * <p>
     * Remove the commit listener
     * </p>
     * @param listener
     */
    public static void removeCommitListener(final ICommitListener listener) {
        if (listener != null) {
            listeners = (ICommitListener[]) ArrayUtils.removeElement(listeners, listener);
        }
    }

    /**
     * <p>
     * Remove all registered commit listener
     * </p>
     */
    public static void removeAllCommitListeners() {
        if (listeners != null && listeners.length > 0) {
            listeners = new ICommitListener[0];
        }
    }

    /**
     * <p>
     * notify the commit event
     * </p>
     */
    private static void notifyShutdown() {
        if (listeners != null && listeners.length > 0) {
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

    private static void notifyCommit() {
        if (listeners != null && listeners.length > 0) {
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
}
