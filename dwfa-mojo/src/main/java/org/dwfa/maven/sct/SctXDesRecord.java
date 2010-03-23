package org.dwfa.maven.sct;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;

class SctXDesRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";

    long id; // DESCRIPTIONID
    int status; // DESCRIPTIONSTATUS
    long conceptId; // CONCEPTID
    String termText; // TERM
    int capStatus; // INITIALCAPITALSTATUS -- capitalization
    int descriptionType; // DESCRIPTIONTYPE
    String languageCode; // LANGUAGECODE
    int xPath;
    int xRevision;

    public SctXDesRecord(long dId, int s, long cId, String text, int cStat, int typeInt, String lang) {
        id = dId;
        status = s;
        conceptId = cId;
        termText = new String(text);
        capStatus = cStat;
        descriptionType = typeInt;
        languageCode = new String(lang);
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctXDesRecord tmp = (SctXDesRecord) obj;
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
        return id + TAB_CHARACTER + status + TAB_CHARACTER + conceptId + TAB_CHARACTER + termText
                + TAB_CHARACTER + capStatus + TAB_CHARACTER + descriptionType + TAB_CHARACTER
                + languageCode + LINE_TERMINATOR;
    }

    // Create string for descriptions.txt file
    public String toStringArf(String date, String path) throws IOException, TerminologyException {

        UUID u = Type3UuidFactory.fromSNOMED(id);
        UUID c = Type3UuidFactory.fromSNOMED(conceptId);

        String descType = ArchitectonicAuxiliary.getSnomedDescriptionType(descriptionType)
                .getUids().iterator().next().toString();

        return u + TAB_CHARACTER // description uuid
                + status + TAB_CHARACTER // status uuid
                + c + TAB_CHARACTER // concept uuid
                + termText + TAB_CHARACTER // term
                + capStatus + TAB_CHARACTER // capitalization status
                + descType + TAB_CHARACTER // description type uuid
                + languageCode + TAB_CHARACTER // language code
                + date + TAB_CHARACTER // effective date
                + path + LINE_TERMINATOR; // path uuid
    }

    // Create string for ids.txt file
    public String toIdsTxt(String source, String date, String path) throws IOException,
            TerminologyException {

        UUID u = Type3UuidFactory.fromSNOMED(id);

        // STATUS IS SET TO
        return u // (canonical) primary uuid
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
    }
}
