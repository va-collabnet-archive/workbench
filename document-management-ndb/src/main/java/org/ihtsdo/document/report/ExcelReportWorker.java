package org.ihtsdo.document.report;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingWorker;

public class ExcelReportWorker extends SwingWorker<File, String> {

	private ReportPanel reportPanel;
	private I_Report report;

	public ExcelReportWorker(ReportPanel panel, I_Report report) {
		this.reportPanel = panel;
		this.report = report;
	}

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
