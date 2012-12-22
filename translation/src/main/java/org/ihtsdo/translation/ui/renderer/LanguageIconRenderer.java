package org.ihtsdo.translation.ui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class LanguageIconRenderer.
 */
public class LanguageIconRenderer extends DefaultTableCellRenderer {

	/**
	 * @param languageTermPanel
	 */
	public LanguageIconRenderer() {
	}

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
		try {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String[] valStr = (String[]) value;
			if (value != null) {
				label.setIcon(IconUtilities.getIconForLanguage(valStr[0]));
			} else {
				label.setIcon(null);
			}
			label.setText("");
			label.setToolTipText(valStr[1]);
			label.setHorizontalAlignment(CENTER);
			return label;
		} catch (Exception e) {
			return null;
		}
	}

}