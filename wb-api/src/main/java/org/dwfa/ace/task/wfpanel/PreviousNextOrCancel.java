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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.DetailSheetClientProperties;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;

public abstract class PreviousNextOrCancel extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    protected transient Condition returnCondition;

    protected transient boolean done;

    protected transient I_ConfigAceFrame config;
    protected transient boolean builderVisible;
    protected transient boolean progressPanelVisible;
    protected transient boolean subversionButtonVisible;
    protected transient boolean inboxButtonVisible;
    protected transient JPanel workflowPanel;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private class PreviousActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.PREVIOUS;
            done = true;
            synchronized (PreviousNextOrCancel.this) {
                PreviousNextOrCancel.this.notifyAll();
            }
        }
    }

    public class ContinueActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (Terms.get().getUncommitted().size() > 0) {
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There are uncommitted changes - please cancel or commit before continuing.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                returnCondition = Condition.CONTINUE;
                done = true;
                synchronized (PreviousNextOrCancel.this) {
                    PreviousNextOrCancel.this.notifyAll();
                }
            }
        }
    }

    public class StopActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            synchronized (PreviousNextOrCancel.this) {
                PreviousNextOrCancel.this.notifyAll();
            }
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

    public boolean isDone() {
        return this.done;
    }

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
        JPanel detailsSheet = config.getWorkflowDetailsSheet();
        if (detailsSheet.isVisible()
            && detailsSheet.getClientProperty(DetailSheetClientProperties.COMPONENT_FOR_FOCUS) != null) {
            JComponent componentToFocus =
                    (JComponent) detailsSheet.getClientProperty(DetailSheetClientProperties.COMPONENT_FOR_FOCUS);
            componentToFocus.requestFocusInWindow();
        } else {
            continueButton.requestFocusInWindow();
        }
        workflowPanel.repaint();
    }

    protected abstract boolean showPrevious();

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

    protected void setup(I_EncodeBusinessProcess process) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        this.done = false;
        config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());

        builderVisible = config.isBuilderToggleVisible();
        config.setBuilderToggleVisible(false);
        subversionButtonVisible = config.isSubversionToggleVisible();
        config.setSubversionToggleVisible(false);
        inboxButtonVisible = config.isInboxToggleVisible();
        config.setInboxToggleVisible(false);
        workflowPanel = config.getWorkflowPanel();
        workflowPanel.setVisible(true);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        if (showPrevious()) {
            return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
        }
        return AbstractTask.CONTINUE_CANCEL;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

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

    protected static String getPreviousImage() {
        return "/16x16/plain/navigate_left.png";
    }

    protected static String getContinueImage() {
        return "/16x16/plain/navigate_right.png";
    }

    protected static String getCancelImage() {
        return "/16x16/plain/navigate_cross.png";
    }
}
