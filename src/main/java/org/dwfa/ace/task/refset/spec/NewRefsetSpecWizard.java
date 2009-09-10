package org.dwfa.ace.task.refset.spec;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.dwfa.util.LogWithAlerts;

public class NewRefsetSpecWizard {

    private boolean createData = true;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private JDialog wizardDialog;

    HashMap<String, JPanel> panels = new HashMap<String, JPanel>();
    private String currentPanel;

    public NewRefsetSpecWizard(Frame owner) {
        wizardDialog = new JDialog(owner);
        wizardDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                createData = false;
                wizardDialog.dispose();
            }
        });

        initComponents();
    }

    public JDialog getDialog() {
        return wizardDialog;
    }

    public Component getOwner() {
        return wizardDialog.getOwner();
    }

    public void setTitle(String s) {
        wizardDialog.setTitle(s);
    }

    public String getTitle() {
        return wizardDialog.getTitle();
    }

    private void initComponents() {

        JPanel buttonPanel = new JPanel();
        JSeparator separator = new JSeparator();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        backButton = new JButton("Back");
        nextButton = new JButton("Next");
        cancelButton = new JButton("Cancel");
        nextButton.setEnabled(true);
        backButton.setEnabled(false);
        cancelButton.setEnabled(true);

        backButton.addActionListener(new ButtonListener());
        nextButton.addActionListener(new ButtonListener());
        cancelButton.addActionListener(new ButtonListener());

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);

        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);

        wizardDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        wizardDialog.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);
    }

    public void registerWizardPanel(String id, JPanel panel) {
        cardPanel.add(panel, id);
        panels.put(id, panel);
    }

    void setBackButtonEnabled(boolean b) {
        backButton.setEnabled(b);
    }

    void setNextButtonEnabled(boolean b) {
        nextButton.setEnabled(b);
    }

    public void showModalDialog() {

        wizardDialog.setModal(true);
        wizardDialog.pack();
        wizardDialog.setLocationRelativeTo(null); // center frame
        wizardDialog.setVisible(true);
    }

    public void setCurrentPanel(String id) {
        currentPanel = id;
        cardLayout.show(cardPanel, currentPanel);
    }

    public JPanel getCurrentPanel() {
        return panels.get(currentPanel);
    }

    class ButtonListener implements ActionListener {

        public ButtonListener() {
            super();
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {

            if (evt.getActionCommand().equals("Cancel")) {
                cancelButtonPressed();
            } else if (evt.getActionCommand().equals("Back")) {
                backButtonPressed();
            } else if (evt.getActionCommand().equals("Next") || evt.getActionCommand().equals("Finish")) {
                nextButtonPressed();
            }
        }

        private void nextButtonPressed() {
            if (currentPanel.equals("panel1")) {
                NewRefsetSpecForm1 form1 = (NewRefsetSpecForm1) getCurrentPanel();
                if (form1.getRefsetNameTextField() != null) {
                    setCurrentPanel("panel2");
                    nextButton.setText("Finish");
                    nextButton.setEnabled(true);
                    backButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Refset name is required.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else if (currentPanel.equals("panel2")) {
                NewRefsetSpecForm2 form2 = (NewRefsetSpecForm2) getCurrentPanel();
                if (form2.getDeadline() != null) {
                    createData = true;
                    wizardDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Deadline is required.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void backButtonPressed() {
            if (currentPanel.equals("panel1")) {
                // nothing to do
                backButton.setEnabled(false);
            } else if (currentPanel.equals("panel2")) {
                setCurrentPanel("panel1");
                nextButton.setText("Next");
                nextButton.setEnabled(true);
                backButton.setEnabled(false);
                cancelButton.setEnabled(true);
            }
        }

        private void cancelButtonPressed() {
            createData = false;
            wizardDialog.dispose();
        }
    }

    public boolean isCreateData() {
        return createData;
    }

    public void setCreateData(boolean createData) {
        this.createData = createData;
    }
}
