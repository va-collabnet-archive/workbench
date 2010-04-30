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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.bpa.util.SortClickListener;

public class ReflexiveRefsetUtil {

    public static JPanel getExtensionPanel(String labelTxt, ReflexiveRefsetMemberTableModel refsetModel,
            I_HostConceptPlugins host, boolean showRowAdd, boolean spaceForAdd) throws Exception {
        JPanel relPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        if (labelTxt != null) {
            JLabel srcRelLabel = new JLabel("     " + labelTxt);
            if (spaceForAdd == false) {
                srcRelLabel = new JLabel(labelTxt);
            }
            srcRelLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 0));
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            relPanel.add(srcRelLabel, c);
        }

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
        if (ACE.editMode && showRowAdd) {
            JButton rowAddAfter = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/paperclip_add.png")));
            relPanel.add(rowAddAfter, c);
            rowAddAfter.setEnabled(false);
            // refsetModel.setAddButton(rowAddAfter);
        } else if (spaceForAdd) {
            JPanel filler = new JPanel();
            filler.setMaximumSize(new Dimension(40, 32));
            filler.setMinimumSize(new Dimension(40, 32));
            filler.setPreferredSize(new Dimension(40, 32));
            relPanel.add(filler, c);
        } else {
            JPanel filler = new JPanel();
            filler.setMaximumSize(new Dimension(0, 0));
            filler.setMinimumSize(new Dimension(0, 0));
            filler.setPreferredSize(new Dimension(0, 0));
            relPanel.add(filler, c);
        }
        c.gridheight = 1;
        c.gridx++;
        c.gridwidth = 1;

        JTableWithDragImage extTable = new JTableWithDragImage(refsetModel);
        SortClickListener.setupSorter(extTable);
        extTable.getTableHeader().setToolTipText(
            "Click to specify sorting");
        ReflexiveRefsetFieldData[] columns = refsetModel.getColumns();
        for (int i = 0; i < extTable.getColumnCount(); i++) {
            TableColumn column = extTable.getColumnModel().getColumn(i);
            ReflexiveRefsetFieldData columnDesc = columns[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        extTable.setDragEnabled(true);
        extTable.setTransferHandler(new TerminologyTransferHandler(extTable));
        extTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relPanel.putClientProperty("extTable", extTable);
        if (ACE.editMode) {
            // extTable.addMouseListener(refsetModel.makePopupListener(extTable,
            // host.getConfig()));
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

        relPanel.add(extTable, c);
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = c.gridy + c.gridheight;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridwidth = 2;

        return relPanel;
    }

}
