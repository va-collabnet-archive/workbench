package org.ihtsdo.document.report;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

public class CsvReportWorker extends SwingWorker<File, String> {
	private ReportPanel reportPanel;
	private I_Report report;

	public CsvReportWorker(ReportPanel panel, I_Report report) {
		this.reportPanel = panel;
		this.report = report;
	}

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
