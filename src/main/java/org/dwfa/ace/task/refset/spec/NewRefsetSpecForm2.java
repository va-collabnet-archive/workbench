package org.dwfa.ace.task.refset.spec;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
    private JLabel deadlineLabel;
    private JLabel priorityLabel;

    private JTextField requestorTextField;
    private DatePicker deadlinePicker;
    private JComboBox priorityComboBox;
    private JComboBox editorComboBox;
    private Set<String> editors;

    public NewRefsetSpecForm2(Set<String> editors) {
        super();
        this.editors = editors;
        init();
    }

    private void init() {
        setDefaultValues();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        requestorLabel = new JLabel("Requestor (optional):");
        editorLabel = new JLabel("Editor (required):");
        deadlineLabel = new JLabel("Deadline (required):");
        priorityLabel = new JLabel("Priority (required):");

        // date picker
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        deadlinePicker = new DatePicker(Calendar.getInstance(), null, dateFormat);

        // text fields
        requestorTextField = new JTextField(20);

        // combo box
        priorityComboBox = new JComboBox(new String[] { "Highest", "High", "Normal", "Low", "Lowest" });
        editorComboBox = new JComboBox(editors.toArray());
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());

        // requestor name label & text box
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

        // editor label & radio button
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

        // deadline label and date chooser
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlineLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlinePicker, gridBagConstraints);

        // priority label and drop down list
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityComboBox, gridBagConstraints);

        // column filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

    }

    public String getSelectedEditor() {
        return (String) editorComboBox.getSelectedItem();
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