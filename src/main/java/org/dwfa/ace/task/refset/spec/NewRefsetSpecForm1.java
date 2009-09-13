package org.dwfa.ace.task.refset.spec;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The first of two refset spec panels - this prompts the user to input the
 * refset name, any additional comments, and file attachments.
 * This panel is used as part of the new refset spec wizard.
 * 
 * @author Chrissy
 * 
 */
public class NewRefsetSpecForm1 extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetNameLabel;
    private JLabel refsetParentLabel;
    private JLabel requirementsLabel;
    private JLabel fileAttachmentLabel;
    private JButton openFileChooserButton;
    private JComboBox refsetParentComboBox;
    private JTextField refsetNameTextField;
    private JTextField refsetRequirementsTextField;
    private NewRefsetSpecWizard wizard;
    private HashSet<File> attachments = new HashSet<File>();
    private Set<String> refsetNames;

    public NewRefsetSpecForm1(NewRefsetSpecWizard wizard, Set<String> refsetNames) {
        super();
        this.refsetNames = refsetNames;
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
        refsetNameLabel = new JLabel("Refset name (required):");
        refsetParentLabel = new JLabel("Refset parent (required):");
        requirementsLabel = new JLabel("Requirements/comments (optional):");
        fileAttachmentLabel = new JLabel("File attachment (optional):");

        // buttons
        openFileChooserButton = new JButton("Attach a file");
        refsetParentComboBox = new JComboBox(refsetNames.toArray());

        // text fields
        refsetNameTextField = new JTextField(20);
        refsetRequirementsTextField = new JTextField(20);
    }

    private void addListeners() {
        openFileChooserButton.addActionListener(new ButtonListener(this));
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // refset name label & text box
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
        this.add(refsetNameTextField, gridBagConstraints);

        // refset parent label
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(refsetParentLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(refsetParentComboBox, gridBagConstraints);

        // requirements label & text box
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(requirementsLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(refsetRequirementsTextField, gridBagConstraints);

        // file attachment label and "Attach a file" button
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(fileAttachmentLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
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
            gridBagConstraints.gridy = 4 + fileCount;
            gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            this.add(checkBox, gridBagConstraints);

            JLabel attachmentLabel = new JLabel(attachment.getName());
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 4 + fileCount;
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
        gridBagConstraints.gridy = 5 + fileCount;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

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
                wizard.getDialog().pack();
                wizard.getDialog().repaint();
            }
        }
    }

    class ButtonListener implements ActionListener {
        NewRefsetSpecForm1 form;

        public ButtonListener(NewRefsetSpecForm1 form) {
            this.form = form;
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
                    wizard.getDialog().pack();
                }
            }
        }
    }

    public String getSelectedParent() {
        return (String) refsetParentComboBox.getSelectedItem();
    }

    public String getRefsetNameTextField() {
        String result = refsetNameTextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public String getRefsetRequirementsTextField() {
        String result = refsetRequirementsTextField.getText();
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
}