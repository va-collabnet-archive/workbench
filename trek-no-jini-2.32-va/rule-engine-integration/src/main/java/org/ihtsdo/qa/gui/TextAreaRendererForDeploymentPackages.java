/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 * The Class TextAreaRendererForDeploymentPackages.
 */
public class TextAreaRendererForDeploymentPackages extends TextAreaRenderer {
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.gui.TextAreaRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
			boolean hasFocus, int row, int column) { 
		// set the Font, Color, etc. 
		renderer.getTableCellRendererComponent(table, value, 
				isSelected, hasFocus, row, column);
		String strValue = table.getValueAt(row, 0).toString();
        if (strValue.startsWith("Deployment Package")) {
        	setBackground(Color.LIGHT_GRAY);
        } else {
        	setBackground(renderer.getBackground());
        }
		setForeground(renderer.getForeground()); 
		setBorder(renderer.getBorder()); 
		setFont(renderer.getFont()); 
		setText(renderer.getText()); 

		TableColumnModel columnModel = table.getColumnModel(); 
		setSize(columnModel.getColumn(column).getWidth(), 0); 
		int height_wanted = (int) getPreferredSize().getHeight(); 
		addSize(table, row, column, height_wanted); 
		height_wanted = findTotalMaximumRowSize(table, row); 
		if (height_wanted != table.getRowHeight(row)) { 
			table.setRowHeight(row, height_wanted); 
		} 
		return this; 
	} 
}
