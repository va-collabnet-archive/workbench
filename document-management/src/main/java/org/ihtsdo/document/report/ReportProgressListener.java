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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * The listener interface for receiving reportProgress events.
 * The class that is interested in processing a reportProgress
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addReportProgressListener<code> method. When
 * the reportProgress event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ReportProgressEvent
 */
public class ReportProgressListener implements PropertyChangeListener {
	// Prevent creation without providing a progress bar.
	/**
	 * Instantiates a new report progress listener.
	 */
	@SuppressWarnings("unused")
	private ReportProgressListener() {
	}

	/**
	 * Instantiates a new report progress listener.
	 *
	 * @param progressBar the progress bar
	 */
	public ReportProgressListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setVisible(true);
		this.progressBar.setIndeterminate(true);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
		}
	}

	/** The progress bar. */
	private JProgressBar progressBar;
}