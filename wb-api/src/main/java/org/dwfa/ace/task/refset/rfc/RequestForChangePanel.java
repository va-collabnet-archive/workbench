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
package org.dwfa.ace.task.refset.rfc;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.commit.TestForReviewRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.bpa.data.ArrayListModel;

/**
 * The request for change panel that allows user to input:
 * 1) refset name (from pulldown menu)
 * 2) editor (from pulldown menu)
 * 3) original request (text field)
 * 4) comments (text field)
 * 5) deadline (date picker)
 * 6) priority (from pulldown menu)
 * 7) request attachments (file chooser)
 * 8) reviewer (from pulldown menu)
 * 
 * @author Chrissy Hill
 * 
 */
public class RequestForChangePanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetNameLabel;
    private JLabel editorLabel;
    private JLabel originalRequestLabel;
    private JLabel commentsLabel;
    private JLabel deadlineLabel;
    private JLabel priorityLabel;
    private JLabel reviewerLabel;
    private JButton openFileChooserButton;
    private JComboBox refsetNameComboBox;
    private JComboBox editorComboBox;
    private JComboBox reviewerComboBox;
    private JComboBox priorityComboBox;
    private JTextArea originalRequestTextField;
    private JTextArea commentsTextField;
    private DatePicker deadlinePicker;
    private JScrollPane originalRequestScrollPane;
    private JScrollPane commentsScrollPane;

    // File Attachments
    private JList attachmentList;
    private HashSet<File> attachmentSet = new HashSet<File>();
    private ArrayListModel<File> attachmentListModel;

    private Set<I_GetConceptData> refsets;
    private Set<? extends I_GetConceptData> allValidUsers;

    private String noReviewText = "no reviewer assigned";

    public RequestForChangePanel(Set<I_GetConceptData> refsets, Set<? extends I_GetConceptData> allValidUsers) {
        super();
        this.refsets = refsets;
        this.allValidUsers = allValidUsers;
        init();
    }

    private void init() {
        setDefaultValues();
        addListeners();
        setUpComboBoxes();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        refsetNameLabel = new JLabel("Refset name (required):");
        editorLabel = new JLabel("Editor (required):");
        originalRequestLabel = new JLabel("Original request (optional):");
        commentsLabel = new JLabel("Comments (optional):");
        deadlineLabel = new JLabel("Deadline (required):");
        priorityLabel = new JLabel("Priority (required):");
        reviewerLabel = new JLabel("Reviewer (optional):");

        // buttons and boxes
        openFileChooserButton = new JButton("Attach a file...");
        refsetNameComboBox = new JComboBox(refsets.toArray());
        priorityComboBox = new JComboBox(new String[] { "Highest", "High", "Normal", "Low", "Lowest" });

        // date picker
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        deadlinePicker = new DatePicker(Calendar.getInstance(), null, dateFormat);

        // text fields
        originalRequestTextField = new JTextArea();
        originalRequestTextField.setLineWrap(true);
        originalRequestTextField.setWrapStyleWord(true);
        originalRequestScrollPane = new JScrollPane(originalRequestTextField);
        originalRequestScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void setUpComboBoxes() {

        Set<I_GetConceptData> editors = getValidEditors();
        Set<Object> reviewers = getValidReviewers();

        if (editorComboBox != null) {
            I_GetConceptData previousEditor = (I_GetConceptData) editorComboBox.getSelectedItem();
            editorComboBox = new JComboBox(editors.toArray());
            if (previousEditor != null || editors.size() == 0) {
                editorComboBox.setSelectedItem(previousEditor);
            }
        } else {
            editorComboBox = new JComboBox(editors.toArray());
        }

        if (reviewerComboBox != null) {
            Object previousReviewer = reviewerComboBox.getSelectedItem();
            reviewerComboBox = new JComboBox(reviewers.toArray());
            if (previousReviewer != null || reviewers.size() == 0) {
                reviewerComboBox.setSelectedItem(previousReviewer);
            } else {
                reviewerComboBox.setSelectedItem(noReviewText);
            }
        } else {
            reviewerComboBox = new JComboBox(reviewers.toArray());
            reviewerComboBox.setSelectedItem(noReviewText);
        }
    }

    private void addListeners() {
        openFileChooserButton.addActionListener(new AddAttachmentActionLister());
        refsetNameComboBox.addActionListener(new RefsetListener());
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // refset name label & box
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(refsetNameLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1;
        if (refsets.size() == 0) {
            this.add(new JLabel("No available refsets."), gbc);
        } else {
            this.add(refsetNameComboBox, gbc);
        }

        // editor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(editorLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(editorComboBox, gbc);

        // reviewer
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(reviewerLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(reviewerComboBox, gbc);

        // original request
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 40, 5);
        this.add(originalRequestLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(originalRequestScrollPane, gbc);

        // comments
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 40, 5);
        this.add(commentsLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(commentsScrollPane, gbc);

        // deadline
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(deadlineLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(deadlinePicker, gbc);

        // priority
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(priorityLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(priorityComboBox, gbc);

        // file attachments
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(15, 5, 0, 5); // padding (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(openFileChooserButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1;
        gbc.insets = new Insets(15, 10, 10, 10); // padding (top, left, bottom, right)

        attachmentListModel = new ArrayListModel<File>();
        attachmentList = new JList(attachmentListModel);
        attachmentList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        attachmentList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");
        attachmentList.getActionMap().put("deleteTask", new DeleteAction());

        JScrollPane attachmentScroller = new JScrollPane(attachmentList);
        attachmentScroller.setMinimumSize(new Dimension(100, 100));
        attachmentScroller.setMaximumSize(new Dimension(500, 300));
        attachmentScroller.setPreferredSize(new Dimension(150, 150));
        attachmentScroller.setBorder(BorderFactory.createTitledBorder("Attachments (optional):"));
        this.add(attachmentScroller, gbc);

        this.validate();
    }

    class RefsetListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            setUpComboBoxes();
            layoutComponents();
        }

    }

    private Set<I_GetConceptData> getValidEditors() {
        I_GetConceptData selectedRefset = getRefset();
        Set<I_GetConceptData> editors = new HashSet<I_GetConceptData>();
        try {
            if (selectedRefset != null) {
                for (I_GetConceptData user : allValidUsers) {
                    if (hasEditPermission(user, selectedRefset)) {
                        editors.add(user);
                    }
                }
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            editors.addAll(allValidUsers);
            return editors;
        }

        return editors;
    }

    private boolean hasEditPermission(I_GetConceptData user, I_GetConceptData selectedRefset) throws Exception {
        TestForEditRefsetPermission permissionTest = new TestForEditRefsetPermission();
        Set<I_GetConceptData> parents = new HashSet<I_GetConceptData>();
        parents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(user));
        parents.addAll(permissionTest.getValidRefsetsFromRolePermissions(user));

        for (I_GetConceptData parent : parents) {
            if (parent.isParentOfOrEqualTo(selectedRefset)) {
                return true;
            }
        }
        return false;
    }

    private Set<Object> getValidReviewers() {
        I_GetConceptData selectedRefset = getRefset();
        Set<Object> permissibleReviewers = new HashSet<Object>();
        permissibleReviewers.add(noReviewText);
        try {
            if (selectedRefset == null) {
                permissibleReviewers.addAll(allValidUsers);
                return permissibleReviewers;
            }

            for (I_GetConceptData user : allValidUsers) {
                if (hasReviewerPermission(user, selectedRefset)) {
                    permissibleReviewers.add(user);
                }
            }

            return permissibleReviewers;
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            permissibleReviewers.addAll(allValidUsers);
            return permissibleReviewers;
        }
    }

    private boolean hasReviewerPermission(I_GetConceptData user, I_GetConceptData selectedRefset) throws Exception {
        TestForReviewRefsetPermission permissionTest = new TestForReviewRefsetPermission();
        Set<I_GetConceptData> parents = new HashSet<I_GetConceptData>();
        parents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(user));
        parents.addAll(permissionTest.getValidRefsetsFromRolePermissions(user));

        for (I_GetConceptData parent : parents) {
            if (parent.isParentOfOrEqualTo(selectedRefset)) {
                return true;
            }
        }
        return false;
    }

    public String getComments() {
        String result = commentsTextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public String getOriginalRequest() {
        String result = originalRequestTextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public I_GetConceptData getRefset() {
        if (refsets.size() == 0) {
            return null;
        } else {
            return (I_GetConceptData) refsetNameComboBox.getSelectedItem();
        }
    }

    public I_GetConceptData getEditor() {
        return (I_GetConceptData) editorComboBox.getSelectedItem();
    }

    public I_GetConceptData getReviewer() {
        Object selectedObject = reviewerComboBox.getSelectedItem();
        if (selectedObject == null || I_GetConceptData.class.isAssignableFrom(selectedObject.getClass())) {
            return (I_GetConceptData) selectedObject;
        } else {
            return null;
        }
    }

    public Calendar getDeadline() {
        return deadlinePicker.getSelectedDate();
    }

    public String getPriority() {
        return (String) priorityComboBox.getSelectedItem();
    }

    private class AddAttachmentActionLister implements ActionListener {
        public AddAttachmentActionLister() {
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getActionCommand().equals(openFileChooserButton.getText())) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setDialogTitle("Attach a File");
                    int returnValue = fileChooser.showDialog(new Frame(), "Attach file");
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (attachmentSet.contains(selectedFile)) {
                            // Warn the user that the file is already attached
                            JOptionPane.showMessageDialog(null, "The file '" + selectedFile.getName() + "' "
                                + " is already an attachment. \nPlease select a different file. ",
                                "Attachment Already Exists Warning", JOptionPane.WARNING_MESSAGE);
                        } else {
                            // Add the attachment
                            attachmentSet.add(selectedFile);
                            attachmentListModel.add(selectedFile);
                        }
                    }
                }
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    public class DeleteAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            File selectedFile = (File) attachmentList.getSelectedValue();
            attachmentListModel.remove(selectedFile);
            attachmentSet.remove(selectedFile);
        }
    }

    public HashSet<File> getAttachments() {
        return attachmentSet;
    }

    public void setAttachments(HashSet<File> files) {
        attachmentSet.clear();
        attachmentSet.addAll(files);
        attachmentListModel.clear();
        attachmentListModel.addAll(files);
    }
}
