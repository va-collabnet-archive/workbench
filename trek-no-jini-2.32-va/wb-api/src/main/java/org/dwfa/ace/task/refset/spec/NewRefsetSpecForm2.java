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
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;

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
    private Set<String> reviewers;
    private TreeMap<String, I_GetConceptData> validUserMap;
    private TreeMap<String, I_GetConceptData> validNewRefsetParentMap;
    private Set<String> selectedReviewers;

    private NewRefsetSpecWizard wizard;

    public NewRefsetSpecForm2(NewRefsetSpecWizard wizard, TreeMap<String, I_GetConceptData> validUserMap,
            TreeMap<String, I_GetConceptData> validNewRefsetParentMap) {
        super();
        this.validUserMap = validUserMap;
        this.validNewRefsetParentMap = validNewRefsetParentMap;
        selectedReviewers = new TreeSet<String>();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        deadlinePicker = new DatePicker(Calendar.getInstance(), null, dateFormat);

        // text fields
        requestorTextField = new JTextField(20);

        // misc
        priorityComboBox = new JComboBox(new String[] { "Highest", "High", "Normal", "Low", "Lowest" });
        addReviewerButton = new JButton("Add reviewer");

        setUpComboBoxes();
    }

    private void setUpComboBoxes() {
        NewRefsetSpecForm1 form1 = (NewRefsetSpecForm1) wizard.getPanelFromString("panel1");
        String parentString = form1.getSelectedParent();

        if (parentString == null) {
            editors = new TreeSet<String>(new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            editors.addAll(validUserMap.keySet());
        } else {
            editors = getPermissibleEditors(parentString);
            reviewers = new TreeSet<String>(new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            reviewers.addAll(validUserMap.keySet());
        }

        int editorIndex = -1;
        if (editorComboBox != null) {
            editorIndex = editorComboBox.getSelectedIndex();
        }

        int reviewerIndex = -1;
        if (reviewerComboBox != null) {
            reviewerIndex = reviewerComboBox.getSelectedIndex();
        }

        editorComboBox = new JComboBox(editors.toArray());
        reviewerComboBox = new JComboBox(reviewers.toArray());

        if (reviewerIndex != -1) {
            reviewerComboBox.setSelectedIndex(reviewerIndex);
        }
        if (editorIndex != -1) {
            editorComboBox.setSelectedIndex(editorIndex);
        }
    }

    private Set<String> getPermissibleEditors(String refsetString) {
        try {
            I_GetConceptData refset = validNewRefsetParentMap.get(refsetString);
            if (refset == null) {
                return validUserMap.keySet();
            }
            Set<String> permissibleEditors = new TreeSet<String>(new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });

            for (String key : validUserMap.keySet()) {
                I_GetConceptData concept = validUserMap.get(key);
                TestForEditRefsetPermission permissionTest = new TestForEditRefsetPermission();
                TreeSet<I_GetConceptData> permissibleRefsetParents = new TreeSet<I_GetConceptData>();
                permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(concept));
                permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(concept));

                if (permissibleRefsetParents.contains(refset)) {
                    permissibleEditors.add(key);
                }
            }
            return permissibleEditors;
        } catch (Exception e) {
            e.printStackTrace();
            return validUserMap.keySet();
        }
    }

    private Set<String> getPermissibleReviewers(String refsetString) {
        try {
            // TODO implement after review data check is created
            Set<String> reviewer = new TreeSet<String>(new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            reviewer.addAll(validUserMap.keySet());
            return reviewer;
        } catch (Exception e) {
            e.printStackTrace();
            return validUserMap.keySet();
        }
    }

    private void addListeners() {
        addReviewerButton.addActionListener(new ButtonListener());
    }

    public void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        setUpComboBoxes();

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
        if (editors.size() == 0) {
            // add a label instead that indicates no editors are available
            JLabel noEditorAvailLabel = new JLabel("No editors available - add these using new-user BP.");
            this.add(noEditorAvailLabel, gridBagConstraints);
        } else {
            this.add(editorComboBox, gridBagConstraints);
        }

        // reviewer
        int reviewerCount = 0;
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
        if (reviewers.size() == 0) {
            // add a label instead that indicates no reviewers are available
            JLabel noReviewersAvailLabel = new JLabel("No reviewers available - add these using new-user BP.");
            this.add(noReviewersAvailLabel, gridBagConstraints);
        } else {
            this.add(reviewerComboBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            this.add(addReviewerButton, gridBagConstraints);

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
                removeSelectedReviewer(reviewer);
            }
        }
    }

    public void removeSelectedReviewer(String reviewer) {
        selectedReviewers.remove(reviewer);
        layoutComponents();
        wizard.getDialog().pack();
        wizard.getDialog().repaint();
    }

    public void removeAllSelectedReviewers() {
        selectedReviewers.clear();
        layoutComponents();
        wizard.getDialog().pack();
        wizard.getDialog().repaint();
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

    public Set<String> getEditors() {
        return editors;
    }
}
