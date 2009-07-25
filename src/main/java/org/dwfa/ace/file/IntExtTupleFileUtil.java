package org.dwfa.ace.file;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class IntExtTupleFileUtil {

    public static String exportTuple(I_ThinExtByRefTuple tuple)
            throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.EXT_INT_TUPLE
                    .getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId())
                    .iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId())
                    .iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId())
                    .iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator()
                    .next();
            if (!typeUuid.equals(RefsetAuxiliary.Concept.INT_EXTENSION
                    .getUids().iterator().next())) {
                throw new TerminologyException(
                        "Non int ext tuple passed to int file util.");
            }

            I_ThinExtByRefPartInteger part = (I_ThinExtByRefPartInteger) tuple
                    .getPart();
            int value = part.getValue();
            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator()
                    .next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId())
                    .iterator().next();
            int effectiveDate = tuple.getVersion();

            // String idTuple = IDTupleFileUtil.exportTuple(termFactory
            // .getId(memberUuid));

            return // idTuple + "\n" +
            tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t"
                    + componentUuid + "\t" + typeUuid + "\t" + value + "\t"
                    + pathUuid + "\t" + statusUuid + "\t" + effectiveDate
                    + "\n";
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
            if (!typeUuid.equals(RefsetAuxiliary.Concept.INT_EXTENSION
                    .getUids().iterator().next())) {
                throw new TerminologyException(
                        "Non int ext string passed to int file util.");
            }
            int value = Integer.parseInt(lineParts[5]);
            UUID pathUuid = UUID.fromString(lineParts[6]);
            UUID statusUuid = UUID.fromString(lineParts[7]);
            int effectiveDate = Integer.parseInt(lineParts[8]);

            RefsetHelper refsetHelper = new RefsetHelper();
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            /*
             * if (!termFactory.hasId(memberUuid)) { throw new Exception(
             * "Relevant ID tuple must occur before reference to a UUID."); }
             */

            refsetHelper.newIntRefsetExtension(termFactory.getId(refsetUuid)
                    .getNativeId(), termFactory.getId(componentUuid)
                    .getNativeId(), value, memberUuid, pathUuid, statusUuid,
                    effectiveDate);

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(
                    "Exception thrown while importing line: " + inputLine);
        }
    }
}
