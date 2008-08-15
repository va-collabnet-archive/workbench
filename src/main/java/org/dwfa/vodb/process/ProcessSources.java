package org.dwfa.vodb.process;

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
import java.util.Arrays;
import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.process.ProcessAceFormatSources.FORMAT;
import org.dwfa.vodb.types.IntSet;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessSources {

    public abstract Logger getLog();

    boolean skipFirstLine;

    private TreeSet<Date> releaseDates = new TreeSet<Date>();

    protected ThinVersionHelper vh = new ThinVersionHelper();

    public ProcessSources(boolean skipFirstLine) throws DatabaseException {
        super();
        this.skipFirstLine = skipFirstLine;
    }

    protected void readConcepts(Reader r, Date releaseDate, FORMAT format, CountDownLatch conceptLatch) throws Exception {
        switch (format) {
        case SNOMED:
            processSnomedFormatConcepts(r, releaseDate, conceptLatch);
            break;
        case ACE:
            processAceFormatConcepts(r, conceptLatch);
            break;

        default:
            throw new Exception("Unsupported format: " + format);
        }
    }

    private void processAceFormatConcepts(Reader r, CountDownLatch conceptLatch) throws IOException, Exception {

        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int concepts = 0;

        skipLineOne(st, conceptLatch);
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
            conceptLatch.countDown();

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

    public void readIds(Reader r, CountDownLatch idLatch) throws IOException, Exception {

        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int ids = 0;
        UUID pathUuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next();

        skipLineOne(st, idLatch);
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

                idLatch.countDown();
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

    private void processSnomedFormatConcepts(Reader r, Date releaseDate, CountDownLatch conceptLatch) throws IOException, Exception {
        // CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
        // ISPRIMITIVE
        long start = System.currentTimeMillis();

        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int concepts = 0;

        skipLineOne(st, conceptLatch);
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
            conceptLatch.countDown();

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

    protected void readRelationships(Reader r, Date releaseDate, FORMAT format, CountDownLatch relationshipLatch) throws Exception {
        switch (format) {
        case SNOMED:
            processSnomedFormatRelationships(r, releaseDate, relationshipLatch);
            break;
        case ACE:
            processAceFormatRelationships(r, relationshipLatch);
            break;

        default:
            throw new Exception("Unsupported format: " + format);
        }
    }

    private void processAceFormatRelationships(Reader r, CountDownLatch relationshipLatch) throws IOException, Exception {
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

        skipLineOne(st, relationshipLatch);
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
            relationshipLatch.countDown();
            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed relationsips: " + rels);
    }

    private void processSnomedFormatRelationships(Reader r, Date releaseDate, CountDownLatch relationshipLatch) throws IOException, Exception {
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

        skipLineOne(st, relationshipLatch);
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

            relationshipLatch.countDown();
            
            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed relationsips: " + rels);
    }

    protected abstract Object getId(StreamTokenizer st);
    
    private class DateFormatterThreadLocal extends ThreadLocal<SimpleDateFormat> {
        String formatStr;

        private DateFormatterThreadLocal(String formatStr) {
            super();
            this.formatStr = formatStr;
        }

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(formatStr);
        }

        
        
    }

    DateFormatterThreadLocal formatter = new DateFormatterThreadLocal("yyyy-MM-dd HH:mm:ss");

    DateFormatterThreadLocal formatter2 = new DateFormatterThreadLocal("yyyyMMdd HH:mm:ss");

    protected Date getDate(StreamTokenizer st) throws ParseException {
        if (st.sval.contains("-")) {
            return formatter.get().parse(st.sval);
        } else {
            return formatter2.get().parse(st.sval);
        }
    }

    protected void readDescriptions(Reader r, Date releaseDate, FORMAT format, CountDownLatch descriptionLatch) throws Exception {
        switch (format) {
        case SNOMED:
            processSnomedFormatDescriptions(r, releaseDate, descriptionLatch);
            break;
        case ACE:
            processAceFormatDescriptions(r, descriptionLatch);
            break;

        default:
            throw new Exception("Unsupported format: " + format);
        }
    }

    private void processAceFormatDescriptions(Reader r, CountDownLatch descriptionLatch) throws IOException, Exception {
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

        skipLineOne(st, descriptionLatch);
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
            descriptionLatch.countDown();
            System.out.println("count down description latch. up to description " + descriptions + "Latch: " + descriptionLatch.getCount());
            // Beginning of loop
            tokenType = st.nextToken();
        }
        getLog()
                .info("Process time: " + (System.currentTimeMillis() - start) + " Parsed descriptions: " + descriptions);
    }

    private void processSnomedFormatDescriptions(Reader r, Date releaseDate, CountDownLatch descriptionLatch) throws IOException, Exception {
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

        skipLineOne(st, descriptionLatch);
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

            descriptionLatch.countDown();
            System.out.println("count down description latch. up to description " + descriptions + "Latch: " + descriptionLatch.getCount());
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

    protected void skipLineOne(StreamTokenizer st, CountDownLatch latch) throws IOException {
        if (skipFirstLine) {
            int tokenType = st.nextToken();
            while (tokenType != StreamTokenizer.TT_EOL) {
                tokenType = st.nextToken();
            }
            latch.countDown();
        }
    }

    public void addReleaseDate(Date releaseDate) {
        releaseDates.add(releaseDate);
    }

    public I_IntSet getReleaseDates() {
        IntSet intSet = new IntSet();
        for (Date rdate : releaseDates) {
        	intSet.add(ThinVersionHelper.convert(rdate.getTime()));
        }
        return intSet;
    }

    public abstract void execute(File snomedDir) throws Exception;

    public abstract void cleanupSNOMED(I_IntSet relsToIgnore) throws Exception;

    public abstract void writeConcept(Date releaseDate, Object conceptKey, Object conceptStatus, boolean defChar,
        Object pathId) throws Exception;

    public abstract void writeRelationship(Date releaseDate, Object relID, Object statusId, Object conceptOneID,
        Object relationshipTypeConceptID, Object conceptTwoID, Object characteristic, Object refinability, int group,
        Object pathId) throws Exception;

    public abstract void writeDescription(Date releaseDate, Object descriptionId, Object status, Object conceptId,
        String text, boolean capStatus, Object typeInt, String lang, Object pathId) throws Exception;
}
