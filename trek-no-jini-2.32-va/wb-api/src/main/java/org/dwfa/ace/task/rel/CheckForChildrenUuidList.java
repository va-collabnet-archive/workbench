/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dwfa.ace.task.rel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

@BeanList(specs = {
    @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN)})
public class CheckForChildrenUuidList extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
    private WizardBI wizard;
    private ArrayList<UUID> uuidList;
    private ArrayList<UUID> uncommittedUuidList;
    private transient Condition returnCondition;
    protected transient boolean done;
    private I_ConfigAceFrame config;
    private ViewCoordinate tempVc;
    private ViewCoordinate vc;
    private I_GetConceptData concept;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidListListPropName);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
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
            ViewCoordinate vc = config.getViewCoordinate();
            tempVc = new ViewCoordinate(vc);
            tempVc.setRelationshipAssertionType(RelAssertionType.STATED);
            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            concept = (I_GetConceptData) host.getTermComponent();

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
        }

        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {
            if (config.getEditingPathSet().isEmpty()) {
                throw new TaskFailedException("You must select at least one editing path. ");
            }

            Set<PositionBI> positionSet = new HashSet<PositionBI>();
            for (PathBI editPath : config.getEditingPathSet()) {
                positionSet.add(Terms.get().newPosition(editPath, Long.MAX_VALUE));
            }
            PositionSetReadOnly positionsForEdit = new PositionSetReadOnly(positionSet);

            // check return condition for CONTINUE
            if (returnCondition == Condition.PREVIOUS) {
                //move to list view
                ArrayList masterList = new ArrayList<List<UUID>>(); 
                //AddUuidListListToListView needs each concept to be in separate list
                for(UUID uuid : uuidList){
                    ArrayList list = new ArrayList<UUID>();
                    list.add(uuid);
                    masterList.add(list);
                }
                process.setProperty(uuidListListPropName, masterList);
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
            testForChildren();
        }

        @Override
        protected Boolean construct() throws Exception {
            //setup(process);
            return true;
        }

        @Override
        protected void finished() {
            if (uuidList.isEmpty() && uncommittedUuidList.isEmpty()) {
                returnCondition = Condition.CONTINUE;
                wizard.setWizardPanelVisible(false);
                done = true;
                notifyTaskDone();
            } else if(!uuidList.isEmpty() && uncommittedUuidList.isEmpty()) { //only committed children 
                wizard.setWizardPanelVisible(true);
                JPanel wizardPanel = wizard.getWizardPanel();
                //make wizard panel
                Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }
                wizardPanel.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1.0;
                c.weighty = 0;
                c.anchor = GridBagConstraints.EAST;

                //add concepts
                c.gridwidth = 2;
                c.ipady = 20;
                wizardPanel.add(new JLabel("<html>Please remove the children of the concept before retiring the concept."), c);
                c.gridy++;
                c.ipady = 20;
                wizardPanel.add(new JLabel("<html>Add concepts to list view for batch editing?"), c);
                c.gridy++;
                c.gridy++;

                //add buttons
                c.gridwidth = 1;
                c.weightx = 0;
                c.ipady = 0;
                JButton updateButton = new JButton("add to list");
                updateButton.addActionListener(new UpdateActionListener());
                wizardPanel.add(updateButton, c);
                c.gridx++;
                c.weightx = 1;
                JButton cancelButton = new JButton("cancel");
                cancelButton.addActionListener(new CancelActionListener());
                wizardPanel.add(cancelButton, c);
                c.gridx = 0;
                c.gridy++;
                c.weighty = 1;
                wizardPanel.add(new JLabel(" "), c);
            } else if(uuidList.isEmpty() && !uncommittedUuidList.isEmpty()){ //only uncommitted children
                wizard.setWizardPanelVisible(true);
                JPanel wizardPanel = wizard.getWizardPanel();
                //make wizard panel
                Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }
                wizardPanel.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1.0;
                c.weighty = 0;
                c.anchor = GridBagConstraints.EAST;

                //add concepts
                c.gridwidth = 2;
                c.ipady = 20;
                wizardPanel.add(new JLabel("<html>There are children concepts which are uncommited."
                        + "  Please resolve the uncomitted changes before continuing."), c);
                c.gridy++;
                c.ipady = 20;
                wizardPanel.add(new JLabel("<html>Add uncomitted children to list view?"
                        + "  (Uncommitted concepts will be highlighted in yellow.)"), c);
                c.gridy++;

                //add buttons
                c.ipady = 0;
                c.gridwidth = 1;
                c.weightx = 0;
                JButton updateButton = new JButton("add to list");
                updateButton.addActionListener(new UpdateActionListener());
                wizardPanel.add(updateButton, c);
                c.gridx++;
                c.weightx = 1;
                JButton cancelButton = new JButton("cancel");
                cancelButton.addActionListener(new CancelActionListener());
                wizardPanel.add(cancelButton, c);
                c.gridx = 0;
                c.gridy++;
                c.weighty = 1;
                wizardPanel.add(new JLabel(" "), c);
                
                for(UUID uuid : uncommittedUuidList){
                    if(!uuidList.contains(uuid)){
                        uuidList.add(uuid);
                    }
                }
                
                
            }else if(!uuidList.isEmpty() && !uncommittedUuidList.isEmpty()){ //uncommitted and committed children
                wizard.setWizardPanelVisible(true);
                JPanel wizardPanel = wizard.getWizardPanel();
                //make wizard panel
                Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }
                wizardPanel.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1.0;
                c.weighty = 0;
                c.anchor = GridBagConstraints.EAST;

                //add concepts
                c.gridwidth = 2;
                c.ipady = 20;
                wizardPanel.add(new JLabel("<html>This concept has children which are both committed and uncommitted.  "
                        + "  Please resolve the uncomitted changes before continuing."), c);
                c.gridy++;
                c.ipady = 20;
                wizardPanel.add(new JLabel("<html>Add uncomitted children to list view?"
                        + "  (Uncommitted concepts will be highlighted in yellow.)"), c);
                c.gridy++;
                c.ipady = 0;
                
                //add buttons
                c.gridwidth = 1;
                c.weightx = 0;
                JButton updateButton = new JButton("add to list");
                updateButton.addActionListener(new UpdateActionListener());
                wizardPanel.add(updateButton, c);
                c.gridx++;
                c.weightx = 1;
                JButton cancelButton = new JButton("cancel");
                cancelButton.addActionListener(new CancelActionListener());
                wizardPanel.add(cancelButton, c);
                c.gridx = 0;
                c.gridy++;
                c.weighty = 1;
                wizardPanel.add(new JLabel(" "), c);
                
                uuidList.clear();;
                for(UUID uuid : uncommittedUuidList){
                        uuidList.add(uuid);
                }
            }
        }
    }

    private void testForChildren() {
        try {
            ConceptVersionBI cv = Ts.get().getConceptVersion(tempVc, concept.getNid());

            Collection<? extends ConceptVersionBI> allRelsIncoming = cv.getRelationshipsIncomingSourceConcepts();
            Collection<? extends ConceptVersionBI> relsIncoming = cv.getRelationshipsIncomingSourceConceptsActiveIsa();
            uuidList = new ArrayList<UUID>();
            uncommittedUuidList = new ArrayList<UUID>();
            if (allRelsIncoming != null) {
                for (ConceptVersionBI relConcept : allRelsIncoming) {
                    if (relConcept.isUncommitted()) {
                        UUID uuid = Terms.get().nidToUuid(relConcept.getConceptNid());
                        uncommittedUuidList.add(uuid);
                    }
                }
            }
            if (relsIncoming != null) {
                for (ConceptVersionBI relConcept : relsIncoming) {
                    UUID uuid = Terms.get().nidToUuid(relConcept.getConceptNid());
                    uuidList.add(uuid);
                }
            }
        } catch (ContradictionException ex) {
            Logger.getLogger(CheckForChildrenUuidList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            e.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
        }
    }

    private class UpdateActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.PREVIOUS;
            done = true;
            notifyTaskDone();
        }
    }

    private class CancelActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            wizard.setWizardPanelVisible(false);
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            notifyTaskDone();
        }
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
        synchronized (CheckForChildrenUuidList.this) {
            CheckForChildrenUuidList.this.notifyAll();
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

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String uuidListListPropName) {
        this.uuidListListPropName = uuidListListPropName;
    }
}
