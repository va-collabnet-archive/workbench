package org.dwfa.mojo.refset.spec;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;

/**
 * This class maps between a specified refset purpose and the associated subset type (integer value).
 * 
 * @author Christine Hill
 * 
 */
public class RefsetPurposeToSubsetTypeMap {

    protected enum SUBSET_TYPE {
        LANGUAGE_SUBSET(1, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        REALM_CONCEPT_SUBSET(2, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        REALM_DESC_SUBSET(3, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        REALM_REL_SUBSET(4, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        CONTEXT_CONCEPT_SUBSET(5, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        CONTEXT_DESC_SUBSET(6, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        NAVIGATION_SUBSET(7, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE),
        DUPLICATE_TERMS_SUBSET(8, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE);

        private SUBSET_TYPE(Integer i, I_ConceptualizeUniversally concept) {
            try {
                refsetPurposeNid = concept.localize().getNid();
            } catch (TerminologyException e) {
                throw new RuntimeException(this.toString(), e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            subsetTypeId = i;
        }

        private int refsetPurposeNid;
        private int subsetTypeId;

        private int getRefsetPurposeNid() {
            return refsetPurposeNid;
        }

        private int getSubsetTypeId() {
            return subsetTypeId;
        }
    };

    public static int convert(I_GetConceptData refsetPurpose) {
        SUBSET_TYPE subsetType = null;

        for (SUBSET_TYPE currentType : SUBSET_TYPE.values()) {
            if (currentType.getRefsetPurposeNid() == refsetPurpose.getConceptId()) {
                subsetType = currentType;
                break;
            }
        }

        if (subsetType != null) {
            return subsetType.getSubsetTypeId();
        } else {
            return -1;
        }

    }

}
