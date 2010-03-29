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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * <h1>SetRefSetPreferences</h1> <br>
 * <p>
 * The <code>SetRefSetPreferences</code> class turns on various refSet options
 * as specified by passed parameters.
 * </p>
 * <p>
 * It is added as a task under tasks/ide/gui/signpost, which enables it to be
 * added to a business process.
 * </P>
 * 
 * <br>
 * <br>
 * 
 * @see <code>org.dwfa.bpa.tasks.AbstractTask</code>
 * @author PeterVawser
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/gui/signpost", type = BeanType.TASK_BEAN) })
public class SetRefSetPreferences extends AbstractTask {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 3;

    private TOGGLES toggle = TOGGLES.ATTRIBUTES;

    private I_ConfigAceFrame config;

    public TOGGLES getToggle() {
        return toggle;
    }

    public void setToggle(TOGGLES toggle) {
        this.toggle = toggle;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(toggle);

    }// End method writeObject

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            toggle = (TOGGLES) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            /*
             * Create an option pane to be used for user input, and a panel to
             * layout
             * input objects to be displayed in the user input dialog.
             */
            final JOptionPane optionPane = new JOptionPane();
            final JPanel optionsPanel = new JPanel(new GridBagLayout());

            /*
             * Set gridbag layout configuration
             */
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.VERTICAL;
            c.weighty = 0.5;
            c.anchor = GridBagConstraints.NORTHWEST;

            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    /*
                     * Add and label a checkbox for each refset type, for a
                     * selected toggle to the panel.
                     * Inital value should be set to the toggles refset type
                     * value.
                     */
                    for (I_HostConceptPlugins.REFSET_TYPES refSetTypes : I_HostConceptPlugins.REFSET_TYPES.values()) {
                        JCheckBox cb = new JCheckBox(refSetTypes.toString());
                        cb.setSelected(config.isRefsetInToggleVisible(
                            I_HostConceptPlugins.REFSET_TYPES.valueOf(refSetTypes.toString()), toggle));
                        c.gridy += 1;

                        optionsPanel.add(cb, c);
                    }// End for loop

                    /*
                     * Create an object array to be added to the option pane
                     * dialog.
                     * This needs to have hvae an entry for the text to be
                     * dispalyed and one for the checkboxes (panel)
                     */
                    Object msg[] = { "Select refsets to be displayed:", optionsPanel };

                    /*
                     * Set configuration of option pane
                     */
                    optionPane.setMessage(msg);
                    optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
                    optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);

                    /*
                     * Create a frame object to be used as the parent frame for
                     * the option pane.
                     * Ensure the frame closes when exited.
                     */
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    JDialog dialog = optionPane.createDialog(frame, "Displayed refSets for " + toggle.toString());
                    dialog.setVisible(true);

                    /*
                     * Determine what action the user took and perform
                     * appropriate processing.
                     */
                    Object value = optionPane.getValue();
                    if (value == null || !(value instanceof Integer)) {
                        System.out.println("Closed");
                    } else {
                        int i = ((Integer) value).intValue();
                        if (i == JOptionPane.OK_OPTION) {
                            /*
                             * User clicked ok. Now need to apply selections to
                             * preferences.
                             */
                            for (Component comp : optionsPanel.getComponents()) {
                                System.out.println("comp == " + ((JCheckBox) comp).getText() + " "
                                    + ((JCheckBox) comp).isSelected());
                                /*
                                 * Toggle the desired refset display to
                                 * true/false.
                                 * If true, set to false. If false, set to true.
                                 */
                                config.setRefsetInToggleVisible(
                                    I_HostConceptPlugins.REFSET_TYPES.valueOf(((JCheckBox) comp).getText()),
                                    TOGGLES.valueOf(toggle.toString()), ((JCheckBox) comp).isSelected());
                            }// End for loop
                        }// End if OK_OPTION
                    }// End if/else

                    /*
                     * Ensure refsets in component toggle are set to visible.
                     */
                    config.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.REFSETS, true);

                    /*
                     * Apply changes to current config frame.
                     * For some reason, without the fireCommit, the process
                     * worked
                     * on the initial execution but not on subsequent
                     * executions, unless the viewer was restarted.
                     * Call to fireCommit fixed this issue.
                     */
                    // config.setActive(true);
                    config.fireCommit();

                }
            });

        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;

    }// End method evaluate

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }// End method complete

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }// End method getConditions

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }// End method getDataContainerIds

}// End class SetRefSetPreferences
