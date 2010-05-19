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
package org.dwfa.ace.task.wfdetailsSheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.AceImages;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui/workflow/detail sheet", type = BeanType.TASK_BEAN) })
public class SetWorkflowDetailsSheetToErrorsAndWarnings extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String errorsAndWarningsPropName = ProcessAttachmentKeys.ERRORS_AND_WARNINGS.getAttachmentKey();

    private transient Exception ex = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(errorsAndWarningsPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
            errorsAndWarningsPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            ex = null;
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
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return Condition.CONTINUE;
    }

    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            Collection<AlertToDataConstraintFailure> warningsAndErrors = (Collection<AlertToDataConstraintFailure>) process.getProperty(errorsAndWarningsPropName);
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            int width = 400;
            int height = 500;
            workflowDetailsSheet.setSize(width, height);

            JPanel dataCheckListPanel = new JPanel(new GridBagLayout());
            layoutAlerts(dataCheckListPanel, warningsAndErrors);
            JScrollPane dataCheckListScroller = new JScrollPane(dataCheckListPanel);

            workflowDetailsSheet.setLayout(new GridLayout(1, 1));
            workflowDetailsSheet.add(dataCheckListScroller);
        } catch (Exception e) {
            ex = e;
        }
    }

    private void layoutAlerts(JPanel dataCheckListPanel, Collection<AlertToDataConstraintFailure> warningsAndErrors) {
        for (Component component : dataCheckListPanel.getComponents()) {
            dataCheckListPanel.remove(component);
        }

        dataCheckListPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;

        for (AlertToDataConstraintFailure alert : warningsAndErrors) {
            setupAlert(alert);
            dataCheckListPanel.add(alert.getRendererComponent(), c);
            c.gridy++;
        }

        c.weighty = 1;
        c.gridy++;
        dataCheckListPanel.add(new JPanel(), c);
    }

    private void setupAlert(AlertToDataConstraintFailure alert) {
        if (alert.getRendererComponent() == null) {

            JLabel label = new JLabel();
            label.setText(alert.getAlertMessage());
            switch (alert.getAlertType()) {
            case ERROR:
                label.setIcon(AceImages.errorIcon);
            case INFORMATIONAL:
                label.setIcon(AceImages.errorIcon);
                break;
            case RESOLVED:
                label.setIcon(AceImages.errorIcon);
                break;
            case WARNING:
                label.setIcon(AceImages.errorIcon);
                break;
            }

            JPanel componentPanel = new JPanel(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 1;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 0;
            componentPanel.add(label, c);
            c.weightx = 0;
            c.gridwidth = 1;
            c.gridy++;
            c.anchor = GridBagConstraints.EAST;
            boolean isSelected = false;
            if (isSelected) {
                componentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 1, 1,
                    Color.BLUE), BorderFactory.createEmptyBorder(1, 0, 0, 0)));
            } else {
                componentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                    Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 0, 1)));
            }

            alert.setRendererComponent(componentPanel);
        }
    }

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
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getErrorsAndWarningsPropName() {
        return errorsAndWarningsPropName;
    }

    public void setErrorsAndWarningsPropName(String errorsAndWarningsPropName) {
        this.errorsAndWarningsPropName = errorsAndWarningsPropName;
    }
}
