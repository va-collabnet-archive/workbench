/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.tasks;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.translation.ui.TranslationConceptEditor4;

/**
 * The Class TranslationWorkFlowStep3Outputs.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class TranslationWorkFlowStep4Outputs extends AbstractTask {
	
	/** The ui panel name. */
	private String uiPanelName;
	
	/** The step role. */
	private TermEntry stepRole;
	
	/** The exit1 label and destination. */
	private String exit1LabelAndDestination;
	
	/** The exit1 next status. */
	private TermEntry exit1NextStatus;
	
	/** The exit2 label and destination. */
	private String exit2LabelAndDestination;
	
	/** The exit2 next status. */
	private TermEntry exit2NextStatus;
	
	/** The exit3 label and destination. */
	private String exit3LabelAndDestination;
	
	/** The exit3 next status. */
	private TermEntry exit3NextStatus;

	/** The exit4 label and destination. */
	private String exit4LabelAndDestination;
	
	/** The exit4 next status. */
	private TermEntry exit4NextStatus;
	
	/** The return condition. */
	private transient Condition returnCondition;
	
	/** The selected destination. */
	private transient String selectedDestination;
	
	/** The selected next status. */
	private transient TermEntry selectedNextStatus;
	
	/** The done. */
	private transient boolean done;
	
	/** The ui panel. */
	private TranslationConceptEditor4 uiPanel;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(uiPanelName);
		out.writeObject(stepRole);
		out.writeObject(exit1LabelAndDestination);
		out.writeObject(exit1NextStatus);
		out.writeObject(exit2LabelAndDestination);
		out.writeObject(exit2NextStatus);
		out.writeObject(exit3LabelAndDestination);
		out.writeObject(exit3NextStatus);
		out.writeObject(exit4LabelAndDestination);
		out.writeObject(exit4NextStatus);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			uiPanelName = (String) in.readObject();
			stepRole = (TermEntry) in.readObject();
			
			exit1LabelAndDestination = (String) in.readObject();
			exit1NextStatus = (TermEntry) in.readObject();
			
			exit2LabelAndDestination = (String) in.readObject();
			exit2NextStatus = (TermEntry) in.readObject();
			
			exit3LabelAndDestination = (String) in.readObject();
			exit3NextStatus = (TermEntry) in.readObject();

			exit4LabelAndDestination = (String) in.readObject();
			exit4NextStatus = (TermEntry) in.readObject();
			
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new translation work flow step.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public TranslationWorkFlowStep4Outputs() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			
			if (uiPanelName == null || stepRole == null || exit1LabelAndDestination == null || exit1NextStatus == null 
					|| exit2LabelAndDestination == null || exit2NextStatus == null || exit3LabelAndDestination == null 
					|| exit3NextStatus == null || exit4LabelAndDestination == null 
					|| exit4NextStatus == null) {
				throw new TaskFailedException("Incomplete step data (null)");
			} else if (uiPanelName.trim().equals("") || exit1LabelAndDestination.trim().equals("")
					|| exit2LabelAndDestination.trim().equals("") || exit3LabelAndDestination.trim().equals("")
					 || exit4LabelAndDestination.trim().equals("")) {
				throw new TaskFailedException("Incomplete step data (empty)");
			}
			
			//TODO: Is there a way to validate destinations without assigning them to a project yet?
			
			//TODO: Implement role verification with profile and throw taskfailed exeption if fails
			
//			config = (I_ConfigAceFrame) worker
//			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
//			Replaced previous line with next line to force active config use
			config=Terms.get().getActiveAceFrameConfig();
//			if (config==null)
//				config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();
			
			WorkListMember workListMember = (WorkListMember) process.readAttachement("A:WORKLIST_MEMBER");
			if (workListMember == null) {
				throw new TaskFailedException("Missing workist member attachment");
			}
			
			WorkList workList=TerminologyProjectDAO.getWorkList(Terms.get().getConcept(workListMember.getWorkListUUID()), config);
			TranslationProject translationProject =(TranslationProject)TerminologyProjectDAO.getProjectForWorklist(workList, config);
	        
			
			// Get some space
			config.setShowQueueViewer(false);
			config.setShowProcessBuilder(false);
			// When running from the queue we never see the task list
			
			// set member on standard workbench editor components
			
			config.setHierarchySelectionAndExpand(workListMember.getConcept());
