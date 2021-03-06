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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.table.IdTableModel.StringWithIdTuple;

public class IdTableRenderer extends AceTableRenderer {

    public IdTableRenderer() {
        super();
        setVerticalAlignment(TOP);
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
            column);
        boolean same = false;
        StringWithIdTuple swt = (StringWithIdTuple) value;
        if (swt == null || swt.getTuple() == null) {
            renderComponent.setText("null");
            return renderComponent;
        }
        boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
        if (row > 0) {
            StringWithIdTuple prevSwt = (StringWithIdTuple) table.getValueAt(row - 1, column);
            same = swt.getTuple().getDenotation().equals(prevSwt.getTuple().getDenotation());
            setBorder(column, this, same, uncommitted, false);
            if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
                renderComponent.setText("");
            }
        } else {
            setBorder(column, this, false, uncommitted, false);
        }
        if (isSelected == false) {
            renderComponent.setBackground(colorForRow(row));
            renderComponent.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }
        TableColumn c = table.getColumnModel().getColumn(column);
        if (BasicHTML.isHTMLString(renderComponent.getText())) {
            View v = BasicHTML.createHTMLView(renderComponent, swt.getCellText());
            v.setSize(c.getWidth(), 0);
            float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
            if (prefYSpan > table.getRowHeight(row)) {
                table.setRowHeight(row, (int) (prefYSpan + 4));
            }
        }
        return renderComponent;
    }
}
