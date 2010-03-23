package org.dwfa.maven.sct;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;

class SctXRelRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    
    private static final String uuidPathSnomedCore = ArchitectonicAuxiliary.Concept.SNOMED_CORE
    .getUids().iterator().next().toString();
    
    // :yyy: private UUID uuid; // COMPUTED RELATIONSHIPID
    long uuidMostSigBits;
    long uuidLeastSigBits;
    long id; // SNOMED RELATIONSHIPID, if applicable
    int status; // status is computed for relationships
    long conceptOneID; // CONCEPTID1
    long roleType; // RELATIONSHIPTYPE
    long conceptTwoID; // CONCEPTID2
    int characteristic; // CHARACTERISTICTYPE
    int refinability; // REFINABILITY
    int group; // RELATIONSHIPGROUP
    boolean exceptionFlag; // to handle Concept ID change exception
    int xPath;
    int xRevision;

    public SctXRelRecord(long relID, int st, long cOneID, long relType, long cTwoID,
            int characterType, int r, int grp) {
        id = relID; // RELATIONSHIPID
        status = st; // status is computed for relationships
        conceptOneID = cOneID; // CONCEPTID1
        roleType = relType; // RELATIONSHIPTYPE
        conceptTwoID = cTwoID; // CONCEPTID2
        characteristic = characterType; // CHARACTERISTICTYPE
        refinability = r; // REFINABILITY
        group = grp; // RELATIONSHIPGROUP
        exceptionFlag = false;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctXRelRecord tmp = (SctXRelRecord) obj;
        // :yyy: return this.uuid.compareTo(tmp.uuid);
        int thisMore = 1;
        int thisLess = -1;
        if (uuidMostSigBits > tmp.uuidMostSigBits) {
            return thisMore;
        } else if (uuidMostSigBits < tmp.uuidMostSigBits) {
            return thisLess;
        } else {
            if (uuidLeastSigBits > tmp.uuidLeastSigBits) {
                return thisMore;
            } else if (uuidLeastSigBits < tmp.uuidLeastSigBits) {
                return thisLess;
            } else {
                if (this.xPath > tmp.xPath) {
                    return thisMore;
                } else if (this.xPath < tmp.xPath) {
                    return thisLess;
                } else {
                    if (this.xRevision > tmp.xRevision) {
                        return thisMore;
                    } else if (this.xRevision < tmp.xRevision) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }

    // Create string to show some input fields for exception reporting
    public String toString() {
        UUID uuid = new UUID(uuidMostSigBits, uuidLeastSigBits); // :yyy:
        return uuid + TAB_CHARACTER + id + TAB_CHARACTER + status + TAB_CHARACTER
                + conceptOneID + TAB_CHARACTER + roleType + TAB_CHARACTER + conceptTwoID
                + LINE_TERMINATOR;
    }

    // Create output string for arf relationships.txt file
    public String toStringArf(String date, String path) throws IOException,
            TerminologyException {

        UUID cOne = Type3UuidFactory.fromSNOMED(conceptOneID);
        UUID relType = Type3UuidFactory.fromSNOMED(roleType);
        UUID cTwo = Type3UuidFactory.fromSNOMED(conceptTwoID);

        String chType = ArchitectonicAuxiliary.getSnomedCharacteristicType(characteristic)
                .getUids().iterator().next().toString();
        String reType = ArchitectonicAuxiliary.getSnomedRefinabilityType(refinability)
                .getUids().iterator().next().toString();

        UUID uuid = new UUID(uuidMostSigBits, uuidLeastSigBits); // :yyy:
        return uuid + TAB_CHARACTER // relationship uuid
                + status + TAB_CHARACTER // status uuid

                + cOne + TAB_CHARACTER // source concept uuid
                + relType + TAB_CHARACTER // relationship type uuid
                + cTwo + TAB_CHARACTER // destination concept uuid

                + chType + TAB_CHARACTER // characteristic type uuid
                + reType + TAB_CHARACTER // refinability uuid

                + group + TAB_CHARACTER // relationship group -- integer
                + date + TAB_CHARACTER + path + LINE_TERMINATOR;
    }

    // Create string for ids.txt file
    public String toIdsTxt(String source, String date, String path) throws IOException,
            TerminologyException {

        // NOTE: Path is SNOMED Core. Not inferred. Not stated. Just core.
        UUID uuid = new UUID(uuidMostSigBits, uuidLeastSigBits); // :yyy:
        return uuid // (canonical) primary uuid
                + TAB_CHARACTER + source // (canonical UUID) source system
                // uuid
                + TAB_CHARACTER + id // (original primary) source id
                // + TAB_CHARACTER + getStatusString(status) -- PARSED
                // STATUS
                // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                + TAB_CHARACTER + status // (canonical) status
                // uuid
                + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                + TAB_CHARACTER + uuidPathSnomedCore + LINE_TERMINATOR; // (canonical)
        // path
        // uuid
    }
}
