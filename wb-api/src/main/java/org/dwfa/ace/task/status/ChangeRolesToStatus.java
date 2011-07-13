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
package org.dwfa.ace.task.status;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf1;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

@BeanList(specs = {
    @Spec(directory = "tasks/ide/status", type = BeanType.TASK_BEAN)})
public class ChangeRolesToStatus extends AbstractTask implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
    private TermEntry newStatus = new TermEntry(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
    private WizardBI wizard;
    private ArrayList<List<UUID>> uuidList;
    private transient Condition returnCondition;
    protected transient boolean done;
    private I_ConfigAceFrame config;
    private ViewCoordinate tempVc;
    private ViewCoordinate vc;
    private I_GetConceptData concept;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newStatus);
        out.writeObject(activeConceptPropName);
        out.writeObject(uuidListListPropName);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            newStatus = (TermEntry) in.readObject();
            activeConceptPropName = (String) in.readObject();
            uuidListListPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            done = false; //reset
            wizard = (WizardBI) worker.readAttachement(WorkerAttachmentKeys.WIZARD_PANEL.name());
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            concept = (I_GetConceptData) process.getProperty(activeConceptPropName);
            ViewCoordinate vc = config.getViewCoordinate();
            tempVc = new ViewCoordinate(vc);
            tempVc.setRelAssertionType(RelAssertionType.STATED);

            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }

            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        doRun(process, worker);
                    }
                });
            }

        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (ExecutionException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }

        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {
            if (config.getEditingPathSet().size() == 0) {
                throw new TaskFailedException("You must select at least one editing path. ");
            }

            Set<PositionBI> positionSet = new HashSet<PositionBI>();
            for (PathBI editPath : config.getEditingPathSet()) {
                positionSet.add(Terms.get().newPosition(editPath, Long.MAX_VALUE));
            }
            PositionSetReadOnly positionsForEdit = new PositionSetReadOnly(positionSet);
            I_GetConceptData newStatusConcept = Terms.get().getConcept(newStatus.ids);

            // check return condition for CONTINUE or ITEM_CANCELLED
            if (returnCondition == Condition.CONTINUE) {
                Collection<? extends RelationshipChronicleBI> relsOut = concept.getRelsOutgoing();
                //get rels that are NOT isa
                for (RelationshipChronicleBI rel : relsOut) {
                    for (RelationshipVersionBI relv : rel.getVersions(tempVc)) {
                        if (!(tempVc.getIsaTypeNids().contains(relv.getTypeNid()))) {
                            //change status
                            for (PathBI editPath : config.getEditingPathSet()) {
                                Set<I_RelPart> partsToAdd = new HashSet<I_RelPart>();
                                I_RelPart newPart = (I_RelPart) relv.makeAnalog(
                                        newStatusConcept.getConceptNid(),
                                        config.getDbConfig().getUserConcept().getNid(),
                                        editPath.getConceptNid(),
                                        Long.MAX_VALUE);
                            }

                        }
                    }
                }
                Terms.get().addUncommitted(concept);

            } else if (returnCondition == Condition.PREVIOUS) {
                process.setProperty(uuidListListPropName, uuidList);
                wizard.setWizardPanelVisible(false);
            } else {
                wizard.setWizardPanelVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
            wizard.setWizardPanelVisible(false);
        }
    }

    private class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
            testForTarget();
            testForRefersTo();
        }

        @Override
        protected Boolean construct() throws Exception {
            //setup(process);
            return true;
        }

        @Override
        protected void finished() {
            if (uuidList.size() == 0) {
                returnCondition = Condition.CONTINUE;
                wizard.setWizardPanelVisible(false);
                done = true;
                notifyTaskDone();
            } else {
                wizard.setWizardPanelVisible(true);
                JPanel wizardPanel = wizard.getWizardPanel();
                //make wizard panel
                Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }

                //add concepts
                wizardPanel.add(new JLabel("<html>The concept is a target of a relationship in another concept(s) <br>or a target in the  'refers to' refset"));
                wizardPanel.add(new JLabel("<html>Add concepts to list view for batch editing?<br>"));

                //add buttons
                wizardPanel.add(new JLabel(" "));
                JButton updateButton = new JButton("add to list");
                updateButton.addActionListener(new updateActionListener());
                wizardPanel.add(updateButton);
                JButton continueButton = new JButton("cancel");
                wizardPanel.add(continueButton);
                continueButton.addActionListener(ChangeRolesToStatus.this);
            }
        }
    }

    private void testForTarget() {
        try {
            //find roles which concept is target of
            Collection<? extends RelationshipChronicleBI> relsIn = concept.getRelsIncoming();
            if (relsIn == null) {
                returnCondition = Condition.CONTINUE;
                done = true;
                notifyTaskDone();
            } else {
                uuidList = new ArrayList<List<UUID>>();
                for (RelationshipChronicleBI rel : relsIn) {
                    //for(I_RelPart rp : rel.getMutableParts())
                    Collection<? extends RelationshipVersionBI> relVersions = rel.getVersions(tempVc);
                    for (RelationshipVersionBI relv : relVersions) {
                        if (relv.isStated()) {
                            if (!(tempVc.getIsaTypeNids().contains(relv.getTypeNid()))) {
                                UUID uuid = Terms.get().nidToUuid(rel.getConceptNid());
                                List<UUID> list = new ArrayList<UUID>();
                                list.add(uuid);
                                uuidList.add(list);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
        }
    }

    private void testForRefersTo() {
        try {
            ConceptChronicleBI refexConcept = null;
            if (Ts.get().hasUuid(SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient().getPrimUuid())) {
                refexConcept = SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient();
            } else {
                refexConcept = SnomedMetadataRf1.REFERS_TO_REFSET_RF1.getLenient();
            }
            //find if concept is member of Refers To refset
            Collection<? extends RefexCnidVersionBI<?>> refexMembers =
                    (Collection<? extends RefexCnidVersionBI<?>>) refexConcept.getCurrentRefsetMembers(tempVc);
            int count = 0;
            for (RefexCnidVersionBI member : refexMembers) {
                if (member.getCnid1() == concept.getNid()) {
                    List<UUID> uuids = Ts.get().getUuidsForNid(Ts.get().getConceptNidForNid(member.getReferencedComponentNid()));
                    uuidList.add(uuids);
                    count++;
                }
            }
            if (count == 0) {
                returnCondition = Condition.CONTINUE;
                done = true;
                notifyTaskDone();
            }

        } catch (IOException e) {
            e.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
        }
    }

    private class updateActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.PREVIOUS;
            done = true;
            notifyTaskDone();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        wizard.setWizardPanelVisible(false);
        returnCondition = Condition.ITEM_CANCELED;
        done = true;
        notifyTaskDone();
    }

    protected void restore(final I_EncodeBusinessProcess process, final I_Work worker) throws
            InterruptedException, InvocationTargetException {
        if (SwingUtilities.isEventDispatchThread()) {
            doRun(process, worker);
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    doRun(process, worker);
                }
            });
        }
    }

    protected void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    protected void notifyTaskDone() {
        synchronized (ChangeRolesToStatus.this) {
            ChangeRolesToStatus.this.notifyAll();
        }
    }

    public boolean isDone() {
        return this.done;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String propName) {
        this.activeConceptPropName = propName;
    }

    public TermEntry getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TermEntry newStatus) {
        this.newStatus = newStatus;
    }

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String uuidListListPropName) {
        this.uuidListListPropName = uuidListListPropName;
    }
}
