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
package org.dwfa.ace.task.search;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.bpa.process.TaskFailedException;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.io.IOException;
import java.util.ListIterator;

public class SearchReplaceDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = -5969210021636439616L;

    private JPanel contentPane;
    private JButton searchReplaceButton;
    private JButton cancelButton;
    private JTextField searchStringTextField;
    private JTextField replaceStringTextField;
    private JComboBox retireAsStatus;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox fullySpecifiedNameCheckBox;
    private JCheckBox preferredTermCheckBox;
    private JCheckBox allCheckBox;
    private JCheckBox synonymCheckBox;

    private boolean isCancelled = false;

    public SearchReplaceDialog() throws TaskFailedException {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(searchReplaceButton);

        searchReplaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        int retiredConceptId = -1;
        I_IntList statusIntList;
        I_TermFactory termFactory;

        try {
            termFactory = LocalVersionedTerminology.get();
            statusIntList = termFactory.getActiveAceFrameConfig().getEditStatusTypePopup();
            retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()
                .iterator()
                .next());

            ListIterator<Integer> itr = statusIntList.listIterator();
            while (itr.hasNext()) {
                Integer integer = itr.next();
                I_GetConceptData status = termFactory.getConcept(integer);
                retireAsStatus.addItem(status);
                if (status.getConceptId() == retiredConceptId) {
                    retireAsStatus.setSelectedItem(status);
                }
            }
            if (retireAsStatus.getSelectedItem() == null) {
                retireAsStatus.setSelectedIndex(0);
            }
        } catch (IOException e) {
            throw new TaskFailedException("Failed populating the list of statuses available for retiring", e);
        } catch (TerminologyException e) {
            throw new TaskFailedException("Failed populating the list of statuses available for retiring", e);
        }
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        isCancelled = true;
        dispose();
    }

    public void displose() {
        searchReplaceButton.removeAll();
        cancelButton.removeAll();
        super.dispose();
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public String getSearchString() {
        return searchStringTextField.getText();
    }

    public String getReplaceString() {
        return replaceStringTextField.getText();
    }

    public boolean isCaseSensitive() {
        return caseSensitiveCheckBox.isSelected();
    }

    public boolean isAll() {
        return allCheckBox.isSelected();
    }

    public boolean isFullySpecifiedName() {
        return fullySpecifiedNameCheckBox.isSelected();
    }

    public boolean isPreferredTerm() {
        return preferredTermCheckBox.isSelected();
    }

    public boolean isSynonym() {
        return synonymCheckBox.isSelected();
    }

    public int getRetireAsStatus() {
        return ((I_GetConceptData) retireAsStatus.getSelectedItem()).getConceptId();
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     * 
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setMaximumSize(new Dimension(550, 200));
        contentPane.setMinimumSize(new Dimension(550, 200));
        contentPane.setPreferredSize(new Dimension(550, 200));

        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);

        final JLabel label1 = new JLabel();
        label1.setText("Find:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Replace with:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Retire as:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label3, gbc);
        searchStringTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(searchStringTextField, gbc);
        replaceStringTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(replaceStringTextField, gbc);
        retireAsStatus = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(retireAsStatus, gbc);

        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer3, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel2, gbc);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel2.add(toolBar$Separator1, gbc);
        caseSensitiveCheckBox = new JCheckBox();
        caseSensitiveCheckBox.setText("Case sensitive");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(caseSensitiveCheckBox, gbc);
        allCheckBox = new JCheckBox();
        allCheckBox.setSelected(true);
        allCheckBox.setText("All");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(allCheckBox, gbc);
        fullySpecifiedNameCheckBox = new JCheckBox();
        fullySpecifiedNameCheckBox.setSelected(true);
        fullySpecifiedNameCheckBox.setText("Fully Specified Name");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(fullySpecifiedNameCheckBox, gbc);
        preferredTermCheckBox = new JCheckBox();
        preferredTermCheckBox.setSelected(true);
        preferredTermCheckBox.setText("Preferred Term");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(preferredTermCheckBox, gbc);
        synonymCheckBox = new JCheckBox();
        synonymCheckBox.setSelected(true);
        synonymCheckBox.setText("Synonym");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(synonymCheckBox, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        searchReplaceButton = new JButton();
        searchReplaceButton.setText("Replace All");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(searchReplaceButton, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(spacer4, gbc);
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(cancelButton, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel3.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(spacer7, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add(spacer8, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
