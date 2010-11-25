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
package org.dwfa.ace.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.path.SelectPathAndPositionPanelWithCombo;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.tk.api.PositionBI;

public class CreatePathPanel extends JPanel implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -6900445108100412148L;
    JTextField desc;
    SelectPathAndPositionPanelWithCombo sppp;
    TermComponentLabel parent;

    /**
     * @param config
     * @throws Exception
     * @throws RemoteException
     * @throws QueryException
     */
    public CreatePathPanel(I_ConfigAceFrame aceConfig) throws Exception {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        this.desc = new JTextField();
        this.desc.setBorder(BorderFactory.createTitledBorder("Description for new path:"));
        this.add(this.desc, c);

        c.gridy++;
        parent = new TermComponentLabel(aceConfig);
        JPanel parentHolder = new JPanel(new GridLayout(1, 1));
        parentHolder.add(parent);
        parentHolder.setBorder(BorderFactory.createTitledBorder("Parent for path:"));
        this.add(parentHolder, c);

        JButton createButton = new JButton("create");
        createButton.addActionListener(this);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 1;
        c.gridy++;
        this.add(createButton, c);
        /*
         * PropertySetListenerGlue browsingPositionGlue = new
         * PropertySetListenerGlue(null, null,
         * null, null, Position.class, config);
         */
        sppp = new SelectPathAndPositionPanelWithCombo(true, "as origin", aceConfig, null);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy++;
        this.add(sppp, c);

    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        try {
            int n = JOptionPane.showConfirmDialog(this,
                "This operation will perform an immediate commit of all changes. \n\nDo you wish to proceed?",
                "Confirm commit...", JOptionPane.YES_NO_OPTION);

            if (n != JOptionPane.YES_OPTION) {
                return;
            }

            AceLog.getAppLog().info("Create new path: " + desc.getText());
            if (desc.getText() == null || desc.getText().length() == 0) {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Path description cannot be empty.");
                return;
            }
            HashSet<PositionBI> origins = new HashSet<PositionBI>();
            origins.addAll(this.sppp.getSelectedPositions());

            AceLog.getAppLog().info(origins.toString());
            if (origins.size() == 0) {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "You must select at least one origin for path.");
                return;
            }
            I_GetConceptData selectedParent =  (I_GetConceptData) parent.getTermComponent();
            if (selectedParent == null) {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "You must designate one parent for the path.");
                return;
            }

            // Create a concept for the path
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            
            UUID pathUUID = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, desc.getText());

            I_GetConceptData cb = Terms.get().newConcept(pathUUID,  false, config);

            // Needs a description record...
            Terms.get().newDescription(UUID.randomUUID(), cb, "en", desc.getText(),
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), 
            		config);
            Terms.get().newDescription(UUID.randomUUID(), cb, "en", desc.getText(),
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), 
            		config);
 

            // Needs a relationship record...
            Terms.get().newRelationship(UUID.randomUUID(), cb,
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            		selectedParent,
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
            		0, config);

            Terms.get().addUncommitted(cb);
            Terms.get().commit();

            // Now make the concept a path

            Terms.get().newPath(origins, cb);

            AceLog.getAppLog().info("Created new path: " + desc.getText() + " uuid: " + pathUUID + " Origins: " + origins);
            this.desc.setText("");
            this.parent.setTermComponent(null);
            Terms.get().commit();
            AceLog.getAppLog().info("Paths after commit: " + Terms.get().getPaths());
            
            

        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
