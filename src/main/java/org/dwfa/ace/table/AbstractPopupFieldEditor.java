package org.dwfa.ace.table;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;

public abstract class AbstractPopupFieldEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox combo;
	AceFrameConfig config;
	public AbstractPopupFieldEditor(AceFrameConfig config) {
		super(new JComboBox());
		combo = new JComboBox();
		combo.setMaximumRowCount(20);
		this.config = config;
		populatePopup();
		editorComponent = combo;

		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
					combo.setSelectedItem(getSelectedItem(value));
			}

			public Object getCellEditorValue() {
				return ((ConceptBean) combo.getSelectedItem()).getConceptId();
			}
		};
		combo.addActionListener(delegate);
	}
	
	public abstract ConceptBean getSelectedItem(Object value);
	

	private void populatePopup() {
		combo.removeAllItems();
		for (int id: getPopupValues()) {
			combo.addItem(ConceptBean.get(id));
		}
	}
	
	public abstract int[] getPopupValues();

	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
		populatePopup();
		return super.getTableCellEditorComponent(table, value, isSelected,
				row, column);
	}


}
