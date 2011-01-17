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
package org.ihtsdo.ace.table;

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
import org.dwfa.ace.table.AceTableRenderer;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WorkflowTextFieldEditor;

public class WorkflowHistoryTableRenderer extends AceTableRenderer {

    private I_ConfigAceFrame frameConfig;
    boolean renderInactive = false;

    public WorkflowHistoryTableRenderer(I_ConfigAceFrame frameConfig) {
        super();
        setVerticalAlignment(TOP);
        this.frameConfig = frameConfig;
    }

    public WorkflowHistoryTableRenderer(I_ConfigAceFrame frameConfig, boolean renderInactive) {
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
        JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
            column);
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

        if (value == null)
        {
            renderComponent.setText("null");
            return renderComponent;
        }
        
        if (WorkflowTextFieldEditor.class.isAssignableFrom(value.getClass())) 
        {
        	WorkflowTextFieldEditor textField = (WorkflowTextFieldEditor) value;
 
        	if (textField == null) 
                renderComponent.setText("null");
            else 
            {
            	renderComponent.setText(textField.getCellText());
            	if (renderInactive == false) 
            		setBorder(column, this, true, false, false);
            }
        }

        return renderComponent;
    }

}
