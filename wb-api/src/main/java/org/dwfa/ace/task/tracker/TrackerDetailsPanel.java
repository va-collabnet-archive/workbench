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
package org.dwfa.ace.task.tracker;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The tracker details panel allows user to input:
 * 1) tracker ID (text field)
 * 2) name 1 (text field)
 * 3) name 2 (text field)
 * 
 * @author Chrissy Hill
 * 
 */
public class TrackerDetailsPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel artfIDLabel;
    private JLabel name1Label;
    private JLabel name2Label;

    private JTextArea artfIDTextField;
    private JTextArea name1TextField;
    private JTextArea name2TextField;

    private JScrollPane artfIDScrollPane;
    private JScrollPane name1ScrollPane;
    private JScrollPane name2ScrollPane;

    public TrackerDetailsPanel() {
        super();
        init();
    }

    private void init() {
        setDefaultValues();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        artfIDLabel = new JLabel("ARTF ID (required):");
        name1Label = new JLabel("Name 1 (required):");
        name2Label = new JLabel("Name 2 (required):");

        // text fields
        artfIDTextField = new JTextArea();
        artfIDTextField.setLineWrap(true);
        artfIDTextField.setWrapStyleWord(true);
        artfIDScrollPane = new JScrollPane(artfIDTextField);
        artfIDScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        name1TextField = new JTextArea();
        name1TextField.setLineWrap(true);
        name1TextField.setWrapStyleWord(true);
        name1ScrollPane = new JScrollPane(name1TextField);
        name1ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        name2TextField = new JTextArea();
        name2TextField.setLineWrap(true);
        name2TextField.setWrapStyleWord(true);
        name2ScrollPane = new JScrollPane(name2TextField);
        name2ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // artf ID label & box
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(artfIDLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1;
        this.add(artfIDScrollPane, gbc);

        // name 1
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 40, 5);
        this.add(name1Label, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(name1ScrollPane, gbc);

        // name 2
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 40, 5);
        this.add(name2Label, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(name2ScrollPane, gbc);

        this.validate();
    }

    public String getName2() {
        String result = name2TextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public String getArtfID() {
        String result = artfIDTextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public String getName1() {
        String result = name1TextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

}
