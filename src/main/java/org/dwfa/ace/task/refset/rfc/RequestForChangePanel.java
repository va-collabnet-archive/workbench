package org.dwfa.ace.task.refset.rfc;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.ace.task.util.DynamicWidthComboBox;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * The request for change panel that allows user to input:
 * 1) refset name (from pulldown menu)
 * 2) editor (from pulldown menu)
 * 3) original request (text field)
 * 4) comments (text field)
 * 5) deadline (date picker)
 * 6) priority (from pulldown menu)
 * 7) request attachments (file chooser)
 * 
 * @author Chrissy
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
    private JLabel fileAttachmentLabel;
    private JButton openFileChooserButton;
    private DynamicWidthComboBox refsetNameComboBox;
    private DynamicWidthComboBox editorComboBox;
    private JComboBox priorityComboBox;
    private JTextArea originalRequestTextField;
    private JTextArea commentsTextField;
    private DatePicker deadlinePicker;
    private JScrollPane originalRequestScrollPane;
    private JScrollPane commentsScrollPane;

    private HashSet<File> attachments = new HashSet<File>();
    private Set<I_GetConceptData> refsets;
    private JPanel wfPanel;

    public RequestForChangePanel(Set<I_GetConceptData> refsets, JPanel wfPanel) {
        super();
        this.wfPanel = wfPanel;
        this.refsets = refsets;
        init();
    }

    private void init() {
        setDefaultValues();
        addListeners();
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
        fileAttachmentLabel = new JLabel("Request attachment(s) (optional):");

        // buttons and boxes
        openFileChooserButton = new JButton("Attach a file");
        refsetNameComboBox = new DynamicWidthComboBox(refsets.toArray());
        refsetNameComboBox.setMaximumSize(new Dimension(200, 25));
        refsetNameComboBox.setMinimumSize(new Dimension(200, 25));
        refsetNameComboBox.setPreferredSize(new Dimension(200, 25));
        editorComboBox = new DynamicWidthComboBox();
        editorComboBox.setMaximumSize(new Dimension(200, 25));
        editorComboBox.setMinimumSize(new Dimension(200, 25));
        editorComboBox.setPreferredSize(new Dimension(200, 25));
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
        originalRequestScrollPane.setPreferredSize(new Dimension(200, 50));
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        commentsScrollPane.setPreferredSize(new Dimension(200, 50));
    }

    private void addListeners() {
        openFileChooserButton.addActionListener(new ButtonListener());
        refsetNameComboBox.addActionListener(new RefsetListener());
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // refset name label & box
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(refsetNameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (refsets.size() == 0) {
            this.add(new JLabel("No available refsets."), gridBagConstraints);
        } else {
            this.add(refsetNameComboBox, gridBagConstraints);
        }

        // editor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(editorLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        try {
            editorComboBox = new DynamicWidthComboBox(getValidEditors().toArray());
        } catch (Exception e) {
            editorComboBox = new DynamicWidthComboBox();
            e.printStackTrace();
        }
        editorComboBox.setMaximumSize(new Dimension(200, 25));
        editorComboBox.setMinimumSize(new Dimension(200, 25));
        editorComboBox.setPreferredSize(new Dimension(200, 25));
        this.add(editorComboBox, gridBagConstraints);

        // original request
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(originalRequestLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(originalRequestScrollPane, gridBagConstraints);

        // comments
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(commentsLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(commentsScrollPane, gridBagConstraints);

        // deadline
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlineLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlinePicker, gridBagConstraints);

        // priority
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityComboBox, gridBagConstraints);

        // file attachments
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(fileAttachmentLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(openFileChooserButton, gridBagConstraints);

        int fileCount = 0;
        for (File attachment : attachments) {

            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(true);
            checkBox.addItemListener(new CheckBoxListener(attachment));
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 7 + fileCount;
            gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            this.add(checkBox, gridBagConstraints);

            JLabel attachmentLabel = new JLabel(attachment.getName());
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 7 + fileCount;
            gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            this.add(attachmentLabel, gridBagConstraints);

            fileCount++;
        }

        // column filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 8 + fileCount;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

        this.setPreferredSize(new Dimension(0, 0));
        this.setMaximumSize(new Dimension(0, 0));
        this.setMinimumSize(new Dimension(0, 0));
        this.revalidate();

        this.setPreferredSize(null);
        this.setMaximumSize(null);
        this.setMinimumSize(null);
        this.repaint();

        wfPanel.setPreferredSize(new Dimension(0, 0));
        wfPanel.setMaximumSize(new Dimension(0, 0));
        wfPanel.setMinimumSize(new Dimension(0, 0));

        wfPanel.setPreferredSize(null);
        wfPanel.setMaximumSize(null);
        wfPanel.setMinimumSize(null);
        wfPanel.repaint();

    }

    class RefsetListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            layoutComponents();
        }

    }

    private Set<I_GetConceptData> getAllUsers() throws IOException, TerminologyException {
        I_GetConceptData userParent =
                LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
            .getConceptId());

        return userParent.getDestRelOrigins(allowedTypes, true, true);
    }

    private Set<I_GetConceptData> getValidEditors() throws Exception {
        I_GetConceptData selectedRefset = getRefset();
        Set<I_GetConceptData> editors = new HashSet<I_GetConceptData>();
        if (selectedRefset != null) {
            for (I_GetConceptData user : getAllUsers()) {
                if (hasPermission(user, selectedRefset)) {
                    editors.add(user);
                }
            }
        }

        return editors;
    }

    private boolean hasPermission(I_GetConceptData user, I_GetConceptData selectedRefset) throws Exception {
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

    class CheckBoxListener implements ItemListener {
        File file;

        public CheckBoxListener(File file) {
            this.file = file;
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                attachments.remove(file);
                layoutComponents();
            }
        }
    }

    class ButtonListener implements ActionListener {

        public ButtonListener() {
        }

        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("Attach a file")) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setDialogTitle("Attach a file");
                int returnValue = fileChooser.showDialog(new Frame(), "Attach file");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    attachments.add(fileChooser.getSelectedFile());
                    layoutComponents();
                }
            }
        }
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

    public HashSet<File> getAttachments() {
        return attachments;
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

    public Calendar getDeadline() {
        return deadlinePicker.getSelectedDate();
    }

    public String getPriority() {
        return (String) priorityComboBox.getSelectedItem();
    }
}