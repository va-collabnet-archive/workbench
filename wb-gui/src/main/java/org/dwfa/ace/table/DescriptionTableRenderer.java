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
package org.dwfa.ace.table;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;

public class DescriptionTableRenderer extends AceTableRenderer {

    boolean renderInactive = false;
    private I_ConfigAceFrame frameConfig;

    public DescriptionTableRenderer(I_ConfigAceFrame frameConfig) {
        super();
        setVerticalAlignment(TOP);
        this.frameConfig = frameConfig;
    }

    public DescriptionTableRenderer(I_ConfigAceFrame frameConfig, boolean renderInactive) {
        this(frameConfig);
        this.renderInactive = renderInactive;
    }

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel renderComponent =
                (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        boolean same = false;
        if (isSelected == false) {
            if (renderInactive) {
                renderComponent.setBackground(UIManager.getColor("Table.background"));
                renderComponent.setForeground(UIManager.getColor("Table.foreground"));
            } else {
                renderComponent.setBackground(colorForRow(row));
                renderComponent.setForeground(UIManager.getColor("Table.foreground"));
            }
        } else {
            renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }

        if (value != null && StringWithDescTuple.class.isAssignableFrom(value.getClass())) {
            StringWithDescTuple swt = (StringWithDescTuple) value;
            if (swt == null || swt.getTuple() == null) {
                renderComponent.setText("null");
                return renderComponent;
            }
            if (swt.getTuple() != null) {
                boolean hasExtensions = false;
                try {
                    hasExtensions = swt.tuple.hasExtensions();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
                if (renderInactive) {
                    boolean active = frameConfig.getAllowedStatus().contains(swt.getTuple().getStatusId());
                    if (active == false) {
                        renderComponent.setBackground(Color.LIGHT_GRAY);
                    }
                }
                if (row > 0) {
                    StringWithDescTuple prevSwt = (StringWithDescTuple) table.getValueAt(row - 1, column);
                    same = swt.getTuple().getDescId() == prevSwt.getTuple().getDescId();
                    if (renderInactive == false) {
                        setBorder(column, this, same, uncommitted, hasExtensions);
                    }
                    if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
                        renderComponent.setText("");
                    }
                } else {
                    if (renderInactive == false) {
                        setBorder(column, this, false, uncommitted, hasExtensions);
                    }
                }
            }
            TableColumn c = table.getColumnModel().getColumn(column);
            if (swt.wrapLines) {
                if (BasicHTML.isHTMLString(renderComponent.getText())) {
                    View v = BasicHTML.createHTMLView(renderComponent, swt.getCellText());
                    v.setSize(c.getWidth(), 0);
                    float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
                    if (prefYSpan > table.getRowHeight(row)) {
                        table.setRowHeight(row, (int) (prefYSpan + 4));
                    }
                } else {
                    View v = BasicHTML.createHTMLView(renderComponent, "<html>" + swt.getCellText());
                    v.setSize(c.getWidth(), 0);
                    float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
                    if (prefYSpan > table.getRowHeight(row)) {
                        table.setRowHeight(row, (int) (prefYSpan + 4));
                        if (isSelected) {
                            renderComponent.setText("<html>Y" + swt.getCellText());
                        }
                    }
                    if (table.getRowHeight(row) > 16) {
                        renderComponent.setText("<html>" + swt.getCellText());
                    }
                }
            }
            // renderComponent.setToolTipText(swt.getCellText());
        }

        return renderComponent;
    }

}
