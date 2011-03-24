/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel.ContinueActionListener;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.example.binding.WbDescType;
import org.ihtsdo.util.swing.GuiUtil;

import org.ihtsdo.tk.helper.TerminologyHelperDrools;

/**
 *
 * @author kec
 */

@BeanList(specs = { @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
        @Spec(directory = "tasks/arena/wizard", type = BeanType.TASK_BEAN) })
public class NewConcept extends PreviousNextOrCancel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    // Task Attribute Properties
    private String instruction = "<html>Enter data for new concept:";
    private String relParentPropName = ProcessAttachmentKeys.REL_PARENT.getAttachmentKey();
    // Other Properties
    private I_ConfigAceFrame config;
    private I_HostConceptPlugins host;
    private transient WizardBI wizard;
    private TerminologyList tl;
    private JTextField fsn;
    private JTextField pref;
    private JTextField us;
    private JTextField gb;
    private JCheckBox gbBox;
    private JCheckBox usBox;
    private JLabel gbLabel;
    private JLabel usLabel;
    private boolean addUsDesc = false;
    private boolean addGbDesc = false;
    private List<Integer> nidList;
    private ConceptCB conceptSpec;
    private DescCAB descSpecGb;
    private DescCAB descSpecUs;
    private RefexCAB refexSpecGb;
    private RefexCAB refexSpecUs;
    private TerminologyConstructorBI tc;
    private ConceptChronicleBI newConcept;


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

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @Override
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            // Present the user interface in the Workflow panel
            wizard = (WizardBI) worker.readAttachement(WorkerAttachmentKeys.WIZARD_PANEL.name());
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            host = (I_HostConceptPlugins) 
            worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());

            DoSwing swinger = new DoSwing(process);
            swinger.execute();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            restore();

            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
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
        }

        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {
            // check return condition for CONTINUE or ITEM_CANCELLED
            if (returnCondition == Condition.CONTINUE) {
            	createBlueprints();
            	if(addUsDesc){
            		tc.construct(descSpecUs);
            		tc.construct(refexSpecUs);
            	}
            	if(addGbDesc){
            		tc.construct(descSpecGb);
            		tc.construct(refexSpecGb);
            	}
            	I_AmTermComponent newTerm = (I_AmTermComponent) newConcept;
            	Ts.get().addUncommitted(newConcept);
            	host.setTermComponent(newTerm);
            	wizard.setWizardPanelVisible(false);
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

    private class DoSwing extends SwingWorker<Boolean, Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
            wizard.setWizardPanelVisible(true);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            setup(process);
            return true;
        }

        @Override
        protected void done() {
            try {
                get();
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
                c.gridwidth = 2;
                c.anchor = GridBagConstraints.EAST;
                wizardPanel.add(new JLabel(instruction), c);
                // Add the processing buttons
                c.weightx = 0.0;
                c.gridx = 2;
                c.gridwidth = 1;
                setUpButtons(wizardPanel, c);

                
                c.gridx = 0;
                c.gridy ++;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridwidth = 6;
                c.gridy ++;
                wizardPanel.add(new JLabel("fully specified name:"), c);
                c.gridy ++;
                c.weightx = 1.0;
                fsn = new JTextField();
                fsn.addActionListener(new CopyTextActionListener());
                wizardPanel.add(fsn, c);

                c.gridy ++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("preferred name:"), c);
                c.gridy ++;
                c.weightx = 1.0;
                pref = new JTextField(); 
                wizardPanel.add(pref, c);

                
                c.gridy ++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("parents:"), c);
                c.gridy ++;
                c.weightx = 1.0;
                c.weighty = 1.0;
                tl = new TerminologyList(config);
                wizardPanel.add(tl, c);
                c.weighty = 0.0;
                
                c.gridy ++;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy ++;
                c.weightx = 1.0;
                c.gridwidth = 1;
                c.weightx = 0.0;
                gbBox = new JCheckBox();
                gbBox.addActionListener(new GbDialectActionListener());
                gbBox.setVisible(false);
                wizardPanel.add(gbBox, c);
                c.gridx++;
                c.gridwidth = 3;
                c.weightx = 1.0;
                gb = new JTextField();
                gb.setVisible(false);
                wizardPanel.add(gb, c);
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                gbLabel = new JLabel("en-GB");
                gbLabel.setVisible(false);
                wizardPanel.add(gbLabel, c);
                c.gridy ++;
                c.gridx = 0;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy ++;
                
                
                c.gridy ++;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy ++;
                c.weightx = 1.0;
                c.gridwidth = 1;
                c.weightx = 0.0;
                usBox = new JCheckBox();
                usBox.addActionListener(new UsDialectActionListener());
                usBox.setVisible(false);
                wizardPanel.add(usBox, c);
                c.gridx++;
                c.gridwidth = 3;
                c.weightx = 1.0;
                us = new JTextField();
                us.setVisible(false);
                wizardPanel.add(us, c);
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                usLabel = new JLabel("en-US");
                usLabel.setVisible(false);
                wizardPanel.add(usLabel, c);
                c.gridy ++;
                c.gridx = 0;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy ++;

                //empty thing
                c.gridx = 0;
                c.gridy++;
                c.weightx = 0;
                c.weighty = 1;
                wizardPanel.add(new JPanel(), c);
                GuiUtil.tickle(wizardPanel);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewConcept.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(NewConcept.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        continueButton.addActionListener(new BlueprintContinueActionListener());
        c.gridx++;

        JButton cancelButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getCancelImage())));
        cancelButton.setToolTipText("cancel");
        wizardPanel.add(cancelButton, c);
        cancelButton.addActionListener(new StopActionListener());
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
    
   public class CopyTextActionListener implements ActionListener{
	   
	   @Override
	   public void actionPerformed(ActionEvent e) {
		String fsnText = fsn.getText();
		int paren = fsnText.indexOf("(");
		String prefText = "";
		
		if (paren == -1){
			prefText = fsnText;
			pref.setText(prefText);
		}
		else{
			prefText = fsnText.substring(0, paren-1);
			pref.setText(prefText);
		}
		
		addSpellingVarients(prefText);
	}
	   
   }
    
    public void addSpellingVarients(String prefText){
    	TerminologyHelperDrools th = new TerminologyHelperDrools();
    	String us = "en-us";
    	String gb = "en-gb";
    	String varient = "";
    	
    	if(th.loadProperties()){
    		if(th.checkTermSpelling(prefText, us) && th.checkTermSpelling(prefText, gb)){
    			//do nothing
    		}else if(th.checkTermSpelling(prefText, us)){ //check if lang is en-us
    			varient = th.getSpellingTerm(prefText,us);
    			this.gb.setText(varient);
    			this.gbBox.setVisible(true);
    			this.gb.setVisible(true);
    			this.gbLabel.setVisible(true);
    		}else if(th.checkTermSpelling(prefText, gb)){ //check if lang is en-gb
    			varient = th.getSpellingTerm(prefText,gb);
    			this.us.setText(varient);
    			this.usBox.setVisible(true);
    			this.us.setVisible(true);
    			this.usLabel.setVisible(true);
    		}
    	}
    }

   
    private void createBlueprints(){
    	UUID isa = UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"); //this is for "Is a"
    	tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(), 
                 config.getViewCoordinate());
    	
    	try {
    	//get parents
    	TerminologyListModel model = (TerminologyListModel) tl.getModel();
    	nidList = model.getNidsInList();
    	UUID[] uuidArray = new UUID[nidList.size()];
   
    	for(int index = 0; index < nidList.size(); index++){
    		uuidArray[index] = Terms.get().nidToUuid(nidList.get(index));
    	}

   	 	
    	//create concept blue print
    	 conceptSpec = new ConceptCB(fsn.getText(), pref.getText(), "en", isa, uuidArray);
    	 newConcept = tc.construct(conceptSpec);
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCAB e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public class BlueprintContinueActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(fsn.getText() == ""){
        		//please enter the fsn
        		JOptionPane.showMessageDialog(new JFrame(), "please enter the fsn");
        	}
        	/*if(){
        	 	// Test for parents
        		//please list parents for the new concept
        		JOptionPane.showMessageDialog(new JFrame(), "please list parents for the new concept");
        	}*/else {
                returnCondition = Condition.CONTINUE;
                done = true;
                NewConcept.this.notifyTaskDone();
                }
        	}
    }	
    public class GbDialectActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			//create desc blueprint and construct 
			try{
    		 descSpecGb = new DescCAB(
    				 		conceptSpec.getComponentUuid(),
    				 		WbDescType.SYNONYM.getLenient().getPrimUuid(), 
    				 		"en", us.getText(),
    				 		false);
    		 
        	//create refsex blueprint for GB dialect refset and construct
    		 UUID gbUuid = UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd");
    		 refexSpecGb = new RefexCAB(
    				TK_REFSET_TYPE.CID,
    				descSpecGb.getComponentNid(),
    				Ts.get().getNidForUuids(gbUuid)); 
    		 addGbDesc = true;
    		
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (InvalidCAB ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
    }
    }
    
    public class UsDialectActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			//create desc blueprint and construct
			try{
				 descSpecUs = new DescCAB(
	   				 		conceptSpec.getComponentUuid(), 
	   				 		WbDescType.SYNONYM.getLenient().getPrimUuid(), "en", 
	   				 		gb.getText(),
	   				 		false);
	   		
	   		 
	       	//create refsex blueprint for US dialect refset and construct
	   		 UUID usUuid = UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6");
	   		 refexSpecUs = new RefexCAB(
	   				 TK_REFSET_TYPE.CID,
	   				 descSpecUs.getComponentNid(),
	   				 Ts.get().getNidForUuids(usUuid)); 
	   		 addUsDesc = true;
	   		
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (InvalidCAB ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}	
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
