package org.dwfa.maven.sct;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;

class SctXConRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    private static final UUID uuidSourceCtv3 = ArchitectonicAuxiliary.Concept.CTV3_ID.getUids()
            .iterator().next();
    private static final UUID uuidSourceSnomedRt = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID
            .getUids().iterator().next();

    // RECORD FIELDS
    long id; // CONCEPTID
    int status; // CONCEPTSTATUS
    String ctv3id; // CTV3ID
    String snomedrtid; // SNOMEDID (SNOMED RT ID)
    int isprimitive; // ISPRIMITIVE
    int xPath;
    int xRevision;

    public SctXConRecord(long i, int s, String ctv, String rt, int p) {
        id = i;
        status = s;
        ctv3id = ctv;
        snomedrtid = rt;
        isprimitive = p;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctXConRecord tmp = (SctXConRecord) obj;
        if (this.id < tmp.id) {
            return -1; // instance less than received
        } else if (this.id > tmp.id) {
            return 1; // instance greater than received
        } else {
            if (this.xPath < tmp.xPath) {
                return -1; // instance less than received
            } else if (this.xPath > tmp.xPath) {
                return 1; // instance greater than received
            } else {
                if (this.xRevision < tmp.xRevision) {
                    return -1; // instance less than received
                } else if (this.xRevision > tmp.xRevision) {
                    return 1; // instance greater than received
                } else {
                    return 0; // instance == received
                }
            }
        }
    }

    // Create string to show some input fields for exception reporting
    public String toString() {
        return id + TAB_CHARACTER + status + TAB_CHARACTER + isprimitive + LINE_TERMINATOR;
    }

    // Create string for concepts.txt file
    public String toStringArf(String date, String path) throws IOException, TerminologyException {

        UUID u = Type3UuidFactory.fromSNOMED(id);

        return u + TAB_CHARACTER + status + TAB_CHARACTER + isprimitive + TAB_CHARACTER + date
                + TAB_CHARACTER + path + LINE_TERMINATOR;
    }

    // Create string for ids.txt file
    public String toIdsTxt(String source, String date, String path) throws IOException,
            TerminologyException {

        String outputStr;
        UUID u = Type3UuidFactory.fromSNOMED(id);

        // STATUS FOR IDs IS SET TO CURRENT '0'
        outputStr = u // (canonical) primary uuid
                + TAB_CHARACTER + source // (canonical UUID) source system
                // uuid
                + TAB_CHARACTER + id // (original primary) source id
                // + TAB_CHARACTER + getStatusString(status) -- PARSED
                // STATUS
                // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                + TAB_CHARACTER + status // (canonical) status
                // uuid
                + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical)
        // path
        // uuid

        if (ctv3id != null) {
            outputStr = outputStr + u // (canonical) primary uuid
                    + TAB_CHARACTER + uuidSourceCtv3 // (canonical UUID)
                    // source
                    // system uuid
                    + TAB_CHARACTER + ctv3id // (original primary) source id
                    // + TAB_CHARACTER + getStatusString(status) -- PARSED
                    // STATUS
                    // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                    + TAB_CHARACTER + status // (canonical)
                    // status
                    // uuid
                    + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective
                    // date
                    + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical)
            // path uuid
        }
        if (snomedrtid != null) {
            outputStr = outputStr + u // (canonical) primary uuid
                    + TAB_CHARACTER + uuidSourceSnomedRt // (canonical UUID)
                    // source
                    // system uuid
                    + TAB_CHARACTER + snomedrtid // (original primary)
                    // source id
                    // + TAB_CHARACTER + getStatusString(status) -- PARSED
                    // STATUS
                    // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                    + TAB_CHARACTER + status // (canonical)
                    // status
                    // uuid
                    + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective
                    // date
                    + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical)
            // path uuid
        }
        return outputStr;
    }
}
