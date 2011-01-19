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

public class WriteDescriptionJob implements Runnable {
    public class WriteDescriptionData {

        private CountDownLatch descriptionLatch;
        private Date statusDate;
        private Object descriptionId;
        private Object status;
        private Object conceptId;
        private String text;
        private boolean capSignificant;
        private Object typeInt;
        private String lang;
        private Object pathId;

        public WriteDescriptionData(CountDownLatch descriptionLatch, Date statusDate, Object descriptionId,
                Object status, Object conceptId, String text, boolean capSignificant, Object typeInt, String lang,
                Object pathId) {

            this.descriptionLatch = descriptionLatch;
            this.statusDate = statusDate;
            this.descriptionId = descriptionId;
            this.status = status;
            this.conceptId = conceptId;
            this.text = text;
            this.capSignificant = capSignificant;
            this.typeInt = typeInt;
            this.lang = lang;
            this.pathId = pathId;

        }

        @Override
        public String toString() {
            return "WriteDescriptionData [capSignificant=" + capSignificant + ", conceptId=" + conceptId
                + ", descriptionId=" + descriptionId + ", descriptionLatch=" + descriptionLatch + ", lang=" + lang
                + ", pathId=" + pathId + ", status=" + status + ", statusDate=" + statusDate + ", text=" + text
                + ", typeInt=" + typeInt + "]";
        }

    }

    List<WriteDescriptionData> batch = new ArrayList<WriteDescriptionData>();
    ProcessSources processor;

    public WriteDescriptionJob(ProcessSources processor) {
        super();
        this.processor = processor;
    }

    public void addTask(CountDownLatch conceptLatch, Date statusDate, Object conceptKey, Object conceptStatus,
            boolean defChar, Object pathId) {

    }

    public void addTask(CountDownLatch descriptionLatch, Date statusDate, Object descriptionId, Object status,
            Object conceptId, String text, boolean capSignificant, Object typeInt, String lang, Object pathId) {
        batch.add(new WriteDescriptionData(descriptionLatch, statusDate, descriptionId, status, conceptId, text,
            capSignificant, typeInt, lang, pathId));
    }

    @Override
    public void run() {
        for (WriteDescriptionData data : batch) {
            try {
                processor.writeDescription(data.descriptionLatch, data.statusDate, data.descriptionId, data.status,
                    data.conceptId, data.text, data.capSignificant, data.typeInt, data.lang,
                    Arrays.asList(new Object[] { data.pathId }));
            } catch (Exception e) {
                throw new RuntimeException("failed importing " + data, e);
            }
        }
    }

}
