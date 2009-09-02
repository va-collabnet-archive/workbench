package org.dwfa.ace.file;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class IDTupleFileUtil {

    public static String exportTuple(I_IdVersioned iIdVersioned) throws TerminologyException, IOException {

        List<I_IdPart> parts = iIdVersioned.getVersions();
        I_IdPart latestPart = null;
        for (I_IdPart part : parts) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                latestPart = part;
            }
        }

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        UUID tupleUuid = ArchitectonicAuxiliary.Concept.ID_TUPLE.getUids().iterator().next();
        UUID primaryUuid = termFactory.getUids(iIdVersioned.getNativeId()).iterator().next();
        UUID sourceSystemUuid = termFactory.getUids(latestPart.getSource()).iterator().next();

        Object sourceId = latestPart.getSourceId();

        UUID pathUuid = termFactory.getUids(latestPart.getPathId()).iterator().next();
        UUID statusUuid = termFactory.getUids(latestPart.getStatusId()).iterator().next();
        int effectiveDate = latestPart.getVersion();

        return tupleUuid + "\t" + primaryUuid + "\t" + sourceSystemUuid + "\t" + sourceId + "\t" + pathUuid + "\t"
            + statusUuid + "\t" + effectiveDate + "\n";
    }

    public static void importTuple(String inputLine) throws TerminologyException {

        try {

            I_TermFactory termFactory = LocalVersionedTerminology.get();
            String[] lineParts = inputLine.split("\t");

            UUID primaryUuid = UUID.fromString(lineParts[1]);
            UUID sourceSystemUuid = UUID.fromString(lineParts[2]);
            String sourceId = lineParts[3];

            UUID pathUuid = UUID.fromString(lineParts[4]);
            UUID statusUuid = UUID.fromString(lineParts[5]);
            int effectiveDate = Integer.parseInt(lineParts[6]);

            if (!termFactory.hasId(primaryUuid)) {
                termFactory.uuidToNativeWithGeneration(primaryUuid, termFactory.getId(
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids()).getNativeId(), termFactory
                    .getPath(new UUID[] { pathUuid }), effectiveDate);

                I_IdVersioned versioned = termFactory.getId(primaryUuid);
                I_IdPart part = versioned.getVersions().get(0).duplicate();
                part.setStatusId(termFactory.uuidToNative(statusUuid));
                part.setPathId(termFactory.uuidToNative(pathUuid));
                part.setSource(termFactory.uuidToNative(sourceSystemUuid));
                part.setVersion(effectiveDate);
                if (sourceSystemUuid
                    .equals(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids().iterator().next())) {
                    part.setSourceId(UUID.fromString(sourceId));
                } else if (sourceSystemUuid.equals(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator()
                    .next())) {
                    part.setSourceId(new Long(sourceId));
                } else {
                    part.setSourceId(sourceId); // use string
                }

                if (!versioned.hasVersion(part)) {
                    versioned.addVersion(part);
                    termFactory.writeId(versioned);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException("Exception thrown while importing line: " + inputLine);
        }
    }
}
