package org.dwfa.ace.file;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptConceptStringExtTupleFileUtil {

    public static String exportTuple(I_ThinExtByRefTuple tuple)
            throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_STRING_TUPLE
                    .getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId())
                    .iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId())
                    .iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId())
                    .iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator()
                    .next(); // this should be concept concept
            if (!typeUuid
                    .equals(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION
                            .getUids().iterator().next())) {
                throw new TerminologyException(
                        "Non concept-concept-string ext tuple passed to concept-concept-string file util.");
            }

            I_ThinExtByRefPartConceptConceptString part = (I_ThinExtByRefPartConceptConceptString) tuple
                    .getPart();
            UUID c1Uuid = termFactory.getUids(part.getC1id()).iterator().next();
            UUID c2Uuid = termFactory.getUids(part.getC2id()).iterator().next();
            String strValue = part.getStr();

            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator()
                    .next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId())
                    .iterator().next();

            return tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t"
                    + componentUuid + "\t" + typeUuid + "\t" + c1Uuid + "\t"
                    + c2Uuid + "\t" + strValue + "\t" + pathUuid + "\t"
                    + statusUuid + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static void importTuple(String inputLine)
            throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID memberUuid = UUID.fromString(lineParts[1]);
            UUID refsetUuid = UUID.fromString(lineParts[2]);
            UUID componentUuid = UUID.fromString(lineParts[3]);
            UUID typeUuid = UUID.fromString(lineParts[4]);
            if (!typeUuid
                    .equals(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION
                            .getUids().iterator().next())) {
                throw new TerminologyException(
                        "Non concept-concept-string ext string passed to concept-concept-string file util.");
            }
            UUID c1Uuid = UUID.fromString(lineParts[5]);
            UUID c2Uuid = UUID.fromString(lineParts[6]);
            String strValue = lineParts[7];
            UUID pathUuid = UUID.fromString(lineParts[8]);
            UUID statusUuid = UUID.fromString(lineParts[9]);

            RefsetHelper refsetHelper = new RefsetHelper();
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            refsetHelper.newConceptConceptStringRefsetExtension(termFactory
                    .getId(refsetUuid).getNativeId(), termFactory.getId(
                    componentUuid).getNativeId(), termFactory.getId(c1Uuid)
                    .getNativeId(), termFactory.getId(c2Uuid).getNativeId(),
                    strValue, memberUuid, pathUuid, statusUuid,
                    Integer.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(
                    "Exception thrown while importing line: " + inputLine);
        }
    }
}
