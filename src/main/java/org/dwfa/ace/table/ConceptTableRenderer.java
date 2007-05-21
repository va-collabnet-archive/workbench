package org.dwfa.ace.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.table.ConceptTableModel.StringWithConceptTuple;


public class ConceptTableRenderer extends AceTableRenderer {


	public ConceptTableRenderer() {
		super();
		setVerticalAlignment(TOP);
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column);
		boolean same = false;
		StringWithConceptTuple swt = (StringWithConceptTuple) value;
		boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
		if (row > 0) {
			StringWithConceptTuple prevSwt = (StringWithConceptTuple) table.getValueAt(
					row - 1, column);
			same = swt.getTuple().getConId() == prevSwt.getTuple().getConId();
			setBorder(column, this, same, uncommitted);
			if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
				renderComponent.setText("");
			}
		} else {
			setBorder(column, this, false, uncommitted);
		}
		if (isSelected == false) {
			renderComponent.setBackground(colorForRow(row));
			renderComponent.setForeground(UIManager
					.getColor("Table.foreground"));
		} else {
			renderComponent.setBackground(UIManager
					.getColor("Table.selectionBackground"));
			renderComponent.setForeground(UIManager
					.getColor("Table.selectionForeground"));
		}
		TableColumn c = table.getColumnModel().getColumn(column);
		if (BasicHTML.isHTMLString(renderComponent.getText())) {
			View v = BasicHTML.createHTMLView(renderComponent, swt.getCellText());
			v.setSize(c.getWidth(), 0);
			float prefYSpan = v.getPreferredSpan(View.Y_AXIS);			
			if (prefYSpan > table.getRowHeight(row)) {
				table.setRowHeight(row, (int) (prefYSpan + 4));
			}
		}
        renderComponent.setToolTipText(swt.getCellText());
		return renderComponent;
	}
}