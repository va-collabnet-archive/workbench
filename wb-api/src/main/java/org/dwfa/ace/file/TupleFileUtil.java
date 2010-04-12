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
import java.util.Collection;
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
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;


public class TupleFileUtil {

    public I_GetConceptData importFile(File importFile, File reportFile, I_ConfigAceFrame importConfig)
            throws TerminologyException {

        try {
            // pathUuids.clear();

            
            BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(reportFile));
            BufferedReader inputFileReader = new BufferedReader(new FileReader(importFile));

            String currentLine = inputFileReader.readLine();
            int lineCount = 1;
            TupleCounter tupleCounter = new TupleCounter();

            Set<I_GetConceptData> concepts = new HashSet<I_GetConceptData>();
            I_GetConceptData memberRefset = null;

            while (currentLine != null) {

                if (!currentLine.trim().equals("")) {
                    String[] lineParts = currentLine.split("\t");

                    UUID tupleUuid = UUID.fromString(lineParts[0]);

                    if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.CON_TUPLE.getUids().iterator().next())) {
                        if (ConceptTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.conceptTupleCount++;
                            concepts.add(ConceptTupleFileUtil.getLastConcept());
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.DESC_TUPLE.getUids().iterator().next())) {
                        if (DescTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount, importConfig)) {
                            tupleCounter.descTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.REL_TUPLE.getUids().iterator().next())) {
                        if (RelTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount, importConfig)) {
                            tupleCounter.relTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_TUPLE.getUids()
                        .iterator().next())) {
                        if (ConceptConceptExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.ccTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_CONCEPT_TUPLE
                        .getUids().iterator().next())) {
                        if (ConceptConceptConceptExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.cccTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_STRING_TUPLE
                        .getUids().iterator().next())) {
                        if (ConceptConceptStringExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.ccsTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_TUPLE.getUids().iterator()
                        .next())) {
                        if (ConceptExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.conceptExtTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_INT_TUPLE.getUids().iterator()
                        .next())) {
                        if (IntExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.intTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.ID_TUPLE.getUids().iterator().next())) {
                        // skip as we'll process them in the second pass
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_STRING_TUPLE.getUids().iterator()
                        .next())) {
                        if (StringExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.stringTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_STRING_TUPLE.getUids()
                        .iterator().next())) {
                        if (ConceptStringExtTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount,
                            importConfig)) {
                            tupleCounter.csTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    } else {
                        throw new TerminologyException("Unimplemented tuple UUID : " + tupleUuid);
                    }
                }

                currentLine = inputFileReader.readLine();
                lineCount++;
            }

            // addPaths();

            // work out which of the concepts added were the member refset
            for (I_GetConceptData concept : concepts) {
                if (isRefsetSpec(concept)) {
                    memberRefset = getMemberRefset(concept);
                    break;
                }
            }

            // second pass - process ID tuples
            inputFileReader.close();
            inputFileReader = new BufferedReader(new FileReader(importFile));
            currentLine = inputFileReader.readLine();
            lineCount = 1;
            while (currentLine != null) {
                if (!currentLine.trim().equals("")) {
                    String[] lineParts = currentLine.split("\t");

                    UUID tupleUuid = UUID.fromString(lineParts[0]);
                    if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.ID_TUPLE.getUids().iterator().next())) {
                        if (IDTupleFileUtil.importTuple(currentLine, outputFileWriter, lineCount, importConfig)) {
                            tupleCounter.idTupleCount++;
                        } else {
                            tupleCounter.errorCount++;
                        }
                    }
                    lineCount++;
                    currentLine = inputFileReader.readLine();
                }
            }

            outputFileWriter.write("------------------");
            outputFileWriter.newLine();
            outputFileWriter.write("Summary of import:");
            outputFileWriter.newLine();
            outputFileWriter.write("ID tuples imported: " + tupleCounter.idTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept tuples imported: " + tupleCounter.conceptTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Desc tuples imported: " + tupleCounter.descTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Rel tuples imported: " + tupleCounter.relTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept ext tuples imported: " + tupleCounter.conceptExtTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-concept ext tuples imported: " + tupleCounter.ccTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-concept-concept ext tuples imported: " + tupleCounter.cccTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-concept-string tuples imported: " + tupleCounter.ccsTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Int ext tuples imported: " + tupleCounter.intTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("String ext tuples imported: " + tupleCounter.stringTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Concept-string ext tuples imported: " + tupleCounter.csTupleCount);
            outputFileWriter.newLine();
            outputFileWriter.write("Errors encountered: " + tupleCounter.errorCount);
            outputFileWriter.newLine();

            outputFileWriter.flush();
            outputFileWriter.close();
            inputFileReader.close();
            
            if (tupleCounter.errorCount > 0) {
                AceLog.getAppLog().alertAndLogException(new IOException(tupleCounter.errorCount + " errors during import. Please examine error log."));
            }

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

    private boolean isRefsetSpec(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData specifiesRefset = termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());

        if (getLatestRelationshipTarget(concept, specifiesRefset) != null) {
            return true;
        }
        return false;
    }

    private boolean isMemberRefset(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData specifiesRefset = termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());

        if (getLatestDestinationRelationshipSource(concept, specifiesRefset) != null) {
            return true;
        }
        return false;
    }

    private I_GetConceptData getMemberRefset(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData specifiesRefset = termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
        return getLatestRelationshipTarget(concept, specifiesRefset);
    }

    public void exportRefsetSpecToFile(BufferedWriter exportFileWriter, I_GetConceptData refsetSpec,
            TupleCounter tupleCounter) throws Exception {
        if (refsetSpec == null) {
            throw new TerminologyException(
                "No refset spec found - the refset spec should have a src relationship of type 'specifies refset' to the member refset.");
        }
        I_TermFactory termFactory = Terms.get();
        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);
        I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();
        I_GetConceptData markedParentRefset = refsetSpecHelper.getMarkedParentRefsetConcept();
        I_GetConceptData commentsRefset = refsetSpecHelper.getCommentsRefsetConcept();
        I_GetConceptData promotionRefset = refsetSpecHelper.getPromotionRefsetConcept();
        if (memberRefset == null) {
            throw new TerminologyException(
                "No member spec found. Please put the refset to be exported in the refset spec panel.");
        }
        if (markedParentRefset == null) {
            throw new TerminologyException(
                "No marked parent refset found - the member refset should have a 'marked parent refset' relationship to the marked parent refset.");
        }

        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        I_IntSet allowedStatus = config.getAllowedStatus();
        I_IntSet allowedTypes = null;
        PositionSetReadOnly positions = config.getViewPositionSetReadOnly();

        // refset spec
        exportFileWriter.append(ConceptTupleFileUtil.exportTuple(refsetSpec));
        tupleCounter.conceptTupleCount++;
        List<? extends I_DescriptionTuple> descTuples =
                refsetSpec.getDescriptionTuples(allowedStatus, allowedTypes, positions, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_DescriptionTuple tuple : descTuples) {
            exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
            tupleCounter.descTupleCount++;
        }

        // marked parent refset
        exportFileWriter.append(ConceptTupleFileUtil.exportTuple(markedParentRefset));
        tupleCounter.conceptTupleCount++;
        descTuples = markedParentRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_DescriptionTuple tuple : descTuples) {
            exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
            tupleCounter.descTupleCount++;
        }

        // member refset
        exportFileWriter.append(ConceptTupleFileUtil.exportTuple(memberRefset));
        tupleCounter.conceptTupleCount++;
        descTuples = memberRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_DescriptionTuple tuple : descTuples) {
            exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
            tupleCounter.descTupleCount++;
        }

        // optional comments refset
        if (commentsRefset != null) {
            exportFileWriter.append(ConceptTupleFileUtil.exportTuple(commentsRefset));
            tupleCounter.conceptTupleCount++;
            descTuples = commentsRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            for (I_DescriptionTuple tuple : descTuples) {
                exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
                tupleCounter.descTupleCount++;
            }
        }

        // optional promotion refset
        if (promotionRefset != null) {
            exportFileWriter.append(ConceptTupleFileUtil.exportTuple(promotionRefset));
            tupleCounter.conceptTupleCount++;
            descTuples = promotionRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            for (I_DescriptionTuple tuple : descTuples) {
                exportFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
                tupleCounter.descTupleCount++;
            }
        }

        // relationships (need to be created after the descriptions)
        List<? extends I_RelTuple> relTuples =
                memberRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, 
                    config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple tuple : relTuples) {
            exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
            tupleCounter.relTupleCount++;
        }
        relTuples = markedParentRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple tuple : relTuples) {
            exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
            tupleCounter.relTupleCount++;
        }
        relTuples = refsetSpec.getSourceRelTuples(allowedStatus, allowedTypes, positions, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple tuple : relTuples) {
            exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
            tupleCounter.relTupleCount++;
        }
        // optional comments and promotions relationships
        if (commentsRefset != null) {
            relTuples = commentsRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            for (I_RelTuple tuple : relTuples) {
                exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
                tupleCounter.relTupleCount++;
            }
        }
        if (promotionRefset != null) {
            relTuples = promotionRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            for (I_RelTuple tuple : relTuples) {
                exportFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
                tupleCounter.relTupleCount++;
            }
        }

        // add refset spec members
        List<? extends I_ExtendByRef> extensions =
                Terms.get().getAllExtensionsForComponent(refsetSpec.getConceptId(), true);
        HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
        HashSet<Integer> fetchedComponents = new HashSet<Integer>();
        fetchedComponents.add(refsetSpec.getConceptId());
        RefsetQueryFactory.addExtensionsToMap((List<? extends I_ExtendByRef>) extensions, extensionMap,
            fetchedComponents, refsetSpec.getConceptId());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
        for (DefaultMutableTreeNode extNode : extensionMap.values()) {
            I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();
            if (ext.getComponentId() == refsetSpec.getConceptId()) {
                root.add(extNode);
            } else {
                extensionMap.get(ext.getComponentId()).add(extNode);
            }
        }

        // process each node of the tree recursively, starting at the root
        // each processed node is exported
        processNode(root, termFactory.getActiveAceFrameConfig(), exportFileWriter, tupleCounter);

        // export refset members
        exportRefsetMembers(memberRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter, tupleCounter);

        // export marked parent refset members
        exportRefsetMembers(markedParentRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter, tupleCounter);

        // optional comments refset members
        if (commentsRefset != null) {
            exportRefsetMembers(commentsRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter, tupleCounter);
        }

        // optional promotion refset members
        if (promotionRefset != null) {
            exportRefsetMembers(promotionRefset, termFactory.getActiveAceFrameConfig(), exportFileWriter, tupleCounter);
        }
    }

    public void exportRefsetSpecToFile(File fileToExportTo, File reportFile, I_GetConceptData refsetSpec)
            throws Exception {

        TupleCounter tupleCounter = new TupleCounter();

        BufferedWriter exportFileWriter = new BufferedWriter(new FileWriter(fileToExportTo, false));
        BufferedWriter reportFileWriter = new BufferedWriter(new FileWriter(reportFile));

        exportRefsetSpecToFile(exportFileWriter, refsetSpec, tupleCounter);

        // id tuples are automatically generated for relationships, descriptions
        // and concepts, so we can calculate the total id tuples from this
        tupleCounter.idTupleCount =
                tupleCounter.relTupleCount + tupleCounter.descTupleCount + tupleCounter.conceptTupleCount;

        reportFileWriter.write("------------------");
        reportFileWriter.newLine();
        reportFileWriter.write("Summary of export:");
        reportFileWriter.newLine();
        reportFileWriter.write("ID tuples exported: " + tupleCounter.idTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept tuples exported: " + tupleCounter.conceptTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Desc tuples exported: " + tupleCounter.descTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Rel tuples exported: " + tupleCounter.relTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept ext tuples exported: " + tupleCounter.conceptExtTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-concept ext tuples exported: " + tupleCounter.ccTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-concept-concept ext tuples exported: " + tupleCounter.cccTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-concept-string tuples exported: " + tupleCounter.ccsTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Int ext tuples exported: " + tupleCounter.intTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("String ext tuples exported: " + tupleCounter.stringTupleCount);
        reportFileWriter.newLine();
        reportFileWriter.write("Concept-string ext tuples exported: " + tupleCounter.csTupleCount);
        reportFileWriter.newLine();

        exportFileWriter.flush();
        exportFileWriter.close();
        reportFileWriter.flush();
        reportFileWriter.close();
    }

    private void exportRefsetMembers(I_GetConceptData refset, I_ConfigAceFrame configFrame,
            BufferedWriter exportFileWriter, TupleCounter tupleCounter) throws Exception {
        Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(refset.getConceptId());

        I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        for (I_ExtendByRef ext : extensions) {
            List<? extends I_ExtendByRefVersion> tuples =
                    ext.getTuples(helper.getCurrentStatusIntSet(), null,
                        configFrame.getPrecedence(), configFrame.getConflictResolutionStrategy());

            if (extensions.size() > 0) {
                I_ExtendByRefVersion thinTuple = tuples.get(0);
                I_ExtendByRefPart thinPart = thinTuple.getMutablePart();

                if (thinPart instanceof I_ExtendByRefPartCidCidCid) {
                    exportFileWriter.append(ConceptConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.cccTupleCount++;
                } else if (thinPart instanceof I_ExtendByRefPartCidCid) {
                    exportFileWriter.append(ConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.ccTupleCount++;
                } else if (thinPart instanceof I_ExtendByRefPartCidCidString) {
                    exportFileWriter.append(ConceptConceptStringExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.ccsTupleCount++;
                } else if (thinPart instanceof I_ExtendByRefPartCidString) {
                    exportFileWriter.append(ConceptStringExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.csTupleCount++;
                } else if (thinPart instanceof I_ExtendByRefPartCid) {
                    exportFileWriter.append(ConceptExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.conceptExtTupleCount++;
                } else if (thinPart instanceof I_ExtendByRefPartStr) {
                    exportFileWriter.append(StringExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.stringTupleCount++;
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
    private void processNode(DefaultMutableTreeNode node, I_ConfigAceFrame configFrame,
            BufferedWriter outputFileWriter, TupleCounter tupleCounter) throws Exception {

        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {
            // determine type of current child
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            I_ExtendByRef currExt = (I_ExtendByRef) childNode.getUserObject();

            List<? extends I_ExtendByRefVersion> extensions =
                    currExt.getTuples(configFrame.getAllowedStatus(), configFrame.getViewPositionSetReadOnly(),
                        configFrame.getPrecedence(), configFrame.getConflictResolutionStrategy());
            if (extensions.size() > 0) {
                I_ExtendByRefVersion thinTuple = extensions.get(0);
                I_ExtendByRefPart thinPart = thinTuple.getMutablePart();

                if (thinPart instanceof I_ExtendByRefPartCidCidCid) {
                    // check for any nested refsets that need to be exported
                    I_ExtendByRefPartCidCidCid part = (I_ExtendByRefPartCidCidCid) thinTuple.getMutablePart();
                    I_GetConceptData clause = Terms.get().getConcept(part.getC2id());
                    I_GetConceptData potentialRefset = Terms.get().getConcept(part.getC3id());
                    if (clause.getConceptId() == RefsetAuxiliary.Concept.CONCEPT_IS_MEMBER_OF.localize().getNid()
                        || clause.getConceptId() == RefsetAuxiliary.Concept.DESC_IS_MEMBER_OF.localize().getNid()
                        || clause.getConceptId() == RefsetAuxiliary.Concept.REL_IS_MEMBER_OF.localize().getNid()) {
                        if (isMemberRefset(potentialRefset)) {
                            RefsetSpec refsetSpecHelper = new RefsetSpec(potentialRefset, true);
                            exportRefsetSpecToFile(outputFileWriter, refsetSpecHelper.getRefsetSpecConcept(),
                                tupleCounter);
                        }
                    }

                    outputFileWriter.append(ConceptConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.cccTupleCount++;
                } else if (thinPart instanceof I_ExtendByRefPartCidCid) {
                    outputFileWriter.append(ConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.ccTupleCount++;
                    // process each grandchild
                    if (!childNode.isLeaf()) {
                        processNode(childNode, configFrame, outputFileWriter, tupleCounter);
                    }
                } else if (thinPart instanceof I_ExtendByRefPartCidCidString) {
                    outputFileWriter.append(ConceptConceptStringExtTupleFileUtil.exportTuple(thinTuple));
                    tupleCounter.ccsTupleCount++;
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
        long latestVersion = Long.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        List<? extends I_RelTuple> relationships =
                concept.getSourceRelTuples(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes,
                    Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getTime() > latestVersion) {
                latestVersion = rel.getTime();
                latestTarget = Terms.get().getConcept(rel.getC2Id());
            }
        }

        return latestTarget;
    }

    /**
     * Gets the latest specified dest relationship's source.
     * 
     * @param relationshipType
     * @return
     * @throws Exception
     */
    public I_GetConceptData getLatestDestinationRelationshipSource(I_GetConceptData concept,
            I_GetConceptData relationshipType) throws Exception {

        I_GetConceptData latestSource = null;
        long latestVersion = Long.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        List<? extends I_RelTuple> relationships =
                concept.getDestRelTuples(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, Terms
                    .get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getTime() > latestVersion) {
                latestVersion = rel.getTime();
                latestSource = Terms.get().getConcept(rel.getC1Id());
            }
        }
        return latestSource;
    }
}
