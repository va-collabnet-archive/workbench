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
import java.io.Serializable;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class MultiLineCellRenderer extends JEditorPane implements TableCellRenderer, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static Border noFocusBorder;

    private Color unselectedForeground;

    private Color unselectedBackground;

    public MultiLineCellRenderer() {
        super("text/html", "<html>");
        noFocusBorder = new EmptyBorder(1, 2, 1, 2);
        setOpaque(true);
        setBorder(noFocusBorder);
    }

    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = c;
    }

    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
            super.setBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
        }

        setFont(table.getFont());

        if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            if (table.isCellEditable(row, column)) {
                super.setForeground(UIManager.getColor("Table.focusCellForeground"));
                super.setBackground(UIManager.getColor("Table.focusCellBackground"));
            }
        } else {
            setBorder(noFocusBorder);
        }

        setValue(value);

        return this;
    }

    protected void setValue(Object value) {
        setText((value == null) ? "" : value.toString());
    }

    public static class UIResource extends MultiLineCellRenderer implements javax.swing.plaf.UIResource {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
    }

}
