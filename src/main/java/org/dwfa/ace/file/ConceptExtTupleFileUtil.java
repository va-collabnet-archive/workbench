package org.dwfa.ace.file;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptExtTupleFileUtil {

    public static String exportTuple(I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.EXT_CONCEPT_TUPLE.getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId()).iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId()).iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId()).iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator().next();
            if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next())) {
                throw new TerminologyException("Non concept ext tuple passed to concept file util.");
            }

            I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple.getPart();
            UUID conceptUuid = termFactory.getUids(part.getConceptId()).iterator().next();
            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator().next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId()).iterator().next();
            int effectiveDate = tuple.getVersion();

            // String idTuple = IDTupleFileUtil.exportTuple(termFactory
            // .getId(memberUuid));

            return // idTuple + "\n" +
            tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t" + componentUuid + "\t" + typeUuid + "\t"
                + conceptUuid + "\t" + pathUuid + "\t" + statusUuid + "\t" + effectiveDate + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static void importTuple(String inputLine) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID memberUuid = UUID.fromString(lineParts[1]);
            UUID refsetUuid = UUID.fromString(lineParts[2]);
            UUID componentUuid = UUID.fromString(lineParts[3]);
            UUID typeUuid = UUID.fromString(lineParts[4]);
            if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next())) {
                throw new TerminologyException("Non concept ext string passed to concept file util.");
            }
            UUID conceptUuid = UUID.fromString(lineParts[5]);
            UUID pathUuid = UUID.fromString(lineParts[6]);
            UUID statusUuid = UUID.fromString(lineParts[7]);
            int effectiveDate = Integer.parseInt(lineParts[8]);

            RefsetHelper refsetHelper = new RefsetHelper();
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            /*
             * if (!termFactory.hasId(memberUuid)) { throw new Exception(
             * "Relevant ID tuple must occur before reference to a UUID."); }
             */

            if (!termFactory.hasId(refsetUuid)) {
                throw new Exception("Refset UUID : " + refsetUuid.toString() + " referenced but doesn't exist.");
            }
            if (!termFactory.hasId(componentUuid)) {
                throw new Exception("Component UUID : " + componentUuid.toString() + " referenced but doesn't exist.");
            }
            if (!termFactory.hasId(pathUuid)) {
                throw new Exception("path UUID : " + pathUuid.toString() + " referenced but doesn't exist.");
            }
            if (!termFactory.hasId(statusUuid)) {
                throw new Exception("status UUID : " + statusUuid.toString() + " referenced but doesn't exist.");
            }

            refsetHelper.newConceptRefsetExtension(termFactory.getId(refsetUuid).getNativeId(), termFactory.getId(
                componentUuid).getNativeId(), termFactory.getId(conceptUuid).getNativeId(), memberUuid, pathUuid,
                statusUuid, effectiveDate);

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException("Exception thrown while importing line: " + inputLine);
        }
    }
}
