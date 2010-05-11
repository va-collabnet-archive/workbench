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
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.table.ImageTableModel.ImageWithImageTuple;
import org.dwfa.ace.table.ImageTableModel.StringWithImageTuple;

public class ImageTableRenderer extends AceTableRenderer {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ImageTableRenderer() {
        super();
        setVerticalAlignment(TOP);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
            column);
        boolean same = false;
        if (value == null) {
            renderComponent.setText("null");
            return renderComponent;
        }
        if (StringWithImageTuple.class.isAssignableFrom(value.getClass())) {
            setIcon(null);
            setupStringWithTuple(table, value, isSelected, row, column, renderComponent, same);
        } else if (ImageWithImageTuple.class.isAssignableFrom(value.getClass())) {
            ImageWithImageTuple icon = (ImageWithImageTuple) value;
            boolean uncommitted = icon.getTuple().getVersion() == Integer.MAX_VALUE;
            renderComponent.setIcon(icon.getImage());
            renderComponent.setText("");
            if (row > 0) {
                ImageWithImageTuple prevImage = (ImageWithImageTuple) table.getValueAt(row - 1, column);
                same = icon.getTuple().getImageId() == prevImage.getTuple().getImageId();
                if (same) {
                    renderComponent.setIcon(null);
                }
            }
            if ((!same) && (icon.getImage().getIconHeight() > table.getRowHeight(row))) {
                table.setRowHeight(row, icon.getImage().getIconHeight() + 4);
            }
            setBorder(column, renderComponent, same, uncommitted);
            setBackground(isSelected, row, renderComponent);
        }

        return renderComponent;
    }

    private void setupStringWithTuple(JTable table, Object value, boolean isSelected, int row, int column,
            JLabel renderComponent, boolean same) {
        StringWithImageTuple swt = (StringWithImageTuple) value;
        boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
        if (row > 0) {
            StringWithImageTuple prevSwt = (StringWithImageTuple) table.getValueAt(row - 1, column);
            same = swt.getTuple().getImageId() == prevSwt.getTuple().getImageId();
            if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
                renderComponent.setText("");
            }
        }

        setBorder(column, renderComponent, same, uncommitted);

        setBackground(isSelected, row, renderComponent);
        TableColumn c = table.getColumnModel().getColumn(column);
        if (BasicHTML.isHTMLString(renderComponent.getText())) {
            View v = BasicHTML.createHTMLView(renderComponent, swt.getCellText());
            v.setSize(c.getWidth(), 0);
            float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
            if (prefYSpan > table.getRowHeight(row)) {
                table.setRowHeight(row, (int) (prefYSpan + 4));
            }
        }
    }

}
