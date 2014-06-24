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
package org.dwfa.ace.path;

import java.awt.Dimension;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PositionBI;

public class SelectPositionSetPanel extends JPanel {

    public class DeleteAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            I_Position p = (I_Position) positionList.getSelectedValue();
            positionListModel.remove(p);
            positionSet.remove(p);
        }
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Set<PositionBI> positionSet = new HashSet<PositionBI>();

    private JList positionList;

    private ArrayListModel<PositionBI> positionListModel;

    private SelectPathAndPositionPanelWithCombo pppwc;

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

    public SelectPositionSetPanel(I_ConfigAceFrame config) throws Exception {
        super(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;

        pppwc = new SelectPathAndPositionPanelWithCombo(true, "", config, null);
        pppwc.setPositionCheckBoxVisible(false);
        pppwc.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(pppwc, gbc);
        gbc.weighty = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JButton addPathButton = new JButton("add position");
        addPathButton.addActionListener(new AddPathActionLister());
        add(addPathButton, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        positionListModel = new ArrayListModel<PositionBI>();
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

    public PositionBI getCurrentPosition() throws TerminologyException, IOException {
        return pppwc.getCurrentPosition();
    }

    public Set<PositionBI> getPositionSet() {
        return positionSet;
    }

}
