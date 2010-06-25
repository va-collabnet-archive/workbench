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
package org.dwfa.ace.task.refset.spec.create;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Table to support selection of more than one reviewer.
 *
 * @author kec
 *
 */
class ReviewerTableModel extends AbstractTableModel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    String[] columnNames = { "name", "reviewer" };
    Object[][] data =
            { { "reviewer 1", new Boolean(false) }, { "reviewer 2", new Boolean(true) },
             { "reviewer 3", new Boolean(false) } };

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        // Note that the data/cell address is constant,
        // no matter where the cell appears onscreen.
        if (col < 1) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}

/**
 * The Create Refset panel is used to start the Create Refset process. It allows the user to input:
 * 1) refset name
 * 2) Refset Parent (from pulldown menu)
 * 3) comments (text field)
 * 4) requestor
 * 5) editor (from pulldown menu)
 * 6) reviewer (from pulldown menu)
 * 7) deadline (date picker)
 * 8) priority (from pulldown menu)
 * 9) file attachments (file chooser)
 *
 * @author Perry Reid
 * @author Chrissy Hill
 * @version 1.0, December 2009
 *
 */
public class CreateRefsetPanel extends JPanel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetNameLabel;
    private JLabel refsetParentLabel;
    private JLabel commentsLabel;
    private JLabel computeTypeLabel;

    private JTextField refsetNameTextField;
    private JTextArea commentsTextField;
    private JScrollPane commentsScrollPane;
    private JButton openFileChooserButton;
    private ButtonGroup computeTypeGroup;
    private JRadioButton conceptTypeChoice;
    private JRadioButton descriptionTypeChoice;

    // File Attachments
    private JList attachmentList;
    private HashSet<File> attachmentSet = new HashSet<File>();
    private ArrayListModel<File> attachmentListModel;

    public CreateRefsetPanel(Set<? extends I_GetConceptData> allValidUsers,
            Set<I_GetConceptData> permissibleRefsetParents) {
        super(new GridBagLayout());

        setDefaultValues();
        setUpComboBoxes();
        layoutComponents();

    }

    private void setDefaultValues() {

        /*
         * -------------------------------------------------
         * Set Default / initial values for all the fields
         * -------------------------------------------------
         */

        // labels
        refsetNameLabel = new JLabel("Refset name (required):");
        refsetParentLabel = new JLabel("Refset parent (required):");
        commentsLabel = new JLabel("Comments (optional):");
        computeTypeLabel = new JLabel("Compute type (required):");

        // Data Controls
        refsetNameTextField = new JTextField(20);
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        openFileChooserButton = new JButton("Attach a file...");
        computeTypeGroup = new ButtonGroup();
        conceptTypeChoice = new JRadioButton("Concept");
        descriptionTypeChoice = new JRadioButton("Description");
        computeTypeGroup.add(conceptTypeChoice);
        computeTypeGroup.add(descriptionTypeChoice);
        conceptTypeChoice.setSelected(true);

        // Add Listeners
        openFileChooserButton.addActionListener(new AddAttachmentActionLister());

    }

    private void setUpComboBoxes() {
        /*
         * -------------------------------------------------
         * Initialize all the ComboBoxes
         * -------------------------------------------------
         */
    }

    private void layoutComponents() {
        /*
         * -------------------------------------------------
         * Layout all the components on the form
         * -------------------------------------------------
         */

        this.setLayout(new GridBagLayout());
        this.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();

        // Refset Name (Label & TextField)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 0, 5); // padding (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weighty = 0;
        this.add(refsetNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5); // padding (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(refsetNameTextField, gbc);

        // Refset Parent (Label & ComboBox)
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(5, 10, 0, 5); // padding (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(refsetParentLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5); // padding
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("Default Refset"), gbc);

        // Comments (Label and Scroll Area)
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(5, 10, 0, 5); // padding
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(commentsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 0, 5); // padding
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(commentsScrollPane, gbc);

        // refset compute type (description or concept)
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 0, 5); // padding (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(computeTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 10, 10); // padding (top, left, bottom, right)
        this.add(conceptTypeChoice, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 10, 10); // padding (top, left, bottom, right)
        this.add(descriptionTypeChoice, gbc);

        // file attachments
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 0, 5); // padding (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(openFileChooserButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 10, 10, 10); // padding (top, left, bottom, right)

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

        // Using validate(), Tell the panel to lay out its subcomponents again. It should be invoked
        // when this container's subcomponents are modified after the container has been displayed.
        this.validate();

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

    /*
     * ====================================================================
     * Getters and Setters
     * ====================================================================
     */

    // -----------------------
    // Refset Name
    // -----------------------
    public String getRefsetName() {
        return refsetNameTextField.getText();
    }

    public void setRefsetName(String refsetName) {
        this.refsetNameTextField.setText(refsetName);
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

    public I_GetConceptData getComputeType() throws TerminologyException, IOException {

        if (conceptTypeChoice.isSelected()) {
            return LocalVersionedTerminology.get().getConcept(
                RefsetAuxiliary.Concept.CONCEPT_COMPUTE_TYPE.localize().getNid());
        } else if (descriptionTypeChoice.isSelected()) {
            return LocalVersionedTerminology.get().getConcept(
                RefsetAuxiliary.Concept.DESCRIPTION_COMPUTE_TYPE.localize().getNid());
        } else {
            return null;
        }
    }

    // -----------------------
    // Refreshes the author and reviewer combo boxes and redraws the panel, when a different refset parent is chosen.
    // -----------------------
    public class RefsetParentActionLister implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            setUpComboBoxes();
            layoutComponents();
        }

    }

}