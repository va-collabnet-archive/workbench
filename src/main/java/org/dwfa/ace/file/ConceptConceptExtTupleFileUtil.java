package org.dwfa.ace.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptConceptExtTupleFileUtil {

    public static String exportTuple(I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_TUPLE.getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId()).iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId()).iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId()).iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator().next(); // this

            if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids().iterator().next())) {
                throw new TerminologyException("Non concept-concept ext tuple passed to concept-concept file util.");
            }

            I_ThinExtByRefPartConceptConcept part = (I_ThinExtByRefPartConceptConcept) tuple.getPart();
            UUID c1Uuid = termFactory.getUids(part.getC1id()).iterator().next();
            UUID c2Uuid = termFactory.getUids(part.getC2id()).iterator().next();

            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator().next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId()).iterator().next();
            int version = tuple.getVersion();

            // String idTuple = IDTupleFileUtil.exportTuple(termFactory
            // .getId(memberUuid));

            return // idTuple + "\n" +
            tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t" + componentUuid + "\t" + typeUuid + "\t" + c1Uuid
                + "\t" + c2Uuid + "\t" + pathUuid + "\t" + statusUuid + "\t" + version + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            UUID pathToOverrideUuid) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID memberUuid;
            UUID refsetUuid;
            UUID componentUuid;
            UUID c1Uuid;
            UUID c2Uuid;
            UUID pathUuid;
            UUID statusUuid;
            int effectiveDate;

            try {
                memberUuid = UUID.fromString(lineParts[1]);
                refsetUuid = UUID.fromString(lineParts[2]);
                componentUuid = UUID.fromString(lineParts[3]);
                c1Uuid = UUID.fromString(lineParts[5]);
                c2Uuid = UUID.fromString(lineParts[6]);
                if (pathToOverrideUuid == null) {
                    pathUuid = UUID.fromString(lineParts[7]);
                } else {
                    pathUuid = pathToOverrideUuid;
                }
                statusUuid = UUID.fromString(lineParts[8]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Integer.parseInt(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse Integer from string -> Integer " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            RefsetHelper refsetHelper = new RefsetHelper();
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(pathUuid, pathUuid);
            }

            if (!termFactory.hasId(refsetUuid)) {
                String errorMessage = "Refset UUID has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(refsetUuid, pathUuid);
            }
            if (!termFactory.hasId(componentUuid)) {
                String errorMessage = "Component UUID has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(componentUuid, pathUuid);
            }
            if (!termFactory.hasId(c1Uuid)) {
                String errorMessage = "c1Uuid UUID has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(c1Uuid, pathUuid);
            }
            if (!termFactory.hasId(c2Uuid)) {
                String errorMessage = "c2Uuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(c2Uuid, pathUuid);
            }

            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(statusUuid, pathUuid);
            }

            try {
                refsetHelper.newConceptConceptRefsetExtension(termFactory.getId(refsetUuid).getNativeId(), termFactory
                    .getId(componentUuid).getNativeId(), termFactory.getId(c1Uuid).getNativeId(), termFactory.getId(
                    c2Uuid).getNativeId(), memberUuid, pathUuid, statusUuid, effectiveDate);
            } catch (Exception e) {
                String errorMessage = "Exception thrown while creating new concept-concept refset extension";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

        } catch (Exception e) {
            String errorMessage = "Exception of unknown cause thrown while importing concept-concept ext tuple";
            try {
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
