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
package org.dwfa.ace.task;

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
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class CheckForConceptsInList extends AbstractTask implements ActionListener{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private String conceptListPropName = ProcessAttachmentKeys.DEFAULT_CONCEPT_LIST.getAttachmentKey();

    private WizardBI wizard;
    private ArrayList<List<UUID>> uuidList;
    private transient Condition returnCondition;
    protected transient boolean done;
    private I_ConfigAceFrame config;
    private ViewCoordinate vc;
    private I_GetConceptData concept;
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(conceptListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	conceptListPropName = (String) in.readObject();
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
             vc = config.getViewCoordinate();
             
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
         } /*catch (IntrospectionException e) {
        	 throw new TaskFailedException(e);
         } catch (IllegalAccessException e) {
        	 throw new TaskFailedException(e);
         }*/

         return returnCondition;
    }
    
    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {
        	if(returnCondition != Condition.CONTINUE){
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
        }

        @Override
        protected Boolean construct() throws Exception {
            return true;
        }

        @Override
        protected void finished(){
        	try {
				checkListView(process);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
	            returnCondition = Condition.ITEM_CANCELED;
	            wizard.setWizardPanelVisible(false);
			} catch (IntrospectionException e) {
				e.printStackTrace();
	            returnCondition = Condition.ITEM_CANCELED;
	            wizard.setWizardPanelVisible(false);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
	            returnCondition = Condition.ITEM_CANCELED;
	            wizard.setWizardPanelVisible(false);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
	            returnCondition = Condition.ITEM_CANCELED;
	            wizard.setWizardPanelVisible(false);
			}
        	if(returnCondition != Condition.CONTINUE){
        		wizard.setWizardPanelVisible(true);
        		JPanel wizardPanel = wizard.getWizardPanel(); 
            	//make wizard panel
            	Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }
                
                //add concepts
                wizardPanel.add(new JLabel("<html>There are concepts in the listview<br>Do you want to replace them?<br>"));
                
                //add buttons
                JButton updateButton = new JButton("replace");
                updateButton.addActionListener(new updateActionListener());
                wizardPanel.add(updateButton);
                JButton continueButton = new JButton("cancel");
                wizardPanel.add(continueButton);
                continueButton.addActionListener(CheckForConceptsInList.this);
        	}else{
        		wizard.setWizardPanelVisible(false);
        	}
        }
    }
    
    
    private class updateActionListener implements ActionListener{

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
    
    private void checkListView(I_EncodeBusinessProcess process) 
    		throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException{
    	//check if concepts
    	List<I_GetConceptData> concepts = (List<I_GetConceptData>) process.getProperty(conceptListPropName);
		if (concepts.size() == 0){
			returnCondition = Condition.CONTINUE;
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
		synchronized (CheckForConceptsInList.this) {
			CheckForConceptsInList.this.notifyAll();
        }
	}
    
    public boolean isDone() {
        return this.done;
    }
    
    public Collection<Condition> getConditions() {
    	return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }
    
    public String getConceptListPropName() {
        return conceptListPropName;
    }

    public void setConceptListPropName(String conceptListPropName) {
        this.conceptListPropName = conceptListPropName;
    }
}
