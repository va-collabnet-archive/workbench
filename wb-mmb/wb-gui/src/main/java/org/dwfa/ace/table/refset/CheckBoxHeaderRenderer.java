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
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.refset.RefsetSpecPanel;

public class CheckBoxHeaderRenderer implements TableCellRenderer, MouseListener {

    private final JCheckBox checkBox = new JCheckBox("");
    private boolean mousePressed = false;
    private int column;
    private JLabel label = new JLabel(new CheckBoxIcon(checkBox));

    public CheckBoxHeaderRenderer(ItemListener itemListener, RefsetSpecPanel panel, JTableHeader header) {
        panel.setCheckBoxRendererComponent(checkBox);
        checkBox.setOpaque(false);
        checkBox.setFont(header.getFont());
        checkBox.addItemListener(itemListener);

        /*
        MouseListener[] listeners = header.getMouseListeners();
        for (int i = 0; i < listeners.length; i++) {
            header.removeMouseListener(listeners[i]);
        }
        */
        header.addMouseListener(this);
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int col) {
        TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
        label = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        label.setIcon(new CheckBoxIcon(checkBox));
        label.setHorizontalTextPosition(SwingConstants.TRAILING);
        label.setIconTextGap(0);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.addMouseListener(this);
        }
        setColumn(col);
        label.setText("");

        return label;
    }

    protected void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public int getPreferredWidth() {
        if (label == null) {
            return 0;
        } else {
            return label.getPreferredSize().width + 8;
        }
    }

    public int getPreferredHeight() {
        if (label == null) {
            return 0;
        } else {
            return label.getPreferredSize().height;
        }
    }

    protected void handleClickEvent(MouseEvent e) {
        if (mousePressed) {
            mousePressed = false;
            JTableHeader header = (JTableHeader) (e.getSource());
            JTable tableView = header.getTable();
            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);

            if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
                checkBox.doClick();
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        handleClickEvent(e);
        ((JTableHeader) e.getSource()).repaint();
    }

    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private static class CheckBoxIcon implements Icon {
        private final JCheckBox check;

        public CheckBoxIcon(JCheckBox check) {
            this.check = check;
        }

        public int getIconWidth() {
            return check.getPreferredSize().width;
        }

        public int getIconHeight() {
            return check.getPreferredSize().height;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            SwingUtilities.paintComponent(g, check, (Container) c, x, y, getIconWidth(), getIconHeight());
        }
    }

    public void addItemListener(ItemListener selectAllCheckBoxListener) {
        checkBox.addItemListener(selectAllCheckBoxListener);
    }

    public void removeItemListener(ItemListener selectAllCheckBoxListener) {
        checkBox.removeItemListener(selectAllCheckBoxListener);
    }
}
