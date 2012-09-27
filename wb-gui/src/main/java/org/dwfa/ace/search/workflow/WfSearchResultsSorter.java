package org.dwfa.ace.search.workflow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WORKFLOW_FIELD;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WorkflowStringWithConceptTuple;

public class WfSearchResultsSorter extends TableRowSorter<TableModel> {

	public WfSearchResultsSorter(TableModel model) {
		super(model);
		
		setComparator(WORKFLOW_FIELD.FSN.getColumnNumber(), new ConceptComparator());
		setComparator(WORKFLOW_FIELD.EDITOR.getColumnNumber(), new ConceptComparator());
		setComparator(WORKFLOW_FIELD.STATE.getColumnNumber(), new ConceptComparator());
		setComparator(WORKFLOW_FIELD.TIMESTAMP.getColumnNumber(), new TimestampComparator());
	}
	
	private class ConceptComparator implements Comparator<WorkflowStringWithConceptTuple>  {
		@Override
		public int compare(WorkflowStringWithConceptTuple o1, WorkflowStringWithConceptTuple o2) {
			return o1.compareTo(o2);
		}
	}

	private class TimestampComparator implements Comparator<WorkflowStringWithConceptTuple>  {
		@Override
		public int compare(WorkflowStringWithConceptTuple o1, WorkflowStringWithConceptTuple o2) {
			String dateString1 = o1.getCellText();
			String dateString2 = o2.getCellText();
			
			DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
			try {
				Long time1 = formatter.parse(dateString1).getTime();
				Long time2 = formatter.parse(dateString2).getTime();
				
				if (time1 < time2) {
					return 1;
				} else {
					return -1;
				}
			} catch (ParseException e) {
				return 1;
			}
		}
	}
}
