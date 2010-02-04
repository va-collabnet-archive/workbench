/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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

    public Batch(Collection<T> items, String description, boolean useMonitor) {
        this(items, description);
        this.useMonitor = useMonitor;
    }

    public void run() {
        if (items != null && (items.size() == 0)) {
            return;
        }
        try {
            try {
                if (useMonitor) {
                    int size = (items != null) ? items.size() : 0;
                    monitor = new BatchMonitor(description, size, reportIterval);
                    monitor.start();
                }

                prepare();
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
        } finally {
            if (monitor != null) {
                monitor.stop();
            }
        }

    }

    protected void process() throws Exception {
        if (items == null) {
            return;
        }
        for (T item : items) {
            processItem(item);
            if (useMonitor) {
                monitor.mark();
            }
        }
    }

    protected void prepare() throws Exception {
    };
    
    protected void processItem(T item) throws Exception {
    };

    protected void onCancel() throws Exception {
    };

    protected void onComplete() throws Exception {
    };

    protected void setItems(Collection<T> items) {
        this.items = items;
        if (useMonitor && monitor != null) {
            monitor.setTotalEvents(items.size());
        }
    }
    
    public void setReportIterval(int reportIterval) {
        this.reportIterval = reportIterval;
    }
    
}
