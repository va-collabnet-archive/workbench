/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.dataimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.util.id.Type3UuidFactory;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.project.refset.ConceptMembershipRefset;
import org.ihtsdo.project.refset.RefsetMemberValueMgr;

/**
 * The Class ImportConceptRefsetAsRefset.
 */
public class ImportConceptRefsetAsRefset {

    /**
     * The output file writer.
     */
    PrintWriter outputFileWriter;
    /**
     * The input file reader.
     */
    BufferedReader inputFileReader;
    /**
     * The line count.
     */
    int lineCount;
    /**
     * The imported.
     */
    int imported;
    /**
     * The deleted.
     */
    int deleted;
    /**
     * The refset concept.
     */
    ConceptMembershipRefset refsetConcept;
    /**
     * The refset uuid.
     */
    UUID refsetUUID;
    /**
     * The refset helper.
     */
    I_HelpRefsets refsetHelper;
    /**
     * The member value mgr.
     */
    private RefsetMemberValueMgr memberValueMgr;
    /**
     * The term factory.
     */
    I_TermFactory termFactory;
    /**
     * The con id hash.
     */
    private HashSet<Long> conIdHash;
    /**
     * The incremental.
     */
    private boolean incremental;

    /**
     * Instantiates a new import concept refset as refset.
     */
    public ImportConceptRefsetAsRefset() {
        termFactory = Terms.get();

    }

    /**
     * Import from file.
     *
     * @param importFile the import file
     * @param reportFile the report file
     * @param refsetName the refset name
     * @param parentId the parent id
     * @return the integer[]
     * @throws Exception the exception
     */
    public Integer[] importFromFile(File importFile, File reportFile, String refsetName, int parentId) throws Exception {

        outputFileWriter = new PrintWriter(new FileWriter(reportFile));

        if (!fileDataControl(importFile)) {
            outputFileWriter.close();
            throw new Exception("There are errors on input file.");
        }
        inputFileReader = new BufferedReader(new FileReader(importFile));

        String currentLine = inputFileReader.readLine();
        int digits = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (Character.isDigit(currentLine.charAt(i))) {
                digits++;
            }
        }
        if (digits < 2) {
            currentLine = inputFileReader.readLine();
        }
        lineCount = 1;
        imported = 0;
        refsetUUID = UUID.nameUUIDFromBytes(refsetName.getBytes());
        if (termFactory.hasId(refsetUUID)) {
            throw new Exception("The refset already exists");
        } else {
            refsetConcept = ConceptMembershipRefset.createNewConceptMembershipRefset(refsetName, parentId);
        }
        memberValueMgr = new RefsetMemberValueMgr(refsetConcept.getRefsetConcept());

        incremental = true;
        while (currentLine != null) {

            if (!currentLine.trim().equals("")) {
                importRefsetLine(currentLine);
            }

            currentLine = inputFileReader.readLine();
            lineCount++;
        }
        inputFileReader.close();

