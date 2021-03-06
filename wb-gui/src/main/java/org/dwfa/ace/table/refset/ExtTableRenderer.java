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

import java.awt.Component;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.table.AceTableRenderer;

public class ExtTableRenderer extends AceTableRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExtTableRenderer() {
        super();
        setVerticalAlignment(TOP);
        setIgnoreExtensions(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel renderComponent =
                (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        boolean same = false;
        if (value != null) {
            if (StringWithExtTuple.class.isAssignableFrom(value.getClass())) {
                StringWithExtTuple swt = (StringWithExtTuple) value;

                boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
                boolean hasExtensions = false;
                try {
                    hasExtensions = swt.getTuple().hasExtensions();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (row > 0) {
                    if (table.getValueAt(row - 1, column).getClass().isAssignableFrom(StringWithExtTuple.class)) {
                        StringWithExtTuple prevSwt = (StringWithExtTuple) table.getValueAt(row - 1, column);
                        same = swt.getTuple().getMemberId() == prevSwt.getTuple().getMemberId();
                        setBorder(column, this, same, uncommitted, hasExtensions);
                        if ((same) && swt.getCellText() != null && (swt.getCellText().equals(prevSwt.getCellText()))) {
                            renderComponent.setText("");
                        }
                    }
                } else {
                    setBorder(column, this, false, uncommitted, hasExtensions);
                }

                TableColumn c = table.getColumnModel().getColumn(column);
                if (swt.getWrapLines()) {
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
                    renderComponent.setToolTipText(swt.getCellText());
                }
            } else {
                renderComponent.setText(value.toString());
                setBorder(column, this, false, false, false);
            }
        }

        if (isSelected == false) {
            renderComponent.setBackground(colorForRow(row));
            renderComponent.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }
        return renderComponent;
    }
}
