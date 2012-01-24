package org.ihtsdo.document.report;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ReportProgressListener implements PropertyChangeListener {
	// Prevent creation without providing a progress bar.
	@SuppressWarnings("unused")
	private ReportProgressListener() {
	}

	public ReportProgressListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setVisible(true);
		this.progressBar.setIndeterminate(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
		}
	}

	private JProgressBar progressBar;
}