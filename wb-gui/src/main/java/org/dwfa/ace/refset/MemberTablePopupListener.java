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
package org.dwfa.ace.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.ReflexiveTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class MemberTablePopupListener extends MouseAdapter {

    private class CommentSpecAction implements ActionListener {
        private String prompt;

        private CommentSpecAction(String prompt) {
            super();
            this.prompt = prompt;
        }

        public void actionPerformed(ActionEvent arg0) {
            String commentText =
                    (String) JOptionPane.showInputDialog(config.getTreeInSpecEditor().getRootPane(), "", prompt
                        + ":             ", JOptionPane.PLAIN_MESSAGE, null, null, "");
            if (commentText != null && commentText.length() > 2) {
                try {
                    I_GetConceptData refsetIdentityConcept = config.getRefsetInSpecEditor();
                    I_HelpRefsets refsetHelper = Terms.get().getRefsetHelper(config);
                    Set<? extends I_GetConceptData> commentRefsets = 
                    	refsetHelper.getCommentsRefsetForRefset(refsetIdentityConcept, config);
                    if (commentRefsets.size() > 0) {
                    	for (I_GetConceptData commentRefsetIdentityConcept: commentRefsets) {
                	    	RefsetPropertyMap refsetMap = 
                	    		new RefsetPropertyMap(REFSET_TYPES.STR);
                	    	refsetMap.put(REFSET_PROPERTY.STRING_VALUE, commentText);
                	    	I_ExtendByRef newExtension = 
                	    		refsetHelper.getOrCreateRefsetExtension(commentRefsetIdentityConcept.getNid(), 
                	    				selectedObject.getTuple().getComponentId(), REFSET_TYPES.STR, refsetMap, UUID.randomUUID());
                			Terms.get().addUncommitted(newExtension);
                    	}
                        for (ReflexiveTableModel m : commentTableModels) {
                            m.propertyChange(null);
                        }
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
    }

    JPopupMenu popup;

    JTable table;

    StringWithExtTuple selectedObject;

    I_ConfigAceFrame config;

    List<ReflexiveTableModel> commentTableModels;

    public MemberTablePopupListener(JTable table, I_ConfigAceFrame config, List<ReflexiveTableModel> commentTableModels) {
        super();
        this.table = table;
        this.config = config;
        this.commentTableModels = commentTableModels;
    }

    private void makePopup(MouseEvent e) {
        try {
            popup = null;
            int column = table.columnAtPoint(e.getPoint());
            int row = table.rowAtPoint(e.getPoint());
            if ((row != -1) && (column != -1)) {
                popup = new JPopupMenu();
                JMenuItem noActionItem = new JMenuItem("");
                popup.add(noActionItem);
                selectedObject = (StringWithExtTuple) table.getValueAt(row, column);
                I_GetConceptData refsetConcept = config.getRefsetInSpecEditor();
                I_DescriptionTuple refsetDesc = refsetConcept.getDescTuple(config.getTableDescPreferenceList(), config);
                String prompt = "Add comment for '" + refsetDesc.getText() + "'";
                JMenuItem commentItem = new JMenuItem(prompt + "...");
                popup.add(commentItem);
                commentItem.addActionListener(new CommentSpecAction(prompt));
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            if (config.getEditingPathSet().size() > 0) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                Object obj = table.getValueAt(row, column);
                if (obj.getClass().isAssignableFrom(StringWithExtTuple.class)) {
                    selectedObject = (StringWithExtTuple) table.getValueAt(row, column);
                    makePopup(e);
                    if (popup != null) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
             } else {
                JOptionPane.showMessageDialog(table.getTopLevelAncestor(),
                    "You must select at least one path to edit on...");
            }
            e.consume();
        }
    }
}
