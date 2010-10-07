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
package org.dwfa.ace.api;

import org.dwfa.ace.batch.BatchCancelledException;
import org.dwfa.ace.batch.BatchMonitor;
import org.dwfa.tapi.TerminologyRuntimeException;

public class ProcessProgressWrapper implements I_ProcessConcepts {

    private int reportingIntervalMs = 2000;

    private BatchMonitor monitor;

    private boolean started = false;

    private I_ProcessConcepts processor;

    private String description;

    public ProcessProgressWrapper(I_ProcessConcepts processor, String description) {
        this.processor = processor;
        this.description = description;
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (started == false) {
            started = true;
            initMonitor();
            monitor.start();
        }

        try {
            this.processor.processConcept(concept);
        } catch (Exception e) {
            cancel();
            throw e;
        }

        if (this.monitor != null) {
            this.monitor.mark();
        }
    }

    public void initMonitor() {
        if (this.monitor != null) {
            this.monitor.stop();
            this.monitor = null;
        }
        try {
            int conceptCount = LocalVersionedTerminology.get().getConceptCount();
            this.monitor = new BatchMonitor(this.description, conceptCount, this.reportingIntervalMs);
        } catch (Exception e) {
            throw new TerminologyRuntimeException(e);
        }
    }

    public void complete() {
        if (this.monitor != null) {
            try {
                this.monitor.complete();
            } catch (BatchCancelledException e) {
            }
        }
    }

    public void cancel() {
        if (this.monitor != null) {
            this.monitor.cancel();
            this.monitor = null;
        }
    }

    public void setReportingIntervalMs(int reportingIntervalMs) {
        this.reportingIntervalMs = reportingIntervalMs;
    }
}
