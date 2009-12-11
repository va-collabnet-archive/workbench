/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.EventObject;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

public abstract class DescriptionTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum DESC_FIELD { SCORE("score", 5, 100, 100),
		DESC_ID("did", 5, 100, 100), CON_ID("cid", 5, 100, 100), TEXT("text",
				5, 300, 2000), LANG("lang", 5, 35, 55), CASE_FIXED("case", 5,
				35, 55), STATUS("status", 5, 50, 250), TYPE("type", 5, 85, 450), VERSION(
				"version", 5, 140, 140), PATH("path", 5, 90, 150);

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

	private I_ConfigAceFrame config;

	public DescriptionTableModel(DESC_FIELD[] columns, I_ConfigAceFrame config) {
		super();
		this.columns = columns;
		this.config = config;
	}
	public final void setColumns(DESC_FIELD[] columns) {
		if (this.columns.length != columns.length) {
			this.columns = columns;
			fireTableStructureChanged();
			return;
		}
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(this.columns[i]) == false) {
				this.columns = columns;
				fireTableStructureChanged();
				return;
			}
		}
	}

	public int getColumnCount() {
		return columns.length;
	}
	private String getPrefText(int id) throws IOException {
		ConceptBean cb = getReferencedConcepts().get(id);
		I_DescriptionTuple desc = cb.getDescTuple(config.getTableDescPreferenceList(), config);
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
			I_DescriptionTuple desc = getDescription(rowIndex);
			if (desc == null) {
				return null;
			}

			switch (columns[columnIndex]) {
			case SCORE: 
				return getScore(rowIndex);
			case DESC_ID:
				return new StringWithDescTuple(Integer.toString(desc
						.getDescId()), desc, false);
			case CON_ID:
				return new StringWithDescTuple(Integer.toString(desc
						.getConceptId()), desc, false);
			case TEXT:
				if (BasicHTML.isHTMLString(desc.getText())) {
					return new StringWithDescTuple(desc.getText(), desc, true);
				} else {
					return new StringWithDescTuple(desc.getText(),
							desc, true);
				}
			case LANG:
				return new StringWithDescTuple(desc.getLang(), desc, false);
			case CASE_FIXED:
				return new StringWithDescTuple(Boolean.toString(desc
						.getInitialCaseSignificant()), desc, false);
			case STATUS:
				if (getReferencedConcepts().containsKey(desc.getStatusId())) {
					return new StringWithDescTuple(getPrefText(desc.getStatusId()), desc, false);
				}
				return new StringWithDescTuple(Integer.toString(desc
						.getStatusId()), desc, false);
			case TYPE:
				if (getReferencedConcepts().containsKey(desc.getTypeId())) {
					return new StringWithDescTuple(getPrefText(desc.getTypeId()), desc, false);
				}
				return new StringWithDescTuple(Integer.toString(desc
						.getTypeId()), desc, false);
			case VERSION:
				if (desc.getVersion() == Integer.MAX_VALUE) {
					return new StringWithDescTuple(ThinVersionHelper.uncommittedHtml(), desc, false);
				}
				return new StringWithDescTuple(ThinVersionHelper.format(desc
						.getVersion()), desc, false);
			case PATH:
				if (getReferencedConcepts().containsKey(desc.getPathId())) {
					return new StringWithDescTuple(getPrefText(desc.getPathId()), desc, false);
				}
				return new StringWithDescTuple(Integer.toString(desc
						.getPathId()), desc, false);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	protected abstract I_DescriptionTuple getDescription(int rowIndex)
			throws IOException;

	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public boolean isCellEditable(int row, int col) {
      if (ACE.editMode == false) {
         return false;
      }
		try {
			if (getDescription(row).getVersion() == Integer.MAX_VALUE) {
				if (AceLog.getAppLog().isLoggable(Level.FINER)) {
					AceLog.getAppLog().finer("Cell is editable: " + row + " " + col);
				}
				return true;
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		try {
			I_DescriptionTuple desc = getDescription(row);
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
				case PATH:
					break;
				}
				fireTableDataChanged();
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		case PATH:

		}
		return String.class;
	}

	public SmallProgressPanel getProgress() {
		return progress;
	}

	public void setProgress(SmallProgressPanel progress) {
		this.progress = progress;
	}

	public static class StringWithDescTuple implements Comparable<StringWithDescTuple>, I_CellTextWithTuple {
		String cellText;

		I_DescriptionTuple tuple;

      boolean wrapLines;
		public StringWithDescTuple(String cellText, I_DescriptionTuple tuple, boolean wrapLines) {
			super();
			this.cellText = cellText;
			this.tuple = tuple;
         this.wrapLines = wrapLines;
		}

		public String getCellText() {
			return cellText;
		}

		public I_DescriptionTuple getTuple() {
			return tuple;
		}

		public String toString() {
			return cellText;
		}

		public int compareTo(StringWithDescTuple another) {
			return cellText.compareTo(another.cellText);
		}

      public boolean getWrapLines() {
         return wrapLines;
      }

      public void setWrapLines(boolean wrapLines) {
         this.wrapLines = wrapLines;
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

		@Override
		public boolean isCellEditable(EventObject evt) {
			if (evt instanceof MouseEvent) {
				int clickCount;
				// For double-click activation
				clickCount = 2;
				return ((MouseEvent) evt).getClickCount() >= clickCount;
			}
			return true;
		}
	}

	public static class DescTypeFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public DescTypeFieldEditor(I_ConfigAceFrame config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditDescTypePopup().getListArray();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithDescTuple swdt = (StringWithDescTuple) value;
			return ConceptBean.get(swdt.getTuple().getTypeId());
		}
		@Override
		public boolean isCellEditable(EventObject evt) {
			if (evt instanceof MouseEvent) {
				int clickCount;
				// For double-click activation
				clickCount = 2;
				return ((MouseEvent) evt).getClickCount() >= clickCount;
			}
			return true;
		}
	}

	public static class DescStatusFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public DescStatusFieldEditor(I_ConfigAceFrame config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditStatusTypePopup().getListArray();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithDescTuple swdt = (StringWithDescTuple) value;
			return ConceptBean.get(swdt.getTuple().getStatusId());
		}
		@Override
		public boolean isCellEditable(EventObject evt) {
			if (evt instanceof MouseEvent) {
				int clickCount;
				// For double-click activation
				clickCount = 2;
				return ((MouseEvent) evt).getClickCount() >= clickCount;
			}
			return true;
		}
	}

	public DESC_FIELD[] getColumnEnums() {
		return columns;
	}
	
	public abstract String getScore(int rowIndex);

}
