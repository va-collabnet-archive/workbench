package org.dwfa.ace.task.refset.members;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

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
	
	private Logger logger = Logger.getLogger(BatchMonitor.class.getName());
	
	BatchReportingThread worker;
	
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
	public void mark() {		
		eventCount++;
	}
	
	/**
	 * Report updated statistics
	 */
	public void report() {
		if (totalEvents != 0) {
			
			long eventsSinceLastReport = eventCount - lastReportEventCount;
			long timeSinceLastReport = new Date().getTime() - lastReportTime;			
			long percentComplete = (eventCount * 100) / totalEvents;
			
			double eventsPerMs = eventsSinceLastReport / (double)timeSinceLastReport;
			long timeToCompleteMs = Math.round((totalEvents - eventCount) / eventsPerMs);
			
			logger.info(description + ": " + percentComplete + "% complete (" + 
					eventCount + " of " + totalEvents + 
					"). " + getEventRate(eventsPerMs) + 
					". Estimated time to complete: " + asTimeFormat(timeToCompleteMs));

			lastReportEventCount = eventCount;		
			lastReportTime = new Date().getTime();
		}
	}
	
	/**
	 * Start this monitor. Batch processing has commenced.
	 */
	public void start() {
		startTime = new Date().getTime();
		lastReportTime = startTime;
		worker = new BatchReportingThread(this, reportCycleMs);
		worker.start();
	}
	
	/** 
	 * Stop the monitor. Batch processing has completed.
	 */
	public void complete() {
		worker.interrupt();		
		long duration = new Date().getTime() - startTime;
		report();
		logger.info("Batch completed: " + description + ". " + eventCount + " events taking " + asTimeFormat(duration));
	}
	
	private String getEventRate(double eventsPerMs) {
		DecimalFormat format = new DecimalFormat("0.00");
		if (eventsPerMs < 0.001) {
			return "Events per minute: " + format.format(eventsPerMs * 60000); 
		} else {
			return "Events per second: " + format.format(eventsPerMs * 1000);
		}			
	}
	
	private String asTimeFormat(long durationMs) {
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
		String millis = String.format("%03d", (durationMs % 1000));
		result.append(seconds).append(".").append(millis).append(" seconds. ");
		return result.toString();
	}
	
	public class BatchReportingThread extends Thread {

		private long reportCycle = 0;
		
		private BatchMonitor batch;
		
		public BatchReportingThread(BatchMonitor batch, long reportCycleMs) {
			this.batch = batch;
			this.reportCycle = reportCycleMs;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					sleep(reportCycle);
					batch.report();
				}
			} catch (InterruptedException e) {}
		}
		
	}
}
