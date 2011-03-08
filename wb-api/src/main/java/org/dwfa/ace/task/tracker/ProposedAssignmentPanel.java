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

/**
 * The Proposed Assignment panel displays to the user:
 * 1) tracker ID (label)
 * 2) tracker title (label)
 * 
 * @author Chrissy Hill
 * 
 */
public class ProposedAssignmentPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel artfIDLabel;
    private JLabel trackerTitleLabel;

    private JLabel artfIDDataLabel;
    private JLabel trackerTitleDataLabel;

    private JScrollPane trackerTitleScrollPane;

    private String artfID;
    private String trackerTitle;

    public ProposedAssignmentPanel(String artfID, String trackerTitle) {
        super();
        init();
        this.artfID = artfID;
        this.trackerTitle = trackerTitle;
    }

    private void init() {
        setDefaultValues();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        artfIDLabel = new JLabel("ARTF ID:");
        trackerTitleLabel = new JLabel("Tracker title:");

        // text fields
        artfIDDataLabel = new JLabel(artfID);

        trackerTitleDataLabel = new JLabel(trackerTitle);
        trackerTitleScrollPane = new JScrollPane(trackerTitleDataLabel);
        trackerTitleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // artf ID
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
        this.add(artfIDDataLabel, gbc);

        // tracker title
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 40, 5);
        this.add(trackerTitleLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(trackerTitleScrollPane, gbc);

        this.validate();
    }

    public String getArtfID() {
        String result = artfIDDataLabel.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public String getTrackerTitle() {
        String result = trackerTitleDataLabel.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

}
