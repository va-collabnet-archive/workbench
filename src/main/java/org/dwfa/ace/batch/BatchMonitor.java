package org.dwfa.ace.batch;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

/**
 * Used to give statistical reporting (progress, time remaining, etc) during a batch processing operation.  
 */
public class BatchMonitor {

	private String description = "Batch process";
	private long startTime = 0;
	private long totalEvents = 0;	
	private long reportCycleMs = 0;
	private long eventCount = 0;
	private long lastReportTime = 0;
	private long lastReportEventCount = 0;
	
	private boolean isCancelled = false;
	
	private Logger logger = Logger.getLogger(BatchMonitor.class.getName());
	
	private BatchReportingThread timer;
	
	private I_ShowActivity activity;
	
	/**
	 * @param description A textual description of the batch
	 * @param totalEvents The total number of expected events
	 * @param reportCycleMs The time between giving an updated report
	 */
	public BatchMonitor(String description, long totalEvents, long reportCycleMs) {
		if (description != null) {
			this.description = description;
		}
		this.totalEvents = totalEvents;
		this.reportCycleMs = reportCycleMs;
	}
	
	/**
	 * Mark an event
	 */
	public void mark() throws BatchCancelledException {
		throwIfCancelled();
		eventCount++;
	}
	
	public void setText(String text) {
		if (activity != null) {
			activity.setProgressInfoLower(text);
		}
	}
	
	public void setIndeterminate(boolean indeterminate) {
		if (activity != null) {
			activity.setIndeterminate(indeterminate);			
		}
		if (indeterminate) {
			totalEvents = 0;
		}
	}
	
	/**
	 * Report updated statistics
	 */
	public void report() {
		if (totalEvents != 0) {
			
			long eventsSinceLastReport = eventCount - lastReportEventCount;
			long timeSinceLastReport = new Date().getTime() - lastReportTime;			
			long percentComplete = (eventCount * 100) / totalEvents;
			
			if (eventsSinceLastReport > 0) {
				
				double eventsPerMs = eventsSinceLastReport / (double)timeSinceLastReport;
				long timeToCompleteMs = Math.round((totalEvents - eventCount) / eventsPerMs);
				
				activity.setProgressInfoLower(percentComplete + "% completed (" + eventCount + " of " + totalEvents + "). " + 
						asTimeFormat(timeToCompleteMs, false) + "remaining");
				activity.setValue((int)eventCount);
				
				logger.info(description + ": " + percentComplete + "% complete (" + eventCount + " of " + totalEvents + "). " + 
						getEventRate(eventsPerMs) + ". Estimated time to complete: " + asTimeFormat(timeToCompleteMs, true));
			} else {
				logger.info(description + ": " + percentComplete + "% complete (" + eventCount + " of " + totalEvents + "). " +
						"No activity since last report!");
			}
			
			lastReportEventCount = eventCount;		
			lastReportTime = new Date().getTime();
		}
	}
	
	/**
	 * Start this monitor. Batch processing has commenced.
	 */
	public void start() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		activity = termFactory.newActivityPanel(false);
		activity.setProgressInfoUpper(description);
		activity.setProgressInfoLower("Commencing...");
		activity.setMaximum((int)totalEvents);
		activity.setValue(0);
		activity.setIndeterminate(false);
		
		startTime = new Date().getTime();
		lastReportTime = startTime;
		timer = new BatchReportingThread(this, reportCycleMs, activity);
		timer.start();
	}

	/** 
	 * Stop the monitor. Batch processing has completed.
	 */
	public void complete() throws BatchCancelledException {
		throwIfCancelled();
		timer.interrupt();		
		long duration = new Date().getTime() - startTime;
		report();
		String timeElapsed = asTimeFormat(duration, true);
		activity.complete();
		activity.setProgressInfoLower("Completed processing " + eventCount + " items in " + timeElapsed);		
		logger.info("Batch completed: " + description + ". " + eventCount + " items processed in " + timeElapsed);
		
	}
	
	/**
	 * Cancel the monitor. Batch has been aborted. 
	 * Any further calls to {@link #mark()} or {@link #complete()} will raise a {@link BatchCancelledException} 
	 */
	public void cancel() {
		isCancelled = true;
		timer.interrupt();	
		activity.complete();
		activity.setProgressInfoLower("Cancelled by user");
	}
	
	private void throwIfCancelled() throws BatchCancelledException {
		if (isCancelled) {
			throw new BatchCancelledException();
		}
	}
	
	private String getEventRate(double eventsPerMs) {
		DecimalFormat format = new DecimalFormat("0.00");
		if (eventsPerMs < 0.001) {
			return "Events per minute: " + format.format(eventsPerMs * 60000); 
		} else {
			return "Events per second: " + format.format(eventsPerMs * 1000);
		}			
	}
	
	private String asTimeFormat(long durationMs, boolean showMillis) {
		StringBuffer result = new StringBuffer();
		long hours = durationMs / 3600000;
		if (hours != 0) {
			result.append(hours).append(" hours, ");
		}
		long minutes = (durationMs % 3600000) / 60000;
		if (minutes != 0) {
			result.append(minutes).append(" minutes, ");
		}
		long seconds = ((durationMs % 3600000) % 60000) / 1000;
		result.append(seconds);
		if (showMillis) {
			String millis = String.format("%03d", (durationMs % 1000));
			result.append(".").append(millis);
		}
		result.append(" seconds ");
		return result.toString();
	}
	
	public class BatchReportingThread extends Thread {

		private long reportCycle = 0;
		
		private BatchMonitor batch;
		private MonitorDialog dialog;
		private I_ShowActivity activity;		
		
		public BatchReportingThread(BatchMonitor batch, long reportCycleMs, I_ShowActivity activity) {
			this.batch = batch;
			this.reportCycle = reportCycleMs;
			this.activity = activity;
			
			this.activity.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							cancel();
						};	
					}
			);			
		}
		
		@Override
		public void run() {
			try {
				showDialog();
				while (true) {
					sleep(reportCycle);
					batch.report();	
				}
			} catch (InterruptedException e) {
			} finally {
				dialog.dispose();
			}
		}
		
		private void showDialog() {
			dialog = new MonitorDialog(activity);
	        dialog.pack();
	        dialog.setTitle("Operation in progress");
	        dialog.setResizable(false);
	        dialog.setModal(false);
	        dialog.setAlwaysOnTop(true);

	        Toolkit toolkit = Toolkit.getDefaultToolkit();
	        Dimension screenSize = toolkit.getScreenSize();

	        // centre on the screen
	        int x = (screenSize.width - dialog.getWidth()) / 2;  
	        int y = (screenSize.height - dialog.getHeight()) / 2;
	        dialog.setLocation(x, y);
	        
	        // beef it up a little
	        dialog.setSize((int)(1.25 * dialog.getWidth()), (int)(1.5 * dialog.getHeight()));
	        
	        dialog.setVisible(true);
		}
	}
	
	public class MonitorDialog extends JDialog {

		private static final long serialVersionUID = 8408703041409497746L;

		public MonitorDialog(I_ShowActivity activity) {
			setContentPane((JPanel)activity);			
		}
		
	}
}
