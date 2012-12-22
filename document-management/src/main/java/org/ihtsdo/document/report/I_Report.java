/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.document.report;

import java.io.File;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * The Interface I_Report.
 */
public interface I_Report {
	
	/**
	 * The Enum Reports.
	 */
	public enum Reports implements Serializable{
		
		/** The WORKLIS t_ member s_ b y_ statu s_ report. */
		WORKLIST_MEMBERS_BY_STATUS_REPORT(new WorklistMemberByStatusReport()), 
		
		/** The WORKLIS t_ stat e_ total s_ report. */
		WORKLIST_STATE_TOTALS_REPORT(new WorklistStateTotalsReport()),
		
		/** The PROJEC t_ historica l_ report. */
		PROJECT_HISTORICAL_REPORT(new ProjectHistoricalReport()),
		//STATUS_CHANGES_REPORT(new StatusChangesReport()),
		/** The ACCUMULATE d_ statu s_ change s_ report. */
		ACCUMULATED_STATUS_CHANGES_REPORT(new AccumulatedStatusChanges());

		/** The report. */
		private final Object report;

		/**
		 * Instantiates a new reports.
		 *
		 * @param report the report
		 */
		private Reports(Object report) {
			this.report = report;
		}
		
		/**
		 * Gets the report.
		 *
		 * @return the report
		 */
		public I_Report getReport(){
			return (I_Report)this.report;
		}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.report.getClass().getName();
		}
	}
	
	/**
	 * Gets the csv.
	 *
	 * @return the csv
	 * @throws Exception the exception
	 */
	public File getCsv()throws Exception; 
	
	/**
	 * Gets the report panel.
	 *
	 * @return the report panel
	 * @throws Exception the exception
	 */
	public JFrame getReportPanel()throws Exception;
	
	/**
	 * Gets the excel source workbook.
	 *
	 * @return the excel source workbook
	 * @throws Exception the exception
	 */
	public File getExcelSourceWorkbook()throws Exception;
	
	/**
	 * Gets the excel pivot table work book.
	 *
	 * @return the excel pivot table work book
	 * @throws Exception the exception
	 */
	public File getExcelPivotTableWorkBook()throws Exception;
	
	/**
	 * Cancel reporting.
	 *
	 * @throws Exception the exception
	 */
	public void cancelReporting() throws Exception;
	
}
