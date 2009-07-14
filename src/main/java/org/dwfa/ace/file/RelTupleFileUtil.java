package org.dwfa.ace.file;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class RelTupleFileUtil {

    public static String exportTuple(I_RelTuple relTuple)
            throws TerminologyException, IOException {

        String result = "";
        I_TermFactory termFactory = LocalVersionedTerminology.get();

        UUID tupleUuid = ArchitectonicAuxiliary.Concept.REL_TUPLE.getUids()
                .iterator().next();
        UUID relUuid = termFactory.getUids(relTuple.getRelId()).iterator()
                .next();
        UUID c1Uuid = termFactory.getUids(relTuple.getC1Id()).iterator().next();
        UUID c2Uuid = termFactory.getUids(relTuple.getC2Id()).iterator().next();
        UUID charUuid = termFactory.getUids(relTuple.getCharacteristicId())
                .iterator().next();
        UUID groupUuid = termFactory.getUids(relTuple.getGroup()).iterator()
                .next();
        UUID refUuid = termFactory.getUids(relTuple.getRefinabilityId())
                .iterator().next();
        UUID relType = termFactory.getUids(relTuple.getTypeId()).iterator()
                .next();
        UUID pathUuid = termFactory.getUids(relTuple.getPathId()).iterator()
                .next();
        UUID statusUuid = termFactory.getUids(relTuple.getStatusId())
                .iterator().next();

        result = tupleUuid + "\t" + relUuid + "\t" + c1Uuid + "\t" + c2Uuid
                + "\t" + charUuid + "\t" + groupUuid + "\t" + refUuid + "\t"
                + relType + "\t" + pathUuid + "\t" + statusUuid + "\n";

        return result;
    }

    public static void importTuple(String inputLine)
            throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID relUuid = UUID.fromString(lineParts[1]);
            UUID c1Uuid = UUID.fromString(lineParts[2]);
            UUID c2Uuid = UUID.fromString(lineParts[3]);
            UUID charUuid = UUID.fromString(lineParts[4]);
            UUID groupUuid = UUID.fromString(lineParts[5]);
            UUID refUuid = UUID.fromString(lineParts[6]);
            UUID relTypeUuid = UUID.fromString(lineParts[7]);
            UUID pathUuid = UUID.fromString(lineParts[8]);
            UUID statusUuid = UUID.fromString(lineParts[9]);

            I_TermFactory termFactory = LocalVersionedTerminology.get();
            I_RelPart newPart = termFactory.newRelPart();

            newPart.setCharacteristicId(termFactory.getId(charUuid)
                    .getNativeId());
            newPart.setGroup(termFactory.getId(groupUuid).getNativeId());
            newPart.setPathId(termFactory.getId(pathUuid).getNativeId());
            newPart.setRefinabilityId(termFactory.getId(refUuid).getNativeId());
            newPart.setTypeId(termFactory.getId(relTypeUuid).getNativeId());
            newPart.setStatusId(termFactory.getId(statusUuid).getNativeId());

            if (termFactory.hasId(c1Uuid)) {

                I_IntSet allowedStatus = termFactory.newIntSet();
                allowedStatus.add(newPart.getStatusId());
                I_IntSet allowedTypes = termFactory.newIntSet();
                allowedStatus.add(newPart.getTypeId());

                I_GetConceptData concept = termFactory
                        .getConcept(new UUID[] { c1Uuid });
                Set<I_Position> positions = termFactory
                        .getActiveAceFrameConfig().getViewPositionSet();
                boolean returnConflictResolvedLatestState = true;
                boolean addUncommitted = true;

                // check if the part exists
                List<I_RelTuple> parts = concept.getSourceRelTuples(
                        allowedStatus, allowedTypes, positions, addUncommitted,
                        returnConflictResolvedLatestState);
                I_RelTuple latestPart = null;
                for (I_RelTuple part : parts) {
                    if (latestPart == null
                            || part.getVersion() >= latestPart.getVersion()) {
                        latestPart = part;
                    }
                }

                if (!latestPart.equals(newPart)) {
                    latestPart.getPart().hasNewData(newPart);
                    termFactory.addUncommitted(concept);
                }
            } else {
                // concept doesn't exist
                throw new TerminologyException(
                        "Concept with ID : "
                                + c1Uuid
                                + " does not exist in database. Cannot complete import of "
                                + inputLine);
            }
        } catch (Exception e) {
            throw new TerminologyException(
                    "Exception thrown while importing line: " + inputLine);
        }
    }
}
