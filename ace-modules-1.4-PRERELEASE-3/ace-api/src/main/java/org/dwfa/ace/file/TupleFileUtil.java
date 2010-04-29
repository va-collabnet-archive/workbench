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
package org.dwfa.ace.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class TupleFileUtil {

    private int conceptTupleCount;
    private int conceptExtTupleCount;
    private int descTupleCount;
    private int relTupleCount;
    private int cccTupleCount;
    private int ccTupleCount;
    private int ccsTupleCount;
    private int intTupleCount;
    private int idTupleCount;
    private int stringTupleCount;
    private int csTupleCount;

    protected static Set<UUID> pathUuids = new HashSet<UUID>();

    public I_GetConceptData importFile(File importFile, File reportFile, UUID pathToOverrideUuid)
            throws TerminologyException {

        try {
            pathUuids.clear();
            BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(reportFile));
            BufferedReader inputFileReader = new BufferedReader(new FileReader(importFile));

            String currentLine = inputFileReader.readLine();
            int lineCount = 1;
            conceptTupleCount = 0;
            conceptExtTupleCount = 0;
            descTupleCount = 0;
            relTupleCount = 0;
            cccTupleCount = 0;
            ccTupleCount = 0;
            ccsTupleCount = 0;
            intTupleCount = 0;
            idTupleCount = 0;
            stringTupleCount = 0;
            csTupleCount = 0;

            Set<I_GetConceptData> concepts = new HashSet<I_GetConceptData>();
            I_GetConceptData memberRefset = null;

            while (currentLine != null) {

                if (!currentLine.trim().equals("")) {
                    String[] lineParts = currentLine.split("\t");

                    UUID tupleUuid = UUID.fromString(lineParts[0]);

                    if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.CON_TUPLE.getUids().iterator().next())) {
                        if (ConceptTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            conceptTupleCount++;
                            concepts.add(ConceptTupleFileUtil.getLastConcept());
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.DESC_TUPLE.getUids().iterator().next())) {
                        if (DescTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount, pathToOverrideUuid)) {
                            descTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.REL_TUPLE.getUids().iterator().next())) {
                        if (RelTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount, pathToOverrideUuid)) {
                            relTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (ConceptConceptExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            ccTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_CONCEPT_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (ConceptConceptConceptExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            cccTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_STRING_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (ConceptConceptStringExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            ccsTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (ConceptExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            conceptExtTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_INT_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (IntExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            intTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.ID_TUPLE.getUids().iterator().next())) {
                        if (IDTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount, pathToOverrideUuid)) {
                            idTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_STRING_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (StringExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            stringTupleCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_STRING_TUPLE.getUids()
                        .iterator()
                        .next())) {
                        if (ConceptStringExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            pathToOverrideUuid)) {
                            csTupleCount++;
                        }
                    } else {
                        throw new TerminologyException("Unimplemented tuple UUID : " + tupleUuid);
                    }
                }

                currentLine = inputFileReader.readLine();
                lineCount++;
            }

            addPaths();

            // work out which of the concepts added were the member refset
            for (I_GetConceptData concept : concepts) {
                if (isRefsetSpec(concept)) {
                    memberRefset = getMemberRefset(concept);
                    break;
                }
            }

            outputFileWriter.write("------------------");
            outputFileWriter.newLine();
            outputFileWriter.write("Summary of import:");
            outputFileWriter.newLine();
            outputFileWriter.write("ID tuples imported: " + idTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept tuples imported: " + conceptTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Desc tuples imported: " + descTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Rel tuples imported: " + relTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept ext tuples imported: " + conceptExtTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-concept ext tuples imported: " + ccTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-concept-concept ext tuples imported: " + cccTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-concept-string tuples imported: " + ccsTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Int ext tuples imported: " + intTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("String ext tuples imported: " + stringTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-string ext tuples imported: " + csTupleCount);
            outputFileWriter.newLine();

            outputFileWriter.flush();
            outputFileWriter.close();
            inputFileReader.close();

            return memberRefset;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new TerminologyException("Failed to import file - file not found: " + importFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TerminologyException("Failed to import file - IO Exception occurred while reading file: "
                + importFile + " " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException("Failed to import file - Exception occurred while reading file: "
                + importFile + " " + e.getLocalizedMessage());
        }
    }

    private void addPaths() throws TerminologyException, IOException {
        // make sure that imported path being used is viewable.
        I_TermFactory termFactory = LocalVersionedTerminology.get();

        for (UUID pathUuid : pathUuids) {
            I_Path path = termFactory.getPath(new UUID[] { pathUuid });
            I_Position position = termFactory.newPosition(path, Integer.MAX_VALUE);
            if (!termFactory.getActiveAceFrameConfig().getViewPositionSet().contains(position)) {
                termFactory.getActiveAceFrameConfig().addViewPosition(position);
            }
        }
    }

    private boolean isRefsetSpec(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData specifiesRefset = termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());

        if (getLatestRelationshipTarget(concept, specifiesRefset) != null) {
            return true;
        }
        return false;
    }

    private I_GetConceptData getMemberRefset(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData specifiesRefset = termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
        return getLatestRelationshipTarget(concept, specifiesRefset);
    }

    public void exportRefsetSpecToFile(File fileToExportTo, File reportFile, I_GetConceptData refsetSpec)
            throws Exception {

        conceptTupleCount = 0;
        conceptExtTupleCount = 0;
        descTupleCount = 0;
        relTupleCount = 0;
        cccTupleCount = 0;
        ccTupleCount = 0;
        ccsTupleCount = 0;
        intTupleCount = 0;
        idTupleCount = 0;
        stringTupleCount = 0;
        csTupleCount = 0;

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        if (refsetSpec == null) {
            throw new TerminologyException(
                "No refset spec found - the refset spec should have a src relationship of type 'specifies refset' to the member refset.");
        }

        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);

        I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();
        I_GetConceptData markedParentRefset = refsetSpecHelper.getMarkedParentRefsetConcept();
        I_GetConceptData commentsRefset = refsetSpecHelper.getCommentsRefsetConcept();
        I_GetConceptData promotionRefset = refsetSpecHelper.getPromotionRefsetConcept();

        BufferedWriter exportFileWriter = new BufferedWriter(new FileWriter(fileToExportTo, false));
        BufferedWriter reportFileWriter = new BufferedWriter(new FileWriter(reportFile));

        if (memberRefset == null) {
            throw new TerminologyException(
                "No member spec found. Please put the refset to be exported in the refset spec panel.");
        }
        if (markedParentRefset == null) {
            throw new TerminologyException(
                "No marked parent refset found - the member refset should have a 'marked parent refset' relationship to the marked parent refset.");
        }

        I_IntSet allowedStatus = termFactory.getActiveAceFrameConfig().getAllowedStatus();
        I_IntSet allowedTypes = null;
        Set<I_Position> positions = termFactory.getActiveAceFrameConfig().getViewPositionSet();

        // refset spec
        exportFileWriter.append(ConceptTupleFileUtil.exportTuple(refsetSpec));
        conceptTupleCount++;
        List<I_DescriptionTuple> descTuples = refsetSpec.getDescriptionTuples(allowedStatus, allowedTypes, positions,
            true);
        for (I_DescriptionTuple tuple : descTuples) {
            exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
            descTupleCount++;
        }

        // marked parent refset
        exportFileWriter.append(ConceptTupleFileUtil.exportTuple(markedParentRefset));
        conceptTupleCount++;
        descTuples = markedParentRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
        for (I_DescriptionTuple tuple : descTuples) {
            exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
            descTupleCount++;
        }

        // member refset
        exportFileWriter.append(ConceptTupleFileUtil.exportTuple(memberRefset));
        conceptTupleCount++;
        descTuples = memberRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
        for (I_DescriptionTuple tuple : descTuples) {
            exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
            descTupleCount++;
        }

        // optional comments refset
        if (commentsRefset != null) {
            exportFileWriter.append(ConceptTupleFileUtil.exportTuple(commentsRefset));
            conceptTupleCount++;
            descTuples = commentsRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
            for (I_DescriptionTuple tuple : descTuples) {
                exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
                descTupleCount++;
            }
        }

        // optional promotion refset
        if (promotionRefset != null) {
            exportFileWriter.append(ConceptTupleFileUtil.exportTuple(promotionRefset));
            conceptTupleCount++;
            descTuples = promotionRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
            for (I_DescriptionTuple tuple : descTuples) {
                exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
                descTupleCount++;
            }
        }

        // relationships (need to be created after the descriptions)
        List<I_RelTuple> relTuples = memberRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
        for (I_RelTuple tuple : relTuples) {
            exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
            relTupleCount++;
        }
        relTuples = markedParentRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
        for (I_RelTuple tuple : relTuples) {
            exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
            relTupleCount++;
        }
        relTuples = refsetSpec.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
        for (I_RelTuple tuple : relTuples) {
            exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
            relTupleCount++;
        }
        // optional comments and promotions relationships
        if (commentsRefset != null) {
            relTuples = commentsRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
            for (I_RelTuple tuple : relTuples) {
                exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
                relTupleCount++;
            }
        }
        if (promotionRefset != null) {
            relTuples = promotionRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
            for (I_RelTuple tuple : relTuples) {
                exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
                relTupleCount++;
            }
        }

        // add refset spec members
        List<I_ThinExtByRefVersioned> extensions = LocalVersionedTerminology.get().getAllExtensionsForComponent(
            refsetSpec.getConceptId(), true);
        HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
        HashSet<Integer> fetchedComponents = new HashSet<Integer>();
        fetchedComponents.add(refsetSpec.getConceptId());
        RefsetQueryFactory.addExtensionsToMap(extensions, extensionMap, fetchedComponents);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
        for (DefaultMutableTreeNode extNode : extensionMap.values()) {
            I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) extNode.getUserObject();
            if (ext.getComponentId() == refsetSpec.getConceptId()) {
                root.add(extNode);
            } else {
                extensionMap.get(ext.getComponentId()).add(extNode);
            }
        }

        // process each node of the tree recursively, starting at the root
        // each processed node is exported
        processNode(root, termFactory.getActiveAceFrameConfig(), exportFileWriter);

        // export refset members
        exportRefsetMembers(memberRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter);

        // export marked parent refset members
        exportRefsetMembers(markedParentRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter);

        // optional comments refset members
        if (commentsRefset != null) {
            exportRefsetMembers(commentsRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter);
        }

        // optional promotion refset members
        if (promotionRefset != null) {
            exportRefsetMembers(promotionRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter);
        }

        // id tuples are automatically generated for relationships, descriptions
        // and concepts, so we can calculate the total id tuples from this
        idTupleCount = relTupleCount + descTupleCount + conceptTupleCount;

        reportFileWriter.write("------------------");
        reportFileWriter.newLine();
        reportFileWriter.write("Summary of export:");
        reportFileWriter.newLine();
        reportFileWriter.write("ID tuples exported: " + idTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept tuples exported: " + conceptTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Desc tuples exported: " + descTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Rel tuples exported: " + relTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept ext tuples exported: " + conceptExtTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-concept ext tuples exported: " + ccTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-concept-concept ext tuples exported: " + cccTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-concept-string tuples exported: " + ccsTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Int ext tuples exported: " + intTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("String ext tuples exported: " + stringTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-string ext tuples exported: " + csTupleCount);
        reportFileWriter.newLine();

        exportFileWriter.flush();
        exportFileWriter.close();
        reportFileWriter.flush();
        reportFileWriter.close();
    }

    private void exportRefsetMembers(I_GetConceptData refset, I_ConfigAceFrame configFrame,
            BufferedWriter outputFileWriter) throws Exception {
        List<I_ThinExtByRefVersioned> extensions = LocalVersionedTerminology.get().getRefsetExtensionMembers(
            refset.getConceptId());

        SpecRefsetHelper helper = new SpecRefsetHelper();
        for (I_ThinExtByRefVersioned ext : extensions) {
            boolean addUncommitted = true;
            boolean returnConflictResolvedLatestState = true;
            List<I_ThinExtByRefTuple> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, addUncommitted,
                returnConflictResolvedLatestState);

            if (extensions.size() > 0) {
                I_ThinExtByRefTuple thinTuple = tuples.get(0);
                I_ThinExtByRefPart thinPart = thinTuple.getPart();

                if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
                    outputFileWriter.append(ConceptConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    cccTupleCount++;
                } else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {
                    outputFileWriter.append(ConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    ccTupleCount++;
                } else if (thinPart instanceof I_ThinExtByRefPartConceptConceptString) {
                    outputFileWriter.append(ConceptConceptStringExtTupleFileUtil.exportTuple(thinTuple));
                    ccsTupleCount++;
                } else if (thinPart instanceof I_ThinExtByRefPartConceptString) {
                    outputFileWriter.append(ConceptStringExtTupleFileUtil.exportTuple(thinTuple));
                    csTupleCount++;
                } else if (thinPart instanceof I_ThinExtByRefPartConcept) {
                    outputFileWriter.append(ConceptExtTupleFileUtil.exportTuple(thinTuple));
                    conceptExtTupleCount++;
                } else if (thinPart instanceof I_ThinExtByRefPartString) {
                    outputFileWriter.append(StringExtTupleFileUtil.exportTuple(thinTuple));
                    stringTupleCount++;
                } else {
                    throw new TerminologyException("Unknown extension type:" + thinPart.toString());
                }
            }
        }
    }

    /**
     * Export each node in our refset spec tree structure. For each child of the
     * node, we recursively determine the refset type (corresponds to a
     * sub-query or statement). This includes checking any grandchildren,
     * great-grandchildren etc.
     * 
     * @param node
     *            The node to which the processing begins.
     * @param configFrame
     * @throws Exception
     */
    private void processNode(DefaultMutableTreeNode node, I_ConfigAceFrame configFrame, BufferedWriter outputFileWriter)
            throws Exception {

        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {
            // determine type of current child
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            I_ThinExtByRefVersioned currExt = (I_ThinExtByRefVersioned) childNode.getUserObject();

            boolean addUncommitted = true;
            boolean returnConflictResolvedLatestState = true;
            List<I_ThinExtByRefTuple> extensions = currExt.getTuples(configFrame.getAllowedStatus(),
                configFrame.getViewPositionSet(), addUncommitted, returnConflictResolvedLatestState);
            if (extensions.size() > 0) {
                I_ThinExtByRefTuple thinTuple = extensions.get(0);
                I_ThinExtByRefPart thinPart = thinTuple.getPart();

                if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
                    outputFileWriter.append(ConceptConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    cccTupleCount++;
                } else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {
                    outputFileWriter.append(ConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    ccTupleCount++;
                    // process each grandchild
                    if (!childNode.isLeaf()) {
                        processNode(childNode, configFrame, outputFileWriter);
                    }
                } else if (thinPart instanceof I_ThinExtByRefPartConceptConceptString) {
                    outputFileWriter.append(ConceptConceptStringExtTupleFileUtil.exportTuple(thinTuple));
                    ccsTupleCount++;
                } else {
                    throw new TerminologyException("Unknown extension type:" + thinPart.toString());
                }
            }

        }
    }

    /**
     * Gets the latest specified relationship's target.
     * 
     * @param relationshipType
     * @return
     * @throws Exception
     */
    public I_GetConceptData getLatestRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType)
            throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        List<I_RelTuple> relationships = concept.getSourceRelTuples(LocalVersionedTerminology.get()
            .getActiveAceFrameConfig()
            .getAllowedStatus(), allowedTypes, LocalVersionedTerminology.get()
            .getActiveAceFrameConfig()
            .getViewPositionSet(), true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestTarget = LocalVersionedTerminology.get().getConcept(rel.getC2Id());
            }
        }

        return latestTarget;
    }
}
