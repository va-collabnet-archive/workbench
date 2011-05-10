package org.ihtsdo.document.report;

import java.io.File;
import java.io.Serializable;

import javax.swing.JFrame;

public interface I_Report {
	
	public enum Reports implements Serializable{
		WORKLIST_MEMBERS_BY_STATUS_REPORT(new WorklistMemberByStatusReport()), 
		WORKLIST_STATE_TOTALS_REPORT(new WorklistStateTotalsReport()),
		PROJECT_HISTORICAL_REPORT(new ProjectHistoricalReport()),
		//STATUS_CHANGES_REPORT(new StatusChangesReport()),
		ACCUMULATED_STATUS_CHANGES_REPORT(new AccumulatedStatusChanges());

		private final Object report;

		private Reports(Object report) {
			this.report = report;
		}
		
		public I_Report getReport(){
			return (I_Report)this.report;
		}

		public String toString() {
			return this.report.getClass().getName();
		}
	}
	
	public File getCsv()throws Exception; 
	public JFrame getReportPanel()throws Exception;
	public File getExcelSourceWorkbook()throws Exception;
	public File getExcelPivotTableWorkBook()throws Exception;
	
}
