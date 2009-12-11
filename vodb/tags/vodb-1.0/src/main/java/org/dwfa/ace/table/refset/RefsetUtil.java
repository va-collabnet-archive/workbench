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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.I_PluginToConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;

public class RefsetUtil {

   public static void addRefsetTables(I_HostConceptPlugins host, I_PluginToConceptPanel plugin, TOGGLES toggle, 
         GridBagConstraints c, Set<EXT_TYPE> visibleExtensions,
         JPanel panel) {
      plugin.clearRefsetListeners();
      if (host.getShowRefsets()) {
         for (EXT_TYPE extType: EXT_TYPE.values()) {
            if (((AceFrameConfig) host.getConfig()).isRefsetInToggleVisible(extType, toggle)) {
               c.gridy++;
               RefsetMemberTableModel refsetModel;
               switch (extType) {
               case BOOLEAN:
                  refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host, extType),
                        extType, toggle);
                  break;
               case CONCEPT:
                  refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host, extType),
                        extType, toggle);
                  break;
               case INTEGER:
                  refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host, extType),
                        extType, toggle);
                  break;
               case LANGUAGE:
                  refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host, extType),
                        extType, toggle);
                  break;
               case SCOPED_LANGUAGE:
                  refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host, extType),
                        extType, toggle);
                  break;
               case MEASUREMENT:
                  refsetModel = new RefsetMemberTableModel(host, RefsetMemberTableModel.getRefsetColumns(host, extType),
                        extType, toggle);
                  break;

               default:
                  refsetModel = null;
                  AceLog.getAppLog().alertAndLogException(new Exception("Can't handle extType: " + extType));
                  break;
               }
               if (refsetModel != null) {
                  plugin.addRefsetListener(refsetModel);
                  c.gridy++;
                  panel.add(getExtensionPanel(extType, refsetModel), c);
                  visibleExtensions.add(extType);
               } else {
                  AceLog.getAppLog().info("refset model is null");
               }
            }
         }
      }
   }

   

   private static JPanel getExtensionPanel(EXT_TYPE extType, RefsetMemberTableModel refsetModel) {
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
         JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
               .getResource("/24x24/plain/paperclip_add.png")));
         relPanel.add(rowAddAfter, c);
         rowAddAfter.setEnabled(false);
         refsetModel.setAddButton(rowAddAfter);
         //rowAddAfter.addActionListener(new AddRelationship(host, host.getConfig()));
         //rowAddAfter.setTransferHandler(new TerminologyTransferHandler());
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

      TableSorter relSortingTable = new TableSorter(refsetModel);
      JTableWithDragImage extTable = new JTableWithDragImage(relSortingTable);
      relSortingTable.setTableHeader(extTable.getTableHeader());
      relSortingTable
            .getTableHeader()
            .setToolTipText(
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

      //setupEditors(host);
      if (ACE.editMode) {
         //extTable.addMouseListener(model
         //      .makePopupListener(extTable, host.getConfig()));
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
      extTable.setDefaultRenderer(StringWithRelTuple.class,
            new RelationshipTableRenderer());
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

   static HashSet<I_PluginToConceptPanel> historyState = new HashSet<I_PluginToConceptPanel>();
   
   public static boolean refSetsChanged(I_HostConceptPlugins host, TOGGLES toggle, I_PluginToConceptPanel plugin, Set<EXT_TYPE> visibleExtensions) {
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
         for (EXT_TYPE extType: EXT_TYPE.values()) {
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
