package org.dwfa.ace.table;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescTuple;

import com.sleepycat.je.DatabaseException;

public abstract class DescriptionTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum DESC_FIELD {
		DESC_ID("did", 5, 100, 100), CON_ID("cid", 5, 100, 100), TEXT("text",
				5, 300, 2000), LANG("lang", 5, 30, 30), CASE_FIXED("case", 5,
				35, 35), STATUS("status", 5, 50, 50), TYPE("type", 5, 85, 85), VERSION(
				"version", 5, 140, 140), BRANCH("pathId", 5, 90, 150);

		private String columnName;

		private int min;

		private int pref;

		private int max;

		private DESC_FIELD(String columnName, int min, int pref, int max) {
			this.columnName = columnName;
			this.min = min;
			this.pref = pref;
			this.max = max;
		}

		public String getColumnName() {
			return columnName;
		}

		public int getMax() {
			return max;
		}

		public int getMin() {
			return min;
		}

		public int getPref() {
			return pref;
		}
	}

	private DESC_FIELD[] columns;

	private SmallProgressPanel progress = new SmallProgressPanel();

	private AceFrameConfig config;

	public DescriptionTableModel(DESC_FIELD[] columns, AceFrameConfig config) {
		super();
		this.columns = columns;
		this.config = config;
	}

	public int getColumnCount() {
		return columns.length;
	}
	private String getPrefText(int id) throws DatabaseException {
		ConceptBean cb = getReferencedConcepts().get(id);
		ThinDescTuple desc = cb.getDescTuple(config.getTableDescPreferenceList(), config);
		if (desc != null) {
			return desc.getText();
		}
		return "null pref desc: " + cb.getInitialText();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			if (rowIndex >= getRowCount()) {
				return null;
			}
			ThinDescTuple desc = getDescription(rowIndex);
			if (desc == null) {
				return null;
			}

			switch (columns[columnIndex]) {
			case DESC_ID:
				return new StringWithDescTuple(Integer.toString(desc
						.getDescId()), desc);
			case CON_ID:
				return new StringWithDescTuple(Integer.toString(desc
						.getConceptId()), desc);
			case TEXT:
				if (BasicHTML.isHTMLString(desc.getText())) {
					return new StringWithDescTuple(desc.getText(), desc);
				} else {
					return new StringWithDescTuple("<html>" + desc.getText(),
							desc);
				}
			case LANG:
				return new StringWithDescTuple(desc.getLang(), desc);
			case CASE_FIXED:
				return new StringWithDescTuple(Boolean.toString(desc
						.getInitialCaseSignificant()), desc);
			case STATUS:
				if (getReferencedConcepts().containsKey(desc.getStatusId())) {
					return new StringWithDescTuple(getPrefText(desc.getStatusId()), desc);
				}
				return new StringWithDescTuple(Integer.toString(desc
						.getStatusId()), desc);
			case TYPE:
				if (getReferencedConcepts().containsKey(desc.getTypeId())) {
					return new StringWithDescTuple(getPrefText(desc.getTypeId()), desc);
				}
				return new StringWithDescTuple(Integer.toString(desc
						.getTypeId()), desc);
			case VERSION:
				if (desc.getVersion() == Integer.MAX_VALUE) {
					return new StringWithDescTuple(ThinVersionHelper.uncommittedHtml(), desc);
				}
				return new StringWithDescTuple(ThinVersionHelper.format(desc
						.getVersion()), desc);
			case BRANCH:
				if (getReferencedConcepts().containsKey(desc.getPathId())) {
					return new StringWithDescTuple(getPrefText(desc.getPathId()), desc);
				}
				return new StringWithDescTuple(Integer.toString(desc
						.getPathId()), desc);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract ThinDescTuple getDescription(int rowIndex)
			throws DatabaseException;

	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public boolean isCellEditable(int row, int col) {
		try {
			if (getDescription(row).getVersion() == Integer.MAX_VALUE) {
				return true;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		try {
			ThinDescTuple desc = getDescription(row);
			if (desc.getVersion() == Integer.MAX_VALUE) {
				switch (columns[col]) {
				case DESC_ID:
					break;
				case CON_ID:
					break;
				case TEXT:
					desc.setText(value.toString());
					break;
				case LANG:
					desc.setLang(value.toString());
					break;
				case CASE_FIXED:
					desc.setInitialCaseSignificant((Boolean) value);
					break;
				case STATUS:
					Integer statusId = (Integer) value;
					desc.setStatusId(statusId);
					getReferencedConcepts().put(statusId,
							ConceptBean.get(statusId));
					break;
				case TYPE:
					Integer typeId = (Integer) value;
					desc.setTypeId(typeId);
					getReferencedConcepts()
							.put(typeId, ConceptBean.get(typeId));
					break;
				case VERSION:
					break;
				case BRANCH:
					break;
				}
				fireTableCellUpdated(row, col);
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public abstract Map<Integer, ConceptBean> getReferencedConcepts();

	public Class<?> getColumnClass(int c) {
		switch (columns[c]) {
		case DESC_ID:
			return Number.class;
		case CON_ID:
			return Number.class;
		case TEXT:
			return StringWithDescTuple.class;
		case LANG:
			return String.class;
		case CASE_FIXED:
			return Boolean.class;
		case STATUS:
			return Number.class;
		case TYPE:
			return Number.class;
		case VERSION:
			return Number.class;
		case BRANCH:

		}
		return String.class;
	}

	public SmallProgressPanel getProgress() {
		return progress;
	}

	public void setProgress(SmallProgressPanel progress) {
		this.progress = progress;
	}

	public static class StringWithDescTuple implements Comparable, I_CellTextWithTuple {
		String cellText;

		ThinDescTuple tuple;

		public StringWithDescTuple(String cellText, ThinDescTuple tuple) {
			super();
			this.cellText = cellText;
			this.tuple = tuple;
		}

		public String getCellText() {
			return cellText;
		}

		public ThinDescTuple getTuple() {
			return tuple;
		}

		public String toString() {
			return cellText;
		}

		public int compareTo(Object o) {
			StringWithDescTuple another = (StringWithDescTuple) o;
			return cellText.compareTo(another.cellText);
		}
	}

	public static class DescTextFieldEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		public DescTextFieldEditor() {
			super(new JTextField());
			final JTextField textField = new JTextField();
			editorComponent = textField;

			delegate = new EditorDelegate() {
				private static final long serialVersionUID = 1L;

				public void setValue(Object value) {
					if (StringWithDescTuple.class.isAssignableFrom(value
							.getClass())) {
						StringWithDescTuple swdt = (StringWithDescTuple) value;
						textField.setText((value != null) ? swdt.tuple
								.getText() : "");
					} else {
						textField.setText((value != null) ? value.toString()
								: "");
					}
				}

				public Object getCellEditorValue() {
					return textField.getText();
				}
			};
			textField.addActionListener(delegate);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			((JComponent) getComponent())
					.setBorder(new LineBorder(Color.black));
			return super.getTableCellEditorComponent(table, value, isSelected,
					row, column);
		}

	}

	public static class DescTypeFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public DescTypeFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditDescTypePopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithDescTuple swdt = (StringWithDescTuple) value;
			return ConceptBean.get(swdt.getTuple().getTypeId());
		}
	}

	public static class DescStatusFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public DescStatusFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditStatusTypePopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithDescTuple swdt = (StringWithDescTuple) value;
			return ConceptBean.get(swdt.getTuple().getStatusId());
		}
	}

	public DESC_FIELD[] getColumnEnums() {
		return columns;
	}


}
