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
package org.dwfa.jini;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import net.jini.core.entry.Entry;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.QueueType;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.impl.LocalFixedTerminology;

public class TermEntry implements Entry, Comparable {

    public UUID[] ids;

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public TermEntry() {
        this.ids = new UUID[1];
        this.ids[0] = UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    public TermEntry(String idStr) {
        this.ids = new UUID[] { UUID.fromString(idStr) };
    }

    public TermEntry(UUID[] ids) {
        this.ids = ids;
    }

    public TermEntry(UUID id) {
        this.ids = new UUID[] { id };
    }

    public TermEntry(Collection<UUID> idCol) {
        this.ids = new UUID[idCol.size()];
        int i = 0;
        for (UUID id : idCol) {
            ids[i] = id;
            i++;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (TermEntry.class.isAssignableFrom(obj.getClass())) {
                TermEntry another = (TermEntry) obj;
                HashSet<UUID> anotherIdSet = new HashSet<UUID>(Arrays.asList(another.ids));
                for (UUID id : ids) {
                    if (anotherIdSet.contains(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public int compareTo(Object obj) {
        TermEntry another = (TermEntry) obj;
        try {
            return getLocalDesc().getText().compareTo(another.getLocalDesc().getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids[0].compareTo(another.ids[0]);
    }

    public I_ConceptualizeLocally getLocalConcept() throws Exception {
        I_StoreLocalFixedTerminology termServer = LocalFixedTerminology.getStore();
        return termServer.getConcept(termServer.getNid(Arrays.asList(ids)));
    }

    public I_DescribeConceptLocally getLocalDesc() throws Exception {
        return getLocalConcept().getDescription(
            ArchitectonicAuxiliary.getLocalToStringDescPrefList(LocalFixedTerminology.getStore()));
    }

    public I_DescribeConceptUniversally getUniversalDesc(I_StoreUniversalFixedTerminology termServer) throws Exception {
        return termServer.getDescription(Arrays.asList(ids));
    }

    public String toString() {
        try {
            return "TermEntry: " + getLocalDesc().getText();
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return "TermEntry: " + Arrays.asList(ids);
    }

    public static TermEntry getQueueType() {
        return new TermEntry(QueueType.Concept.QUEUE_TYPE.getUids());
    }

    public static TermEntry getInboxQueueType() {
        return new TermEntry(QueueType.Concept.INBOX_QUEUE.getUids());
    }

    public static TermEntry getOutboxQueueType() {
        return new TermEntry(QueueType.Concept.OUTBOX_QUEUE.getUids());
    }

    public static TermEntry getAgingQueueType() {
        return new TermEntry(QueueType.Concept.AGING_QUEUE.getUids());
    }

    public static TermEntry getArchivalQueueType() {
        return new TermEntry(QueueType.Concept.ARCHIVAL_QUEUE.getUids());
    }

    public static TermEntry getComputeQueueType() {
        return new TermEntry(QueueType.Concept.COMPUTE_QUEUE.getUids());
    }

    public static TermEntry getSynchronizationQueueType() {
        return new TermEntry(QueueType.Concept.SYNCHRONIZATION_QUEUE.getUids());
    }

    public static TermEntry getLauncherQueueType() {
        return new TermEntry(QueueType.Concept.LAUNCHER_QUEUE.getUids());
    }

    public static TermEntry getSnomedDisabilityEval() {
        return new TermEntry(SNOMED.Concept.Disability_evaluation_procedure.getUids());
    }

    public static TermEntry getSnomedPhysicianManagement() {
        return new TermEntry(SNOMED.Concept.Physician_visit_with_evaluation_AND_OR_management_service.getUids());
    }

    public static TermEntry getSnomedGeneralEyeMD() {
        return new TermEntry(SNOMED.Concept.Medical_ophthalmologist.getUids());
    }

    public static TermEntry getRetinalSurgeon() {
        return new TermEntry(SNOMED.Concept.Retinal_surgeon.getUids());
    }

    public static TermEntry getSnomedGeneralMed() {
        return new TermEntry(SNOMED.Concept.Family_medicine_specialist.getUids());
    }

    public static TermEntry getSnomedInternalMed() {
        return new TermEntry(SNOMED.Concept.Internal_medicine_specialist.getUids());
    }

    public UUID[] getIds() {
        return ids;
    }

    public void setIds(UUID[] ids) {
        this.ids = ids;
    }
}
