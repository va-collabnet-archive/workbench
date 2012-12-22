package org.ihtsdo.translation.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class AcceptabilityIconRenderer.
 */
public class AcceptabilityIconRenderer extends DefaultTableCellRenderer {

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
		Object[] values = null;
		try {
			values = (Object[]) value;
		} catch (Exception e) {
		}
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		label.setOpaque(true);

		if (values != null) {
			label.setIcon(IconUtilities.getIconForAcceptability(values[1].toString()));
			label.setToolTipText((((Boolean) values[0]) ? "" : "Inactive ") + values[1].toString());
			if (!(Boolean) values[0]) {
				label.setText("INACT");
				label.setForeground(Color.RED);
				label.setVerticalTextPosition(JLabel.HORIZONTAL);
				label.setHorizontalTextPosition(JLabel.CENTER);
				Font fonts = new Font(Font.SANS_SERIF, Font.BOLD, 11);
				label.setFont(fonts);
			} else {
				label.setText("");
			}
		} else {
			label.setIcon(null);
			label.setToolTipText("");
			label.setText("");
		}
		label.setHorizontalAlignment(CENTER);
		return label;
	}

}