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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public abstract class AceTableRenderer extends DefaultTableCellRenderer {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static Color IVORY = new Color(0xFFFFF0);
    public static Color KAHAKI = new Color(0xF0E68C);
    public static Color LEMON_CHIFFON = new Color(0xFFFACD);

    public static Color INACTIVE = Color.LIGHT_GRAY;
    public static Color STRIPE = LEMON_CHIFFON;
    public static Color UNCOMMITTED_COLOR = new Color(128, 224, 72);

    protected void setBorder(int column, JLabel renderComponent, boolean same, boolean uncommitted) {
        if (!same) {
            if (uncommitted && column == 0) {
                renderComponent.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0,
                    0, Color.LIGHT_GRAY), BorderFactory.createMatteBorder(0, 3, 0, 0, UNCOMMITTED_COLOR)));

            } else {
                renderComponent.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
            }
        } else {
            if (uncommitted && column == 0) {
                renderComponent.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0,
                    0, Color.WHITE), BorderFactory.createMatteBorder(0, 3, 0, 0, UNCOMMITTED_COLOR)));
            } else {
                renderComponent.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE));
            }
        }
    }

    /**
     * Returns the appropriate background color for the given row.
     */
    public static Color colorForRow(int row) {
        return (row % 2 == 0) ? DescriptionTableRenderer.STRIPE : Color.white;
    }

    protected void setBackground(boolean isSelected, int row, JLabel renderComponent) {
        if (isSelected == false) {
            renderComponent.setBackground(colorForRow(row));
            renderComponent.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }
    }

}
