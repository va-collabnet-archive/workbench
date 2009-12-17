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
package org.dwfa.cement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.tapi.impl.UniversalFixedRel;
import org.dwfa.util.id.Type3UuidFactory;

public class QueueType implements I_AddToMemoryTermServer {
    public enum Concept implements I_ConceptEnumeration, I_ConceptualizeUniversally {
        QUEUE_TYPE("Queue Type"), AGING_QUEUE("aging queue", QUEUE_TYPE), ARCHIVAL_QUEUE("archival queue", QUEUE_TYPE), COMPUTE_QUEUE("compute queue", QUEUE_TYPE), INBOX_QUEUE("inbox queue", QUEUE_TYPE), LAUNCHER_QUEUE("launcher queue", QUEUE_TYPE), OUTBOX_QUEUE("outbox queue", QUEUE_TYPE), SYNCHRONIZATION_QUEUE("synchronization queue", QUEUE_TYPE), WEB_QUEUE("web queue", QUEUE_TYPE), ;

        private Collection<UUID> conceptUids = new ArrayList<UUID>();

        private Boolean primitive = true;

        private UniversalFixedRel[] rels;

        private UniversalFixedDescription[] descriptions;

        public String[] parents_S;
        public String[] descriptions_S;
        public String[] getParents_S() {
            return parents_S;
        }
        public String[] getDescriptions_S() {
            return descriptions_S;
        }
        private Concept(String descriptionString) {
            this(new String[] { descriptionString }, new I_ConceptualizeUniversally[] {});
        }

        private Concept(String descriptionString, I_ConceptualizeUniversally parent) {
            this(new String[] { descriptionString }, new I_ConceptualizeUniversally[] { parent });
        }

        private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents) {
            this.conceptUids.add(Type3UuidFactory.fromEnum(this));
            if (parents.length > 0) {
                parents_S = new String[parents.length];
                for (int i = 0; i < parents.length; i++) {
                    parents_S[i] = parents[i].toString();
                }
            }
            if (descriptionStrings.length > 0) {
                descriptions_S = descriptionStrings;
            }
            try {
                this.rels = DocumentAuxiliary.makeRels(this, parents);
                this.descriptions = DocumentAuxiliary.makeDescriptions(this, descriptionStrings, descTypeOrder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isPrimitive(I_StoreUniversalFixedTerminology server) {
            return true;
        }

        public Collection<UUID> getUids() {
            return conceptUids;
        }

        public boolean isUniversal() {
            return true;
        }

        public I_ManifestLocally localize(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getDestRelConcepts(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
                I_StoreUniversalFixedTerminology extensionServer) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
            return LocalFixedConcept.get(getUids(), primitive);
        }
    }

    private static I_ConceptualizeUniversally[] descTypeOrder = {
                                                                 ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE,
                                                                 ArchitectonicAuxiliary.Concept.EXTENSION_TABLE };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.cement.I_AddToMemoryTermServer#addToMemoryTermServer(org.dwfa
     * .cement.MemoryTermServer)
     */
    public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
        server.addRoot(Concept.QUEUE_TYPE);
        for (Concept s : Concept.values()) {
            server.add(s);
            for (I_DescribeConceptUniversally d : s.descriptions) {
                server.add(d);
            }
            for (I_RelateConceptsUniversally r : s.rels) {
                server.add(r);
            }
        }
    }

}
