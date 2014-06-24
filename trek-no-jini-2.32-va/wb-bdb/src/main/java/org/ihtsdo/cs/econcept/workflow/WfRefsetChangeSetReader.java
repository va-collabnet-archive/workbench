package org.ihtsdo.cs.econcept.workflow;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.BdbProperty;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lucene.WfHxLuceneWriterAccessor;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Read Changeset files already imported searching for WfHx records in the changesets. If found, add changes
 * to WfHxLucene Directory.
 *
 * @author Jesse Efron
 */
public class WfRefsetChangeSetReader implements I_ReadChangeSet {

    private static final long serialVersionUID = 1L;
    private File changeSetFile;
    private File csreFile;
    private File csrcFile;
    private DataInputStream dataStream;
    private transient FileWriter csreOut;
    private transient FileWriter csrcOut;
    private I_Count counter;
    private int count = 0;
    private int conceptCount = 0;
    private Long nextCommit;
    private boolean noCommit = false;
    private boolean initialized = false;
    private String nextCommitStr;
    private final String wfPropertySuffix = "-WF";
    private static File firstFileRead = null;
    private transient List<I_ValidateChangeSetChanges> validators = new ArrayList<I_ValidateChangeSetChanges>();
    private static HashSet<TkRefexAbstractMember<?>> wfMembersToCommit = new HashSet<TkRefexAbstractMember<?>>();
    private static List<TkRefexAbstractMember<?>> unresolvedAnnotations = new ArrayList<TkRefexAbstractMember<?>>();
    private static WfConceptToRefsetMembersMap membersMap = new WfConceptToRefsetMembersMap();

    @Override
    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        if ((firstFileRead != null) && (firstFileRead.equals(changeSetFile))) {
            return Long.MAX_VALUE;
        }

