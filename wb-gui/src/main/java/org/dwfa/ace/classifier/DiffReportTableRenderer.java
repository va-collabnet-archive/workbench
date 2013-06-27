/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dwfa.ace.classifier;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.table.AceTableRenderer;

public class DiffReportTableRenderer extends AceTableRenderer {

    private static final long serialVersionUID = 1L;
    boolean renderInactive = false;
    @SuppressWarnings("unused")
    private I_ConfigAceFrame frameConfig;

    public DiffReportTableRenderer(I_ConfigAceFrame frameConfig) {
        super();
        setVerticalAlignment(TOP);
        this.frameConfig = frameConfig;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);

        if (isSelected == false) {
            if (renderInactive) {
                renderComponent.setBackground(UIManager.getColor("Table.background"));
                renderComponent.setForeground(UIManager.getColor("Table.foreground"));
            } else {
                renderComponent.setBackground(colorForRow(row));
                renderComponent.setForeground(UIManager.getColor("Table.foreground"));
            }
            SnoRelReport sr = (SnoRelReport) value;
            if (column == 0) {
                if (sr.isAdded) {
                    renderComponent.setText("<html><font face='Dialog' size='3' color='black'>+");
                } else {
                    renderComponent.setText("<html><font face='Dialog' size='3' color='black'>-");
                }
            } else if (column == 1) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='black'>"
                        + sr.snoRel.toStringC1());
            } else if (column == 2) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='black'>"
                        + sr.snoRel.toStringType());
            } else if (column == 3) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='black'>"
                        + sr.snoRel.toStringC2());
            } else if (column == 4) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='black'>"
                        + sr.snoRel.toStringGroup());
            }
        } else {
            renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
            SnoRelReport sr = (SnoRelReport) value;
            if (column == 0) {
                if (sr.isAdded) {
                    renderComponent.setText("<html><font face='Dialog' size='3' color='white'>+");
                } else {
                    renderComponent.setText("<html><font face='Dialog' size='3' color='white'>-");
                }
            } else if (column == 1) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='white'>"
                        + sr.snoRel.toStringC1());
            } else if (column == 2) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='white'>"
                        + sr.snoRel.toStringType());
            } else if (column == 3) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='white'>"
                        + sr.snoRel.toStringC2());
            } else if (column == 4) {
                renderComponent.setText("<html><font face='Dialog' size='3' color='white'>"
                        + sr.snoRel.toStringGroup());
            }
        }

        setHorizontalAlignment(SwingConstants.LEFT);

        setBorder(column, this, false, false, false); // .., same, uncommitted
        // if (renderInactive) renderComponent.setBackground(Color.LIGHT_GRAY);

        return renderComponent;
    }
}
