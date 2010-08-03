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
package org.ihtsdo.ace.task.signpost;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;


import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;



@BeanList(specs = { @Spec(directory = "tasks/ide/gui/signpost", type = BeanType.TASK_BEAN) })
public class ShowWFSearch extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String htmlPropName = ProcessAttachmentKeys.SIGNPOST_HTML.getAttachmentKey();
    private static final Insets insets = new Insets(0, 0, 0, 0);
    JFrame frame;
    
    
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(htmlPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
            htmlPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    
    

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        
    	try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            final String htmlStr = (String) process.getProperty(htmlPropName);
            final JPanel workflowPanel = config.getSignpostPanel();
            
            /* final JFrame frame = new JFrame("GridBagLayout");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridBagLayout());
            JButton button;
            // Row One - Three Buttons
            button = new JButton("One");
            addComponent(frame, button, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            button = new JButton("Two");
            addComponent(frame, button, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            button = new JButton("Three");
            addComponent(frame, button, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            // Row Two - Two Buttons
            button = new JButton("Four");
            addComponent(frame, button, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            button = new JButton("Five");
            addComponent(frame, button, 2, 1, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            // Row Three - Two Buttons
            button = new JButton("Six");
            addComponent(frame, button, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            button = new JButton("Seven");
            addComponent(frame, button, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
            frame.setSize(500, 200);
            frame.setVisible(true);*/
            
            
            SwingUtilities.invokeAndWait(new Runnable() {
            	public void run() {            		
            	
                    Component[] components = workflowPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        workflowPanel.remove(components[i]);
                    }
                    
                    showImage = new javax.swing.JLabel();
        	        //showImage.setIcon(new javax.swing.ImageIcon("C:\\Workbench_Bundle\\Jesse WF Initial\\sample-test\\src\\WF_Image.jpg"));
        	        showImage.setIcon(new javax.swing.ImageIcon("icons/WF_Image.jpg"));
        	        workflowPanel.add(showImage);
        	        showImage.setVisible(true);
        	        
                    /*String[] searchCriterion = {"Workflow Search Options......", "Modeler", "Status", "FSN", "Future Option" };                    
                    JComboBox comboBox = new JComboBox();
                    for (int i = 0; i < searchCriterion.length; i++){
                    	comboBox.addItem(searchCriterion[i]);
                    }                    
        			JTextField seachValue = new JTextField(30);
        			JButton searchButton =new JButton("Search");   
        			searchButton.addActionListener(new MyAction());
                    workflowPanel.setLayout(new GridBagLayout());
                    addComponent(workflowPanel, comboBox, 0, 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE);
        			addComponent(workflowPanel, seachValue, 1, 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE);
        			addComponent(workflowPanel, searchButton, 2, 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE);        	
        			comboBox.addActionListener(new ActionListener() {      
    		    	public void actionPerformed(ActionEvent e) {
    		    		seachValue.setText("select value : " + comboBox.getSelectedIndex() + "   " + ((JComboBox) e.getSource()).getSelectedItem());
    		      	}*/
                    //GridBagConstraints c = new GridBagConstraints();                  
                    //c.weightx = 0.5;
        			//c.fill = GridBagConstraints.NONE;
        			
        			//workflowPanel.add(comboBox, c);
        			//workflowPanel.add(seachValue, c);        			
        			//workflowPanel.add(searchButton, c);
                    //workflowPanel.add(new JLabel(htmlStr), c);
                    //JScrollPane scroll_pane = new JScrollPane(new JLabel(htmlStr));
                    //workflowPanel.add(scroll_pane, c);
                    
                    workflowPanel.validate();
                    Container cont = workflowPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                }
            });
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }
    
    private static void addComponent(Container container, Component component, int gridx, int gridy,
    	      int gridwidth, int gridheight, int anchor, int fill) {
    	    GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0,
    	        anchor, fill, insets, 0, 0);
    	    container.add(component, gbc);
    	  }
    
    
    public class MyAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
          JOptionPane.showMessageDialog(frame,"Hi Varsha!!!!!!!!!!");
        }
      }
    
    private javax.swing.JLabel showImage;
    
 /*   public class trial() {
    	public void trial(){
    		showImage = new javax.swing.JLabel();
	        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	        showImage.setIcon(new javax.swing.ImageIcon("C:\\Workbench_Bundle\\Jesse WF Initial\\sample-test\\src\\riya.jpg"));
	        add(showImage);
	        pack();
    	}
    }*/

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getHtmlPropName() {
        return htmlPropName;
    }

    public void setHtmlPropName(String instruction) {
        this.htmlPropName = instruction;
    }
}
