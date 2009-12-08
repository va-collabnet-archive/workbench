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
package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;

public class DescSearchResultsTablePopupListener implements MouseListener, ActionListener {

    private I_DescriptionTuple descTuple;
    private ConceptBean descConcept;
    private int selectedRow;
    private I_ConfigAceFrame config;
    private JTable descTable;

    DescSearchResultsTablePopupListener(I_ConfigAceFrame config) {
        super();
        this.config = config;
    }

    public void mouseClicked(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            handlePopup(e);
            e.consume();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            handlePopup(e);
        }
    }

    private void handlePopup(MouseEvent e) {
        descTable = (JTable) e.getSource();
        if (descTable.getCellRect(descTable.getSelectedRow(), descTable.getSelectedColumn(), true).contains(
            e.getPoint())) {
            selectedRow = descTable.getSelectedRow();
            StringWithDescTuple swdt = (StringWithDescTuple) descTable.getValueAt(selectedRow, 0);
            descTuple = swdt.getTuple();
            descConcept = ConceptBean.get(descTuple.getConceptId());
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(" ");
            popup.add(menuItem);
            addProcessItems(popup, new File(AceFrame.pluginRoot, "search-results"));
            popup.addSeparator();
            menuItem = new JMenuItem("Show in taxonomy");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab L-1");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            popup.addSeparator();
            menuItem = new JMenuItem("Put in Concept Tab R-1");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab R-2");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab R-3");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab R-4");
            popup.add(menuItem);
            popup.addSeparator();
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Add to list");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            popup.show(descTable, e.getX(), e.getY());
            e.consume();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Show in taxonomy")) {
            try {
                AceFrameConfig frameConfig = (AceFrameConfig) config;
                new ExpandPathToNodeStateListener(frameConfig.getAceFrame().getCdePanel().getTree(), config,
                    descConcept);
                config.setHierarchySelection(descConcept);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        } else if (e.getActionCommand().equals("Put in Concept Tab L-1")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(5);
            viewer.setTermComponent(descConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-1")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(1);
            viewer.setTermComponent(descConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-2")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(2);
            viewer.setTermComponent(descConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-3")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(3);
            viewer.setTermComponent(descConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-4")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(4);
            viewer.setTermComponent(descConcept);
        } else if (e.getActionCommand().equals("Add to list")) {
            JList conceptList = config.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
            model.addElement(descConcept);
        }
    }

    private void addProcessItems(JPopupMenu popup, File directory) {
        try {
            ProcessPopupUtil.addSubMenuItems(popup, directory, config.getWorker(), null);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

}
