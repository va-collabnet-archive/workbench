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
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.I_KeepTaskInInbox;
import org.ihtsdo.translation.ui.TranslationConceptEditor6;

/**
 * The Class TranslationWorkFlowStep3Outputs.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class PropBasedTransWorkFlowStep2Outputs extends AbstractTask {

	/** The profile. */
	private String profilePropName;

	/** The worklist item. */
	private String workListItemPropName = ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey();

	/** The step role. */
	private TermEntry stepRole;

	/** The exit1 label and destination. */
	private String exit1PropName;

	/** The exit1 label. */
	private String exit1Label;

	/** The exit1 destination. */
	private String exit1Destination;

	/** The exit1 next status. */
	private TermEntry exit1NextStatus;

	/** The exit2 label and destination. */
	private String exit2PropName;

	/** The exit2 label. */
	private String exit2Label;

	/** The exit2 destination. */
	private String exit2Destination;

	/** The exit2 next status. */
	private TermEntry exit2NextStatus;

	/** The todo status. */
	private TermEntry todoStatus;

	/** The return condition. */
	private transient Condition returnCondition;

	/** The selected destination. */
	private transient String selectedDestination;

	/** The selected next status. */
	private transient TermEntry selectedNextStatus;

	/** The done. */
	private transient boolean done;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	//	private static final String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";

	/** The ui panel. */
	private TranslationConceptEditor6 uiPanel;

	/** The config2. */
	private I_ConfigAceFrame config2;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(workListItemPropName);
		out.writeObject(stepRole);
		out.writeObject(exit1PropName);
		out.writeObject(exit1Label);
		out.writeObject(exit1NextStatus);
		out.writeObject(exit2PropName);
		out.writeObject(exit2Label);
		out.writeObject(exit2NextStatus);
		out.writeObject(todoStatus);
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
			profilePropName = (String) in.readObject();
			workListItemPropName = (String) in.readObject();
			stepRole = (TermEntry) in.readObject();
			exit1PropName = (String) in.readObject();
			exit1Label = (String) in.readObject();
			exit1NextStatus = (TermEntry) in.readObject();
			exit2PropName = (String) in.readObject();
			exit2Label = (String) in.readObject();
			exit2NextStatus = (TermEntry) in.readObject();
			todoStatus = (TermEntry) in.readObject();


		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/**
	 * Instantiates a new translation work flow step.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public PropBasedTransWorkFlowStep2Outputs() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						AceFrameConfig config = (AceFrameConfig)Terms.get().getActiveAceFrameConfig();
						StringBuffer sb = new StringBuffer("Opening inbox item.");
						try{
							while(true){
								config.setStatusMessage(sb.toString());
								for (int j = 0; j < 5; j++) {
									sb.append('.');
									config.setStatusMessage(sb.toString());
									Thread.sleep(250);
								}
								sb = new StringBuffer("Opening inbox item.");
							}
						} catch (InterruptedException e) {
							config.setStatusMessage("");
						}
					} catch (TerminologyException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
			if (profilePropName == null || stepRole == null || exit1PropName == null || exit1NextStatus == null 
					|| exit2PropName == null || exit2NextStatus == null) {
				t.interrupt();
				throw new TaskFailedException("Incomplete step data (null)");
			} else if (profilePropName.isEmpty() || exit1PropName.isEmpty()
					|| exit2PropName.isEmpty() ) {
				t.interrupt();
				throw new TaskFailedException("Incomplete step data (empty)");
			}

			config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();

//			if (config==null)
//				config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
//			else
//				process.setProperty(getProfilePropName(), config);

			WorkListMember workListMember = (WorkListMember) process.readAttachement(getWorkListItemPropName());
			if (workListMember == null) {
				t.interrupt();
				throw new TaskFailedException("Missing workist member attachment");
			}
			process.setProperty(ProcessAttachmentKeys.LAST_USER_TASKID.getAttachmentKey(), process.getCurrentTaskId());
			WorkList workList=TerminologyProjectDAO.getWorkList(Terms.get().getConcept(workListMember.getWorkListUUID()), config);
			TranslationProject translationProject =(TranslationProject)TerminologyProjectDAO.getProjectForWorklist(workList, config);

			// Get some space

			config2 =(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();
			config2.setShowQueueViewer(false);
			config2.setShowProcessBuilder(false);
			config2.setShowSearch(false);
			config2.setShowPreferences(false);
			config2.setShowSignpostPanel(false);
			// When running from the queue we never see the task list

			// set member on standard workbench editor components

			config2.setHierarchySelectionAndExpand(workListMember.getConcept());

			AceFrameConfig aceConfig = (AceFrameConfig) config2;

			AceFrame ace=aceConfig.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				int tabCount=tp.getTabCount();
				I_GetConceptData role=Terms.get().getConcept(stepRole.ids);
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)){
						uiPanel=(TranslationConceptEditor6)tp.getComponentAt(i);
						if (!uiPanel.verifySavePending(null, false)){
							uiPanel=null;
							t.interrupt();
							return Condition.STOP;
						}
						uiPanel.AutokeepInInbox();

						while (!uiPanel.getUnloaded()){

							Thread.sleep(1000);
						}
						uiPanel.setAutoKeepFunction(new ThisAutoKeep());
						uiPanel.updateUI(translationProject, workListMember, role);
						break;
					}
				}
				boolean bPanelExists=false;
				tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)){
						tp.setSelectedIndex(i);
						bPanelExists=true;
						break;
					}
				}
				if (!bPanelExists){
					uiPanel = new TranslationConceptEditor6();
					tp.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, uiPanel);
					tp.setSelectedIndex(tabCount);
					uiPanel.setAutoKeepFunction(new ThisAutoKeep());
					uiPanel.setUnloaded(false);
					uiPanel.updateUI(translationProject, workListMember, role);
				}
				tp.revalidate();
				tp.repaint();
				t.interrupt();
			}
			else{
				t.interrupt();
				throw new TaskFailedException("Cannot set panel to main panel.");
			}

			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {

					List<Component> buttons=new ArrayList<Component>();
					//---- exit1Button ----
					JButton exit1Button=new JButton();
					exit1Button.setText(exit1Label);
					try {
						exit1Destination = (String) process.getProperty(getExit1PropName());
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IntrospectionException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					exit1Button.addActionListener(new Exit1ActionListener());

					buttons.add(exit1Button);

					//---- exit2Button ----
					JButton exit2Button=new JButton();
					exit2Button.setText(exit2Label);
					try {
						exit2Destination = (String) process.getProperty(getExit2PropName());
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IntrospectionException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					exit2Button.addActionListener(new Exit2ActionListener());

					buttons.add(exit2Button);

					//---- exit4Button ----
					JButton exit4Button=new JButton();
					exit4Button.setText("Save as To Do");
					exit4Button.addActionListener(new Exit4ActionListener());

					buttons.add(exit4Button);

					//---- exit3Button ----
					JButton exit3Button=new JButton();
					exit3Button.setText("Keep in inbox");
					exit3Button.addActionListener(new Exit3ActionListener());

					buttons.add(exit3Button);

					uiPanel.setWorkflowButtons(buttons);
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
			String[] parsedSubj=TerminologyProjectDAO.getParsedItemSubject(process.getSubject());

			if (selectedNextStatus!=null){
				workListMember.setActivityStatus(Terms.get().getConcept(selectedNextStatus.ids[0]));
				TerminologyProjectDAO.updateWorkListMemberMetadata(workListMember, config);
				if (parsedSubj.length==TerminologyProjectDAO.subjectIndexes.values().length){
					parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_ID.ordinal()]=String.valueOf(selectedNextStatus.getLocalConcept().getNid());
				}
			}
			if (returnCondition!=Condition.STOP){
				Terms.get().commit();
				parsedSubj[TerminologyProjectDAO.subjectIndexes.TAGS_ARRAY.ordinal()]="";

				process.setDestination(selectedDestination);
				process.validateDestination();
			}
			if (selectedNextStatus!=null){
				PromotionRefset promoRefset = workList.getPromotionRefset(config);
				Long statusTime = promoRefset.getLastStatusTime(workListMember.getId(), config);

				parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_TIME.ordinal()]=String.valueOf(statusTime);
			}
			process.setSubject(TerminologyProjectDAO.getSubjectFromArray(parsedSubj));
			process.setOriginator(aceConfig.getUsername());
			return returnCondition;
		} catch (Exception e) {
			uiPanel.setUnloaded(true);
			throw new TaskFailedException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

		if (returnCondition==Condition.STOP ){
			try {
				ServiceID serviceID = null;
				Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
				System.out.println(
						"Moving process " + process.getProcessID() + " to destination: " + process.getDestination());
				Entry[] attrSetTemplates = new Entry[] { new ElectronicAddress(process.getDestination()) };
				ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
				ServiceItemFilter filter = null;
				ServiceItem service = worker.lookup(template, filter);
				if (service == null) {
					throw new TaskFailedException("No queue with the specified address could be found: "
							+ process.getDestination());
				}
				I_QueueProcesses q = (I_QueueProcesses) service.service;
				q.write(process, worker.getActiveTransaction());
				worker.commitTransactionIfActive();
				System.out.println("Moved process " + process.getProcessID() + " to queue: " + q.getNodeInboxAddress());
			} catch (Exception e) {
				if (uiPanel!=null)
				uiPanel.setUnloaded(true);
				throw new TaskFailedException(e);
			}
		}
		if (uiPanel!=null)
		uiPanel.setUnloaded(true);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		List<Condition> possibleExits = new ArrayList<Condition>();
		possibleExits.add(Condition.ITEM_COMPLETE);
		possibleExits.add(Condition.CONTINUE);
		possibleExits.add(Condition.STOP);

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
			if (uiPanel.verifySavePending(null,LanguageUtil.isItemBeingSent(e))) {
				returnCondition = Condition.ITEM_COMPLETE;
				selectedDestination = exit1Destination;
				selectedNextStatus = exit1NextStatus;
				uiPanel.setAutoKeepFunction(null);
				done = true;
				synchronized (PropBasedTransWorkFlowStep2Outputs.this) {
					PropBasedTransWorkFlowStep2Outputs.this.notifyAll();
				}
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
			if (uiPanel.verifySavePending(null,LanguageUtil.isItemBeingSent(e))) {
				returnCondition = Condition.CONTINUE;
				selectedDestination = exit2Destination;
				selectedNextStatus = exit2NextStatus;
				uiPanel.setAutoKeepFunction(null);
				done = true;
				synchronized (PropBasedTransWorkFlowStep2Outputs.this) {
					PropBasedTransWorkFlowStep2Outputs.this.notifyAll();
				}
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
			if (uiPanel.verifySavePending(null,LanguageUtil.isItemBeingSent(e))) {
				returnCondition = Condition.STOP;
				uiPanel.setAutoKeepFunction(null);
				done = true;
				synchronized (PropBasedTransWorkFlowStep2Outputs.this) {
					PropBasedTransWorkFlowStep2Outputs.this.notifyAll();
				}
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
			if (uiPanel.verifySavePending("Do you want to save changes befor sending it to your todo folder?",LanguageUtil.isItemBeingSent(e))) {
				returnCondition = Condition.STOP;
				selectedNextStatus = todoStatus;
				uiPanel.setAutoKeepFunction(null);
				done = true;
				synchronized (PropBasedTransWorkFlowStep2Outputs.this) {
					PropBasedTransWorkFlowStep2Outputs.this.notifyAll();
				}
			}
		}
	}
	
	/**
	 * The Class ThisAutoKeep.
	 */
	public class ThisAutoKeep implements I_KeepTaskInInbox{

		/* (non-Javadoc)
		 * @see org.ihtsdo.translation.ui.I_KeepTaskInInbox#KeepInInbox()
		 */
		public void KeepInInbox(){
			returnCondition = Condition.STOP;
			done=true;
			synchronized (PropBasedTransWorkFlowStep2Outputs.this) {
				PropBasedTransWorkFlowStep2Outputs.this.notifyAll();
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

	/**
	 * Gets the profile prop name.
	 *
	 * @return the profile prop name
	 */
	public String getProfilePropName() {
		return profilePropName;
	}

	/**
	 * Sets the profile prop name.
	 *
	 * @param profilePropName the new profile prop name
	 */
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	/**
	 * Gets the exit1 prop name.
	 *
	 * @return the exit1 prop name
	 */
	public String getExit1PropName() {
		return exit1PropName;
	}

	/**
	 * Sets the exit1 prop name.
	 *
	 * @param exit1PropName the new exit1 prop name
	 */
	public void setExit1PropName(String exit1PropName) {
		this.exit1PropName = exit1PropName;
	}

	/**
	 * Gets the exit2 prop name.
	 *
	 * @return the exit2 prop name
	 */
	public String getExit2PropName() {
		return exit2PropName;
	}

	/**
	 * Sets the exit2 prop name.
	 *
	 * @param exit2PropName the new exit2 prop name
	 */
	public void setExit2PropName(String exit2PropName) {
		this.exit2PropName = exit2PropName;
	}

	/**
	 * Gets the work list item prop name.
	 *
	 * @return the work list item prop name
	 */
	public String getWorkListItemPropName() {
		return workListItemPropName;
	}

	/**
	 * Sets the work list item prop name.
	 *
	 * @param workListItemPropName the new work list item prop name
	 */
	public void setWorkListItemPropName(String workListItemPropName) {
		this.workListItemPropName = workListItemPropName;
	}

	/**
	 * Gets the exit1 label.
	 *
	 * @return the exit1 label
	 */
	public String getExit1Label() {
		return exit1Label;
	}

	/**
	 * Sets the exit1 label.
	 *
	 * @param exit1Label the new exit1 label
	 */
	public void setExit1Label(String exit1Label) {
		this.exit1Label = exit1Label;
	}

	/**
	 * Gets the exit1 destination.
	 *
	 * @return the exit1 destination
	 */
	public String getExit1Destination() {
		return exit1Destination;
	}

	/**
	 * Sets the exit1 destination.
	 *
	 * @param exit1Destination the new exit1 destination
	 */
	public void setExit1Destination(String exit1Destination) {
		this.exit1Destination = exit1Destination;
	}

	/**
	 * Gets the exit2 label.
	 *
	 * @return the exit2 label
	 */
	public String getExit2Label() {
		return exit2Label;
	}

	/**
	 * Sets the exit2 label.
	 *
	 * @param exit2Label the new exit2 label
	 */
	public void setExit2Label(String exit2Label) {
		this.exit2Label = exit2Label;
	}

	/**
	 * Gets the exit2 destination.
	 *
	 * @return the exit2 destination
	 */
	public String getExit2Destination() {
		return exit2Destination;
	}

	/**
	 * Sets the exit2 destination.
	 *
	 * @param exit2Destination the new exit2 destination
	 */
	public void setExit2Destination(String exit2Destination) {
		this.exit2Destination = exit2Destination;
	}

	/**
	 * Gets the todo status.
	 *
	 * @return the todo status
	 */
	public TermEntry getTodoStatus() {
		return todoStatus;
	}

	/**
	 * Sets the todo status.
	 *
	 * @param todoStatus the new todo status
	 */
	public void setTodoStatus(TermEntry todoStatus) {
		this.todoStatus = todoStatus;
	}

}