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
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.AceTableRenderer;

public class CheckBoxCellRenderer implements TableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JCheckBox checkBox;

    public CheckBoxCellRenderer() {
        checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public CheckBoxCellRenderer(ItemListener itemListener) {
        super();
        checkBox.addItemListener(itemListener);
    }

    public int getPreferredWidth() {
        return checkBox.getPreferredSize().width;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (Boolean.class.isAssignableFrom(value.getClass())) {
            checkBox.setSelected((Boolean) value);
            setBackground(isSelected, row, checkBox);
            return checkBox;
        }
        AceLog.getAppLog().warning("Wrong renderer for row: " + row + " column: " + column);
        return new JLabel(value.toString());
    }

    protected void setBackground(boolean isSelected, int row, JCheckBox renderComponent) {
        if (!isSelected) {
            renderComponent.setBackground(AceTableRenderer.colorForRow(row));
            renderComponent.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }
    }
}
