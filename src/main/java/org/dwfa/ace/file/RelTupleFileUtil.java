package org.dwfa.ace.file;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class RelTupleFileUtil {

    public static String exportTuple(I_RelTuple relTuple)
            throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.REL_TUPLE.getUids()
                    .iterator().next();
            UUID relUuid = termFactory.getUids(relTuple.getRelId()).iterator()
                    .next();
            UUID c1Uuid = termFactory.getUids(relTuple.getC1Id()).iterator()
                    .next();
            UUID c2Uuid = termFactory.getUids(relTuple.getC2Id()).iterator()
                    .next();
            UUID charUuid = termFactory.getUids(relTuple.getCharacteristicId())
                    .iterator().next();
            int group = relTuple.getGroup();
            UUID refUuid = termFactory.getUids(relTuple.getRefinabilityId())
                    .iterator().next();
            UUID relTypeUuid = termFactory.getUids(relTuple.getTypeId())
                    .iterator().next();
            UUID pathUuid = termFactory.getUids(relTuple.getPathId())
                    .iterator().next();
            UUID statusUuid = termFactory.getUids(relTuple.getStatusId())
                    .iterator().next();
            int effectiveDate = relTuple.getVersion();

            String idTuple = IDTupleFileUtil.exportTuple(termFactory
                    .getId(relUuid));

            return idTuple + tupleUuid + "\t" + relUuid + "\t" + c1Uuid + "\t"
                    + c2Uuid + "\t" + charUuid + "\t" + group + "\t" + refUuid
                    + "\t" + relTypeUuid + "\t" + pathUuid + "\t" + statusUuid
                    + "\t" + effectiveDate + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static void importTuple(String inputLine)
            throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID relUuid = UUID.fromString(lineParts[1]);
            UUID c1Uuid = UUID.fromString(lineParts[2]);
            UUID c2Uuid = UUID.fromString(lineParts[3]);
            UUID charUuid = UUID.fromString(lineParts[4]);
            int group = Integer.parseInt(lineParts[5]);
            UUID refUuid = UUID.fromString(lineParts[6]);
            UUID relTypeUuid = UUID.fromString(lineParts[7]);
            UUID pathUuid = UUID.fromString(lineParts[8]);
            UUID statusUuid = UUID.fromString(lineParts[9]);
            int effectiveDate = Integer.parseInt(lineParts[10]);

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (!termFactory.hasId(c1Uuid)) {
                throw new Exception(
                        "Relevant c1 UUID tuple must occur before reference to a UUID.");
            }
            if (!termFactory.hasId(relUuid)) {
                throw new Exception(
                        "Relevant REL UUID tuple must occur before reference to a UUID.");
            }
            if (!termFactory.hasId(c2Uuid)) {
                throw new Exception(
                        "Relevant c2 UUID tuple must occur before reference to a UUID.");
            }

            I_IntSet allowedStatus = termFactory.newIntSet();
            allowedStatus.add(termFactory.getId(statusUuid).getNativeId());
            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(termFactory.getId(relTypeUuid).getNativeId());

            I_GetConceptData concept = termFactory
                    .getConcept(new UUID[] { c1Uuid });
            Set<I_Position> positions = termFactory.getActiveAceFrameConfig()
                    .getViewPositionSet();
            boolean returnConflictResolvedLatestState = true;
            boolean addUncommitted = true;

            // check if the part exists
            List<I_RelTuple> parts = concept.getSourceRelTuples(allowedStatus,
                    allowedTypes, positions, addUncommitted,
                    returnConflictResolvedLatestState);
            I_RelTuple latestTuple = null;
            for (I_RelTuple part : parts) {
                if (latestTuple == null
                        || part.getVersion() >= latestTuple.getVersion()) {
                    latestTuple = part;
                }
            }

            if (latestTuple == null) {
                Collection<I_Path> paths = termFactory.getPaths();
                paths.clear();
                paths.add(termFactory.getPath(new UUID[] { pathUuid }));
                termFactory.uuidToNativeWithGeneration(relUuid,
                        ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                                .localize().getNid(), paths, effectiveDate);

                I_RelVersioned v = termFactory.newRelationship(relUuid,
                        concept, termFactory
                                .getConcept(new UUID[] { relTypeUuid }),
                        termFactory.getConcept(new UUID[] { c2Uuid }),
                        termFactory.getConcept(new UUID[] { charUuid }),
                        termFactory.getConcept(new UUID[] { refUuid }),
                        termFactory.getConcept(new UUID[] { statusUuid }),
                        group, termFactory.getActiveAceFrameConfig());

                I_RelPart newPart = v.getLastTuple().getPart();
                newPart.setCharacteristicId(termFactory.getId(charUuid)
                        .getNativeId());
                newPart.setGroup(group);
                newPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                newPart.setRefinabilityId(termFactory.getId(refUuid)
                        .getNativeId());
                newPart.setTypeId(termFactory.getId(relTypeUuid).getNativeId());
                newPart
                        .setStatusId(termFactory.getId(statusUuid)
                                .getNativeId());
                newPart.setVersion(effectiveDate);

                v.addVersion(newPart);
                termFactory.addUncommitted(concept);
                // termFactory.commit();
            } else {

                I_RelPart newPart = latestTuple.getPart().duplicate();
                newPart.setCharacteristicId(termFactory.getId(charUuid)
                        .getNativeId());
                newPart.setGroup(group);
                newPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                newPart.setRefinabilityId(termFactory.getId(refUuid)
                        .getNativeId());
                newPart.setTypeId(termFactory.getId(relTypeUuid).getNativeId());
                newPart
                        .setStatusId(termFactory.getId(statusUuid)
                                .getNativeId());
                newPart.setVersion(effectiveDate);

                latestTuple.getRelVersioned().addVersion(newPart);
                termFactory.addUncommitted(concept);
                // termFactory.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getLocalizedMessage()
                    + "Exception thrown while importing line: " + inputLine);
        }
    }
}
