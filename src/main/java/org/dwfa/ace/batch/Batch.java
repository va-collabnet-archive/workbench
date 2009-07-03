package org.dwfa.ace.batch;

import java.util.Collection;

public class Batch<T> implements Runnable {

	protected int reportIterval = 3000;
	protected Collection<T> items;
	protected String description;
	
	protected BatchMonitor monitor;
	protected boolean useMonitor = true;
	
	public Batch(Collection<T> items, String description) {
		this.items = items;
		this.description = description;
	}
	
	public Batch(Collection<T> items, String description, 
			boolean useMonitor) {
		this(items, description);
		this.useMonitor = useMonitor;
	}
	
	public void run() {
		try {
			try {
				if (useMonitor) {
					monitor = new BatchMonitor(description, items.size(), reportIterval);
					monitor.start();
				}
				
				process();
				onComplete();
				
				if (useMonitor) { 
					monitor.complete();
				}
			} catch (BatchCancelledException ex) {
				onCancel();
			} catch (Exception ex) {
				onCancel();
				throw ex;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void process() throws Exception {
		for (T item : items) {
			processItem(item);
			if (useMonitor) {
				monitor.mark();
			}
		}
	}
	
	protected void processItem(T item) throws Exception {};
	
	protected void onCancel() throws Exception {};
	
	protected void onComplete() throws Exception {};

	public void setReportIterval(int reportIterval) {
		this.reportIterval = reportIterval;
	}
}
