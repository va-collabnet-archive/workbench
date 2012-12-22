/*
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
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class Translation_InboxReviewEscalate.
 */
public abstract class Translation_InboxReviewEscalate extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/** The profile prop name. */
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

	/** The return condition. */
	protected transient Condition returnCondition;

	/** The done. */
	protected transient boolean done;

	/** The config. */
	protected transient I_ConfigAceFrame config;
	
	/** The builder visible. */
	protected transient boolean builderVisible;
	
	/** The progress panel visible. */
	protected transient boolean progressPanelVisible;
	
	/** The subversion button visible. */
	protected transient boolean subversionButtonVisible;
	
	/** The inbox button visible. */
	protected transient boolean inboxButtonVisible;
	
	/** The workflow panel. */
	protected transient JPanel workflowPanel;

	/**
	 * Write object.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
	}

	/**
	 * Read object.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			profilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/**
	 * The listener interface for receiving previousAction events.
	 * The class that is interested in processing a previousAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPreviousActionListener<code> method. When
	 * the previousAction event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see PreviousActionEvent
	 */
	private class PreviousActionListener implements ActionListener {

		/**
		 * Action performed.
		 *
		 * @param e the e
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.PREVIOUS;
			done = true;
			synchronized (Translation_InboxReviewEscalate.this) {
				Translation_InboxReviewEscalate.this.notifyAll();
			}
		}
	}

	/**
	 * The listener interface for receiving continueAction events.
	 * The class that is interested in processing a continueAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addContinueActionListener<code> method. When
	 * the continueAction event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ContinueActionEvent
	 */
	public class ContinueActionListener implements ActionListener {

		/**
		 * Action performed.
		 *
		 * @param e the e
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.CONTINUE;
			done = true;
			synchronized (Translation_InboxReviewEscalate.this) {
				Translation_InboxReviewEscalate.this.notifyAll();
			}
		}
	}

	/**
	 * The listener interface for receiving stopAction events.
	 * The class that is interested in processing a stopAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addStopActionListener<code> method. When
	 * the stopAction event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see StopActionEvent
	 */
	public class StopActionListener implements ActionListener {

		/**
		 * Action performed.
		 *
		 * @param e the e
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.ITEM_CANCELED;
			done = true;
			synchronized (Translation_InboxReviewEscalate.this) {
				Translation_InboxReviewEscalate.this.notifyAll();
			}
		}
	}

	/**
	 * Wait till done.
	 *
	 * @param l the l
	 */
	protected void waitTillDone(Logger l) {
		while (!this.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				l.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	/**
	 * Checks if is done.
	 *
	 * @return true, if is done
	 */
	public boolean isDone() {
		return this.done;
	}

	/**
	 * Setup previous next or cancel buttons.
	 *
	 * @param workflowPanel the workflow panel
	 * @param c the c
	 */
	protected void setupPreviousNextOrCancelButtons(final JPanel workflowPanel, GridBagConstraints c) {
		c.gridx++;
		workflowPanel.add(new JLabel("  "), c);
		c.gridx++;
		c.anchor = GridBagConstraints.SOUTHWEST;
		if (showPrevious()) {
			JButton previousButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getPreviousImage())));
			previousButton.setToolTipText("go back");
			workflowPanel.add(previousButton, c);
			previousButton.addActionListener(new PreviousActionListener());
			c.gridx++;
		}
		JButton continueButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getContinueImage())));
		continueButton.setToolTipText("continue");
		workflowPanel.add(continueButton, c);
		continueButton.addActionListener(new ContinueActionListener());
		c.gridx++;

		JButton cancelButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getCancelImage())));
		cancelButton.setToolTipText("cancel");
		workflowPanel.add(cancelButton, c);
		cancelButton.addActionListener(new StopActionListener());
		c.gridx++;
		workflowPanel.add(new JLabel("     "), c);
		workflowPanel.validate();
		Container cont = workflowPanel;
		while (cont != null) {
			cont.validate();
			cont = cont.getParent();
		}
		continueButton.requestFocusInWindow();
		workflowPanel.repaint();
	}

	/**
	 * Show previous.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean showPrevious();

	/**
	 * Restore.
	 *
	 * @throws InterruptedException the interrupted exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	protected void restore() throws InterruptedException, InvocationTargetException {
		if (SwingUtilities.isEventDispatchThread()) {
			doRun();
		} else {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					doRun();
				}
			});
		}
		config.setBuilderToggleVisible(builderVisible);
		config.setSubversionToggleVisible(subversionButtonVisible);
		config.setInboxToggleVisible(inboxButtonVisible);
	}

	/**
	 * Sets the up.
	 *
	 * @param process the new up
	 * @throws IntrospectionException the introspection exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	protected void setup(I_EncodeBusinessProcess process) throws IntrospectionException, IllegalAccessException,
	InvocationTargetException {
		this.done = false;
		try {
			config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();

			builderVisible = config.isBuilderToggleVisible();
			config.setBuilderToggleVisible(false);
			subversionButtonVisible = config.isBuilderToggleVisible();
			config.setSubversionToggleVisible(false);
			inboxButtonVisible = config.isInboxToggleVisible();
			config.setInboxToggleVisible(false);
			workflowPanel = config.getWorkflowPanel();
			workflowPanel.setVisible(true);} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	/**
	 * Gets the conditions.
	 *
	 * @return the conditions
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		if (showPrevious()) {
			return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
		}
		return AbstractTask.CONTINUE_CANCEL;
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
	 * Do run.
	 */
	private void doRun() {
		Component[] components = workflowPanel.getComponents();
		for (int i = 0; i < components.length; i++) {
			workflowPanel.remove(components[i]);
		}
		workflowPanel.setVisible(false);
		workflowPanel.repaint();
		workflowPanel.validate();
		Container cont = workflowPanel;
		while (cont != null) {
			cont.validate();
			cont = cont.getParent();
		}
	}

	/**
	 * Gets the previous image.
	 *
	 * @return the previous image
	 */
	protected static String getPreviousImage() {
		return "/16x16/plain/navigate_left.png";
	}

	/**
	 * Gets the continue image.
	 *
	 * @return the continue image
	 */
	protected static String getContinueImage() {
		return "/16x16/plain/navigate_right.png";
	}

	/**
	 * Gets the cancel image.
	 *
	 * @return the cancel image
	 */
	protected static String getCancelImage() {
		return "/16x16/plain/navigate_cross.png";
	}
}
