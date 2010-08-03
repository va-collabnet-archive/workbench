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
package org.ihtsdo.cement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.I_AddToMemoryTermServer;
import org.dwfa.cement.I_ConceptEnumeration;
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

/**
 * A taxonomy for organization sections of clinical documentation.
 * 
 * @author kec
 * 
 */
public class WorkflowAuxiliary implements I_AddToMemoryTermServer {

    public enum Concept implements I_ConceptEnumeration, I_ConceptualizeUniversally {
    	WORKFLOW_AUXILIARY(new String[] { "Workflow" }, new I_ConceptualizeUniversally[] {}, new int[] { 0 }), 
        EDITOR_CATEGORY(new String[] { "Editor Category" }, new I_ConceptualizeUniversally[] { WORKFLOW_AUXILIARY }, new int[] { 0 }), 
        ACTION(new String[] { "Workflow Action" }, new I_ConceptualizeUniversally[] { WORKFLOW_AUXILIARY }, new int[] { 10 }), 
        STATE(new String[] { "Workflow State" }, new I_ConceptualizeUniversally[] { WORKFLOW_AUXILIARY }, new int[] { 10 }), 
        USE_CASE(new String[] { "Workflow Use Case" }, new I_ConceptualizeUniversally[] { WORKFLOW_AUXILIARY }, new int[] { 10 }), 
        WORKFLOW_HISTORY_INFORMATION(new String[] { "Workflow History Information" }, new I_ConceptualizeUniversally[] { WORKFLOW_AUXILIARY }, new int[] { 10 }), 
        ROLE_A(new String[] { "Role A" }, new I_ConceptualizeUniversally[] { EDITOR_CATEGORY }, new int[] { 10 }), 
        ROLE_B(new String[] { "Role B" }, new I_ConceptualizeUniversally[] { EDITOR_CATEGORY }, new int[] { 10 }), 
        ROLE_BPLUS(new String[] { "Role B+" }, new I_ConceptualizeUniversally[] { EDITOR_CATEGORY }, new int[] { 10 }), 
        ROLE_C(new String[] { "Role C" }, new I_ConceptualizeUniversally[] { EDITOR_CATEGORY }, new int[] { 10 }), 
        ROLE_D(new String[] { "Role D" }, new I_ConceptualizeUniversally[] { EDITOR_CATEGORY }, new int[] { 10 }), 
        ROLE_ANY(new String[] { "Role Any" }, new I_ConceptualizeUniversally[] { EDITOR_CATEGORY }, new int[] { 10 }), 
        ACCEPT(new String[] { "Accept Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        CONSENSUS(new String[] { "Consensus Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        DISCUSS(new String[] { "Discuss Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        EMPTY(new String[] { "Empty Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        ESCALATE(new String[] { "Escalate Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        RETIRE(new String[] { "Retire Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        REVIEW(new String[] { "Review Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        REVIEW_CHIEF_TERM(new String[] { "Review Chief Term Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        WORK_UP(new String[] { "Work Up Workflow Action" }, new I_ConceptualizeUniversally[] { ACTION }, new int[] { 10 }), 
        APPROVED(new String[] { "Approve Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        CHANGED(new String[] { "Change Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        CHANGED_IN_BATCH(new String[] { "Changed in Batch Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        CONSENSUSED(new String[] { "Consensus Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        DISCUSSED(new String[] { "Discuss Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        DONE(new String[] { "Done Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        ESCALATED(new String[] { "Escalate Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        FIRST_REVIEW(new String[] { "First Review Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        NEW(new String[] { "New Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        REVIEWED_CHIEF_TERM(new String[] { "Review Chief Term Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }),         
        REVISED(new String[] { "Revised Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }),         
        SECOND_REVIEW(new String[] { "Second Review Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        TO_RETIRE(new String[] { "To Retire Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        WORKED_UP(new String[] { "Work Up Workflow State" }, new I_ConceptualizeUniversally[] { STATE }, new int[] { 10 }), 
        NEW_USE_CASE(new String[] { "New Use Case" }, new I_ConceptualizeUniversally[] { USE_CASE }, new int[] { 10 }), 
        EDIT_USE_CASE(new String[] { "Edit Use Case" }, new I_ConceptualizeUniversally[] { USE_CASE }, new int[] { 10 }), 
        ;

        private ArrayList<UUID> conceptUids = new ArrayList<UUID>();

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

        private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents, int[] relOrder) {
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
                this.rels = makeRels(this, parents);
                int i = 0;
            /*    for (UniversalFixedRel r : rels) {
                    int order = relOrder[i++];
                    UniversalFixedIntExtension ext = new UniversalFixedIntExtension(Type3UuidFactory.forExtension(r,
                        RefsetAuxiliary.Concept.INT_EXTENSION, RefsetAuxiliary.Concept.DOCUMENT_SECTION_ORDER), order);
                    HashMap<I_ConceptualizeUniversally, I_ExtendUniversally> extensionsForComponent = new HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>();
                    extensionsForComponent.put(RefsetAuxiliary.Concept.DOCUMENT_SECTION_ORDER, ext);
                    extensions.put(r, extensionsForComponent);
                }
*/
                this.descriptions = makeDescriptions(this, descriptionStrings, descTypeOrder);
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

		@Override
		public UUID getPrimoridalUid() throws IOException, TerminologyException {
			return conceptUids.get(0);
		}
    }

    //private static HashMap<I_ManifestUniversally, HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>> extensions = new HashMap<I_ManifestUniversally, HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.cement.I_AddToMemoryTermServer#addToMemoryTermServer(org.dwfa
     * .cement.MemoryTermServer)
     */
    public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
        server.addRoot(Concept.WORKFLOW_AUXILIARY);
        for (Concept s : Concept.values()) {
            server.add(s);
            for (I_DescribeConceptUniversally d : s.descriptions) {
                server.add(d);
            }
            for (I_RelateConceptsUniversally r : s.rels) {
                server.add(r);
            }
        }
/*        for (Map.Entry<I_ManifestUniversally, HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>> componentAndExtensions : extensions.entrySet()) {
            for (Map.Entry<I_ConceptualizeUniversally, I_ExtendUniversally> extTypeAndExt : componentAndExtensions.getValue()
                    .entrySet()) {
                server.addExtension(componentAndExtensions.getKey().localize(),
                    (I_ConceptualizeLocally) extTypeAndExt.getKey().localize(), extTypeAndExt.getValue().localize());
            }
        }
  */  }

    public static UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents)
    throws Exception {
        I_ConceptualizeUniversally[] relTypes = new I_ConceptualizeUniversally[parents.length];
        Arrays.fill(relTypes, ArchitectonicAuxiliary.Concept.IS_A_REL);
        return makeRels(source, parents, relTypes, ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP);
    }

    public static UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents,
            I_ConceptualizeUniversally characteristicType) throws Exception {
        I_ConceptualizeUniversally[] relTypes = new I_ConceptualizeUniversally[parents.length];
        Arrays.fill(relTypes, ArchitectonicAuxiliary.Concept.IS_A_REL);
        return makeRels(source, parents, relTypes, characteristicType);
    }

    public static UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents,
            I_ConceptualizeUniversally[] relTypes) throws Exception {
        return makeRels(source, parents, relTypes, ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP);
    }

    public static UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents,
            I_ConceptualizeUniversally[] relTypes, I_ConceptualizeUniversally characteristicType) throws Exception {
        UniversalFixedRel[] rels = new UniversalFixedRel[parents.length];
        int i = 0;
        for (I_ConceptualizeUniversally p : parents) {
            int relGrp = 0;
            int parentIndex = i++;
            Collection<UUID> relUids = Type3UuidFactory.forRel(source.getUids(), relTypes[parentIndex].getUids(),
                p.getUids());
            rels[parentIndex] = new UniversalFixedRel(relUids, source.getUids(), relTypes[parentIndex].getUids(),
                p.getUids(), characteristicType.getUids(), ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids(),
                relGrp);
        }
        return rels;
    }

    static List<I_ConceptualizeLocally> localDescTypeOrder;

    public static List<I_ConceptualizeLocally> getDescTypeOrder() throws IOException, TerminologyException {
        if (localDescTypeOrder == null) {
            localDescTypeOrder = new ArrayList<I_ConceptualizeLocally>();
            localDescTypeOrder.add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize());
            localDescTypeOrder.add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize());
        }
        return localDescTypeOrder;
    }

    private static I_ConceptualizeUniversally[] descTypeOrder = {
        ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE,
        ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE };

    public static UniversalFixedDescription[] makeDescriptions(I_ConceptualizeUniversally source,
            String[] descriptionStrings, I_ConceptualizeUniversally[] descTypeOrder) throws Exception {
        UniversalFixedDescription[] descriptions = new UniversalFixedDescription[descriptionStrings.length];
        int i = 0;
        boolean initialCapSig = true;
        String langCode = "en";
        for (String descText : descriptionStrings) {
            if (descText != null) {
                descriptions[i] = new UniversalFixedDescription(Type3UuidFactory.forDesc(source.getUids(),
                    descTypeOrder[i].getUids(), descText), ArchitectonicAuxiliary.Concept.CURRENT.getUids(),
                    source.getUids(), initialCapSig, descTypeOrder[i].getUids(), descText, langCode);
            }
            i++;
        }
        return descriptions;
    }

}
