package org.dwfa.ace.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class TupleFileUtil {

    public void importFile(File file) throws TerminologyException {

        try {
            BufferedReader inputFileReader = new BufferedReader(new FileReader(
                    file));

            String currentLine = inputFileReader.readLine();

            while (currentLine != null) {

                String[] lineParts = currentLine.split("\t");

                UUID tupleUuid = UUID.fromString(lineParts[0]);

                if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.CON_TUPLE
                        .getUids().iterator().next())) {
                    ConceptTupleFileUtil.importTuple(currentLine);
                }
                if (tupleUuid.equals(ArchitectonicAuxiliary.Concept.DESC_TUPLE
                        .getUids().iterator().next())) {
                    DescTupleFileUtil.importTuple(currentLine);
                } else {
                    throw new TerminologyException(
                            "Unimplemented tuple UUID : " + tupleUuid);
                }

                currentLine = inputFileReader.readLine();
            }

        } catch (FileNotFoundException e) {
            throw new TerminologyException(
                    "Failed to import file - file not found: " + file);
        } catch (IOException e) {
            throw new TerminologyException(
                    "Failed to import file - IO Exception occurred while reading file: "
                            + file);
        }
    }

    public void exportRefsetSpecToFile(File file, I_GetConceptData refsetSpec)
            throws TerminologyException {
        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            I_GetConceptData memberRefset = getLatestRelationshipTarget(
                    refsetSpec,
                    termFactory
                            .getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET
                                    .getUids()));
            I_GetConceptData markedParentRefset = getLatestRelationshipTarget(
                    memberRefset,
                    termFactory
                            .getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET
                                    .getUids()));

            BufferedWriter outputFileWriter = new BufferedWriter(
                    new FileWriter(file, true));

            if (refsetSpec == null) {
                throw new TerminologyException("Refset spec is null.");
            }
            if (memberRefset == null) {
                throw new TerminologyException("Member refset is null.");
            }
            if (markedParentRefset == null) {
                throw new TerminologyException("Marked parent refset is null.");
            }

            // TODO add descriptions/relationships
            outputFileWriter.append(ConceptTupleFileUtil
                    .exportTuple(memberRefset));
            outputFileWriter.append(ConceptTupleFileUtil
                    .exportTuple(refsetSpec));
            outputFileWriter.append(ConceptTupleFileUtil
                    .exportTuple(markedParentRefset));

            // TODO add refset spec members

        } catch (Exception e) {
            throw new TerminologyException("Failed to export tuple.");
        }
    }

    /**
     * Gets the latest specified relationship's target.
     * 
     * @param relationshipType
     * @return
     * @throws Exception
     */
    public I_GetConceptData getLatestRelationshipTarget(
            I_GetConceptData concept, I_GetConceptData relationshipType)
            throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        List<I_RelTuple> relationships = concept.getSourceRelTuples(null,
                allowedTypes, null, true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestTarget = LocalVersionedTerminology.get().getConcept(
                        rel.getC2Id());
            }
        }

        return latestTarget;
    }
}