//			config.selectConceptViewer(1);

			AceFrameConfig aceConfig =(AceFrameConfig)Terms.get().getActiveAceFrameConfig();
			
			AceFrame ace=aceConfig.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				boolean bPanelExists=false;
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)){
						uiPanel=(TranslationConceptEditor4)tp.getComponentAt(i);
						uiPanel.unloadData();
						uiPanel.updateUI(translationProject, workListMember);
						tp.setSelectedIndex(i);
						bPanelExists=true;
						break;
					}
				}
				if (!bPanelExists){
					//if (uiPanelName.trim().equals("standardTranslationPanel")) {
					uiPanel = new TranslationConceptEditor4();
					uiPanel.updateUI(translationProject, workListMember);
					//}
					tp.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, uiPanel);
					tp.setSelectedIndex(tabCount);
				}
				tp.revalidate();
				tp.repaint();
			}
			else{
				throw new TaskFailedException("Cannot set panel to main panel.");
			}

			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {

					List<Component> buttons=new ArrayList<Component>();
					int iconPlace=-1;
					//---- exit1Button ----
					JButton exit1Button=new JButton();
					exit1Button.setText(exit1LabelAndDestination.substring(0, exit1LabelAndDestination.indexOf(",")));
					iconPlace=exit1LabelAndDestination.indexOf(",", exit1LabelAndDestination.indexOf(",")+1);
					if (iconPlace>-1)
						exit1Button.setIcon(new ImageIcon("icons/" + exit1LabelAndDestination.substring(iconPlace + 1).trim()));
					exit1Button.addActionListener(new Exit1ActionListener());
					
					buttons.add(exit1Button);
					
					//---- exit2Button ----
					JButton exit2Button=new JButton();
					exit2Button.setText(exit2LabelAndDestination.substring(0, exit2LabelAndDestination.indexOf(",")));
					iconPlace=exit2LabelAndDestination.indexOf(",", exit2LabelAndDestination.indexOf(",")+1);
					if (iconPlace>-1)
						exit2Button.setIcon(new ImageIcon("icons/" + exit2LabelAndDestination.substring(iconPlace + 1).trim()));
					exit2Button.addActionListener(new Exit2ActionListener());

					buttons.add(exit2Button);
					
					//---- exit3Button ----
					JButton exit3Button=new JButton();
					exit3Button.setText(exit3LabelAndDestination.substring(0, exit3LabelAndDestination.indexOf(",")));
					iconPlace=exit3LabelAndDestination.indexOf(",", exit3LabelAndDestination.indexOf(",")+1);
					if (iconPlace>-1)
						exit3Button.setIcon(new ImageIcon("icons/" + exit3LabelAndDestination.substring(iconPlace + 1).trim()));
					exit3Button.addActionListener(new Exit3ActionListener());

					buttons.add(exit3Button);

					//---- exit4Button ----
					JButton exit4Button=new JButton();
					exit4Button.setText(exit4LabelAndDestination.substring(0, exit4LabelAndDestination.indexOf(",")));
					iconPlace=exit4LabelAndDestination.indexOf(",", exit4LabelAndDestination.indexOf(",")+1);
					if (iconPlace>-1)
						exit4Button.setIcon(new ImageIcon("icons/" + exit4LabelAndDestination.substring(iconPlace + 1).trim()));
					exit4Button.addActionListener(new Exit4ActionListener());

					buttons.add(exit4Button);
					
					uiPanel.setWorkflowButtons(buttons);

//					Container cont = workflowPanel;
//					while (cont != null) {
//						cont.validate();
//						cont = cont.getParent();
//					}
				}
			});
			synchronized (this) {
				this.waitTillDone(worker.getLogger());
			}
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					uiPanel.removeWorkflowButtons();
					uiPanel.unloadData();
					uiPanel.revalidate();
					uiPanel.repaint();

				}

			});
			
			workListMember.setActivityStatus(selectedNextStatus.ids[0]);
			TerminologyProjectDAO.updateWorkListMemberMetadata(workListMember, config);

			process.setDestination(selectedDestination);
	        process.validateDestination();
			
			return returnCondition;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		List<Condition> possibleExits = new ArrayList<Condition>();
		possibleExits.add(Condition.ITEM_COMPLETE);
		possibleExits.add(Condition.CONTINUE);
		possibleExits.add(Condition.PREVIOUS);
		possibleExits.add(Condition.ITEM_SKIPPED);
		
		return possibleExits;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}
	
	/**
	 * The listener interface for receiving exit1Action events.
	 * The class that is interested in processing a exit1Action
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addExit1ActionListener<code> method. When
	 * the exit1Action event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see Exit1ActionEvent
	 */
	private class Exit1ActionListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.ITEM_COMPLETE;
			int iconPlace=exit1LabelAndDestination.indexOf(",", exit1LabelAndDestination.indexOf(",")+1);
			if (iconPlace>-1)
				selectedDestination = 
					exit1LabelAndDestination.substring(exit1LabelAndDestination.indexOf(",") + 1, iconPlace ).trim();
			else
				selectedDestination = 
					exit1LabelAndDestination.substring(exit1LabelAndDestination.indexOf(",") + 1, exit1LabelAndDestination.length()).trim();
			selectedNextStatus = exit1NextStatus;
			done = true;
			synchronized (TranslationWorkFlowStep4Outputs.this) {
				TranslationWorkFlowStep4Outputs.this.notifyAll();
			}
		}
	}
	
	/**
	 * The listener interface for receiving exit2Action events.
	 * The class that is interested in processing a exit2Action
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addExit2ActionListener<code> method. When
	 * the exit2Action event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see Exit2ActionEvent
	 */
	private class Exit2ActionListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.CONTINUE;
			int iconPlace=exit2LabelAndDestination.indexOf(",", exit2LabelAndDestination.indexOf(",")+1);
			if (iconPlace>-1)
				selectedDestination = 
					exit2LabelAndDestination.substring(exit2LabelAndDestination.indexOf(",") + 1, iconPlace ).trim();
			else
				selectedDestination =
					exit2LabelAndDestination.substring(exit2LabelAndDestination.indexOf(",") + 1, exit2LabelAndDestination.length()).trim();
			selectedNextStatus = exit2NextStatus;
			done = true;
			synchronized (TranslationWorkFlowStep4Outputs.this) {
				TranslationWorkFlowStep4Outputs.this.notifyAll();
			}
		}
	}
	
	/**
	 * The listener interface for receiving exit3Action events.
	 * The class that is interested in processing a exit3Action
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addExit3ActionListener<code> method. When
	 * the exit3Action event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see Exit3ActionEvent
	 */
	private class Exit3ActionListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.PREVIOUS;
			int iconPlace=exit3LabelAndDestination.indexOf(",", exit3LabelAndDestination.indexOf(",")+1);
			if (iconPlace>-1)
				selectedDestination = 
					exit3LabelAndDestination.substring(exit3LabelAndDestination.indexOf(",") + 1, iconPlace ).trim();
			else
				selectedDestination = 
					exit3LabelAndDestination.substring(exit3LabelAndDestination.indexOf(",") + 1, exit3LabelAndDestination.length()).trim();
			selectedNextStatus = exit3NextStatus;
			done = true;
			synchronized (TranslationWorkFlowStep4Outputs.this) {
				TranslationWorkFlowStep4Outputs.this.notifyAll();
			}
		}
	}
	/**
	 * The listener interface for receiving exit4Action events.
	 * The class that is interested in processing a exit4Action
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addExit4ActionListener<code> method. When
	 * the exit4Action event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see Exit4ActionEvent
	 */
	private class Exit4ActionListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.ITEM_SKIPPED;
			int iconPlace=exit4LabelAndDestination.indexOf(",", exit4LabelAndDestination.indexOf(",")+1);
			if (iconPlace>-1)
				selectedDestination = 
					exit4LabelAndDestination.substring(exit4LabelAndDestination.indexOf(",") + 1, iconPlace ).trim();
			else
				selectedDestination = 
					exit4LabelAndDestination.substring(exit4LabelAndDestination.indexOf(",") + 1, exit4LabelAndDestination.length()).trim();
			selectedNextStatus = exit4NextStatus;
			done = true;
			synchronized (TranslationWorkFlowStep4Outputs.this) {
				TranslationWorkFlowStep4Outputs.this.notifyAll();
			}
		}
	}
	/**
	 * Wait till done.
	 * 
	 * @param l the l
	 */
	private void waitTillDone(Logger l) {
		while (!this.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				l.log(Level.SEVERE, e.getMessage(), e);
			}
		}

	}

	/**
	 * Gets the ui panel name.
	 * 
	 * @return the ui panel name
	 */
	public String getUiPanelName() {
		return uiPanelName;
	}

	/**
	 * Sets the ui panel name.
	 * 
	 * @param uiPanelName the new ui panel name
	 */
	public void setUiPanelName(String uiPanelName) {
		this.uiPanelName = uiPanelName;
	}

	/**
	 * Gets the step role.
	 * 
	 * @return the step role
	 */
	public TermEntry getStepRole() {
		return stepRole;
	}

	/**
	 * Sets the step role.
	 * 
	 * @param stepRole the new step role
	 */
	public void setStepRole(TermEntry stepRole) {
		this.stepRole = stepRole;
	}

	/**
	 * Gets the exit1 label and destination.
	 * 
	 * @return the exit1 label and destination
	 */
	public String getExit1LabelAndDestination() {
		return exit1LabelAndDestination;
	}

	/**
	 * Sets the exit1 label and destination.
	 * 
	 * @param exit1LabelAndDestination the new exit1 label and destination
	 */
	public void setExit1LabelAndDestination(String exit1LabelAndDestination) {
		this.exit1LabelAndDestination = exit1LabelAndDestination;
	}

	/**
	 * Gets the exit1 next status.
	 * 
	 * @return the exit1 next status
	 */
	public TermEntry getExit1NextStatus() {
		return exit1NextStatus;
	}

	/**
	 * Sets the exit1 next status.
	 * 
	 * @param exit1NextStatus the new exit1 next status
	 */
	public void setExit1NextStatus(TermEntry exit1NextStatus) {
		this.exit1NextStatus = exit1NextStatus;
	}

	/**
	 * Gets the exit2 label and destination.
	 * 
	 * @return the exit2 label and destination
	 */
	public String getExit2LabelAndDestination() {
		return exit2LabelAndDestination;
	}

	/**
	 * Sets the exit2 label and destination.
	 * 
	 * @param exit2LabelAndDestination the new exit2 label and destination
	 */
	public void setExit2LabelAndDestination(String exit2LabelAndDestination) {
		this.exit2LabelAndDestination = exit2LabelAndDestination;
	}

	/**
	 * Gets the exit2 next status.
	 * 
	 * @return the exit2 next status
	 */
	public TermEntry getExit2NextStatus() {
		return exit2NextStatus;
	}

	/**
	 * Sets the exit2 next status.
	 * 
	 * @param exit2NextStatus the new exit2 next status
	 */
	public void setExit2NextStatus(TermEntry exit2NextStatus) {
		this.exit2NextStatus = exit2NextStatus;
	}

	/**
	 * Gets the exit3 label and destination.
	 * 
	 * @return the exit3 label and destination
	 */
	public String getExit3LabelAndDestination() {
		return exit3LabelAndDestination;
	}

	/**
	 * Sets the exit3 label and destination.
	 * 
	 * @param exit3LabelAndDestination the new exit3 label and destination
	 */
	public void setExit3LabelAndDestination(String exit3LabelAndDestination) {
		this.exit3LabelAndDestination = exit3LabelAndDestination;
	}

	/**
	 * Gets the exit3 next status.
	 * 
	 * @return the exit3 next status
	 */
	public TermEntry getExit3NextStatus() {
		return exit3NextStatus;
	}

	/**
	 * Sets the exit3 next status.
	 * 
	 * @param exit3NextStatus the new exit3 next status
	 */
	public void setExit3NextStatus(TermEntry exit3NextStatus) {
		this.exit3NextStatus = exit3NextStatus;
	}

	/**
	 * Gets the return condition.
	 * 
	 * @return the return condition
	 */
	public Condition getReturnCondition() {
		return returnCondition;
	}

	/**
	 * Sets the return condition.
	 * 
	 * @param returnCondition the new return condition
	 */
	public void setReturnCondition(Condition returnCondition) {
		this.returnCondition = returnCondition;
	}

	/**
	 * Gets the selected destination.
	 * 
	 * @return the selected destination
	 */
	public String getSelectedDestination() {
		return selectedDestination;
	}

	/**
	 * Sets the selected destination.
	 * 
	 * @param selectedDestination the new selected destination
	 */
	public void setSelectedDestination(String selectedDestination) {
		this.selectedDestination = selectedDestination;
	}

	/**
	 * Gets the selected next status.
	 * 
	 * @return the selected next status
	 */
	public TermEntry getSelectedNextStatus() {
		return selectedNextStatus;
	}

	/**
	 * Sets the selected next status.
	 * 
	 * @param selectedNextStatus the new selected next status
	 */
	public void setSelectedNextStatus(TermEntry selectedNextStatus) {
		this.selectedNextStatus = selectedNextStatus;
	}

	/**
	 * Checks if is done.
	 * 
	 * @return true, if is done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Sets the done.
	 * 
	 * @param done the new done
	 */
	public void setDone(boolean done) {
		this.done = done;
	}

	public String getExit4LabelAndDestination() {
		return exit4LabelAndDestination;
	}

	public void setExit4LabelAndDestination(String exit4LabelAndDestination) {
		this.exit4LabelAndDestination = exit4LabelAndDestination;
	}

	public TermEntry getExit4NextStatus() {
		return exit4NextStatus;
	}

	public void setExit4NextStatus(TermEntry exit4NextStatus) {
		this.exit4NextStatus = exit4NextStatus;
	}

}