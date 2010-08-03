package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

public class TextAreaRendererForDeploymentPackages extends TextAreaRenderer {
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
