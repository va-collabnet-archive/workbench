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
package org.dwfa.vodb.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WriteConceptJob implements Runnable {
    public class WriteConceptData {
        CountDownLatch conceptLatch;
        Date statusDate;
        Object conceptKey;
        Object conceptStatus;
        boolean defChar;
        Object pathId;

        public WriteConceptData(CountDownLatch conceptLatch, Date statusDate, Object conceptKey, Object conceptStatus,
                boolean defChar, Object pathId) {
            super();
            this.conceptLatch = conceptLatch;
            this.statusDate = statusDate;
            this.conceptKey = conceptKey;
            this.conceptStatus = conceptStatus;
            this.defChar = defChar;
            this.pathId = pathId;
        }

        @Override
        public String toString() {
            return "WriteConceptData [conceptKey=" + conceptKey + ", conceptLatch=" + conceptLatch + ", conceptStatus="
                + conceptStatus + ", defChar=" + defChar + ", pathId=" + pathId + ", statusDate=" + statusDate + "]";
        }
    }

    List<WriteConceptData> batch = new ArrayList<WriteConceptData>();
    ProcessSources processor;

    public WriteConceptJob(ProcessSources processor) {
        super();
        this.processor = processor;
    }

    public void addTask(CountDownLatch conceptLatch, Date statusDate, Object conceptKey, Object conceptStatus,
            boolean defChar, Object pathId) {
        batch.add(new WriteConceptData(conceptLatch, statusDate, conceptKey, conceptStatus, defChar, pathId));
    }

    @Override
    public void run() {
        for (WriteConceptData data : batch) {
            try {
                processor.writeConcept(data.conceptLatch, data.statusDate, data.conceptKey, data.conceptStatus,
                    data.defChar, Arrays.asList(new Object[] { data.pathId }));
            } catch (Exception e) {
                throw new RuntimeException("failed importing " + data, e);
            }
        }
    }

}
