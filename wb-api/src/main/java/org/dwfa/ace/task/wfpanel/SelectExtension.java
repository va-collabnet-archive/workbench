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
package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.example.binding.Snomed;



//import org.ihtsdo.arena;

@BeanList(specs = { @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN)})
public class SelectExtension extends PreviousNextOrCancel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    // Task Attribute Properties
    private String instruction = "<html>Select extension for concept being moved:";
    private String relParentPropName = ProcessAttachmentKeys.REL_PARENT.getAttachmentKey();

    // Other Properties
    private JComboBox refsetSelectionComboBox;
    private I_GetConceptData selectedParentConcept;
    private WizardBI wizard; 
    

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(instruction);
        out.writeObject(relParentPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                instruction = (String) in.readObject();
                relParentPropName = (String) in.readObject();
            } else {
                instruction = "<html>Select Parent for Concept Being Retired:";
            }

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

   
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
    	
        try {
            // Present the user interface in the Workflow panel
        	wizard = (WizardBI) worker.readAttachement(WorkerAttachmentKeys.WIZARD_PANEL.name());
        	
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            restore();

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
            // check return condition for CONTINUE or ITEM_CANCELLED
            if (returnCondition == Condition.CONTINUE) {

                // get selected item from ComboBox presented to the user
                selectedParentConcept = (I_GetConceptData) refsetSelectionComboBox.getSelectedItem();
                
                process.setProperty(relParentPropName, selectedParentConcept);    
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
        }
        wizard.setWizardPanelVisible(false);
    }

    /**
     * This method overrides a method by the same name in the parent class. It
     * is used
     * to tell the parent class (PreviousNextOrCancel) whether to show
     * the previous button or not. Since we only want the Next and Cancel
     * buttons,
     * this method returns false .
     */
    @Override
    protected boolean showPrevious() {
        return false;
    }

   
    private class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;
        
        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
            wizard.setWizardPanelVisible(true); 
        }

        @Override
        protected Boolean construct() throws Exception {
            setup(process);
            return true;
        }

        @Override
        protected void finished() {
    
        	JPanel wizardPanel = wizard.getWizardPanel(); 
        	
        	Component[] components = wizardPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                wizardPanel.remove(components[i]);
            }
        	
            wizardPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH; 

            // Add the Instructions
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            wizardPanel.add(new JLabel(instruction), c);

            // Add the Refset Purpose List ComboBox
            c.gridx++;
            c.gridy = 0;
            I_GetConceptData parentList[] = new I_GetConceptData[3];
            try {
            	UUID[] uuidsArray = Snomed.CORE.getUuids();
            	List<UUID> uuidsCore = Arrays.asList(uuidsArray);
            	uuidsArray = Snomed.EXTENSION_0.getUuids();
            	List<UUID> uuidsExtn1 = Arrays.asList(uuidsArray);
            	uuidsArray = Snomed.EXTENSION_13.getUuids();
            	List<UUID> uuidsExtn2 = Arrays.asList(uuidsArray);
            	
            	parentList[0] = Terms.get().getConcept(uuidsCore);
            	parentList[1] = Terms.get().getConcept(uuidsExtn1);
            	parentList[2] = Terms.get().getConcept(uuidsExtn2);
            } catch (TerminologyException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
                   
            refsetSelectionComboBox = new JComboBox(parentList);   
            wizardPanel.add(refsetSelectionComboBox, c);
            
            // Add the processing buttons
            c.weightx = 0.0;
            setUpButtons(wizardPanel, c);
            
            //empty thing
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
        	c.weighty = 1;
            wizardPanel.add(new JPanel(), c);
        }

    }

    protected static String getToDoImage() {
        return "/24x24/plain/inbox_into.png";
    }

    protected void setUpButtons(final JPanel wizardPanel, GridBagConstraints c) {
        c.gridx++;
        wizardPanel.add(new JLabel("  "), c);
        c.gridx++;
        c.anchor = GridBagConstraints.SOUTHWEST;

        JButton continueButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getContinueImage())));
        continueButton.setToolTipText("continue");
        wizardPanel.add(continueButton, c);
        continueButton.addActionListener(new ContinueActionListener());
        c.gridx++;

        JButton cancelButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getCancelImage())));
        cancelButton.setToolTipText("cancel");
        wizardPanel.add(cancelButton, c);
        cancelButton.addActionListener(new CancelActionListener());
        c.gridx++;
        wizardPanel.add(new JLabel("     "), c);
        wizardPanel.validate();
        Container cont = wizardPanel;
        while (cont != null) {
            cont.validate();
            cont = cont.getParent();
        }
        continueButton.requestFocusInWindow();
        wizardPanel.repaint();
    }
    
    private class CancelActionListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            notifyTaskDone();
        }
        
    }

    /**
     * Get the instructions for this task
     * 
     * @return The instructions for this task.
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Set the instructions for this task
     * 
     * @param instruction The instructions for this task.
     * @return void
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
    
    public String getRelParentPropName() {
        return relParentPropName;
    }

    public void setRelParentPropName(String newStatusPropName) {
        this.relParentPropName = newStatusPropName;
    }

}
