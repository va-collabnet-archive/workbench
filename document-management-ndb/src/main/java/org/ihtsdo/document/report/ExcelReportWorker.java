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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingWorker;

/**
 * The Class ExcelReportWorker.
 */
public class ExcelReportWorker extends SwingWorker<File, String> {

	/** The report panel. */
	private ReportPanel reportPanel;
	
	/** The report. */
	private I_Report report;

	/**
	 * Instantiates a new excel report worker.
	 *
	 * @param panel the panel
	 * @param report the report
	 */
	public ExcelReportWorker(ReportPanel panel, I_Report report) {
		this.reportPanel = panel;
		this.report = report;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected File doInBackground() throws Exception {
		File excelFile = null;
		try {
			excelFile = report.getExcelSourceWorkbook();
			if (excelFile != null) {
				try {
					Desktop desktop = null;
					if (Desktop.isDesktopSupported()) {
						desktop = Desktop.getDesktop();
						desktop.open(excelFile);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			} else {
				reportPanel.showError(ReportPanel.NO_DATA);
			}
		} catch (IllegalArgumentException e2) {
			reportPanel.showError(ReportPanel.EXEL_SIZE_EXCEDED_EXCEPTION);
		} catch (Exception e1) {
			reportPanel.showError(ReportPanel.EXCEPTION);
		}
		return excelFile;
	}

}
