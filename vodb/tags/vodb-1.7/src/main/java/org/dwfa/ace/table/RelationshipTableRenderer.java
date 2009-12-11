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

import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;

public class RelationshipTableRenderer extends AceTableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel renderComponent =  (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		boolean same = false;
		StringWithRelTuple swt = (StringWithRelTuple) value;
		boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;

		if (row > 0) {
			StringWithRelTuple prevSwt = (StringWithRelTuple) table.getValueAt(row - 1, column);
			same = swt.getTuple().getRelId() == prevSwt.getTuple().getRelId();
			setBorder(column, this, same, uncommitted);
			if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
				renderComponent.setText("");
			}
		} else {
			setBorder(column, this, false, uncommitted);
		}
		
        if (isSelected == false) {
        	renderComponent.setBackground(colorForRow(row));
        	renderComponent.setForeground(UIManager.getColor("Table.foreground"));
        } else {
        	renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
        	renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }		
        renderComponent.setToolTipText(swt.getCellText());
		return renderComponent;
	}


}
