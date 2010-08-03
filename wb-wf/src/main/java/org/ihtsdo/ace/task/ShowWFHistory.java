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
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;


/* 
* @author Varsha Parekh
* 
*/
@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class ShowWFHistory extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    
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
    
    
    /**
     * @TODO use a type 1 uuid generator instead of a random uuid...
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    	
        try {        	        	
        	final I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            I_GetConceptData selectedConceptInList =  (I_GetConceptData) configFrame.getListConceptViewer().getTermComponent();
            final JPanel signPostPanel = configFrame.getSignpostPanel();
           
            
            /*
            System.out.println("<======List Size ======>" + configFrame.getBatchConceptList().size());            
            System.out.println("<=== WF: Logged In User======>" + configFrame.getUsername());    
            I_GetConceptData lastViewConcept = configFrame.getLastViewed();
            System.out.println("WF: last viewed concept=========>" + lastViewConcept.getInitialText());              
            JList conceptList = configFrame.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();            
            for (int i = 0; i < model.getSize(); i++) {
             I_GetConceptData conceptInList = model.getElementAt(i);
             System.out.println("WF: concepts in list view=========>" +conceptInList.getInitialText());
            }
            */            
            if (selectedConceptInList == null) {
                throw new TaskFailedException("There is no concept selected from the list view to see workflow history...");
            }else{
	        	 WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
	        	 final Set<WorkflowHistoryJavaBean> beanList = searcher.searchForWFHistory(selectedConceptInList);
	        	 
	        	 if (beanList.size() == 0)
	                 throw new TaskFailedException("There is no workflow history associated with the selected concept: " + selectedConceptInList);

		        	 SwingUtilities.invokeAndWait(new Runnable() {
		                 public void run() {                	 
		                	 Component[] signPostcomponents = signPostPanel.getComponents();
		                     for (int i = 0; i < signPostcomponents.length; i++) {
		                         signPostPanel.remove(signPostcomponents[0]);
		                     } 
		                    
		                    JTable table;
		             		TableColumn tcol;	
		             		String col [] = {"WorkflowId","FSN","Action","ConceptId","Clinical Editor","Path","State","TimeStamp","UseCase"};
		             		String data[][] = new String[beanList.size()][]; 
		   	        	         	
		             		Iterator<WorkflowHistoryJavaBean> itr = beanList.iterator();
		             		int i = 0;
		             		
		             		while (itr.hasNext())
		             		{
		             			WorkflowHistoryJavaBean bean = (WorkflowHistoryJavaBean)itr.next();
		             			try {
		             				
				   	         	 	final String workflowId =  bean.getWorkflowId().toString();
				   	         	 	final String fsn =  bean.getFSN();
				   	         	 	final String action = bean.getAction().getInitialText();
				   	         	 	final String conceptId = bean.getConceptId().toString();
				   	         	 	final String modeler = bean.getModeler().getInitialText();
				   	         	 	final String path =  bean.getPath().getInitialText();
				   	         	 	final String state =  bean.getState().getInitialText();
				   	         	 	final String timeStamp =  bean.getTimeStamp();
				   	         	 	final String useCase = bean.getUseCase().toString();

				   	         	 	String d[] = new String[] {workflowId, fsn, action, conceptId, modeler, path, state, timeStamp, useCase}; 
				   	         	 	data[i] = d;             	          
		             			}
		             			catch (IOException e) {
		             				data[i] = new String[] {"", "", "", "", "", "", "", "", ""};
		             			}
		             			
		             			i++;
		             		}
		             		
		             	    DefaultTableModel model = new DefaultTableModel(data,col);
		             	    table = new JTable(model);
		             	    table.setFont((new Font("Lucida Grande", Font.PLAIN, 11)));
		             	    tcol = table.getColumnModel().getColumn(0);
		             	    tcol.setCellRenderer(new CustomTableCellRenderer());
		             	    tcol = table.getColumnModel().getColumn(1);
		             	    tcol.setCellRenderer(new CustomTableCellRenderer());
		             	    tcol = table.getColumnModel().getColumn(2);
		             	    tcol.setCellRenderer(new CustomTableCellRenderer());
		             	    tcol = table.getColumnModel().getColumn(3);
		            	    tcol.setCellRenderer(new CustomTableCellRenderer());
		            	    tcol = table.getColumnModel().getColumn(4);
		             	    tcol.setCellRenderer(new CustomTableCellRenderer());
		             	    tcol = table.getColumnModel().getColumn(5);
		            	    tcol.setCellRenderer(new CustomTableCellRenderer());
		            	    tcol = table.getColumnModel().getColumn(6);
		            	    tcol.setCellRenderer(new CustomTableCellRenderer());
		            	    tcol = table.getColumnModel().getColumn(7);
		            	    tcol.setCellRenderer(new CustomTableCellRenderer());
		            	    tcol = table.getColumnModel().getColumn(8);
		            	    tcol.setCellRenderer(new CustomTableCellRenderer());
		            	    
		            	    
		             	    JTableHeader header = table.getTableHeader();
		             	    //header.setBackground(Color.darkGray);
		             	    JScrollPane pane = new JScrollPane(table);
		             	    configFrame.setShowSignpostPanel(true);
		             	   
		             	    signPostPanel.setLayout(new BorderLayout());             	    
		             	    signPostPanel.add(pane ,BorderLayout.CENTER);    
		             	   
		                    signPostPanel.validate();
		                    Container cont = signPostPanel;
		                    while (cont != null) {
		                        cont.validate();
		                        cont = cont.getParent();
		                    }
		                 }
		        	});
	            }
        	return Condition.CONTINUE;          
          	
		} catch (TerminologyException e) {
				System.out.println(e.getMessage());			
        } catch (IOException e) {      
            throw new TaskFailedException(e);
	    } catch (InterruptedException e) {
	        throw new TaskFailedException(e);
	    } catch (InvocationTargetException e) {
	        throw new TaskFailedException(e);
	    } catch (IllegalArgumentException e) {
	        throw new TaskFailedException(e);
	    } catch (Exception e) {
			System.out.println(e.getMessage());
	    }
	    return Condition.CONTINUE;
    }        
    
    public class CustomTableCellRenderer extends DefaultTableCellRenderer{
        public Component getTableCellRendererComponent (JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
        	  Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
	          if (isSelected) {
	            //cell.setBackground(Color.green);
	          }else {
	            if (row % 2 == 0) {
	             // cell.setBackground(Color.cyan);
	            }else {
	             // cell.setBackground(Color.blue);
	            }
	          }
	          return cell;
        }
    }
   
       
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
