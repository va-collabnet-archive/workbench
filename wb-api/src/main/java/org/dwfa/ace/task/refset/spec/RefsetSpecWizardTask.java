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
package org.dwfa.ace.task.refset.spec;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.util.FileContent;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The RefsetSpecWizardTask prompts the user to input required data to create a
 * new refset. This includes: refset name, additional comments, requestor,
 * deadline, priority, file attachments and a designated editor.
 * 
 * @author Chrissy Hill
 * 
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class RefsetSpecWizardTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private Condition condition;
    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {

            termFactory = Terms.get();
            final// TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            condition = Condition.ITEM_CANCELED;
            I_GetConceptData userParent = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
            I_IntSet allowedTypes = termFactory.getActiveAceFrameConfig().getDestRelTypes();

            // create list of editors -> FSN, for use in the drop down list
            final Set<? extends I_GetConceptData> allValidUsers = userParent.getDestRelOrigins(allowedTypes);
            final TreeMap<String, I_GetConceptData> validUserMap = new TreeMap<String, I_GetConceptData>();
            I_GetConceptData fsnConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            I_IntSet fsnAllowedTypes = termFactory.newIntSet();
            fsnAllowedTypes.add(fsnConcept.getConceptNid());
            for (I_GetConceptData validUser : allValidUsers) {
                String latestDescription = null;
                int latestVersion = Integer.MIN_VALUE;
                List<? extends I_DescriptionTuple> descriptionResults =
                        validUser.getDescriptionTuples(null, fsnAllowedTypes, null, config.getPrecedence(), config
                            .getConflictResolutionStrategy());

                for (I_DescriptionTuple descriptionTuple : descriptionResults) {
                    if (descriptionTuple.getVersion() > latestVersion) {
                        latestVersion = descriptionTuple.getVersion();
                        latestDescription = descriptionTuple.getText();
                    }
                }

                if (getInbox(validUser) != null) {
                    validUserMap.put(latestDescription, validUser);
                }
            }

            // check permissions for the current user - they require
            // "create new refset" permission either as a user role or
            // individual permission
            I_GetConceptData owner = config.getDbConfig().getUserConcept();
            TestForCreateNewRefsetPermission permissionTest = new TestForCreateNewRefsetPermission();
            Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
            permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(owner));
            permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(owner));

            final TreeMap<String, I_GetConceptData> validNewRefsetParentMap =
                    createFsnConceptMap(permissibleRefsetParents);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {

                    JFrame frame = new JFrame("New refset spec wizard");
                    NewRefsetSpecWizard wizard = new NewRefsetSpecWizard(frame);
                    wizard.getDialog().setTitle("New refset spec wizard");

                    NewRefsetSpecForm1 panel1 = new NewRefsetSpecForm1(wizard, validNewRefsetParentMap.keySet());
                    wizard.registerWizardPanel("panel1", panel1);

                    NewRefsetSpecForm2 panel2 = new NewRefsetSpecForm2(wizard, validUserMap, validNewRefsetParentMap);
                    wizard.registerWizardPanel("panel2", panel2);

                    wizard.setCurrentPanel("panel1");
                    frame.setLocationRelativeTo(null); // center frame

                    wizard.showModalDialog();
                    boolean createData = wizard.isCreateData();
                    if (createData) {

                        String refsetName = panel1.getRefsetNameTextField();
                        String comments = panel1.getRefsetRequirementsTextField();
                        if (comments == null) {
                            comments = "";
                        }
                        HashSet<File> attachments = panel1.getAttachments();
                        I_GetConceptData refsetParent = validNewRefsetParentMap.get(panel1.getSelectedParent());

                        String requestor = panel2.getRequestor();
                        I_GetConceptData editor = validUserMap.get(panel2.getSelectedEditor());

                        Calendar deadline = panel2.getDeadline();
                        String priority = panel2.getPriority();

                        Set<String> reviewerNames = panel2.getSelectedReviewers();
                        Set<UUID> reviewers = new HashSet<UUID>();

                        Priority p;
                        if (priority.equals("Highest")) {
                            p = Priority.HIGHEST;
                        } else if (priority.equals("High")) {
                            p = Priority.HIGH;
                        } else if (priority.equals("Normal")) {
                            p = Priority.NORMAL;
                        } else if (priority.equals("Low")) {
                            p = Priority.LOW;
                        } else if (priority.equals("Lowest")) {
                            p = Priority.LOWEST;
                        } else {
                            p = null;
                        }

                        try {
                            I_GetConceptData owner =
                                    termFactory.getActiveAceFrameConfig().getDbConfig().getUserConcept();

                            if (owner == null) {
                                RefsetSpecWizardTask.this.setCondition(Condition.ITEM_CANCELED);
                                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                    "Refset wizard cannot be completed. The current user has not been set.", "",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                            for (String reviewerName : reviewerNames) {
                                reviewers.add((validUserMap.get(reviewerName)).getUids().iterator().next());
                            }

                            process.setOriginator(config.getUsername());
                            String editorInbox = getInbox(editor);
                            process.setName(refsetName);
                            process.setSubject("Creation Request");

                            if (editorInbox == null) {
                                RefsetSpecWizardTask.this.setCondition(Condition.ITEM_CANCELED);
                                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                    "Refset wizard cannot be completed. The selected editor has no assigned inbox.",
                                    "", JOptionPane.ERROR_MESSAGE);
                            } else {
                                process.setDestination(editorInbox);
                                process.setPriority(p);
                                process.setDeadline(deadline.getTime());
                                process.setProperty(ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey(),
                                    refsetParent.getUids().iterator().next());
                                process
                                    .setProperty(ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey(), refsetName);
                                process.setProperty(ProcessAttachmentKeys.MESSAGE.getAttachmentKey(), comments);
                                process.setProperty(ProcessAttachmentKeys.REQUESTOR.getAttachmentKey(), requestor);
                                process.setProperty(ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey(), reviewers
                                    .toArray(new UUID[] {}));
                                process.setProperty(ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey(),
                                    new UUID[] { owner.getUids().iterator().next() });
                                process.setProperty(ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey(),
                                    new UUID[] { editor.getUids().iterator().next() });
                                for (File file : attachments) {
                                    process.writeAttachment(file.getName(), new FileContent(file));
                                }

                                RefsetSpecWizardTask.this.setCondition(Condition.ITEM_COMPLETE);
                                wizard.getDialog().dispose();
                            }
                        } catch (Exception e) {
                            RefsetSpecWizardTask.this.setCondition(Condition.ITEM_CANCELED);
                            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Refset wizard cannot be completed. " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    } else {
                        RefsetSpecWizardTask.this.setCondition(Condition.ITEM_CANCELED);
                    }
                }
            });

            return getCondition();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskFailedException(ex);
        }
    }

    private TreeMap<String, I_GetConceptData> createFsnConceptMap(Set<I_GetConceptData> concepts)
            throws TerminologyException, IOException {
        TreeMap<String, I_GetConceptData> map = new TreeMap<String, I_GetConceptData>();

        for (I_GetConceptData concept : concepts) {

            map.put(concept.getInitialText(), concept);
        }
        return map;
    }

    public String getInbox(I_GetConceptData concept) throws TerminologyException, IOException {
        // find the inbox string using the concept's "user inbox" description
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        I_GetConceptData descriptionType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(descriptionType.getConceptNid());
        String latestDescription = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet activeStatuses = Terms.get().newIntSet();
        activeStatuses.add(Terms.get().getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.getUids())).getConceptNid());
        activeStatuses.add(Terms.get().getConcept((ArchitectonicAuxiliary.Concept.CURRENT.getUids())).getConceptNid());
        activeStatuses.add(Terms.get().getConcept((ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.getUids()))
            .getConceptNid());
        activeStatuses.add(Terms.get().getConcept((ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()))
            .getConceptNid());
        activeStatuses.add(Terms.get().getConcept((ArchitectonicAuxiliary.Concept.LIMITED.getUids())).getConceptNid());
        activeStatuses.add(Terms.get().getConcept((ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()))
            .getConceptNid());

        List<? extends I_DescriptionTuple> descriptionResults =
                concept.getDescriptionTuples(activeStatuses, allowedTypes, null, config.getPrecedence(), config
                    .getConflictResolutionStrategy());
        for (I_DescriptionTuple descriptionTuple : descriptionResults) {
            if (descriptionTuple.getVersion() > latestVersion) {
                latestVersion = descriptionTuple.getVersion();
                latestDescription = descriptionTuple.getText();
            }
        }
        return latestDescription;
    }

    public void setCondition(Condition c) {
        condition = c;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public Condition getCondition() {
        return condition;
    }
}