        if (nextCommit == null) {
            if (dataStream == null) {
                nextCommit = Long.MAX_VALUE;
                nextCommitStr = "end of time";
            } else {
                try {
                    nextCommit = dataStream.readLong();
                    assert nextCommit != Long.MAX_VALUE;
                    nextCommitStr = TimeHelper.getFileDateFormat().format(
                            new Date(nextCommit));
                } catch (EOFException e) {
                    AceLog.getAppLog().info(
                            "No next commit time for file: " + changeSetFile);
                    nextCommit = Long.MAX_VALUE;
                    nextCommitStr = "end of time";
                }
            }
        }
        return nextCommit;
    }

    @Override
    public void readUntil(long endTime, Set<ConceptChronicleBI> annotationIndexes) throws IOException,
            ClassNotFoundException {
        HashSet<TimePathId> values = new HashSet<TimePathId>();

        if ((firstFileRead != null) && (firstFileRead.equals(changeSetFile))) {
            // Close previous error files of previous filters (does nothing if logging is off)
            for (AbstractWfChangeSetFilter scrubber : getPerChangesetWorkflowFilters()) {
                scrubber.closeErrorFile();
            }

            // Get All Refset Members found in all Change Set files
            HashSet<TkRefexAbstractMember<?>> membersToScrub = new HashSet<TkRefexAbstractMember<?>>();
            membersToScrub.addAll(membersMap.getAllMembers());

            // Filter out based on across-all-changesets filters
            for (AbstractWfChangeSetFilter scrubber : getAllChangesetsWorkflowFilters()) {
                if (scrubber.scrubMembers(membersToScrub)) {
                    membersToScrub = scrubber.getApprovedMembers();
                }
                scrubber.closeErrorFile();
            }

            wfMembersToCommit.clear();
            wfMembersToCommit.addAll(membersToScrub);

            // Done Scrubbing, now commit wfMembersToCommit into the workflow refsets
            importWfRefsetMembers(annotationIndexes);

            // Update the WfHx Refset based on wfMembersToCommit as well
            conceptCount = updateWfHxLuceneIndex();

            // Clear for future imports
            unresolvedAnnotations.clear();
            membersMap.clear();
            firstFileRead = null;
            wfMembersToCommit.clear();

            return;
        }

        while ((nextCommitTime() <= endTime)
                && (nextCommitTime() != Long.MAX_VALUE)) {
            try {
                EConcept eConcept = new EConcept(dataStream);
                if (csreOut != null) {
                    csreOut.append("\n*******************************\n");
                    csreOut.append(TimeHelper.formatDateForFile(nextCommitTime()));
                    csreOut.append("\n*******************************\n");
                    csreOut.append(eConcept.toString());
                }

                count++;
                if (counter != null) {
                    counter.increment();
                }

                // Process EConcept for WF Refset members and scrub them in preparation for all-member scrub/import/lucene gen
                addToEConceptProcessList(eConcept, nextCommit, values);

                conceptCount++;
                nextCommit = dataStream.readLong();
            } catch (EOFException ex) {
                dataStream.close();
                if (changeSetFile.length() == 0) {
                    changeSetFile.delete();
                }
                nextCommit = Long.MAX_VALUE;
                Terms.get().setProperty(
                        changeSetFile.getName() + wfPropertySuffix,
                        Long.toString(changeSetFile.length()));
                Terms.get().setProperty(
                        BdbProperty.LAST_CHANGE_SET_READ.toString()
                        + wfPropertySuffix, changeSetFile.getName());
                if (csreOut != null) {
                    csreOut.flush();
                    csreOut.close();
                    csreFile.delete();
                }
                if (csrcOut != null) {
                    csrcOut.flush();
                    csrcOut.close();
                    csrcFile.delete();
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        if (firstFileRead == null) {
            firstFileRead = changeSetFile;
        }

    }

    private int updateWfHxLuceneIndex() {
        if (wfMembersToCommit.size() > 0) {
            try {
                Runnable luceneWriter = WfHxLuceneWriterAccessor.addWfHxLuceneMembersFromEConcept(wfMembersToCommit);
                        
                if (luceneWriter != null) {
                    luceneWriter.run();
//                   wait until done to get count
                    return WfHxLuceneWriterAccessor.importCount;
                    
                }
            } catch (InterruptedException e) {
                AceLog.getAppLog().log(Level.WARNING, "Failed to generate WfHx Lucene Index on Change Set Import");
            }
        }
        return 0;
    }

    private void importWfRefsetMembers(Set<ConceptChronicleBI> annotationIndexes) {
        // reconstitute now scrubbed members into membersMap to assist in sorting for alg
        membersMap.clear();
        for (TkRefexAbstractMember<?> member : wfMembersToCommit) {
            membersMap.addNewMember(member);
        }

        try {
            for (UUID eConUid : membersMap.getKeySet()) {
                int conceptNid = Bdb.uuidToNid(eConUid);
                assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

                Concept c = Concept.get(conceptNid);

                if (c.isAnnotationStyleRefex()) {
                    for (TkRefexAbstractMember<?> er : membersMap.getMembers(eConUid)) {
                        ConceptComponent<?, ?> cc;
                        Object referencedComponent = Ts.get().getComponent(
                                er.getComponentUuid());

                        if (referencedComponent != null) {
                            if (referencedComponent instanceof Concept) {
                                cc = ((Concept) referencedComponent).getConAttrs();
                            } else {
                                cc = (ConceptComponent<?, ?>) referencedComponent;
                            }

                            RefsetMember<?, ?> r = (RefsetMember<?, ?>) Ts.get().getComponent(
                                    er.getPrimordialComponentUuid());

                            if (r == null) {
                                cc.addAnnotation(RefsetMemberFactory.create(er, Ts.get().getConceptNidForNid(cc.getNid())));
                            } else {
                                r.merge((RefsetMember) RefsetMemberFactory.create(
                                        er,
                                        Ts.get().getConceptNidForNid(cc.getNid())), 
                                        annotationIndexes);
                            }
                        } else {
                            unresolvedAnnotations.add(er);
                        }
                    }
                } else {
                    if ((c.getRefsetMembers() == null)
                            || c.getRefsetMembers().isEmpty()) {
                        for (TkRefexAbstractMember<?> er : membersMap.getMembers(eConUid)) {
                            if (!WorkflowHelper.getRefsetUidList().contains(
                                    er.refsetUuid)) {
                                continue;
                            }

                            RefsetMember<?, ?> refsetMember = RefsetMemberFactory.create(er, c.getConceptNid());

                            c.getData().add(refsetMember);
                            //						Terms.get().addUncommittedNoChecks(refsetMember);
                        }
                    } else {
                        Set<Integer> currentMemberNids = c.getData().getMemberNids();

                        for (TkRefexAbstractMember<?> er : membersMap.getMembers(eConUid)) {
                            if (!WorkflowHelper.getRefsetUidList().contains(
                                    er.refsetUuid)) {
                                continue;
                            }
                            int rNid = Bdb.uuidToNid(er.primordialUuid);
                            RefsetMember<?, ?> r = c.getRefsetMember(rNid);

                            if (currentMemberNids.contains(rNid) && (r != null)) {
                                r.merge((RefsetMember) RefsetMemberFactory.create(
                                        er, c.getNid()), annotationIndexes);
                            } else {
                                c.getRefsetMembers().add(
                                        RefsetMemberFactory.create(er, c.getNid()));
                            }
                        }
                    }
                }

                // Commit Concept
                BdbCommitManager.addUncommittedNoChecks(c);
            }

            Concept.resolveUnresolvedAnnotations(unresolvedAnnotations, annotationIndexes);
        } catch (Exception e) {
            AceLog.getEditLog().severe("Failed importing wfMembersToCommit");
        }
    }

    private void addToEConceptProcessList(EConcept eConcept, long time, Set<TimePathId> values)
            throws IOException, ClassNotFoundException {
        try {
            assert time != Long.MAX_VALUE;
            ArrayList<TkRefexAbstractMember<?>> discoveredMembers = new ArrayList<TkRefexAbstractMember<?>>();

            // Identify the annotations associated with this concept 
            if (eConcept.getConceptAttributes() != null && eConcept.getConceptAttributes().getAnnotations() != null) {
                discoveredMembers.addAll(eConcept.getConceptAttributes().getAnnotations());
            }

            // If Refset Concept, identify refset members (non annotations)
            if (eConcept.getRefsetMembers() != null) {
                discoveredMembers.addAll(eConcept.getRefsetMembers());
            }

            // Process all annotation & non-annotation members
            if (discoveredMembers.size() > 0) {
                HashSet<TkRefexAbstractMember<?>> membersToScrub = new HashSet<TkRefexAbstractMember<?>>();
                membersToScrub.addAll(discoveredMembers);

                // Filter out based on Single CS Filters
                for (AbstractWfChangeSetFilter scrubber : getPerChangesetWorkflowFilters()) {
                    if (membersToScrub.size() > 0) {
                        if (scrubber.scrubMembers(membersToScrub)) {
                            membersToScrub = scrubber.getApprovedMembers();
                        }
                    }
                }

                for (TkRefexAbstractMember<?> member : membersToScrub) {
                    if (!membersMap.alreadyProcessed(member)) {
                        membersMap.addNewMember(member);
                    }
                }
            }
        } catch (Exception e) {
            AceLog.getEditLog().severe(
                    "Error committing bean in change set: " + changeSetFile
                    + "\nUniversalAceBean:  \n" + eConcept);
            throw new ToIoException(e);
        }
    }

    @Override
    public void read(Set<ConceptChronicleBI> annotationIndexes) throws IOException, ClassNotFoundException {
        readUntil(Long.MAX_VALUE, annotationIndexes);
    }

    private void lazyInit() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        String lastImportSize = null;
        if (changeSetFile != null) {
            lastImportSize = Terms.get().getProperty(
                    changeSetFile.getName() + wfPropertySuffix);
        }
        if (lastImportSize != null) {
            long lastSize = Long.parseLong(lastImportSize);
            if (lastSize == changeSetFile.length()) {
                nextCommit = Long.MAX_VALUE;
                initialized = true;
            }
        }
        if (initialized == false) {
            if (changeSetFile != null) {
                FileInputStream fis = new FileInputStream(changeSetFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                dataStream = new DataInputStream(bis);

                if (EConceptChangeSetWriter.writeDebugFiles) {
                    csreFile = new File(changeSetFile.getParentFile(),
                            changeSetFile.getName() + ".csre");
                    csreOut = new FileWriter(csreFile, true);
                    csrcFile = new File(changeSetFile.getParentFile(),
                            changeSetFile.getName() + ".csrc");
                    csrcOut = new FileWriter(csrcFile, true);
                }
            }
            initialized = true;
        }
    }

    @Override
    public File getChangeSetFile() {
        return changeSetFile;
    }

    @Override
    public void setChangeSetFile(File changeSetFile) {
        this.changeSetFile = changeSetFile;
    }

    @Override
    public void setCounter(I_Count counter) {
        this.counter = counter;
    }

    @Override
    public List<I_ValidateChangeSetChanges> getValidators() {
        return validators;
    }

    @Override
    public boolean isContentMerged() {
        return true;
    }

    @Override
    public int availableBytes() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        lazyInit();
        if ((firstFileRead != null) && (firstFileRead.equals(changeSetFile))) {
            return 0;
        } else {
            if (dataStream != null) {
                return dataStream.available();
            }
        }

        return 0;
    }

    public boolean isNoCommit() {
        return noCommit;
    }

    public void setNoCommit(boolean noCommit) {
        this.noCommit = noCommit;
    }

    private List<AbstractWfChangeSetFilter> getAllChangesetsWorkflowFilters() {
        List<AbstractWfChangeSetFilter> retSet = new ArrayList<AbstractWfChangeSetFilter>();

        // Filter out multiple members with same SAP
        retSet.add(new WfDuplicateSapMemberFilter("duplicateSapMember.txt"));

        // Filter out those members that were adjudicated offline and then synced, thus causing conflicts with others that did same
        retSet.add(new WfDuplicateAutomatedAdjudicatorSyncFilter("duplicateAdjudicationsFilter.txt"));

        return retSet;
    }

    private List<AbstractWfChangeSetFilter> getPerChangesetWorkflowFilters() {
        List<AbstractWfChangeSetFilter> retSet = new ArrayList<AbstractWfChangeSetFilter>();

        // Filter out badly-formed eConcepts
        retSet.add(new WfBadConceptFilter("badConceptFilter.txt"));

        // Filter out non-Wf Refsets
        retSet.add(new WfRefsetFilter("refsetFilter.txt"));

        // For wfHxRefsetMembers, filter out members whose date is before the wfHx.txt final date
        //retSet.add(new WfPostLastReleaseFilter("releaseDateFilter.txt"));

        return retSet;
    }
    
    @Override
    public int getConceptCount() {
        return conceptCount;
    }
    
    @Override
    public boolean isForWorkflow(){
        return true;
    }
}
