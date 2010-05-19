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
package org.dwfa.ace.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

/**
 * Generates the conflict resolution report.
 */
public class ConflictReportWriter implements Runnable {

    public static enum RESOLUTION {
        INCLUDE, EXCLUDE
    };

    private static enum FIELD_NAME {
        INCL_TYPE_A, INCL_TYPE_B, RESOLUTION
    };

    private static final String reportFilePrefix = "conflicts-";
    private static final String reportFilePostfix = ".txt";

    private File reportFile;

    private I_GetConceptData refset;

    private Map<Integer, I_GetConceptData> conceptCache;

    private I_TermFactory termFactory;

    private ConflictMap conflictMap = new ConflictMap();

    public ConflictReportWriter(String parentDir, I_GetConceptData refsetConcept) {
        this.refset = refsetConcept;
        reportFile = new File(parentDir, prepareFilename(refset.toString()));
        reportFile.getParentFile().mkdirs();
        termFactory = Terms.get();
    }

    /**
     * Allows an external cache to be utilised by this class
     */
    public void setConceptCache(Map<Integer, I_GetConceptData> conceptCache) {
        this.conceptCache = conceptCache;
    }

    public void addConflict(int subjectId, int conceptAId, int conceptAInclTypeId, int conceptBId,
            int conceptBInclTypeId, RESOLUTION resolution) {
        FieldMap fields = new FieldMap();
        fields.put(FIELD_NAME.INCL_TYPE_A, conceptAInclTypeId);
        fields.put(FIELD_NAME.INCL_TYPE_B, conceptBInclTypeId);
        fields.put(FIELD_NAME.RESOLUTION, resolution);

        if (conflictMap.containsKey(conceptAId)) {
            Conflict conflict = conflictMap.get(conceptAId);
            if (conflict.containsKey(conceptBId)) {
                Subject subject = conflict.get(conceptBId);
                if (subject.containsKey(subjectId)) {
                    subject.get(subjectId).add(fields);
                } else {
                    // Never encountered subject before
                    ArrayList<FieldMap> fieldMap = new ArrayList<FieldMap>();
                    fieldMap.add(fields);
                    subject.put(subjectId, fieldMap);
                }
            } else {
                // Never encountered B before
                Subject subject = new Subject();
                ArrayList<FieldMap> fieldMap = new ArrayList<FieldMap>();
                fieldMap.add(fields);
                subject.put(subjectId, fieldMap);
                conflict.put(conceptBId, subject);
            }
        } else {
            // Never encountered A before
            Conflict conflict = new Conflict();
            Subject subject = new Subject();
            ArrayList<FieldMap> fieldMap = new ArrayList<FieldMap>();
            fieldMap.add(fields);
            subject.put(subjectId, fieldMap);
            conflict.put(conceptBId, subject);
            conflictMap.put(conceptAId, conflict);
        }
    }

    public void run() {

        BufferedWriter reportWriter = null;
        try {
            String timestamp = new Date().toString();

            reportWriter = new BufferedWriter(new FileWriter(reportFile));
            reportWriter.write("Conflicts in refset " + refset + " at " + timestamp + " are: ");
            reportWriter.newLine();
            reportWriter.newLine();

            for (Integer conceptAId : conflictMap.keySet()) {
                int conflictCount = 0;
                StringBuffer conflictReport = new StringBuffer();

                Conflict conflicts = conflictMap.get(conceptAId);
                conflictReport.append(" contradictions because of \"" + getConcept(conceptAId) + "\"\n");
                for (Integer conceptBId : conflicts.keySet()) {
                    Subject subjectMap = conflicts.get(conceptBId);
                    conflictReport.append("\t" + subjectMap.size() + " contradicts  \"" + getConcept(conceptBId)
                        + "\"\n");
                    conflictCount += subjectMap.size();
                    for (Integer subjectId : subjectMap.keySet()) {
                        ArrayList<FieldMap> fields = subjectMap.get(subjectId);
                        for (FieldMap fieldMap : fields) {
                            conflictReport.append("\t\t* Resolved to "
                                + ((fieldMap.get(FIELD_NAME.RESOLUTION) == RESOLUTION.INCLUDE) ? "include: "
                                                                                              : "exclude: "));
                            conflictReport.append("\"" + getConcept(subjectId) + "\": ["
                                + getConcept((Integer) fieldMap.get(FIELD_NAME.INCL_TYPE_A)) + "]:\""
                                + getConcept(conceptAId));
                            conflictReport.append("\" contradicts  ["
                                + getConcept((Integer) fieldMap.get(FIELD_NAME.INCL_TYPE_B)) + "]:\""
                                + getConcept(conceptBId) + "\"\n");
                        }
                    }
                }

                conflictReport.append("\n");
                conflictReport.insert(0, conflictCount);
                reportWriter.write(conflictReport.toString());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reportWriter != null) {
                    reportWriter.flush();
                    reportWriter.close();
                }
            } catch (IOException ex) {
            }
            ;
        }
    }

    private I_GetConceptData getConcept(int id) throws TerminologyException, IOException {
        I_GetConceptData concept = null;
        if (conceptCache != null) {
            concept = conceptCache.get(id);
        }
        if (concept == null) {
            concept = termFactory.getConcept(id);
            conceptCache.put(id, concept);
        }
        return concept;
    }

    private String prepareFilename(String refsetname) {
        return reportFilePrefix + refsetname.replace(' ', '-').replace(File.separatorChar, '-') + reportFilePostfix;
    }

    /**
     * Maps one concept with another it  contradicts.
     * The id of the concept is the index for the grouping level in the report.
     */
    private class ConflictMap extends HashMap<Integer, Conflict> {
    }

    /** Maps a conflicted concept with a number of subjects */
    private class Conflict extends HashMap<Integer, Subject> {
    }

    /**
     * The subject is target to be resolved from two other conflicting concepts.
     * Provides a collection of Fields (name-value pairs) for a concept.
     */
    private class Subject extends HashMap<Integer, ArrayList<FieldMap>> {
    }

    /**
     * A name-value pair of attributes to be associated with a conflict subject.
     */
    private class FieldMap extends HashMap<FIELD_NAME, Object> {
    }
}
