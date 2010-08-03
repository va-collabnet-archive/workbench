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
package org.ihtsdo.ace.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import org.dwfa.ace.task.WorkerAttachmentKeys;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;


/* 
* @author Varsha Parekh
* 
*/
@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class WFConcept extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private I_ConfigAceFrame config = null;
    
    private static final Insets insets = new Insets(0, 0, 0, 0);
    JFrame frame;
    I_GetConceptData workflowConcept;
    private List<WorkflowHistoryJavaBean> possibleActions = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }
    
    public  void addComponent(Container container, Component component, int gridx, int gridy,
  	      int gridwidth, int gridheight, int anchor, int fill) {
  	    GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0,
  	        anchor, fill, insets, 0, 0);
  	    container.add(component, gbc);
  	  }

    /**
     * @TODO use a type 1 uuid generator instead of a random uuid...
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {    	 
    	 try {
            I_TermFactory tf = Terms.get();
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            Set<I_Position> positionSet = new HashSet<I_Position>();
            for (I_Path path : config.getEditingPathSet()) {
            	positionSet.add(tf.newPosition(path, Integer.MAX_VALUE));
            }
      
            final JPanel workflowPanel = config.getWorkflowPanel();          
            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            System.out.println("WF: Hierarchy Selection====>" + host.getHierarchySelection().getInitialText());
            I_GetConceptData test = config.getLastViewed();
            System.out.println("WF: last viewed concept=========>" + test.getInitialText());            
            workflowConcept = (I_GetConceptData) host.getTermComponent();
           
            if (workflowConcept == null) {
                throw new TaskFailedException("There is no concept in the component view to send to workflow actions...");
            }else{
            	System.out.println("WF: workflowConcept Selection====>" + workflowConcept.getInitialText());
            	     
                    	    String modelerStr = tf.getActiveAceFrameConfig().getUsername();
                            I_GetConceptData modeler = WorkflowRefsetHelper.lookupModeler(modelerStr);
                    	    EditorCategoryRefsetSearcher searcher = new EditorCategoryRefsetSearcher();
                    	  	try {
								possibleActions = searcher.searchForPossibleActions(modeler, workflowConcept);
							} catch (Exception e1) {
								throw new TaskFailedException("There is no workflow history associated with the selected concept...");
							}
							
            	            if(possibleActions.size() > 0){
	                    	  	try {
									SwingUtilities.invokeAndWait(new Runnable() {
									    public void run() {
									    System.out.println("<=====Inside SwingUtilities.invokeAndWait run method ====>");
									    clearWorkflowPanel();
									    workflowPanel.setVisible(true);
									    workflowPanel.setSize(700, 50);
									    workflowPanel.setLayout(new GridBagLayout());	  
									  /*  GridBagConstraints c = new GridBagConstraints();
									    c.weightx = 0.5;
									    c.fill = GridBagConstraints.LAST_LINE_END;
									    c.gridheight=1;
									    c.gridwidth=1;                    
									    c.gridx = 0;
									    c.gridy = 0;*/
									     	for(int i=0; i< possibleActions.size(); i++){
												WorkflowHistoryJavaBean templateBean = possibleActions.get(i);
												UUID conceptId =templateBean.getConceptId();
									            UUID workflowId =templateBean.getWorkflowId();
									            String fsn=templateBean.getFSN();
									            System.out.println("WF: conceptId : " + conceptId + "workflowId : " +workflowId + "fsn : " + fsn);
         
												JButton button;
												try {
													button = new JButton(templateBean.getAction().getInitialText());
												} catch (IOException e) {
													button = new JButton("COULDN\"T READ ACTION");
												}
												 
												//workflowPanel.add(button, c);
												addComponent(workflowPanel, button, i, 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE);
												button.addActionListener(new MyAction(templateBean));
									     	  }
									     	
									     	
									 	 	JButton cancelButton = new JButton("Cancel");
									 	 	//workflowPanel.add(cancelButton, c);
									        addComponent(workflowPanel, cancelButton, possibleActions.size(), 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE);
									        cancelButton.addActionListener(new CloseWindowAction());
									    }
									 });
								} catch (InterruptedException e) {
									System.out.println(e.getMessage());
								} catch (InvocationTargetException e) {
									System.out.println(e.getMessage());
								}
            	      }else{
            	    	  throw new TaskFailedException("There is no workflow history associated with the selected concept...");
            	      }
        		}
            
            return Condition.CONTINUE;
        } catch (TerminologyException e) {           
            throw new TaskFailedException(e);
        } catch (IOException e) {      
            throw new TaskFailedException(e);
	    } catch (IllegalArgumentException e) {
	        throw new TaskFailedException(e);
	    }
	    
    }

    
    public class MyAction implements ActionListener {
    	WorkflowHistoryJavaBean bean = null;
    	
    	public MyAction(WorkflowHistoryJavaBean b) {
    		bean = b;
    	}
    	
    	public void actionPerformed(ActionEvent e) {
    		WorkflowHistoryRefsetWriter writer;
          	try {
	        	  writer = new WorkflowHistoryRefsetWriter();
	        	  writer.updateWorkflowHistory(bean);	 
				 // workflowConcept.promote((I_Position) config.getViewPositionSetReadOnly(), config.getPromotionPathSetReadOnly(), config.getAllowedStatus(), config.getPrecedence());	
				  clearWorkflowPanel();
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			} catch (TerminologyException e1) {
				System.out.println(e1.getMessage());
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
			}
		}      
    }
    
    public class CloseWindowAction implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		clearWorkflowPanel();
    	}
    }
    
   
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    private void clearWorkflowPanel() {
        JPanel workflowPanel = config.getWorkflowPanel();
        Component[] workflowComponents = workflowPanel.getComponents();

        for (int i = 0; i < workflowComponents.length; i++) {
       	 	workflowPanel.remove(workflowComponents[i]);
        }  
        
        workflowPanel.setSize(500, 50);
        workflowPanel.updateUI();

    }
}
