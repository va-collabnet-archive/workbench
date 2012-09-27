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
package org.dwfa.ace.grant;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class GrantPanel extends JPanel {
    I_GetConceptData userConcept;

    private class CreateGrants implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                I_TermFactory tf = Terms.get();

                if (taxonomyLabel.getTermComponent() != null) {
                    I_GetConceptData taxonomy = (I_GetConceptData) taxonomyLabel.getTermComponent();
                    for (int i = 0; i < roleGrantTableModel.getSize(); i++) {
                        I_GetConceptData grant = roleGrantTableModel.getElementAt(i);
                        I_RelVersioned grantRel =
                                tf.newRelationship(UUID.randomUUID(), userConcept, grant, taxonomy, tf
                                    .getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), tf
                                    .getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()), tf
                                    .getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, grantorConfig);
                        uncommittedGrants.add(grantRel);
                        Terms.get().addUncommitted(userConcept);

                    }
                }
                roleGrantTableModel.clear();
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public class GrantListRenderer extends DefaultListCellRenderer {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            I_RelVersioned grantRel = (I_RelVersioned) value;
            I_RelTuple grantRelTuple = grantRel.getFirstTuple();
            try {
                I_GetConceptData grantType = Terms.get().getConcept(grantRelTuple.getTypeId());
                I_GetConceptData grantTaxonomy = Terms.get().getConcept(grantRelTuple.getC2Id());
                List<String> htmlParts = new ArrayList<String>();
                addConceptDescription(htmlParts, grantType, "#191970");
                htmlParts.add("<font color='#191970'>:&nbsp;&nbsp;</font>");
                addConceptDescription(htmlParts, grantTaxonomy, "black");
                htmlParts.add("&nbsp;&nbsp;");
                setTextToHtml(htmlParts);

            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }

            return this;
        }

        private void setTextToHtml(List<String> htmlParts) {
            StringBuffer buff = new StringBuffer();
            if (htmlParts.size() > 0) {
                buff.append("<html>");
                for (String prefix : htmlParts) {
                    buff.append(prefix);
                }
            }
            setText(buff.toString());
        }

        private void addConceptDescription(List<String> htmlParts, I_GetConceptData concept, String color)
                throws IOException {
            htmlParts.add("<font color='" + color + "'>");
            addConceptDescription(htmlParts, concept);
            htmlParts.add("</font>");
        }

        private void addConceptDescription(List<String> htmlParts, I_GetConceptData concept) throws IOException {
            I_DescriptionTuple desc = concept.getDescTuple(config.getTreeDescPreferenceList(), config);
            if (desc != null) {
                String text = desc.getText();
                if (text.toLowerCase().startsWith("<html>")) {
                    htmlParts.add(text.substring(5));
                } else {
                    htmlParts.add(text);
                }
            } else {
                htmlParts.add(concept.toString());
            }
        }

    }

    private class DeleteGrant extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
                I_RelVersioned grant = (I_RelVersioned) addedGrantList.getSelectedValue();
                uncommittedGrants.remove(grant);
                Terms.get().forget(grant);
                Terms.get().addUncommitted(userConcept);

            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private I_ConfigAceFrame config;
    private I_ConfigAceFrame grantorConfig;

    private TerminologyListModel roleGrantTableModel;
    private TermComponentLabel taxonomyLabel;
    private ArrayListModel<I_RelVersioned> uncommittedGrants;

    private JList addedGrantList;

    public GrantPanel(I_ConfigAceFrame config, I_ConfigAceFrame grantorConfig) throws Exception {
        this(config, grantorConfig, config.getDbConfig().getUserConcept());
    }

    public GrantPanel(I_ConfigAceFrame config, I_ConfigAceFrame grantorConfig, I_GetConceptData userConcept)
            throws Exception {

        super(new GridBagLayout());
        this.config = config;
        this.grantorConfig = grantorConfig;
        this.userConcept = userConcept;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;

        TermComponentLabel userLabel = new TermComponentLabel(config);
        userLabel.setTermComponent(userConcept);
        userLabel.setFrozen(true);
        JPanel userPanel = new JPanel(new GridLayout(1, 1));
        userPanel.add(userLabel);
        userPanel.setBorder(BorderFactory.createTitledBorder("Create grants for user: "));
        add(userPanel, gbc);

        gbc.gridy++;
        taxonomyLabel = new TermComponentLabel(config);
        JPanel taxonomyLabelPanel = new JPanel(new GridLayout(1, 1));
        taxonomyLabelPanel.add(taxonomyLabel);
        taxonomyLabelPanel.setBorder(BorderFactory.createTitledBorder("Taxonomy for grants: "));
        add(taxonomyLabelPanel, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        roleGrantTableModel = new TerminologyListModel();
        TerminologyList roleGrantList = new TerminologyList(roleGrantTableModel, config);
        roleGrantList.setBorder(BorderFactory.createTitledBorder("Roles/permissions to grant: "));
        add(new JScrollPane(roleGrantList), gbc);

        gbc.gridy++;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        JButton addGrants = new JButton("add grants");
        addGrants.addActionListener(new CreateGrants());
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(addGrants, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        uncommittedGrants = new ArrayListModel<I_RelVersioned>();
        addedGrantList = new JList(uncommittedGrants);
        addedGrantList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteGrant");
        addedGrantList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteGrant");
        addedGrantList.getActionMap().put("deleteGrant", new DeleteGrant());
        addedGrantList.setCellRenderer(new GrantListRenderer());
        JScrollPane grantListScroller = new JScrollPane(addedGrantList);
        grantListScroller.setBorder(BorderFactory.createTitledBorder("Grants to commit: "));
        add(grantListScroller, gbc);
    }

}
