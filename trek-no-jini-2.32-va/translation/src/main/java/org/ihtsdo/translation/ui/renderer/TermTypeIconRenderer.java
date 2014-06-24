package org.ihtsdo.translation.ui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class TermTypeIconRenderer.
 */
public class TermTypeIconRenderer extends DefaultTableCellRenderer {


	/**
	 * @param languageTermPanel
	 */
	public TermTypeIconRenderer() {
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

		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Object[] termType_status = (Object[]) value;
		String termType = termType_status[0].toString();
		String status = termType_status[1].toString();
		Boolean isCoreDesc = (Boolean) termType_status[2]; 
		label.setIcon(IconUtilities.getIconForTermType_Status(termType, status,isCoreDesc));
		label.setText("");
		label.setToolTipText(status + " " + termType);
		label.setHorizontalAlignment(CENTER);

		return label;
	}

}