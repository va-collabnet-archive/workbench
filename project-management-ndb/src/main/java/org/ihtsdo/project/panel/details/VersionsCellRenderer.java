package org.ihtsdo.project.panel.details;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class VersionsCellRenderer extends DefaultTableCellRenderer implements
		TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -403734528390550896L;

	private final boolean foreground;

	private final Font font;
	private final Color colorForNew;
	private final Color colorForUpdated;
	private final Color colorForDeleted;
	private final Color rowLabelColor;
	private final Color defaultColor;
	private final Color separatorColor;

	// -------------------------- PUBLIC INSTANCE METHODS
	/**
	* constructor
	*
	* @param font for to render the column
	* @param foreground foreground colour
	* @param horizontalAlignment e.g. JLabel.CENTER
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