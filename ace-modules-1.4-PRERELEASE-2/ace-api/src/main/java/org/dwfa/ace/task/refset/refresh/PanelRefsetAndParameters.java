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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.commit.TestForReviewRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

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
    private JLabel commentsLabel;
    private JButton openFileChooserButton;
    private JComboBox refsetSpecComboBox;
    private JTextArea commentsTextField;
    private JScrollPane commentsScrollPane;

    private JList attachmentList;
    private HashSet<File> attachmentSet = new HashSet<File>();
    private ArrayListModel<File> attachmentListModel;

    private Set<I_GetConceptData> refsets;
    private Set<I_GetConceptData> editors;
    private Set<I_GetConceptData> reviewers;

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
        commentsLabel = new JLabel("Comments (optional):");

        // buttons and boxes
        openFileChooserButton = new JButton("Attach a file...");
        refsetSpecComboBox = new JComboBox(refsets.toArray());

        // text fields
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add Listeners
        openFileChooserButton.addActionListener(new AddAttachmentActionLister());
        refsetSpecComboBox.addActionListener(new RefsetListener());

        /*
         * -------------------------------------------------
         * Layout the components
         * -------------------------------------------------
         */
        layoutComponents();
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        // refset name label & box
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(refsetSpecLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (refsets.size() == 0) {
            this.add(new JLabel("No available Refset Specs"), gridBagConstraints);
        } else {
            this.add(refsetSpecComboBox, gridBagConstraints);
        }

        // comments
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(commentsLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(commentsScrollPane, gridBagConstraints);

        // file attachments
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        this.add(openFileChooserButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1;

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
        add(attachmentScroller, gridBagConstraints);

        // Using validate(), Tell the panel to o lay out its subcomponents
        // again. It should be invoked
        // when this container's subcomponents are modified after the container
        // has been displayed.
        this.validate();

    }

    private Set<I_GetConceptData> getAllUsers() throws IOException, TerminologyException {
        I_GetConceptData userParent = LocalVersionedTerminology.get().getConcept(
            ArchitectonicAuxiliary.Concept.USER.getUids());
        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(LocalVersionedTerminology.get()
            .getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
            .getConceptId());
        return userParent.getDestRelOrigins(allowedTypes, true, true);
    }

    private Set<I_GetConceptData> getValidEditors() throws Exception {
        I_GetConceptData selectedRefset = getRefset();
        Set<I_GetConceptData> validEditors = new HashSet<I_GetConceptData>();
        if (selectedRefset != null) {
            for (I_GetConceptData user : getAllUsers()) {
                if (hasEditorPermission(user, selectedRefset)) {
                    validEditors.add(user);
                }
            }
        }
        return validEditors;
    }

    private Set<I_GetConceptData> getValidReviewers() throws Exception {
        I_GetConceptData selectedRefset = getRefset();
        Set<I_GetConceptData> validReviewers = new HashSet<I_GetConceptData>();
        if (selectedRefset != null) {
            for (I_GetConceptData user : getAllUsers()) {
                if (hasReviewerPermission(user, selectedRefset)) {
                    validReviewers.add(user);
                }
            }
        }
        return validReviewers;
    }

    private boolean hasEditorPermission(I_GetConceptData user, I_GetConceptData selectedRefset) throws Exception {
        TestForEditRefsetPermission permissionTest = new TestForEditRefsetPermission();
        Set<I_GetConceptData> parents = new HashSet<I_GetConceptData>();
        parents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(user));
        parents.addAll(permissionTest.getValidRefsetsFromRolePermissions(user));

        for (I_GetConceptData parent : parents) {
            if (parent.isParentOfOrEqualTo(selectedRefset, true)) {
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
            if (parent.isParentOfOrEqualTo(selectedRefset, true)) {
                return true;
            }
        }
        return false;
    }

    class RefsetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

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
