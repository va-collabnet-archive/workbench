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
package org.dwfa.ace.task.refset.refresh;

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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.commit.TestForReviewRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * The Refresh Refset panel that allows user to input:
 * 1) refset name (from pulldown menu)
 * 2) editor (from pulldown menu)
 * 3) comments (text field)
 * 4) deadline (date picker)
 * 5) priority (from pulldown menu)
 * 6) request attachments (file chooser)
 * 
 * @author Perry Reid
 * @version 1.0, November 2009
 * 
 */
public class PanelRefsetAndParameters extends JPanel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetSpecLabel;
    private JLabel editorLabel;
    private JLabel reviewerLabel;
    private JLabel commentsLabel;
    private JLabel deadlineLabel;
    private JLabel priorityLabel;
    private JButton openFileChooserButton;
    private JComboBox refsetSpecComboBox;
    private JComboBox editorComboBox;
    private JComboBox reviewerComboBox;
    private JComboBox priorityComboBox;
    private JTextArea commentsTextField;
    private DatePicker deadlinePicker;
    private JScrollPane commentsScrollPane;

    private JList attachmentList;
    private HashSet<File> attachmentSet = new HashSet<File>();
    private ArrayListModel<File> attachmentListModel;

    private Set<? extends I_GetConceptData> refsets;
    private Set<? extends I_GetConceptData> editors;
    private Set<Object> reviewers;

    private String noReviewText = "no reviewer assigned";

    /**
     * 
     * @param refsets
     */
    public PanelRefsetAndParameters(Set<I_GetConceptData> refsets) {
        super(new GridBagLayout());
        this.refsets = refsets;

        /*
         * -------------------------------------------------
         * Set Default / initial values for all the fields
         * -------------------------------------------------
         */
        // labels
        refsetSpecLabel = new JLabel("Refset Spec (required):");
        editorLabel = new JLabel("Editor (required):");
        reviewerLabel = new JLabel("Reviewer (required):");
        deadlineLabel = new JLabel("Deadline (required):");
        priorityLabel = new JLabel("Priority (required):");
        commentsLabel = new JLabel("Comments (optional):");

        // buttons and boxes
        openFileChooserButton = new JButton("Attach a file...");
        refsetSpecComboBox = new JComboBox(refsets.toArray());
        editorComboBox = new JComboBox();
        reviewerComboBox = new JComboBox();
        priorityComboBox = new JComboBox(new String[] { "Highest", "High", "Normal", "Low", "Lowest" });

        // date picker
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        deadlinePicker = new DatePicker(Calendar.getInstance(), null, dateFormat);

        // text fields
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add Listeners
        openFileChooserButton.addActionListener(new AddAttachmentActionLister());
        refsetSpecComboBox.addActionListener(new RefsetListener());

        setUpComboBoxes();
        layoutComponents();
    }

    private void setUpComboBoxes() {
        /*
         * -------------------------------------------------
         * Initialize all the ComboBoxes
         * -------------------------------------------------
         */
        I_GetConceptData refsetParent = getRefset();

        if (refsetParent == null) {
            editors = getAllUsers();
            reviewers = new HashSet<Object>(getAllUsers());
        } else {
            editors = getValidEditors();
            reviewers = getValidReviewers();
        }

        if (editorComboBox != null) {
            I_GetConceptData previousEditor = (I_GetConceptData) editorComboBox.getSelectedItem();
            editorComboBox = new JComboBox(editors.toArray());
            if (previousEditor != null || editors.size() == 0) {
                editorComboBox.setSelectedItem(previousEditor);
            }
        }

        if (reviewerComboBox != null) {
            Object previousReviewer = reviewerComboBox.getSelectedItem();
            reviewerComboBox = new JComboBox(reviewers.toArray());
            if (previousReviewer != null || reviewers.size() == 0) {
                reviewerComboBox.setSelectedItem(previousReviewer);
            } else {
                reviewerComboBox.setSelectedItem(noReviewText);
            }
        }
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();

        // refset name label & box
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(refsetSpecLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        if (refsets.size() == 0) {
            this.add(new JLabel("No available Refset Specs"), gbc);
        } else {
            this.add(refsetSpecComboBox, gbc);
        }

        // editor
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(editorLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;

        if (editors == null || editors.size() == 0) {
            this.add(new JLabel("No available editors."), gbc);
        } else {
            this.add(editorComboBox, gbc);
        }

        // reviewer
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(reviewerLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;

        if (reviewers == null || reviewers.size() == 0) {
            this.add(new JLabel("No available reviewers."), gbc);
        } else {
            this.add(reviewerComboBox, gbc);
        }

        // deadline
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(deadlineLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        this.add(deadlinePicker, gbc);

        // priority
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(priorityLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(priorityComboBox, gbc);

        // comments
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 5, 5); // padding
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(commentsLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(commentsScrollPane, gbc);

        // file attachments
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 0, 5); // padding
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(openFileChooserButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

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
        add(attachmentScroller, gbc);

        // Using validate(), Tell the panel to lay out its subcomponents again. It should be invoked
        // when this container's subcomponents are modified after the container has been displayed.
        this.validate();

    }

    private Set<? extends I_GetConceptData> getAllUsers() {
        try {
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            I_GetConceptData userParent =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
            I_IntSet allowedTypes = config.getDestRelTypes();
            Set<I_GetConceptData> users = new TreeSet<I_GetConceptData>(new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            users.addAll(userParent.getDestRelOrigins(allowedTypes));
            return users;
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return getAllUsers();
        }
    }

    private Set<? extends I_GetConceptData> getValidEditors() {
        try {
            I_GetConceptData selectedRefset = getRefset();
            Set<I_GetConceptData> validEditors = new TreeSet<I_GetConceptData>(new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            if (selectedRefset != null) {
                for (I_GetConceptData user : getAllUsers()) {
                    if (hasEditorPermission(user, selectedRefset)) {
                        validEditors.add(user);
                    }
                }
            }
            return validEditors;
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return getAllUsers();
        }
    }

    private Set<Object> getValidReviewers() {
        I_GetConceptData selectedRefset = getRefset();
        Set<Object> permissibleReviewers = new TreeSet<Object>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        permissibleReviewers.add(noReviewText);
        try {
            if (selectedRefset == null) {
                permissibleReviewers.addAll(getAllUsers());
                return permissibleReviewers;
            }

            for (I_GetConceptData user : getAllUsers()) {
                if (hasReviewerPermission(user, selectedRefset)) {
                    permissibleReviewers.add(user);
                }
            }

            return permissibleReviewers;
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            permissibleReviewers.addAll(getAllUsers());
            return permissibleReviewers;
        }
    }

    private boolean hasEditorPermission(I_GetConceptData user, I_GetConceptData selectedRefset) throws Exception {
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

    class RefsetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setUpComboBoxes();
            layoutComponents();
        }
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

    // -----------------------
    // Refset
    // -----------------------
    public I_GetConceptData getRefset() {
        if (refsets.size() == 0) {
            return null;
        } else {
            return (I_GetConceptData) refsetSpecComboBox.getSelectedItem();
        }
    }

    public void setRefset(I_GetConceptData newRefset) {
        this.refsetSpecComboBox.setSelectedItem(newRefset);
    }

    // -----------------------
    // Editor
    // -----------------------
    public I_GetConceptData getEditor() {
        I_GetConceptData selectedEditor = (I_GetConceptData) editorComboBox.getSelectedItem();
        return selectedEditor;
    }

    public void setEditor(I_GetConceptData newEditor) {
        this.editorComboBox.setSelectedItem(newEditor);
    }

    // -----------------------
    // Reviewer
    // -----------------------
    public I_GetConceptData getReviewer() {
        Object selectedObject = reviewerComboBox.getSelectedItem();
        if (selectedObject == null || I_GetConceptData.class.isAssignableFrom(selectedObject.getClass())) {
            return (I_GetConceptData) selectedObject;
        } else {
            return null;
        }
    }

    public void setReviewer(I_GetConceptData newReviewer) {
        this.reviewerComboBox.setSelectedItem(newReviewer);
    }

    // -----------------------
    // Deadline
    // -----------------------
    public Calendar getDeadline() {
        return (Calendar) deadlinePicker.getSelectedDate();
    }

    public void setDeadline(Calendar newDeadline) {
        deadlinePicker.setSelectedDate(newDeadline);
    }

    // -----------------------
    // Priority
    // -----------------------
    public String getPriority() {
        return (String) priorityComboBox.getSelectedItem();
    }

    public void setPriority(String newPriority) {
        priorityComboBox.setSelectedItem(newPriority);
    }

    // -----------------------
    // Comments
    // -----------------------
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

    public void setComments(String newComments) {
        commentsTextField.setText(newComments);
    }

    // -----------------------
    // Attachments
    // -----------------------
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