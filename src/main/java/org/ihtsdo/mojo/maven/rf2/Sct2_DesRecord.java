/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.UUID;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;

class Sct2_DesRecord implements Comparable<Sct2_DesRecord>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    long desSnoIdL; // DESCRIPTIONID
    String desUuidStr; // id
    String effDateStr; // effectiveTime
    long timeL;
    boolean isActive; // STATUS
    long statusConceptL; // extended from AttributeValue file
    String pathStr;
    String conUuidStr; // CONCEPTID
    String termText; // TERM
    boolean capStatus; // INITIALCAPITALSTATUS -- capitalization
    String descriptionTypeStr; // DESCRIPTIONTYPE
    String languageCodeStr; // LANGUAGECODE

    public Sct2_DesRecord(long dId, String dateStr, boolean activeB, String path,
            String conUuidStr, String termStr,
            boolean capitalization, String desTypeStr, String langCodeStr,
            long statusConceptL) throws ParseException {
        desSnoIdL = dId;
        UUID tmpUUID = Type3UuidFactory.fromSNOMED(desSnoIdL);
        this.desUuidStr = tmpUUID.toString();
        this.effDateStr = dateStr;
        this.timeL =  Rf2x.convertDateToTime(dateStr);
        this.isActive = activeB;

        this.conUuidStr = conUuidStr; // CONCEPTID

        this.termText = termStr; // TERM
        this.capStatus = capitalization; // INITIALCAPITALSTATUS -- capitalization
        this.descriptionTypeStr = desTypeStr; // DESCRIPTIONTYPE
        this.languageCodeStr = langCodeStr; // LANGUAGECODE

        this.pathStr = path;

        this.statusConceptL = statusConceptL;
    }

    static void attachStatus(Sct2_DesRecord[] a, Rf2_RefsetCRecord[] b) {
        int idxA = 0;
        int idxB = 0;
        Arrays.sort(a);
        Arrays.sort(b);

        while (idxA < a.length && idxB < b.length) {
            if (a[idxA].desSnoIdL == b[idxB].referencedComponentIdL) {
                long timeRangeInL = b[idxB].timeL;
                long timeRangeOutL = Long.MAX_VALUE;
                if (idxB + 1 < b.length && a[idxA].desSnoIdL == b[idxB + 1].referencedComponentIdL) {
                    timeRangeOutL = b[idxB + 1].timeL;
                }

                if (a[idxA].timeL < timeRangeInL) {
                    idxA++;
                } else if (a[idxA].timeL >= timeRangeInL && a[idxA].timeL < timeRangeOutL) {
                    a[idxA].statusConceptL = b[idxB].valueIdL;
                    idxA++;
                    idxB++;
                } else {
                    idxB++;
                }

            } else if (a[idxA].desSnoIdL < b[idxB].referencedComponentIdL) {
                idxA++;
            } else {
                idxB++;
            }
        }
    }

    public static Sct2_DesRecord[] parseDescriptions(Rf2File f) throws IOException, ParseException {

        int count = Rf2File.countFileLines(f);
        Sct2_DesRecord[] a = new Sct2_DesRecord[count];

        // DATA COLUMNS
        int ID = 0; // id
        int EFFECTIVE_TIME = 1; // effectiveTime
        int ACTIVE = 2; // active
        int MODULE_ID = 3; // moduleId
        int CONCEPT_ID = 4; // conceptId
        int LANGUAGE_CODE = 5; // languageCodeStr
        int TYPE_ID = 6; // typeId
        int TERM = 7; // term
        int CASE_SIGNIFICANCE_ID = 8; // caseSignificanceId

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f.file), "UTF-8"));

        int idx = 0;
        r.readLine();  // Header row
        while (r.ready()) {
            String[] line = r.readLine().split(TAB_CHARACTER);

            a[idx] = new Sct2_DesRecord(Long.parseLong(line[ID]),
                    Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                    Rf2x.convertStringToBoolean(line[ACTIVE]),
                    Rf2x.convertIdToUuidStr(line[MODULE_ID]),
                    Rf2x.convertIdToUuidStr(line[CONCEPT_ID]),
                    line[TERM],
                    Rf2x.convertCaseSignificanceIdToCapStatus(line[CASE_SIGNIFICANCE_ID]),
                    Rf2x.convertIdToUuidStr(line[TYPE_ID]),
                    line[LANGUAGE_CODE],
                    Long.MAX_VALUE);
            idx++;
        }

        return a;
    }

    // Create string to show some input fields for exception reporting
    // DESCRIPTIONID DESCRIPTIONSTATUS CONCEPTID TERM INITIALCAPITALSTATUS DESCRIPTIONTYPE LANGUAGECODE
    public static String toStringHeader() {
        return "DESCRIPTIONID" + TAB_CHARACTER + "DESCRIPTIONSTATUS" + TAB_CHARACTER + "CONCEPTID"
                + TAB_CHARACTER + "TERM" + TAB_CHARACTER + "INITIALCAPITALSTATUS" + TAB_CHARACTER
                + "DESCRIPTIONTYPE" + TAB_CHARACTER + "LANGUAGECODE";
    }

    // Create string to show some input fields for exception reporting
    @Override
    public String toString() {
        return desUuidStr + TAB_CHARACTER + isActive + TAB_CHARACTER + conUuidStr + TAB_CHARACTER
                + termText + TAB_CHARACTER + capStatus + TAB_CHARACTER + descriptionTypeStr
                + TAB_CHARACTER + languageCodeStr;
    }

    public void writeArf(BufferedWriter writer) throws IOException, TerminologyException {
        // Description UUID
        writer.append(desUuidStr + TAB_CHARACTER);

        // Status UUID
        if (statusConceptL < Long.MAX_VALUE) {
            writer.append(Rf2x.convertIdToUuidStr(statusConceptL) + TAB_CHARACTER);
        } else {
            writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);
        }

        // Concept UUID
        writer.append(conUuidStr + TAB_CHARACTER);

        // Term
        writer.append(termText + TAB_CHARACTER);

        // Capitalization Status
        if (capStatus) {
            writer.append("1" + TAB_CHARACTER);
        } else {
            writer.append("0" + TAB_CHARACTER);
        }

        // Description Type UUID
        writer.append(descriptionTypeStr + TAB_CHARACTER);

        // Language Code
        writer.append(languageCodeStr + TAB_CHARACTER);

        // Effective Date   yyyy-MM-dd HH:mm:ss
        writer.append(effDateStr + TAB_CHARACTER);

        // Path UUID
        writer.append(pathStr + LINE_TERMINATOR);
    }

    @Override
    public int compareTo(Sct2_DesRecord t) {
        if (this.desSnoIdL < t.desSnoIdL) {
            return -1; // instance less than received
        } else if (this.desSnoIdL > t.desSnoIdL) {
            return 1; // instance greater than received
        } else {
            if (this.timeL < t.timeL) {
                return -1; // instance less than received
            } else if (this.timeL > t.timeL) {
                return 1; // instance greater than received
            }
        }
        return 0; // instance == received
    }
}