        termFactory.commit();
        outputFileWriter.println("Imported from file " + importFile.getName() + " : " + imported + " concepts");
        outputFileWriter.flush();
        outputFileWriter.close();
        return new Integer[]{imported, 0};
    }

    /**
     * Import refset line.
     *
     * @param inputLine the input line
     * @return true, if successful
     * @throws Exception the exception
     */
    private boolean importRefsetLine(String inputLine) {

        String memberId;
        String[] lineParts = inputLine.split("\t");

        memberId = lineParts[5];

        int conceptMemberId = 0;
        try {
            Long.parseLong(memberId);
            conceptMemberId = termFactory.getId(Type3UuidFactory.fromSNOMED(memberId)).getNid();
        } catch (NumberFormatException e) {
            try {
                conceptMemberId = termFactory.getId(UUID.fromString(memberId)).getNid();
            } catch (Exception e1) {
                String errorMessage = "Cannot find concept for memberId: " + memberId + e1.getLocalizedMessage();
                outputFileWriter.println("Error on line " + lineCount + " : ");
                outputFileWriter.println(errorMessage);
                e1.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            try {
                SearchResult results = Terms.get().doLuceneSearch(memberId);
                if (results.topDocs.scoreDocs.length > 0) {
                    conceptMemberId = Integer.valueOf(results.searcher.doc(results.topDocs.scoreDocs[0].doc).get("cnid"));
                } else {
                    String errorMessage = "Cannot find concept for memberId: " + memberId;
                    outputFileWriter.println("Error on line " + lineCount + " : ");
                    outputFileWriter.println(errorMessage);
                    return false;
                }
            } catch (Exception e1) {
                String errorMessage = "Cannot find concept for memberId: " + memberId + e1.getLocalizedMessage();
                outputFileWriter.println("Error on line " + lineCount + " : ");
                outputFileWriter.println(errorMessage);
                AceLog.getAppLog().alertAndLogException(e);
                return false;
            }
        }
        try {
            memberValueMgr.putConceptMember(conceptMemberId);
        } catch (Exception e) {
            String errorMessage = "Cannot add concept as member: " + memberId + e.getLocalizedMessage();
            outputFileWriter.println("Error on line " + lineCount + " : ");
            outputFileWriter.println(errorMessage);
            return false;
        }
        imported++;
        if (!incremental) {
            conIdHash.add(Long.parseLong(String.valueOf(conceptMemberId)));
        }
        return true;
    }

    /**
     * Import from file to exist refset.
     *
     * @param importFile the import file
     * @param reportFile the report file
     * @param refset the refset
     * @param incremental the incremental
     * @return the integer[]
     * @throws Exception the exception
     */
    public Integer[] importFromFileToExistRefset(File importFile, File reportFile, I_GetConceptData refset, boolean incremental) throws Exception {

        outputFileWriter = new PrintWriter(new FileWriter(reportFile));

        if (!fileDataControl(importFile)) {
            outputFileWriter.close();
            throw new Exception("There are errors on input file.");
        }

        inputFileReader = new BufferedReader(new FileReader(importFile));

        this.incremental = incremental;
        conIdHash = new HashSet<Long>();
        String currentLine = inputFileReader.readLine();
        int digits = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (Character.isDigit(currentLine.charAt(i))) {
                digits++;
            }
        }
        if (digits < 2) {
            currentLine = inputFileReader.readLine();
        }

        lineCount = 1;
        imported = 0;
        deleted = 0;

        // refsetConcept=new ConceptMembershipRefset(refset);
        // memberValueMgr =new
        // RefsetMemberValueMgr(refsetConcept.getRefsetConcept());

        memberValueMgr = new RefsetMemberValueMgr(refset);
        while (currentLine != null) {

            if (!currentLine.trim().equals("")) {
                importRefsetLine(currentLine);
            }
            currentLine = inputFileReader.readLine();
            lineCount++;
        }
        inputFileReader.close();
        if (!incremental) {
            inactivateNotExistentMembers(refset);
        }
        termFactory.commit();
        outputFileWriter.println("Imported from file " + importFile.getName() + " : " + imported + " concepts");

        if (!incremental) {
            outputFileWriter.println("Deleted from Refset " + refset.toString() + " : " + deleted + " concepts");
        }
        outputFileWriter.flush();
        outputFileWriter.close();

        return new Integer[]{imported, deleted};

    }

    /**
     * File data control.
     *
     * @param importFile the import file
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean fileDataControl(File importFile) throws IOException {
        BufferedReader inputFileReaderCtrl = new BufferedReader(new FileReader(importFile));

        boolean ret = true;
        HashSet<String> conUuidHash = new HashSet<String>();
        String currentLine = inputFileReaderCtrl.readLine();
        lineCount = 1;
        String memberId;
        while (currentLine != null) {

            if (!currentLine.trim().equals("")) {
                String[] lineParts = currentLine.split("\t");
                memberId = lineParts[5];
                if (conUuidHash.contains(memberId)) {
                    ret = false;
                    outputFileWriter.println("Error on line " + lineCount + " : ");
                    outputFileWriter.println("Duplicated component " + memberId);
                } else {
                    conUuidHash.add(memberId);
                }

            }
            currentLine = inputFileReaderCtrl.readLine();
            lineCount++;
        }
        inputFileReaderCtrl.close();
        outputFileWriter.flush();
        return ret;

    }

    /**
     * Inactivate not existent members.
     *
     * @param concept the concept
     * @throws Exception the exception
     */
    private void inactivateNotExistentMembers(I_GetConceptData concept) throws Exception {

        Collection<? extends I_ExtendByRef> extensions = termFactory.getRefsetExtensionMembers(concept.getConceptNid());
        for (I_ExtendByRef extension : extensions) {
            if (!conIdHash.contains(extension.getComponentNid())) {
                memberValueMgr.delConceptMember(extension.getComponentNid());
                deleted++;
            }

        }

    }
}
