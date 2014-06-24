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
package org.ihtsdo.project.view.details;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * The Class VersionsCellRenderer.
 */
public class VersionsCellRenderer extends DefaultTableCellRenderer implements
		TableCellRenderer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -403734528390550896L;

	/** The foreground. */
	private final boolean foreground;

	/** The font. */
	private final Font font;
	
	/** The color for new. */
	private final Color colorForNew;
	
	/** The color for updated. */
	private final Color colorForUpdated;
	
	/** The color for deleted. */
	private final Color colorForDeleted;
	
	/** The row label color. */
	private final Color rowLabelColor;
	
	/** The default color. */
	private final Color defaultColor;
	
	/** The separator color. */
	private final Color separatorColor;

	// -------------------------- PUBLIC INSTANCE METHODS
	/**
	 * constructor.
	 *
	 * @param font for to render the column
	 * @param colorForNew the color for new
	 * @param colorForUpdated the color for updated
	 * @param colorForDeleted the color for deleted
	 * @param rowLabelColor the row label color
	 * @param separatorColor the separator color
	 * @param defaultColor the default color
	 * @param foreground foreground colour
	 */
	public VersionsCellRenderer( Font font,Color colorForNew,
			Color colorForUpdated,Color colorForDeleted,
			Color rowLabelColor, Color separatorColor,Color defaultColor,
	boolean foreground )
	{
	this.foreground = foreground;
	this.font = font;
	this.colorForNew = colorForNew;
	this.colorForUpdated = colorForUpdated;
	this.colorForDeleted = colorForDeleted;
	this.defaultColor=defaultColor;
	this.rowLabelColor=rowLabelColor;
	this.separatorColor=separatorColor;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent( JTable table,
	Object value,
	boolean
	isSelected,
	boolean hasFocus,
	int row,
	int column )
	{
	JLabel template = (JLabel)
	super.getTableCellRendererComponent( table, value,
	isSelected, hasFocus, row, column );
//	ImageIcon icon=new ImageIcon("/icons/89.png");
	DefaultTableModel tModel= (DefaultTableModel)table.getModel();
	String oldvalueant=((CellObject)tModel.getValueAt(row, 0)).getAtrName();
	if (oldvalueant.equals("")){
		template.setBackground(separatorColor);
		return template;
	}
	if (column>1){
		String oldvalue="";
		oldvalueant="";
		for (int j=1;j<column;j++){
			oldvalueant=((CellObject)tModel.getValueAt(row, j)).getAtrName();
			if (!oldvalueant.equals(""))
				oldvalue=oldvalueant;
		}
		String newValue=((CellObject)value).getAtrName();
		if (!newValue.equals(oldvalue)){
			if (oldvalue.equals("")){
				if (foreground){
					template.setForeground(colorForNew);
				}else{
					template.setBackground(colorForNew);
				}
			}else{
				if(!newValue.equals("")){
					if (foreground){
						template.setForeground(colorForUpdated);
					}else{
						template.setBackground(colorForUpdated);						
					}
				}else{
					if (foreground){
						template.setForeground(colorForDeleted);
					}else{
						template.setBackground(colorForDeleted);						
					}
				}
			}
			template.setFont(font);
		}
		else{
			template.setBackground(defaultColor);
		}
	}
	else{
		if(column==0){
//			template.setIcon(icon);
			if (foreground){
				template.setForeground(rowLabelColor);
			}else{
				template.setBackground(rowLabelColor);						
			}
		}
		else{
			if (foreground){
				template.setForeground(defaultColor);
			}else{
				template.setBackground(defaultColor);						
			}
		}
	}
//	// we don't handle setting selected background here.
//	// We don't get called when selection changes.
//	// leave it up to JTable to set the background to selected or
	if ( value != null )
	{
	template.setText( value.toString() );
	}
	else
	{
	template.setText( null );
	}
	return template;
	}
}