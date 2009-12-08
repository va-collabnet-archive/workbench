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
package org.dwfa.ace.task.refset.spec;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.util.AceDateFormat;

/**
 * The second of two refset spec panels - this prompts the user to input the
 * requestor, select an editor, deadline and priority.
 * This panel is used as part of the new refset spec wizard.
 * 
 * @author Chrissy
 * 
 */
public class NewRefsetSpecForm2 extends JPanel {

    private static final long serialVersionUID = 1L;
    // components
    private JLabel requestorLabel;
    private JLabel editorLabel;
    private JLabel reviewerLabel;
    private JLabel deadlineLabel;
    private JLabel priorityLabel;

    private JTextField requestorTextField;
    private DatePicker deadlinePicker;
    private JComboBox priorityComboBox;
    private JComboBox editorComboBox;
    private JComboBox reviewerComboBox;
    private JButton addReviewerButton;
    private Set<String> editors;
    private Set<String> selectedReviewers;

    private NewRefsetSpecWizard wizard;

    public NewRefsetSpecForm2(NewRefsetSpecWizard wizard, Set<String> editors) {
        super();
        this.editors = editors;
        selectedReviewers = new HashSet<String>();
        this.wizard = wizard;
        init();
    }

    private void init() {
        setDefaultValues();
        addListeners();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        requestorLabel = new JLabel("Requestor (optional):");
        editorLabel = new JLabel("Editor (required):");
        deadlineLabel = new JLabel("Deadline (required):");
        priorityLabel = new JLabel("Priority (required):");
        reviewerLabel = new JLabel("Reviewer(s) (optional):");

        // date picker
        SimpleDateFormat dateFormat = (SimpleDateFormat) AceDateFormat.getShortDisplayDateFormat();
        deadlinePicker = new DatePicker(Calendar.getInstance(), null, dateFormat);

        // text fields
        requestorTextField = new JTextField(20);

        // misc
        priorityComboBox = new JComboBox(new String[] { "Highest", "High", "Normal", "Low", "Lowest" });
        editorComboBox = new JComboBox(editors.toArray());
        reviewerComboBox = new JComboBox(editors.toArray());
        addReviewerButton = new JButton("Add reviewer");
    }

    private void addListeners() {
        addReviewerButton.addActionListener(new ButtonListener());
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // requestor
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(requestorLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(requestorTextField, gridBagConstraints);

        // editor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(editorLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(editorComboBox, gridBagConstraints);

        // reviewer
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(reviewerLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(reviewerComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(addReviewerButton, gridBagConstraints);

        int reviewerCount = 0;
        for (String reviewer : selectedReviewers) {

            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(true);
            checkBox.addItemListener(new CheckBoxListener(reviewer));
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 3 + reviewerCount;
            gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            this.add(checkBox, gridBagConstraints);

            JLabel attachmentLabel = new JLabel(reviewer);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 3 + reviewerCount;
            gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            this.add(attachmentLabel, gridBagConstraints);

            reviewerCount++;
        }

        // deadline
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4 + reviewerCount;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlineLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4 + reviewerCount;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlinePicker, gridBagConstraints);

        // priority
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5 + reviewerCount;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5 + reviewerCount;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityComboBox, gridBagConstraints);

        // column filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6 + reviewerCount;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

    }

    class ButtonListener implements ActionListener {

        public ButtonListener() {
        }

        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("Add reviewer")) {
                selectedReviewers.add(getSelectedReviewer());
                layoutComponents();
                wizard.getDialog().pack();
            }
        }
    }

    class CheckBoxListener implements ItemListener {
        String reviewer;

        public CheckBoxListener(String reviewer) {
            this.reviewer = reviewer;
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                selectedReviewers.remove(reviewer);
                layoutComponents();
                wizard.getDialog().pack();
                wizard.getDialog().repaint();
            }
        }
    }

    public String getSelectedEditor() {
        return (String) editorComboBox.getSelectedItem();
    }

    public String getSelectedReviewer() {
        return (String) reviewerComboBox.getSelectedItem();
    }

    public Set<String> getSelectedReviewers() {
        return selectedReviewers;
    }

    public String getRequestor() {
        String result = requestorTextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public Calendar getDeadline() {
        return deadlinePicker.getSelectedDate();
    }

    public String getPriority() {
        return (String) priorityComboBox.getSelectedItem();
    }
}
