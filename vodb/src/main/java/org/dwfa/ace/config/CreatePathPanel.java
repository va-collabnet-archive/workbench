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

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.path.SelectPathAndPositionPanelWithCombo;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

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
            HashSet<I_Position> origins = new HashSet<I_Position>();
            origins.addAll(this.sppp.getSelectedPositions());

            AceLog.getAppLog().info(origins.toString());
            if (origins.size() == 0) {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "You must select at least one origin for path.");
                return;
            }
            ConceptBean selectedParent = (ConceptBean) parent.getTermComponent();
            if (selectedParent == null) {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "You must designate one parent for path.");
                return;
            }

            I_TermFactory termFactory = AceConfig.getVodb();
            I_Path workbenchPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            int currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            int presentTime = ThinVersionHelper.convert(System.currentTimeMillis());
            int unspecifiedSourceId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());

            // Create a concept for the path

            int newNativeId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(), unspecifiedSourceId,
                workbenchPath, presentTime);
            ConceptBean cb = ConceptBean.get(newNativeId);
            cb.getUncommittedIds().add(newNativeId);

            // Needs a concept record...
            I_ConceptAttributeVersioned con = new ThinConVersioned(newNativeId, 1);
            ThinConPart part = new ThinConPart();
            part.setPathId(workbenchPath.getConceptId());
            part.setVersion(Integer.MAX_VALUE);

            part.setStatusId(currentStatusId);
            part.setDefined(false);
            con.addVersion(part);
            cb.setUncommittedConceptAttributes(con);
            cb.getUncommittedIds().add(con.getConId());

            // Needs a description record...
            int nativeDescId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(), unspecifiedSourceId,
                workbenchPath, presentTime);
            ThinDescVersioned descV = new ThinDescVersioned(nativeDescId, newNativeId, 1);
            ThinDescPart descPart = new ThinDescPart();
            descPart.setPathId(workbenchPath.getConceptId());
            descPart.setVersion(Integer.MAX_VALUE);
            descPart.setStatusId(currentStatusId);
            descPart.setInitialCaseSignificant(true);
            descPart.setTypeId(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
            descPart.setLang("en");
            descPart.setText(desc.getText());
            descV.addVersion(descPart);
            cb.getUncommittedDescriptions().add(descV);
            cb.getUncommittedIds().add(descV.getDescId());

            // Needs a relationship record...
            int nativeRelId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(), unspecifiedSourceId,
                workbenchPath, presentTime);
            ThinRelVersioned relV = new ThinRelVersioned(nativeRelId, newNativeId, selectedParent.getConceptId(), 1);
            ThinRelPart relPart = new ThinRelPart();
            relPart.setPathId(workbenchPath.getConceptId());
            relPart.setVersion(Integer.MAX_VALUE);
            relPart.setStatusId(currentStatusId);
            relPart.setTypeId(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
            relPart.setCharacteristicId(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()));
            relPart.setRefinabilityId(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()));
            relPart.setGroup(0);
            relV.addVersion(relPart);
            cb.getUncommittedSourceRels().add(relV);
            cb.getUncommittedIds().add(relV.getRelId());

            ACE.addUncommitted(cb);
            ACE.commit();

            // Now make the concept a path

            termFactory.newPath(origins, cb);

            AceLog.getAppLog().info("Created new path: " + desc.getText() + " " + origins);
            this.desc.setText("");
            this.parent.setTermComponent(null);
            ACE.commit();

        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
