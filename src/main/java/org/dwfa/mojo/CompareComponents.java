package org.dwfa.mojo;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_RelTuple;
import java.util.List;

/**
 * Util class to compare tuples. Does not check version or branch information.
 * Optionally will check that neither tuple is flagged (returns false if so).
 * @author Christine Hill
 *
 */
public class CompareComponents {

	public static boolean reject = true;
	public static boolean compareToFlagged(int nid, int flagged) {
		if (!reject) {
			return nid==flagged;
		} else {
			return (!(nid==flagged));
		}
	}
	
    public static boolean attributeEqual(I_ConceptAttributeTuple tuple1,
            I_ConceptAttributeTuple tuple2) {
        if ((tuple1.getConceptStatus() != tuple2.getConceptStatus())
              || (tuple1.isDefined() != tuple2.isDefined())) {
            System.out.println("Found non-matching concept tuples");
            return false;
        }
        return true;
    }

    public static boolean attributeEqual(
            I_ConceptAttributeTuple tuple1, I_ConceptAttributeTuple tuple2,
            int flaggedStatusId) {
        if (compareToFlagged(tuple1.getConceptStatus(),flaggedStatusId)) {
            return false;
        } else if (compareToFlagged(tuple2.getConceptStatus(),flaggedStatusId)) {
            return false;
        } else {
            return attributeEqual(tuple1, tuple2);
        }
    }

    
    
    public static boolean descriptionEqual(I_DescriptionTuple tuple1,
            I_DescriptionTuple tuple2) {
        if ((tuple1.getInitialCaseSignificant() != tuple2.getInitialCaseSignificant())
                  || (!tuple1.getLang().equals(tuple2.getLang())) ||
                  (tuple1.getStatusId() != tuple2.getStatusId()) ||
                  (!tuple1.getText().equals(tuple2.getText())) ||
                  (tuple1.getTypeId() != tuple2.getTypeId())) {
                System.out.println("Found non-matching desc tuples");
                return false;
        }
        return true;
    }

    public static boolean descriptionEqual(
            I_DescriptionTuple tuple1,
            I_DescriptionTuple tuple2, int flaggedStatusId) {
        if (compareToFlagged(tuple1.getStatusId(),flaggedStatusId)) {
            return false;
        } else if (compareToFlagged(tuple2.getStatusId(),flaggedStatusId)) {
            return false;
        } else {
            return descriptionEqual(tuple1, tuple2);
        }
    }

    public static boolean relationshipEqual(I_RelTuple tuple1,
            I_RelTuple tuple2) {
        if ((tuple1.getCharacteristicId() != tuple2.getCharacteristicId()) ||
              (tuple1.getGroup() != tuple2.getGroup()) ||
              (tuple1.getRefinabilityId() != tuple2.getRefinabilityId()) ||
              (tuple1.getRelTypeId() != tuple2.getRelTypeId()) ||
              (tuple1.getStatusId() != tuple2.getStatusId())) {
            System.out.println("Found non matching rel tuples");
            return false;
        }
        return true;
    }

    public static boolean relationshipEqual(I_RelTuple tuple1,
            I_RelTuple tuple2, int flaggedStatusId) {
        if (compareToFlagged(tuple1.getStatusId(),flaggedStatusId)) {
            return false;
        } else if (compareToFlagged(tuple1.getStatusId(),flaggedStatusId)) {
            return false;
        } else {
            return relationshipEqual(tuple1, tuple2);
        }
    }

    public static boolean attributeListsEqual(
            List<I_ConceptAttributeTuple> attributeList1,
            List<I_ConceptAttributeTuple> attributeList2,
            int flaggedStatusId) {
        if (attributeList1.size() != attributeList2.size()) {
            return false;
        } else {
            for (int i = 0; i < attributeList1.size(); i++) {
                if (!attributeEqual(attributeList1.get(i),
                        attributeList2.get(i), flaggedStatusId)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean descriptionListsEqual(
            List<I_DescriptionTuple> descriptionList1,
            List<I_DescriptionTuple> descriptionList2,
            int flaggedStatusId) {
        if (descriptionList1.size() != descriptionList2.size()) {
            return false;
        } else {
            for (int i = 0; i < descriptionList1.size(); i++) {
                if (!descriptionEqual(descriptionList1.get(i),
                        descriptionList2.get(i), flaggedStatusId)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean relationshipListsEqual(
            List<I_RelTuple> relationshipList1,
            List<I_RelTuple> relationshipList2,
            int flaggedStatusId) {
        if (relationshipList1.size() != relationshipList2.size()) {
            return false;
        } else {
            for (int i = 0; i < relationshipList1.size(); i++) {
                if (!relationshipEqual(relationshipList1.get(i),
                        relationshipList2.get(i), flaggedStatusId)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean attributeListsEqual(
            List<I_ConceptAttributeTuple> attributeList1,
            List<I_ConceptAttributeTuple> attributeList2) {
        if (attributeList1.size() != attributeList2.size()) {
            return false;
        } else {
            for (int i = 0; i < attributeList1.size(); i++) {
                if (!attributeEqual(attributeList1.get(i),
                        attributeList2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean descriptionListsEqual(
            List<I_DescriptionTuple> descriptionList1,
            List<I_DescriptionTuple> descriptionList2) {
        if (descriptionList1.size() != descriptionList2.size()) {
            return false;
        } else {
            for (int i = 0; i < descriptionList1.size(); i++) {
                if (!descriptionEqual(descriptionList1.get(i),
                        descriptionList2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean relationshipListsEqual(
            List<I_RelTuple> relationshipList1,
            List<I_RelTuple> relationshipList2) {
        if (relationshipList1.size() != relationshipList2.size()) {
            return false;
        } else {
            for (int i = 0; i < relationshipList1.size(); i++) {
                if (!relationshipEqual(relationshipList1.get(i),
                        relationshipList2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean componentEqual(
            List<I_ConceptAttributeTuple> attributeList1,
            List<I_ConceptAttributeTuple> attributeList2,
            List<I_DescriptionTuple> descriptionList1,
            List<I_DescriptionTuple> descriptionList2,
            List<I_RelTuple> relationshipList1,
            List<I_RelTuple> relationshipList2,
            int flaggedStatusId) {

        return (attributeListsEqual(
                    attributeList1, attributeList2, flaggedStatusId) &&
                relationshipListsEqual(
                    relationshipList1, relationshipList2, flaggedStatusId) &&
                descriptionListsEqual(
                    descriptionList1, descriptionList2, flaggedStatusId));
    }

    public static boolean componentEqual(
            List<I_ConceptAttributeTuple> attributeList1,
            List<I_ConceptAttributeTuple> attributeList2,
            List<I_DescriptionTuple> descriptionList1,
            List<I_DescriptionTuple> descriptionList2,
            List<I_RelTuple> relationshipList1,
            List<I_RelTuple> relationshipList2) {

        return (attributeListsEqual(
                    attributeList1, attributeList2) &&
                relationshipListsEqual(
                    relationshipList1, relationshipList2) &&
                descriptionListsEqual(
                    descriptionList1, descriptionList2));
    }
}
