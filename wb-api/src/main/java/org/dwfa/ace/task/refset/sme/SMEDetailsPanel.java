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
package org.dwfa.ace.task.refset.sme;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dwfa.ace.api.I_GetConceptData;

/**
 * The refset and subject matter expert panel allows user to input:
 * 1) refset name (from pulldown menu)
 * 2) subject matter expert name (text field)
 * 3) comment (optional) (text field)
 * 
 * @author Chrissy Hill
 * 
 */
public class SMEDetailsPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetNameLabel;
    private JLabel smeNameLabel;
    private JLabel commentsLabel;

    private JComboBox refsetNameComboBox;
    private JTextArea smeNameTextField;
    private JTextArea commentsTextField;

    private Set<I_GetConceptData> refsets;
    private JScrollPane smeNameScrollPane;
    private JScrollPane commentsScrollPane;

    public SMEDetailsPanel(Set<I_GetConceptData> refsets) {
        super();
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
        smeNameLabel = new JLabel("Subject matter expert name (required):");
        commentsLabel = new JLabel("Comments (optional):");

        // buttons and boxes
        refsetNameComboBox = new JComboBox(refsets.toArray());

        // text fields
        smeNameTextField = new JTextArea();
        smeNameTextField.setLineWrap(true);
        smeNameTextField.setWrapStyleWord(true);
        smeNameScrollPane = new JScrollPane(smeNameTextField);
        smeNameScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void addListeners() {
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

        // original request
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 40, 5);
        this.add(smeNameLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(smeNameScrollPane, gbc);

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

        this.validate();
    }

    class RefsetListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            layoutComponents();
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

    public String getSmeNameRequest() {
        String result = smeNameTextField.getText();
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

    public void focusOnRefsetName() {
        refsetNameComboBox.requestFocusInWindow();
    }
}
