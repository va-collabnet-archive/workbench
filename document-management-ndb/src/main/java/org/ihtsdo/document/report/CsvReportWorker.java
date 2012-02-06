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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

/**
 * The Class CsvReportWorker.
 */
public class CsvReportWorker extends SwingWorker<File, String> {
	
	/** The report panel. */
	private ReportPanel reportPanel;
	
	/** The report. */
	private I_Report report;

	/**
	 * Instantiates a new csv report worker.
	 *
	 * @param panel the panel
	 * @param report the report
	 */
	public CsvReportWorker(ReportPanel panel, I_Report report) {
		this.reportPanel = panel;
		this.report = report;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected File doInBackground() throws Exception {
		File result = null;

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Select folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(reportPanel.getFrame(reportPanel)) == JFileChooser.OPEN_DIALOG) {
			File selectedFolder = chooser.getSelectedFile();
			SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-hh-mm");
			result = new File(selectedFolder, sdf.format(new Date()) + "_"
					+ report.toString().replace(' ', '_') + ".csv");
			try {
				File csvFile = report.getCsv();
				if (csvFile != null) {
					ExcelReportUtil.copyFile(csvFile, result);
					try {
						Desktop desktop = null;
						if (Desktop.isDesktopSupported()) {
							desktop = Desktop.getDesktop();
							desktop.open(result);
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				} else {
					reportPanel.showError(ReportPanel.NO_DATA);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				reportPanel.showError(ReportPanel.EXCEPTION);
			} catch (Exception e2) {
				e2.printStackTrace();
				reportPanel.showError(ReportPanel.EXCEPTION);
			} finally {
				chooser.setVisible(false);
			}
		}
		return result;
	}

}
