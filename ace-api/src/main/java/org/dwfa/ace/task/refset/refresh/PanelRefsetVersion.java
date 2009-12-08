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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.tapi.TerminologyException;

import org.dwfa.ace.path.*;

/**
 * Panel to collect the version of the Refset
 * 
 * @author Perry Reid
 * @version 1, November 2009
 */
public class PanelRefsetVersion extends JPanel {

    private static final long serialVersionUID = 1L;
    private Set<I_Position> positionSet = new HashSet<I_Position>();
    private JList positionList;
    private ArrayListModel<I_Position> positionListModel;
    private SelectPathAndPositionPanelWithCombo pppwc;
    private JLabel selectedRefsetSpecLabel;

    public class DeleteAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            I_Position p = (I_Position) positionList.getSelectedValue();
            positionListModel.remove(p);
            positionSet.remove(p);
        }
    }

    private class AddPathActionLister implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (positionSet.contains(getCurrentPosition()) == false) {
                    positionSet.add(getCurrentPosition());
                    positionListModel.add(getCurrentPosition());
                }
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public PanelRefsetVersion(I_ConfigAceFrame config) throws Exception {
        super(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        Font sansSerifFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);

        // Add the Selected Refset label
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        add(new JLabel("Selected Refset Spec:"), gbc);

        // Add the Selected Refset as a reminder
        // (The real value will be set by another task)
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        // Get the name of the selected Refset
        selectedRefsetSpecLabel = new JLabel("   NO REFSET SPEC SELECTED");
        selectedRefsetSpecLabel.setFont(sansSerifFont);
        selectedRefsetSpecLabel.setForeground(Color.blue);
        add(selectedRefsetSpecLabel, gbc);

        // Add the path and positions
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        pppwc = new SelectPathAndPositionPanelWithCombo(true, "", config, null);
        pppwc.setPositionCheckBoxVisible(false);
        pppwc.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pppwc, gbc);

        // Add the "add position" button
        gbc.weighty = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JButton addPathButton = new JButton("add position");
        addPathButton.addActionListener(new AddPathActionLister());
        add(addPathButton, gbc);

        // Add the "position set" list control
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        positionListModel = new ArrayListModel<I_Position>();
        positionList = new JList(positionListModel);
        positionList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        positionList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");
        positionList.getActionMap().put("deleteTask", new DeleteAction());

        JScrollPane positionScroller = new JScrollPane(positionList);
        positionScroller.setMinimumSize(new Dimension(100, 100));
        positionScroller.setMaximumSize(new Dimension(500, 500));
        positionScroller.setPreferredSize(new Dimension(150, 150));
        positionScroller.setBorder(BorderFactory.createTitledBorder("Position set: "));
        add(positionScroller, gbc);
    }

    public I_Position getCurrentPosition() throws TerminologyException, IOException {
        return pppwc.getCurrentPosition();
    }

    public Set<I_Position> getPositionSet() {
        return positionSet;
    }

    public void setPositionSet(Set<I_Position> newPositions) {
        positionListModel.clear();
        positionSet.clear();
        positionListModel.addAll(newPositions);
        positionSet.addAll(newPositions);
    }

    public String getSelectedRefsetSpecLabel() {
        return this.selectedRefsetSpecLabel.getText();
    }

    public void setSelectedRefsetSpecLabel(String refsetName) {
        this.selectedRefsetSpecLabel.setText("   " + refsetName);
    }

}
