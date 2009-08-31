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
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class TupleFileUtil {

    public void importFile(File file) throws TerminologyException {

        try {
            BufferedReader inputFileReader = new BufferedReader(new FileReader(file));

            String currentLine = inputFileReader.readLine();

            while (currentLine != null) {

                if (!currentLine.trim().equals("")) {
                    String[] lineParts = currentLine.split("\t");

                    UUID tupleUuid = UUID.fromString(lineParts[0]);

                    if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.CON_TUPLE.getUids().iterator().next())) {
                        ConceptTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.DESC_TUPLE.getUids().iterator().next())) {
                        DescTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.REL_TUPLE.getUids().iterator().next())) {
                        RelTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_TUPLE.getUids()
                        .iterator().next())) {
                        ConceptConceptExtTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_CONCEPT_TUPLE
                        .getUids().iterator().next())) {
                        ConceptConceptConceptExtTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_STRING_TUPLE
                        .getUids().iterator().next())) {
                        ConceptConceptStringExtTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_CONCEPT_TUPLE.getUids().iterator()
                        .next())) {
                        ConceptExtTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.EXT_INT_TUPLE.getUids().iterator()
                        .next())) {
                        IntExtTupleFileUtil.importTuple(currentLine);
                    } else if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.ID_TUPLE.getUids().iterator().next())) {
                        IDTupleFileUtil.importTuple(currentLine);
                    } else {
                        throw new TerminologyException("Unimplemented tuple UUID : " + tupleUuid);
                    }
                }

                currentLine = inputFileReader.readLine();
            }

        } catch (FileNotFoundException e) {
            throw new TerminologyException("Failed to import file - file not found: " + file);
        } catch (IOException e) {
            throw new TerminologyException("Failed to import file - IO Exception occurred while reading file: " + file);
        }
    }

    public void exportRefsetSpecToFile(File file, I_GetConceptData refsetSpec) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();

        I_GetConceptData memberRefset =
                getLatestRelationshipTarget(refsetSpec, termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET
                    .getUids()));
        I_GetConceptData markedParentRefset =
                getLatestRelationshipTarget(memberRefset, termFactory
                    .getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids()));

        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(file, false));

        if (refsetSpec == null) {
            throw new TerminologyException(
                "No refset spec found - the refset spec should have a src relationship of type 'specifies refset' to the member refset.");
        }
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
        outputFileWriter.append(ConceptTupleFileUtil.exportTuple(refsetSpec));
        List<I_DescriptionTuple> descTuples =
                refsetSpec.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
        for (I_DescriptionTuple tuple : descTuples) {
            outputFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
        }

        // marked parent refset
        outputFileWriter.append(ConceptTupleFileUtil.exportTuple(markedParentRefset));
        descTuples = markedParentRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
        for (I_DescriptionTuple tuple : descTuples) {
            outputFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
        }

        // member refset
        outputFileWriter.append(ConceptTupleFileUtil.exportTuple(memberRefset));
        descTuples = memberRefset.getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
        for (I_DescriptionTuple tuple : descTuples) {
            outputFileWriter.append(DescTupleFileUtil.exportTuple(tuple));
        }

        // relationships (need to be created after the descriptions)
        List<I_RelTuple> relTuples =
                memberRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
        for (I_RelTuple tuple : relTuples) {
            outputFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
        }
        relTuples = markedParentRefset.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
        for (I_RelTuple tuple : relTuples) {
            outputFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
        }
        relTuples = refsetSpec.getSourceRelTuples(allowedStatus, allowedTypes, positions, true, true);
        for (I_RelTuple tuple : relTuples) {
            outputFileWriter.append(RelTupleFileUtil.exportTuple(tuple));
        }

        // add refset spec members
        List<I_ThinExtByRefVersioned> extensions =
                LocalVersionedTerminology.get().getAllExtensionsForComponent(refsetSpec.getConceptId(), true);
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
        processNode(root, termFactory.getActiveAceFrameConfig(), outputFileWriter);

        outputFileWriter.flush();
        outputFileWriter.close();
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
            List<I_ThinExtByRefTuple> extensions =
                    currExt.getTuples(configFrame.getAllowedStatus(), configFrame.getViewPositionSet(), addUncommitted,
                        returnConflictResolvedLatestState);
            I_ThinExtByRefTuple thinTuple = extensions.get(0);
            I_ThinExtByRefPart thinPart = thinTuple.getPart();

            if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
                outputFileWriter.append(ConceptConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
            } else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {
                outputFileWriter.append(ConceptConceptExtTupleFileUtil.exportTuple(thinTuple));
                // process each grandchild
                if (!childNode.isLeaf()) {
                    processNode(childNode, configFrame, outputFileWriter);
                }
            } else if (thinPart instanceof I_ThinExtByRefPartConceptConceptString) {
                outputFileWriter.append(ConceptConceptStringExtTupleFileUtil.exportTuple(thinTuple));
            } else {
                throw new TerminologyException("Unknown extension type:" + thinPart.toString());
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

        List<I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestTarget = LocalVersionedTerminology.get().getConcept(rel.getC2Id());
            }
        }

        return latestTarget;
    }
}
