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
package org.dwfa.ace.table.refset;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.gui.concept.I_PluginToConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.ConceptFieldEditor;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.IntList;

public class RefsetUtil {

    public static void addRefsetTables(I_HostConceptPlugins host, org.dwfa.ace.api.I_PluginToConceptPanel plugin,
            TOGGLES toggle, GridBagConstraints c, Set<EXT_TYPE> visibleExtensions, JPanel panel) {
        plugin.clearRefsetListeners();
        if (host.getShowRefsets()) {
            for (EXT_TYPE extType : EXT_TYPE.values()) {
                if (((AceFrameConfig) host.getConfig()).isRefsetInToggleVisible(extType, toggle)) {
                    c.gridy++;
                    RefsetMemberTableModel refsetModel;
                    refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host,
                        extType), extType, toggle);

                    plugin.addRefsetListener(refsetModel);
                    c.gridy++;
                    try {
                        panel.add(getExtensionPanel(extType, refsetModel, host, toggle), c);
                    } catch (Exception e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                    visibleExtensions.add(extType);
                }
            }
        }
    }

    public static JPanel getExtensionPanel(EXT_TYPE extType, RefsetMemberTableModel refsetModel,
            I_HostConceptPlugins host, TOGGLES toggle) throws Exception {
        JPanel relPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel srcRelLabel = new JLabel("     " + extType.getInterfaceName() + " extensions");
        srcRelLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 0));
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        relPanel.add(srcRelLabel, c);

        SmallProgressPanel progress = new SmallProgressPanel();
        progress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        relPanel.add(progress, c);
        refsetModel.setProgress(progress);
        progress.setActive(false);
        progress.setProgressInfo("");

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;

        c.gridwidth = 1;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridheight = 2;
        if (ACE.editMode) {
            JButton rowAddAfter = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/paperclip_add.png")));
            relPanel.add(rowAddAfter, c);
            rowAddAfter.setEnabled(false);
            rowAddAfter.setToolTipText("add new refset member");
            refsetModel.setAddButton(rowAddAfter);
        } else {
            JPanel filler = new JPanel();
            filler.setMaximumSize(new Dimension(40, 32));
            filler.setMinimumSize(new Dimension(40, 32));
            filler.setPreferredSize(new Dimension(40, 32));
            relPanel.add(filler, c);

        }
        c.gridheight = 1;
        c.gridx++;
        c.gridwidth = 1;

        TableSorter refsetSortingTable = new TableSorter(refsetModel);
        JTableWithDragImage extTable = new JTableWithDragImage(refsetSortingTable);
        refsetSortingTable.setTableHeader(extTable.getTableHeader());
        refsetSortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");
        REFSET_FIELDS[] columnEnums = refsetModel.getColumns();
        for (int i = 0; i < extTable.getColumnCount(); i++) {
            TableColumn column = extTable.getColumnModel().getColumn(i);
            REFSET_FIELDS columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        extTable.setDragEnabled(true);
        extTable.setTransferHandler(new TerminologyTransferHandler(extTable));
        extTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (ACE.editMode) {
            extTable.addMouseListener(refsetModel.makePopupListener(extTable, host.getConfig()));
        }
        // Set up tool tips for column headers.
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        relPanel.add(extTable.getTableHeader(), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridheight = 5;
        extTable.setDefaultRenderer(StringWithExtTuple.class, new ExtTableRenderer());
        extTable.setDefaultRenderer(Number.class, new ExtTableRenderer());
        extTable.setDefaultRenderer(Boolean.class, new ExtTableRenderer());
        extTable.setDefaultRenderer(Integer.class, new ExtTableRenderer());
        extTable.setDefaultRenderer(Double.class, new ExtTableRenderer());
        extTable.setDefaultRenderer(String.class, new ExtTableRenderer());

        switch (extType) {
        case BOOLEAN:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getBooleanPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getBooleanPreferences()
                .getStatusPopupIds());
            JComboBox comboBox = new JComboBox() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void setSelectedItem(Object anObject) {
                    Boolean value = null;
                    if (Boolean.class.isAssignableFrom(anObject.getClass())) {
                        value = (Boolean) anObject;
                    } else if (StringWithExtTuple.class.isAssignableFrom(anObject.getClass())) {
                        I_CellTextWithTuple swt = (I_CellTextWithTuple) anObject;
                        value = Boolean.parseBoolean(swt.getCellText());
                    }
                    super.setSelectedItem(value);
                }
            };
            comboBox.addItem(new Boolean(true));
            comboBox.addItem(new Boolean(false));
            extTable.getColumn(REFSET_FIELDS.BOOLEAN_VALUE).setCellEditor(new DefaultCellEditor(comboBox));
            break;
        case STRING:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getStringPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getStringPreferences()
                .getStatusPopupIds());
            extTable.getColumn(REFSET_FIELDS.STRING_VALUE).setCellEditor(
                new RefsetMemberTableModel.StringExtFieldEditor());

            break;

        case CONCEPT:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptPreferences()
                .getStatusPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.CONCEPT_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptPreferences()
                .getConceptPopupIds());

            break;
        case CON_INT:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConIntPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConIntPreferences()
                .getStatusPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.CONCEPT_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConIntPreferences()
                .getConceptPopupIds());

            break;
        case CONCEPT_CONCEPT_STRING:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptConceptStringPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptConceptStringPreferences()
                .getStatusPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.CONCEPT_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptConceptStringPreferences()
                .getConceptPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.CONCEPT_2_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getConceptConceptStringPreferences()
                .getConceptPopupIds());
            extTable.getColumn(REFSET_FIELDS.CONCEPT_CONCEPT_STRING_VALUE).setCellEditor(
                new RefsetMemberTableModel.StringExtFieldEditor());

            break;
        case INTEGER:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getIntegerPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getIntegerPreferences()
                .getStatusPopupIds());
            break;

        case MEASUREMENT:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getMeasurementPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getMeasurementPreferences()
                .getStatusPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.MEASUREMENT_UNITS_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getMeasurementPreferences()
                .getUnitsOfMeasurePopupIds());
            break;
        case LANGUAGE:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguagePreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguagePreferences()
                .getStatusPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.ACCEPTABILITY, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguagePreferences()
                .getAcceptabilityPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.CORRECTNESS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguagePreferences()
                .getCorrectnessPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.DEGREE_OF_SYNONYMY, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguagePreferences()
                .getDegreeOfSynonymyPopupIds());

            break;
        case SCOPED_LANGUAGE:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getStatusPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.ACCEPTABILITY, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getAcceptabilityPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.CORRECTNESS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getCorrectnessPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.DEGREE_OF_SYNONYMY, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getDegreeOfSynonymyPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.TAG, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getTagPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.SCOPE, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getLanguageScopedPreferences()
                .getScopePopupIds());
            break;
        case CROSS_MAP:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getCrossMapPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getCrossMapPreferences()
                .getStatusPopupIds());
            // TODO finish the combo fields
            break;
        case CROSS_MAP_FOR_REL:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getCrossMapForRelPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getCrossMapForRelPreferences()
                .getStatusPopupIds());
            // TODO finish the combo fields
            break;
        case TEMPLATE:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getTemplatePreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getIntegerPreferences()
                .getStatusPopupIds());
            // TODO finish the combo fields
            break;
        case TEMPLATE_FOR_REL:
            setComboForField(host, extTable, REFSET_FIELDS.REFSET_ID, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getTemplateForRelPreferences()
                .getRefsetPopupIds());
            setComboForField(host, extTable, REFSET_FIELDS.STATUS, (IntList) host.getConfig()
                .getRefsetPreferencesForToggle(toggle)
                .getIntegerPreferences()
                .getStatusPopupIds());
            // TODO finish the combo fields
            break;

        default:
            throw new RuntimeException("Can't handle extension type: " + extType);
        }
        relPanel.add(extTable, c);
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = c.gridy + c.gridheight;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridwidth = 2;

        AceLog.getAppLog().info("Added table for: " + extType);
        return relPanel;
    }

    private static void setComboForField(I_HostConceptPlugins host, JTableWithDragImage extTable,
            REFSET_FIELDS comboField, IntList comboIntList) {
        ConceptFieldEditor conceptCombo = new RefsetMemberTableModel.ConceptFieldEditor(host.getConfig(), comboIntList,
            comboField);
        extTable.getColumn(comboField).setCellEditor(conceptCombo);
    }

    static HashSet<org.dwfa.ace.api.I_PluginToConceptPanel> historyState = new HashSet<org.dwfa.ace.api.I_PluginToConceptPanel>();

    public static boolean refSetsChanged(I_HostConceptPlugins host, TOGGLES toggle,
            org.dwfa.ace.api.I_PluginToConceptPanel plugin, Set<EXT_TYPE> visibleExtensions) {
        if (host.getShowRefsets()) {
            if (historyState.contains(plugin) == host.getShowHistory()) {
            } else {
                if (host.getShowHistory()) {
                    historyState.add(plugin);
                } else {
                    historyState.remove(plugin);
                }

                return true;
            }

            Set<EXT_TYPE> newVisibleSet = new HashSet<EXT_TYPE>();
            for (EXT_TYPE extType : EXT_TYPE.values()) {
                if (((AceFrameConfig) host.getConfig()).isRefsetInToggleVisible(extType, toggle)) {
                    newVisibleSet.add(extType);
                }
            }
            if (newVisibleSet.containsAll(visibleExtensions) && visibleExtensions.containsAll(newVisibleSet)) {
                // visible extensions did not change...
            } else {
                return true;
            }
        } else {
            if (visibleExtensions.size() != 0) {
                return true;
            }
        }
        return false;
    }

}
