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
package org.dwfa.ace.task.rel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

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
    private ArrayList<List<UUID>> uuidList;
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
            tempVc.setRelAssertionType(RelAssertionType.STATED);
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
            testForChildren();
        }

        @Override
        protected Boolean construct() throws Exception {
            //setup(process);
            return true;
        }

        @Override
        protected void finished() {
            if (uuidList.isEmpty()) {
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
                wizardPanel.add(new JLabel("<html>Please remove the children of the concept before retiring the concept"));
                wizardPanel.add(new JLabel("<html>Add concepts to list view for batch editing?<br>"));

                //add buttons
                wizardPanel.add(new JLabel(" "));
                JButton updateButton = new JButton("add to list");
                updateButton.addActionListener(new UpdateActionListener());
                wizardPanel.add(updateButton);
                JButton cancelButton = new JButton("cancel");
                wizardPanel.add(cancelButton);
                cancelButton.addActionListener(new CancelActionListener());
            }
        }
    }

    private void testForChildren() {
        try {
            ConceptVersionBI cv = Ts.get().getConceptVersion(tempVc, concept.getNid());

            Collection<? extends ConceptVersionBI> relsIncoming = cv.getRelsIncomingOriginsActiveIsa();
            if (relsIncoming != null) {
                uuidList = new ArrayList<List<UUID>>();
                for (ConceptVersionBI rel : relsIncoming) {
                    UUID uuid = Terms.get().nidToUuid(rel.getConceptNid());
                    List<UUID> list = new ArrayList<UUID>();
                    list.add(uuid);
                    uuidList.add(list);
                }
            }
        } catch (ContraditionException ex) {
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
