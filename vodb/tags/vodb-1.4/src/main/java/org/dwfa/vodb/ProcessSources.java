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
package org.dwfa.vodb;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.ProcessAceFormatSources.FORMAT;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessSources {

    public abstract Logger getLog();

    boolean skipFirstLine;

    private List<Date> releaseDates = new ArrayList<Date>();

    protected ThinVersionHelper vh = new ThinVersionHelper();

    public ProcessSources(boolean skipFirstLine) throws DatabaseException {
        super();
        this.skipFirstLine = skipFirstLine;
    }

    public void readLicitWords(Reader br) throws IOException {
        long start = System.currentTimeMillis();
        int licitWords = 0;
        StreamTokenizer st = new StreamTokenizer(br);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);

        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            writeLicitWord(st.sval);
            licitWords++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed licit words: " + licitWords);
    }

    public abstract void writeLicitWord(String word) throws IOException;

    public abstract void writeIllicitWord(String word) throws IOException;

    public abstract void optimizeLicitWords() throws IOException;

    public void readIllicitWords(Reader br) throws Exception {
        long start = System.currentTimeMillis();
        int illicitWords = 0;
        StreamTokenizer st = new StreamTokenizer(br);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            writeIllicitWord(st.sval);
            illicitWords++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info(
                      "Process time: " + (System.currentTimeMillis() - start) + " Parsed illicit words: "
                              + illicitWords);
    }

    protected void readConcepts(Reader r, Date releaseDate, FORMAT format) throws Exception {
        switch (format) {
        case SNOMED:
            processSnomedFormatConcepts(r, releaseDate);
            break;
        case ACE:
            processAceFormatConcepts(r);
            break;

        default:
            throw new Exception("Unsupported format: " + format);
        }
    }

    private void processAceFormatConcepts(Reader r) throws IOException, Exception {

        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int concepts = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            Object conceptKey = getId(st);
            // CONCEPTSTATUS
            tokenType = st.nextToken();
            Object conceptStatus = getStatus(st);
            // ISPRIMITIVE
            tokenType = st.nextToken();
            // convert to "defined"
            boolean defChar = !parseBoolean(st);

            tokenType = st.nextToken();
            Date statusDate = getDate(st);

            tokenType = st.nextToken();
            Object pathId = getId(st);

            writeConcept(statusDate, conceptKey, conceptStatus, defChar, Arrays.asList(new Object[] { pathId }));
            concepts++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed concepts: " + concepts);
    }

    public void readIds(Reader r) throws IOException, Exception {

        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int ids = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            if (st.sval.equals("Primary UUID")) {
                AceLog.getAppLog().warning("Unexpected data encounterd in id stream: " + st.sval);
                // go to CR or LF
                tokenType = st.nextToken();
                while (tokenType != 13 && tokenType != 10) { // is CR
                    // LF
                    tokenType = st.nextToken();
                }

                // Beginning of loop
                tokenType = st.nextToken();
                while (tokenType == 10) {
                    tokenType = st.nextToken();
                }
            } else {
                UUID primaryUuid = (UUID) getId(st);
                tokenType = st.nextToken();
                UUID sourceSystemUuid = (UUID) getId(st);
                tokenType = st.nextToken();
                Object sourceId = st.sval;
                if (st.sval.length() == 36) {
                    sourceId = getId(st);
                }
                tokenType = st.nextToken();
                UUID statusUuid = (UUID) getId(st);
                tokenType = st.nextToken();
                Date statusDate = new Date();
                if (st.sval.equals("null") == false) {
                    statusDate = getDate(st);
                }

                UUID pathUuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next();
                
                tokenType = st.nextToken();
                if ((tokenType != 13) && (tokenType != 10)) {
                    pathUuid = (UUID) getId(st);
                    tokenType = st.nextToken();
                }

                writeId(primaryUuid, sourceSystemUuid, sourceId, statusUuid, statusDate, pathUuid);
                ids++;

                // CR or LF
                if (tokenType == 13) { // is CR
                    // LF
                    tokenType = st.nextToken();
                }

                // Beginning of loop
                tokenType = st.nextToken();
                while (tokenType == 10) {
                    tokenType = st.nextToken();
                }
            }

        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed ids: " + ids);
    }

    public abstract void writeId(UUID primaryUuid, UUID sourceSystemUuid, Object sourceId, UUID statusUuid,
        Date statusDate, UUID pathUuid) throws Exception;

    private void processSnomedFormatConcepts(Reader r, Date releaseDate) throws IOException, Exception {
        // CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
        // ISPRIMITIVE
        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int concepts = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            Object conceptKey = getId(st);
            // CONCEPTSTATUS
            tokenType = st.nextToken();
            Object conceptStatus = getStatus(st);
            // FULLYSPECIFIEDNAME
            // Present in the descriptions table, so can ignore
            tokenType = st.nextToken();
            // CTV3ID
            // Do nothing with the legacy CTV3ID
            tokenType = st.nextToken();
            // SNOMEDID
            // Do nothing with the legacy SNOMED id
            tokenType = st.nextToken();
            // ISPRIMITIVE
            tokenType = st.nextToken();
            // convert to "defined"
            boolean defChar = !parseBoolean(st);
            writeConcept(releaseDate, conceptKey, conceptStatus, defChar,
                         ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
            concepts++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed SNOMED concepts: " + concepts);
    }

    protected void readRelationships(Reader r, Date releaseDate, FORMAT format) throws Exception {
        switch (format) {
        case SNOMED:
            processSnomedFormatRelationships(r, releaseDate);
            break;
        case ACE:
            processAceFormatRelationships(r);
            break;

        default:
            throw new Exception("Unsupported format: " + format);
        }
    }

    private void processAceFormatRelationships(Reader r) throws IOException, Exception {
        // RELATIONSHIPID
        // STATUSID
        // CONCEPTID1
        // RELATIONSHIPTYPE
        // CONCEPTID2
        // CHARACTERISTICTYPE
        // REFINABILITY
        // RELATIONSHIPGROUP

        long start = System.currentTimeMillis();
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int rels = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            // RELATIONSHIPID
            Object relID = getId(st);
            // STATUSID
            tokenType = st.nextToken();
            Object statusId = getId(st);
            // CONCEPTID1
            tokenType = st.nextToken();
            Object conceptOneID = getId(st);
            // RELATIONSHIPTYPE
            tokenType = st.nextToken();
            Object relationshipTypeConceptID = getId(st);
            // CONCEPTID2
            tokenType = st.nextToken();
            Object conceptTwoID = getId(st);
            // CHARACTERISTICTYPE
            tokenType = st.nextToken();
            Object characteristic = getCharacteristic(st);
            // REFINABILITY
            tokenType = st.nextToken();
            Object refinability = getRefinability(st);
            // RELATIONSHIPGROUP
            tokenType = st.nextToken();
            int group = Integer.parseInt(st.sval);

            tokenType = st.nextToken();
            Date statusDate = getDate(st);

            tokenType = st.nextToken();
            Object pathId = getId(st);

            writeRelationship(statusDate, relID, Arrays.asList(new Object[] { statusId }), conceptOneID,
                              relationshipTypeConceptID, conceptTwoID, characteristic, refinability, group, Arrays
                                      .asList(new Object[] { pathId }));
            rels++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed relationsips: " + rels);
    }

    private void processSnomedFormatRelationships(Reader r, Date releaseDate) throws IOException, Exception {
        // RELATIONSHIPID
        // CONCEPTID1
        // RELATIONSHIPTYPE
        // CONCEPTID2
        // CHARACTERISTICTYPE
        // REFINABILITY
        // RELATIONSHIPGROUP

        long start = System.currentTimeMillis();
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int rels = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOF) {
            // RELATIONSHIPID
            Object relID = getId(st);
            // CONCEPTID1
            tokenType = st.nextToken();
            Object conceptOneID = getId(st);
            // RELATIONSHIPTYPE
            tokenType = st.nextToken();
            Object relationshipTypeConceptID = getId(st);
            // CONCEPTID2
            tokenType = st.nextToken();
            Object conceptTwoID = getId(st);
            // CHARACTERISTICTYPE
            tokenType = st.nextToken();
            Object characteristic = getCharacteristic(st);
            // REFINABILITY
            tokenType = st.nextToken();
            Object refinability = getRefinability(st);
            // RELATIONSHIPGROUP
            tokenType = st.nextToken();
            int group = Integer.parseInt(st.sval);

            writeRelationship(releaseDate, relID, ArchitectonicAuxiliary.Concept.CURRENT.getUids(), conceptOneID,
                              relationshipTypeConceptID, conceptTwoID, characteristic, refinability, group,
                              ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
            rels++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed relationsips: " + rels);
    }

    protected abstract Object getId(StreamTokenizer st);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    protected Date getDate(StreamTokenizer st) throws ParseException {
        if (st.sval.contains("-")) {
            return formatter.parse(st.sval);
        } else {
            return formatter2.parse(st.sval);
        }
    }

    protected void readDescriptions(Reader r, Date releaseDate, FORMAT format) throws Exception {
        switch (format) {
        case SNOMED:
            processSnomedFormatDescriptions(r, releaseDate);
            break;
        case ACE:
            processAceFormatDescriptions(r);
            break;

        default:
            throw new Exception("Unsupported format: " + format);
        }
    }

    private void processAceFormatDescriptions(Reader r) throws IOException, Exception {
        // DESCRIPTIONID
        // DESCRIPTIONSTATUS
        // CONCEPTID
        // TERM
        // INITIALCAPITALSTATUS
        // DESCRIPTIONTYPE
        // LANGUAGECODE
        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int descriptions = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();

        while (tokenType != StreamTokenizer.TT_EOF) {
            // DESCRIPTIONID
            Object descriptionId = getId(st);

            // DESCRIPTIONSTATUS
            tokenType = st.nextToken();

            Object status = getStatus(st);
            // CONCEPTID
            tokenType = st.nextToken();
            Object conceptId = getId(st);
            // TERM
            tokenType = st.nextToken();
            String text = st.sval;
            // INITIALCAPITALSTATUS
            tokenType = st.nextToken();
            boolean capSignificant = parseBoolean(st);

            // DESCRIPTIONTYPE
            tokenType = st.nextToken();
            Object typeInt = getDescType(st);

            // LANGUAGECODE
            tokenType = st.nextToken();
            String lang = st.sval;

            tokenType = st.nextToken();
            Date statusDate = getDate(st);

            tokenType = st.nextToken();
            Object pathId = getId(st);

            writeDescription(statusDate, descriptionId, status, conceptId, text, capSignificant, typeInt, lang, Arrays
                    .asList(new Object[] { pathId }));
            descriptions++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog()
                .info("Process time: " + (System.currentTimeMillis() - start) + " Parsed descriptions: " + descriptions);
    }

    private void processSnomedFormatDescriptions(Reader r, Date releaseDate) throws IOException, Exception {
        // DESCRIPTIONID
        // DESCRIPTIONSTATUS
        // CONCEPTID
        // TERM
        // INITIALCAPITALSTATUS
        // DESCRIPTIONTYPE
        // LANGUAGECODE
        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int descriptions = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();

        while (tokenType != StreamTokenizer.TT_EOF) {
            // DESCRIPTIONID
            Object descriptionId = getId(st);

            // DESCRIPTIONSTATUS
            tokenType = st.nextToken();

            Object status = getStatus(st);
            // CONCEPTID
            tokenType = st.nextToken();
            Object conceptId = getId(st);
            // TERM
            tokenType = st.nextToken();
            String text = st.sval;
            // INITIALCAPITALSTATUS
            tokenType = st.nextToken();
            boolean capSignificant = parseBoolean(st);

            // DESCRIPTIONTYPE
            tokenType = st.nextToken();
            Object typeInt = getDescType(st);

            // LANGUAGECODE
            tokenType = st.nextToken();
            String lang = st.sval;

            writeDescription(releaseDate, descriptionId, status, conceptId, text, capSignificant, typeInt, lang,
                             ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
            descriptions++;

            // CR or LF
            tokenType = st.nextToken();
            if (tokenType == 13) { // is CR
                // LF
                tokenType = st.nextToken();
            }

            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog()
                .info("Process time: " + (System.currentTimeMillis() - start) + " Parsed descriptions: " + descriptions);
    }

    private boolean parseBoolean(StreamTokenizer st) {
        if (st.sval.equals("1")) {
            return true;
        }
        if (st.sval.toLowerCase().startsWith("t")) {
            return true;
        }
        return false;
    }

    protected abstract Object getStatus(StreamTokenizer st);

    protected abstract Object getDescType(StreamTokenizer st);

    protected abstract Object getRefinability(StreamTokenizer st);

    protected abstract Object getCharacteristic(StreamTokenizer st);

    private void skipLineOne(StreamTokenizer st) throws IOException {
        if (skipFirstLine) {
            int tokenType = st.nextToken();
            while (tokenType != StreamTokenizer.TT_EOL) {
                tokenType = st.nextToken();
            }
        }
    }

    public void addReleaseDate(Date releaseDate) {
        releaseDates.add(releaseDate);
    }

    public int[] getReleaseDates() {
        int[] releases = new int[releaseDates.size()];
        int i = 0;
        for (Date rdate : releaseDates) {
            releases[i] = ThinVersionHelper.convert(rdate.getTime());
            i++;
        }
        return releases;
    }

    public abstract void execute(File snomedDir) throws Exception;

    public abstract void cleanup(I_IntSet relsToIgnore) throws Exception;

    public abstract void writeConcept(Date releaseDate, Object conceptKey, Object conceptStatus, boolean defChar,
        Object pathId) throws Exception;

    public abstract void writeRelationship(Date releaseDate, Object relID, Object statusId, Object conceptOneID,
        Object relationshipTypeConceptID, Object conceptTwoID, Object characteristic, Object refinability, int group,
        Object pathId) throws Exception;

    public abstract void writeDescription(Date releaseDate, Object descriptionId, Object status, Object conceptId,
        String text, boolean capStatus, Object typeInt, String lang, Object pathId) throws Exception;
}
