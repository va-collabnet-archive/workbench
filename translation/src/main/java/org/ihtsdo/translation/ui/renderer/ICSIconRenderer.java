package org.ihtsdo.translation.ui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class ICSIconRenderer.
 */
public class ICSIconRenderer extends DefaultTableCellRenderer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
	 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value != null) {
			label.setIcon(IconUtilities.getIconForICS((Boolean) value));
		} else {
			label.setIcon(null);
		}
		label.setText("");
		if (value != null) {
			label.setToolTipText(value.toString());
		}
		label.setHorizontalAlignment(CENTER);

		return label;
	}

}