/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.refset.refresh;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.spec.ConceptSpec;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The RefreshRefsetSpecCompareTask uses the information
 * collected in the RefreshRefsetSpecWizardTask task to create a list of
 * differences
 * between the selected Refset and the selected version of SNOMED.
 * 
 * @author Perry Reid
 * @version 1, November 2009
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class RefreshRefsetSpecCompareTask extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Concept Constants: (taken from: SNOMED CT Concept -> Linkage concept ->
    // Attribute -> Concept history attribute)
    public static final ConceptSpec SAME_AS = new ConceptSpec("SAME AS", "87594159-50f0-3b5f-aa4f-f6061c0ce497");
    public static final ConceptSpec MAY_BE_A = new ConceptSpec("MAY BE A", "721dadc2-53a0-3ffa-8abd-80ff6aa87db2");
    public static final ConceptSpec REPLACED_BY = new ConceptSpec("REPLACED BY", "0b010f24-523b-3ae4-b3a2-ec1f425c8a85");
    public static final ConceptSpec MOVED_TO = new ConceptSpec("MOVED TO", "c3394436-568c-327a-9d20-4a258d65a936");
    // Concept Constants: (taken from: Terminology Auxiliary Concept -> status
    // -> inactive)
    public static final ConceptSpec MOVED_ELSEWHERE = new ConceptSpec("moved elsewhere",
        "76367831-522f-3250-83a4-8609ab298436");

    // Task Attribute Properties
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String refsetPositionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
    private String snomedPositionSetPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
    private String reviewCountPropName = ProcessAttachmentKeys.REVIEW_COUNT.getAttachmentKey();
    private String reviewIndexPropName = ProcessAttachmentKeys.REVIEW_INDEX.getAttachmentKey();

    // Other Properties
    private Condition condition;
    private I_TermFactory termFactory;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(refsetPositionSetPropName);
        out.writeObject(snomedPositionSetPropName);
        out.writeObject(uuidListListPropName);
        out.writeObject(reviewCountPropName);
        out.writeObject(reviewIndexPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
                refsetPositionSetPropName = (String) in.readObject();
                snomedPositionSetPropName = (String) in.readObject();
                uuidListListPropName = (String) in.readObject();
                reviewCountPropName = (String) in.readObject();
                reviewIndexPropName = (String) in.readObject();
            }

            // Initialize transient properties...

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a process to another user's input queue).
     * 
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /**
     * Performs the primary action of the task, which in this case is Identify
     * all the
     * differences between the selected Refset Spec and the selected version of
     * SNOMED
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            /*
             * --------------------------------------------
             * Get Values from process Keys
             * -------------------------------------------
             */

            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());

            termFactory = LocalVersionedTerminology.get();
            UUID refsetUuid = (UUID) process.getProperty(refsetUuidPropName);
            I_GetConceptData refsetConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(refsetUuid);

            I_GetConceptData refsetSpecConcept = null;
            if (refsetConcept != null) {
                Set<I_GetConceptData> specs = RefsetHelper.getSpecificationRefsetForRefset(refsetConcept, config);
                if (specs.size() > 0) {
                    refsetSpecConcept = specs.iterator().next();
                }
            }
            assert refsetSpecConcept != null;

            // Get the Refset Position (must convert from UniversalAcePosition
            // type)
            // Retrieve the positions as Set<UniversalAcePosition> and convert
            // them back to Set<I_Position>
            Set<I_Position> refsetPositionSet = new HashSet<I_Position>();
            Set<UniversalAcePosition> universalRefsetPositions = (Set<UniversalAcePosition>) process.getProperty(refsetPositionSetPropName);
            for (UniversalAcePosition univPos : universalRefsetPositions) {
                I_Path path = termFactory.getPath(univPos.getPathId());
                I_Position thinPos = termFactory.newPosition(path, termFactory.convertToThinVersion(univPos.getTime()));
                refsetPositionSet.add(thinPos);
            }

            // Get the SNOMED Position (must convert from UniversalAcePosition
            // type)
            // Retrieve the positions as Set<UniversalAcePosition> and convert
            // them back to Set<I_Position>
            Set<I_Position> snomedPositionSet = new HashSet<I_Position>();
            Set<UniversalAcePosition> universalSnomedPositions = (Set<UniversalAcePosition>) process.getProperty(snomedPositionSetPropName);
            for (UniversalAcePosition univPos : universalSnomedPositions) {
                I_Path path = termFactory.getPath(univPos.getPathId());
                I_Position thinPos = termFactory.newPosition(path, termFactory.convertToThinVersion(univPos.getTime()));
                snomedPositionSet.add(thinPos);
            }

            // DEBUG: Echo out the retrieved values
            System.out.println("PARAMETERS PASSED IN THROUGH KEYS");
            System.out.println("=================================");
            System.out.println("   REFSET SPEC NAME = " + refsetSpecConcept.getInitialText());
            System.out.println("   REFSET POSITION SET = " + refsetPositionSet.toString());
            System.out.println("   SNOMED POSITION SET = " + snomedPositionSet.toString());

            /*
             * ---------------------------------------------------
             * Define some local variables to support the queries
             * ---------------------------------------------------
             */
            // The changesList will be used to loop through and launch a review
            // process
            // for each entry it contains
            List<Collection<UUID>> changesList = new ArrayList<Collection<UUID>>();

            // The allowedStatus are used in the queries
            I_IntSet allowedStatus = null;

            // Define the status: "Not Current"
            // The status "Not Current" means a member of the set of not current
            // status
            // values. Since there is no single value, you need to test for
            // membership
            // in the set of all the children of inactive:
            I_IntSet notCurrentStatus = termFactory.newIntSet();
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.CONFLICTING.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.NOT_YET_CREATED.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.RETIRED_MISSPELLED.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.IMPLIED_RELATIONSHIP.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid());
            notCurrentStatus.add(ArchitectonicAuxiliary.Concept.EXTINCT.localize().getNid());

            // Define the status: "Current"
            // The status "Current" means a member of the set of current status
            // values. Since there is no single value, you need to test for
            // membership
            // in the set of all the children of active:
            I_IntSet currentStatus = config.getAllowedStatus();

            int refsetSpecNid = refsetSpecConcept.getNid();
            Collection<I_ThinExtByRefVersioned> possibleSpecs = termFactory.getRefsetExtensionMembers(refsetSpecNid);

            // TODO Remove DEBUG statements!
            System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                + "\n	Finding clauses in this Refset Spec that need to be refreshed."
                + "\n	possibleSpecs = termFactory.getAllExtensionsForComponent(refsetSpecNid);" + "\n		refsetSpecNid="
                + refsetSpecNid + "\n		possibleSpecs=" + possibleSpecs.size());

            for (I_ThinExtByRefVersioned ext : possibleSpecs) {
                if (ext.getRefsetId() == refsetSpecNid) {
                    // we are now knowing it is a spec.

                    // TODO Remove DEBUG statements!
                    System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                        + "\n	we are now knowing it is a spec since: (ext.getRefsetId() == refsetSpecNid)");

                    // Retrieves tuples matching the specified allowedStatuses
                    // and positions - tuples are
                    // returned in the supplied specTuples List parameter
                    allowedStatus = currentStatus;
                    boolean addUncommitted = false;
                    boolean returnConflictResolvedLatestState = false;
                    List<I_ThinExtByRefTuple> specTuples = new ArrayList<I_ThinExtByRefTuple>();
                    ext.addTuples(allowedStatus, refsetPositionSet, specTuples, addUncommitted,
                        returnConflictResolvedLatestState);

                    // TODO Remove DEBUG statements!
                    System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                        + "\n	Retrieving all the tuples for this RefsetSpec that are current..." + "\n		specTuples="
                        + specTuples.size());

                    // Search the results of the previous query for tuples of
                    // the type CONCEPT_CONCEPT_CONCEPT_EXTENSION or
                    // CONCEPT_CONCEPT_EXTENSION
                    int conceptConceptConceptNid = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize()
                        .getNid();
                    int conceptConceptNid = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();
                    for (I_ThinExtByRefTuple tuple : specTuples) {
                        if (tuple.getTypeId() == conceptConceptConceptNid) {

                            // TODO Remove DEBUG statements!
                            System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                + "\n	Found a tuple of type: CONCEPT_CONCEPT_CONCEPT_EXTENSION");

                            // Break down the tuple into it's component parts
                            // and check to see if any of
                            // those parts need to be refreshed from the
                            // selected version of SNOMED.
                            I_ThinExtByRefPartConceptConceptConcept ccPart = (I_ThinExtByRefPartConceptConceptConcept) tuple.getPart();
                            I_GetConceptData part1 = termFactory.getConcept(ccPart.getC1id());
                            I_GetConceptData part2 = termFactory.getConcept(ccPart.getC2id());
                            I_GetConceptData part3 = termFactory.getConcept(ccPart.getC3id());

                            boolean hasRetiredConcept = false;
                            if (part1.getConceptAttributeTuples(notCurrentStatus, snomedPositionSet).size() > 0) {
                                // Need to refresh this one...
                                hasRetiredConcept = true;
                                // TODO Remove DEBUG statements!
                                System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                    + "\n	Need to refresh part 1: " + part1.getInitialText());

                            }
                            if (part2.getConceptAttributeTuples(notCurrentStatus, snomedPositionSet).size() > 0) {
                                // Need to refresh this one...
                                hasRetiredConcept = true;
                                // TODO Remove DEBUG statements!
                                System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                    + "\n	Need to refresh part 2: " + part1.getInitialText());
                            }
                            if (part3.getConceptAttributeTuples(notCurrentStatus, snomedPositionSet).size() > 0) {
                                // Need to refresh this one...
                                hasRetiredConcept = true;
                                // TODO Remove DEBUG statements!
                                System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                    + "\n	Need to refresh part 3: " + part1.getInitialText());
                            }

                            // Add to tuple to changesList
                            if (hasRetiredConcept) {
                                changesList.add(termFactory.getUids(tuple.getMemberId()));
                            }

                        } else if (tuple.getTypeId() == conceptConceptNid) {

                            // TODO Remove DEBUG statements!
                            System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                + "\n	Found a tuple of type: CONCEPT_CONCEPT_EXTENSION");

                            // Break down the tuple into it's component parts
                            // and check to see if any of
                            // those parts need to be refreshed from the
                            // selected version of SNOMED.
                            I_ThinExtByRefPartConceptConcept ccPart = (I_ThinExtByRefPartConceptConcept) tuple.getPart();
                            I_GetConceptData part1 = termFactory.getConcept(ccPart.getC1id());
                            I_GetConceptData part2 = termFactory.getConcept(ccPart.getC2id());
                            boolean hasRetiredConcept = false;

                            if (part1.getConceptAttributeTuples(notCurrentStatus, snomedPositionSet).size() > 0) {
                                // Need to refresh this one...
                                hasRetiredConcept = true;
                                // TODO Remove DEBUG statements!
                                System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                    + "\n	Need to refresh part 1: " + part1.getInitialText());
                            }
                            if (part2.getConceptAttributeTuples(notCurrentStatus, snomedPositionSet).size() > 0) {
                                // Need to refresh this one...
                                hasRetiredConcept = true;
                                // TODO Remove DEBUG statements!
                                System.out.println("DEBUG: RefreshRefsetSpecCompareTask.evaluate()"
                                    + "\n	Need to refresh part 2: " + part1.getInitialText());
                            }
                            if (hasRetiredConcept) {
                                changesList.add(termFactory.getUids(tuple.getMemberId()));
                            }
                        } // End If
                    } // End For
                } // End If
            } // End For

            /*
             * -------------------------------------------------------------
             * Store the list of differences in the uuidListListPropName
             * ------------------------------------------------------------
             */
            if (changesList == null || changesList.size() == 0) {
                // Nothing to process... Cancel the task so we can warn the
                // user.
                RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_CANCELED);
            } else {
                // Set task completion status to ITEM_COMPLETE
                RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_COMPLETE);
                process.setProperty(this.uuidListListPropName, changesList);
            }

            return getCondition();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskFailedException(ex);
        }

    }

    public void setCondition(Condition c) {
        condition = c;
    }

    public Condition getCondition() {
        return condition;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getRefsetPositionSetPropName() {
        return refsetPositionSetPropName;
    }

    public void setRefsetPositionSetPropName(String refsetPositionSetPropName) {
        this.refsetPositionSetPropName = refsetPositionSetPropName;
    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

    public String getSnomedPositionSetPropName() {
        return snomedPositionSetPropName;
    }

    public void setSnomedPositionSetPropName(String snomedPositionSetPropName) {
        this.snomedPositionSetPropName = snomedPositionSetPropName;
    }

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String uuidListListPropName) {
        this.uuidListListPropName = uuidListListPropName;
    }

    public String getReviewCountPropName() {
        return reviewCountPropName;
    }

    public void setReviewCountPropName(String reviewCountPropName) {
        this.reviewCountPropName = reviewCountPropName;
    }

    public String getReviewIndexPropName() {
        return reviewIndexPropName;
    }

    public void setReviewIndexPropName(String reviewIndexPropName) {
        this.reviewIndexPropName = reviewIndexPropName;
    }

}
