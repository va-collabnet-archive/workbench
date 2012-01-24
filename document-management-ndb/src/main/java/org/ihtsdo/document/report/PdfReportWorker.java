package org.ihtsdo.document.report;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

public class PdfReportWorker extends SwingWorker<String, String>{
	private ReportPanel reportPanel;
	private I_Report report;

	public PdfReportWorker(ReportPanel panel, I_Report report) {
		this.reportPanel = panel;
		this.report = report;
	}
	@Override
	protected String doInBackground() throws Exception {
		JFrame reportFrame;
		try {
			reportFrame = report.getReportPanel();
			if (reportFrame != null) {
				reportFrame.setSize(new Dimension(750, 800));
				reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				reportFrame.setVisible(true);
			} else {
				reportPanel.showError(ReportPanel.NO_DATA);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			reportPanel.showError(ReportPanel.EXCEPTION);
		}
		return "Done";
	}

}
