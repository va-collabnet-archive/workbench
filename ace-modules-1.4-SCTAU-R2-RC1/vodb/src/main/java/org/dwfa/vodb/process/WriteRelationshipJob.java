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

public class WriteRelationshipJob implements Runnable {
    public class WriteRelationshipData {

        public Date statusDate;
        public Object relID;
        public Object statusId;
        public Object conceptOneID;
        public Object relationshipTypeConceptID;
        public Object conceptTwoID;
        public Object characteristic;
        public Object refinability;
        public int group;
        public Object pathId;
        private CountDownLatch latch;

        public WriteRelationshipData(CountDownLatch latch, Date statusDate, Object relID, Object statusId,
                Object conceptOneID, Object relationshipTypeConceptID, Object conceptTwoID, Object characteristic,
                Object refinability, int group, Object pathId) {
            super();
            this.latch = latch;
            this.statusDate = statusDate;
            this.relID = relID;
            this.statusId = statusId;
            this.conceptOneID = conceptOneID;
            this.relationshipTypeConceptID = relationshipTypeConceptID;
            this.conceptTwoID = conceptTwoID;
            this.characteristic = characteristic;
            this.refinability = refinability;
            this.group = group;
            this.pathId = pathId;
        }

        @Override
        public String toString() {
            return "WriteRelationshipData [characteristic=" + characteristic + ", conceptOneID=" + conceptOneID
                + ", conceptTwoID=" + conceptTwoID + ", group=" + group + ", latch=" + latch + ", pathId=" + pathId
                + ", refinability=" + refinability + ", relID=" + relID + ", relationshipTypeConceptID="
                + relationshipTypeConceptID + ", statusDate=" + statusDate + ", statusId=" + statusId + "]";
        }

    }

    List<WriteRelationshipData> batch = new ArrayList<WriteRelationshipData>();
    ProcessSources processor;

    public WriteRelationshipJob(ProcessSources processor) {
        super();
        this.processor = processor;
    }

    public void addTask(CountDownLatch latch, Date statusDate, Object relID, Object statusId, Object conceptOneID,
            Object relationshipTypeConceptID, Object conceptTwoID, Object characteristic, Object refinability,
            int group, Object pathId) {
        batch.add(new WriteRelationshipData(latch, statusDate, relID, statusId, conceptOneID,
            relationshipTypeConceptID, conceptTwoID, characteristic, refinability, group, pathId));
    }

    @Override
    public void run() {
        for (WriteRelationshipData data : batch) {
            try {
                processor.writeRelationship(data.latch, data.statusDate, data.relID,
                    Arrays.asList(new Object[] { data.statusId }), data.conceptOneID, data.relationshipTypeConceptID,
                    data.conceptTwoID, data.characteristic, data.refinability, data.group,
                    Arrays.asList(new Object[] { data.pathId }));
            } catch (Exception e) {
                throw new RuntimeException("failed importing " + data, e);
            }
        }
    }

}
