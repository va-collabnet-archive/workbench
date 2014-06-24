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
package org.ihtsdo.project.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfInstanceContainer;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;

public class WorkflowInstanceTableModel extends DefaultTableModel implements WfInstanceContainer {

	private static final long serialVersionUID = 1L;

	private Integer searchResults = null;

	// First is most recent and Last is the oldest
	private LinkedList<Object[]> data = new LinkedList<Object[]>();

	public enum WORKFLOW_FIELD {
		FSN("FSN", 0, 5, 800, 650), EDITOR("WorkflowUser", 1, 5, 250, 250), STATE("State", 2, 5, 250, 250), TIMESTAMP("TimeStamp", 3, 5, 225, 225);

		/*
		 * FSN("FSN", 5, 400, 600), STATE("State", 5, 115, 150),
		 * ACTION("Action", 5, 115, 150), EDITOR("Editor", 5, 150, 180),
		 * PATH("Path", 5, 115, 150), TIMESTAMP("TimeStamp", 5, 75, 200);
		 */

		private int min;
		private int pref;
		private int max;
		private String columnName;
		private int columnNumber;

		private WORKFLOW_FIELD(String columnName, int columnNumber, int min, int pref, int max) {
			this.columnName = columnName;
			this.columnNumber = columnNumber;
			this.min = min;
			this.pref = pref;
			this.max = max;
		}

		public String getColumnName() {
			return columnName;
		}

		public int getColumnNumber() {
			return columnNumber;
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

	private WORKFLOW_FIELD[] columns;

	private I_ConfigAceFrame config;
	private DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

	public WorkflowInstanceTableModel(WORKFLOW_FIELD[] columns, I_ConfigAceFrame config) {
		super();
		this.columns = columns;
		this.config = config;
	}

	public final void setColumns(WORKFLOW_FIELD[] columns) {
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

	public Object getValueAt(int row, int col) {
		if (data != null && !data.isEmpty()) {

			switch (columns[col]) {
			case FSN:
				return data.get(row)[WORKFLOW_FIELD.FSN.getColumnNumber()];
			case STATE:
				return data.get(row)[WORKFLOW_FIELD.STATE.getColumnNumber()];
			case EDITOR:
				return data.get(row)[WORKFLOW_FIELD.EDITOR.getColumnNumber()];
			case TIMESTAMP:
				return data.get(row)[WORKFLOW_FIELD.TIMESTAMP.getColumnNumber()];
			}
			fireTableDataChanged();
		}
		return "";
	}

	public int getColumnCount() {
		return columns.length;
	}

	public WorkflowInstanceTableModel(String[][] data, String[] columns) {
		super(data, columns);
		this.data = new LinkedList<Object[]>();
	}
        
        public LinkedList<Object[]> getData(){
            return data;
        }

	public Object[] getBean(int rowIndex) {
		if (rowIndex < 0 || data == null || rowIndex == data.size()) {
			return null;
		}
		return data.get(rowIndex);
	}

	public int getRowCount() {
		if (data == null)
			return 0;
		else
			return data.size();
	}

	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public boolean isCellEditable(int row, int col) {
//		if (ACE.editMode == false) {
//			return false;
//		}
//
//		if (row < 0 || row >= getRowCount()) {
//			return false;
//		}
//
//		if (AceLog.getAppLog().isLoggable(Level.FINER)) {
//			AceLog.getAppLog().finer("Cell is editable: " + row + " " + col);
//		}

		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		try {
			boolean changed = false;

			switch (columns[col]) {
			case FSN:
				data.get(row)[WORKFLOW_FIELD.FSN.getColumnNumber()] = value;
			case STATE:
				data.get(row)[WORKFLOW_FIELD.STATE.getColumnNumber()] = value;
			case EDITOR:
				data.get(row)[WORKFLOW_FIELD.EDITOR.getColumnNumber()] = value;
			case TIMESTAMP:
				data.get(row)[WORKFLOW_FIELD.TIMESTAMP.getColumnNumber()] = value;
				break;
			}
			fireTableDataChanged();

			if (changed) {
				AceLog.getAppLog().info("Description table changed");
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	@Override
	public void addWfInstance(WfProcessInstanceBI wfProcessInstanceBI) {
		WorkflowInstanceSearchResult wisr = null;
		try {
			String action = "action";
			String state = Ts.get().getConcept(wfProcessInstanceBI.getState().getUuid()).toUserString();
			WfUserBI assignedUser = wfProcessInstanceBI.getAssignedUser();
			String modeler = "";
			if (assignedUser != null) {
				modeler = assignedUser.getName();
			} else {
				modeler = "not assigned";
			}
			Long dueDate = wfProcessInstanceBI.getDueDate();
			Long lastChangeDate = wfProcessInstanceBI.getLastChangeTime();
			ConceptChronicleBI conceptFsn = Ts.get().getConcept(wfProcessInstanceBI.getComponentPrimUuid());
			wisr = new WorkflowInstanceSearchResult(action, state, modeler, lastChangeDate, conceptFsn.toUserString(), conceptFsn.toUserString());
			Object[] rowToadd = new Object[WORKFLOW_FIELD.values().length];
			WorkflowResultItem rItem=new WorkflowResultItem(wfProcessInstanceBI.getComponentPrimUuid(), wisr.getFsn());
			rowToadd[WORKFLOW_FIELD.FSN.getColumnNumber()] = rItem;
			rowToadd[WORKFLOW_FIELD.EDITOR.getColumnNumber()] = wisr.getModeler();
			rowToadd[WORKFLOW_FIELD.STATE.getColumnNumber()] = wisr.getState();
			rowToadd[WORKFLOW_FIELD.TIMESTAMP.getColumnNumber()] = TimeHelper.formatDate(wisr.getTime());
			addRow(rowToadd);
		} catch (Exception ex) {
		}
	}
	

	public void addRow(Object[] matches2) {
		data.add(matches2);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});
	}

	protected int getWfHistoryCount() {
		return data.size();
	}

	public WORKFLOW_FIELD[] getColumnEnums() {
		return columns;
	}

	public void clearResults() {
		data.clear();
		data = null;
		data = new LinkedList<Object[]>();
		fireTableDataChanged();
	}

	public boolean hasMatches() {
		return searchResults != null;
	}

}

