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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.tapi.TerminologyException;

class Sct2_ConRecord implements Comparable<Sct2_ConRecord>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    // RECORD FIELDS
    long conSnoIdL; //  id
    String effDateStr; // effectiveTime
    long timeL;
    boolean isActive; // CONCEPTSTATUS
    long statusConceptL; // extended from AttributeValue file
    String pathStr; // Module ID
    boolean isPrimitiveB; // ISPRIMITIVE

    public Sct2_ConRecord(long conIdL, String dateStr, boolean active, String path, boolean isPrim, long statusConceptL) throws ParseException {
        this.conSnoIdL = conIdL;
        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;

        /* this.pathStr = path; */
        this.pathStr = "8c230474-9f11-30ce-9cad-185a96fd03a2";
        this.isPrimitiveB = isPrim;

        this.statusConceptL = statusConceptL;
    }

    public Sct2_ConRecord(Sct2_ConRecord in, long time, long status) throws ParseException {
        this.conSnoIdL = in.conSnoIdL;
        this.effDateStr = in.effDateStr;
        this.timeL = time;
        this.isActive = in.isActive;

        this.pathStr = in.pathStr;
        this.isPrimitiveB = in.isPrimitiveB;

        this.statusConceptL = status;
    }

    static Sct2_ConRecord[] attachStatus(Sct2_ConRecord[] a, Rf2_RefsetCRecord[] b) throws ParseException {
        int idxA = 0;
        int idxB = 0;
        Arrays.sort(a);
        Arrays.sort(b);

        ArrayList<Sct2_ConRecord> addedRecords = new ArrayList<Sct2_ConRecord>();

        while (idxA < a.length && idxB < b.length) {
            // MATCHED IDS
            if (a[idxA].conSnoIdL == b[idxB].referencedComponentIdL) {
                // determine time range
                long timeRangeInL = b[idxB].timeL;
                long timeRangeOutL = Long.MAX_VALUE;
                if (idxB + 1 < b.length && a[idxA].conSnoIdL == b[idxB + 1].referencedComponentIdL) {
                    timeRangeOutL = b[idxB + 1].timeL;
                }

                // EXPAND STATUS
                if (a[idxA].timeL < timeRangeInL) {
                    idxA++; // before range, leave status unchanged
                } else if (a[idxA].timeL == timeRangeInL) {
                    if (b[idxB].isActive) {
                        a[idxA].statusConceptL = b[idxB].valueIdL;
                    }
                    idxA++;
                    idxB++;
                } else if (a[idxA].timeL > timeRangeInL && a[idxA].timeL < timeRangeOutL) {
                    if (b[idxB].isActive) {
                        a[idxA].statusConceptL = b[idxB].valueIdL;
                    }
                    idxA++;
                    idxB++;
                } else if (a[idxA].timeL >= timeRangeOutL) {
                    // ADD STATUS CHANGE EVENT
                    if (b[idxB + 1].isActive) {
                        addedRecords.add(new Sct2_ConRecord(a[idxA], b[idxB + 1].timeL, b[idxB + 1].valueIdL));
                    } else {
                        addedRecords.add(new Sct2_ConRecord(a[idxA], b[idxB + 1].timeL, Long.MAX_VALUE));
                    }
                    idxB++;
                }

                // GET NEXT IDS
            } else if (a[idxA].conSnoIdL < b[idxB].referencedComponentIdL) {
                idxA++;
            } else {
                idxB++;
            }
        }

        if (addedRecords.size() > 0) {
            int offsetI = a.length;
            a = Arrays.copyOf(a, a.length + addedRecords.size());
            for (int i = 0; i < addedRecords.size(); i++) {
                a[offsetI + i] = addedRecords.get(i);
            }
        }

        return a;
    }

    static Sct2_ConRecord[] parseConcepts(Rf2File f) throws MojoFailureException {
        try {
            int count = Rf2File.countFileLines(f);
            Sct2_ConRecord[] a = new Sct2_ConRecord[count];

            // DATA COLUMNS
            int ID = 0;// id
            int EFFECTIVE_TIME = 1; // effectiveTime
            int ACTIVE = 2; // active
            int MODULE_ID = 3; // moduleId
            int DEFINITION_STATUS_ID = 4; // definitionStatusId

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f.file), "UTF-8"));

            int idx = 0;
            br.readLine(); // Header row
            while (br.ready()) {
                String[] line = br.readLine().split(TAB_CHARACTER);

                a[idx] = new Sct2_ConRecord(Long.parseLong(line[ID]),
                        Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                        Rf2x.convertStringToBoolean(line[ACTIVE]),
                        Rf2x.convertIdToUuidStr(line[MODULE_ID]),
                        Rf2x.convertDefinitionStatusToIsPrimitive(line[DEFINITION_STATUS_ID]),
                        Long.MAX_VALUE);
                idx++;
            }

            return a;
        } catch (ParseException ex) {
            Logger.getLogger(Sct2_ConRecord.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("error parsing rf2 concepts", ex);
        } catch (IOException ex) {
            Logger.getLogger(Sct2_ConRecord.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("error parsing rf2 concepts", ex);
        }
    }

    // Create string to show some input fields for exception reporting
    @Override
    public String toString() {
        return conSnoIdL + TAB_CHARACTER + isActive + TAB_CHARACTER + isPrimitiveB;
    }

    public void writeArf(BufferedWriter writer) throws IOException, TerminologyException {
        // Concept UUID
        writer.append(Rf2x.convertIdToUuidStr(conSnoIdL) + TAB_CHARACTER);

        // Status UUID
        if (statusConceptL < Long.MAX_VALUE) {
            writer.append(Rf2x.convertIdToUuidStr(statusConceptL) + TAB_CHARACTER);
        } else {
            writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);
        }

        // Primitive string 0 (false == defined) or 1 (true == primitive)
        if (isPrimitiveB) {
            writer.append("1" + TAB_CHARACTER);
        } else {
            writer.append("0" + TAB_CHARACTER);
        }

        // Effective Date yyyy-MM-dd HH:mm:ss
        writer.append(effDateStr + TAB_CHARACTER);

        // Path UUID
        writer.append(pathStr + LINE_TERMINATOR);
    }

    @Override
    public int compareTo(Sct2_ConRecord t) {
        if (this.conSnoIdL < t.conSnoIdL) {
            return -1; // instance less than received
        } else if (this.conSnoIdL > t.conSnoIdL) {
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