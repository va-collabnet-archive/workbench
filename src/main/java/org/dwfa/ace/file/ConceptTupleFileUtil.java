package org.dwfa.ace.file;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptTupleFileUtil {

    public static String exportTuple(I_GetConceptData concept)
            throws TerminologyException, IOException {

        RefsetUtilImpl refsetUtil = new RefsetUtilImpl();
        I_ConceptAttributePart part = refsetUtil
                .getLastestAttributePart(concept);
        I_TermFactory termFactory = LocalVersionedTerminology.get();

        UUID conceptTupleUuid = ArchitectonicAuxiliary.Concept.CON_TUPLE
                .getUids().iterator().next();
        UUID conceptUuid = termFactory.getUids(concept.getConceptId())
                .iterator().next();
        UUID statusUuid = termFactory.getUids(part.getStatusId()).iterator()
                .next();
        boolean isDefined = part.isDefined();
        UUID pathUuid = termFactory.getUids(part.getPathId()).iterator().next();
        int effectiveDate = part.getVersion();

        return conceptTupleUuid + "\t" + conceptUuid + "\t" + isDefined + "\t"
                + pathUuid + "\t" + statusUuid + "\t" + effectiveDate + "\n";
    }

    public static void importTuple(String inputLine)
            throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID conceptUuid = UUID.fromString(lineParts[1]);
            boolean isDefined = Boolean.parseBoolean(lineParts[2]);
            UUID pathUuid = UUID.fromString(lineParts[3]);
            UUID statusUuid = UUID.fromString(lineParts[4]);
            int effectiveDate = Integer.parseInt(lineParts[5]);

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (termFactory.hasId(conceptUuid)) {

                int conceptId = termFactory.getId(conceptUuid).getNativeId();
                I_IntSet allowedStatus = termFactory.newIntSet();
                allowedStatus.add(termFactory.getId(statusUuid).getNativeId());
                I_GetConceptData concept = termFactory.getConcept(conceptId);
                Set<I_Position> positions = termFactory
                        .getActiveAceFrameConfig().getViewPositionSet();
                boolean addUncommitted = true;
                boolean returnConflictResolvedLatestState = true;

                // check if the part exists
                List<I_ConceptAttributeTuple> parts = concept
                        .getConceptAttributeTuples(allowedStatus, positions,
                                addUncommitted,
                                returnConflictResolvedLatestState);
                I_ConceptAttributeTuple latestTuple = null;
                for (I_ConceptAttributeTuple part : parts) {
                    if (latestTuple == null
                            || part.getVersion() >= latestTuple.getVersion()) {
                        latestTuple = part;
                    }
                }

                if (latestTuple == null) {
                    throw new Exception(
                            "Concept UUID exists but has no tuples.");
                } else {
                    System.out.println("******** 1");
                    I_ConceptAttributePart newPart = latestTuple.getPart()
                            .duplicate();
                    newPart.setStatusId(termFactory.getId(statusUuid)
                            .getNativeId());
                    newPart.setDefined(isDefined);
                    newPart
                            .setPathId(termFactory.getId(pathUuid)
                                    .getNativeId());
                    newPart.setVersion(effectiveDate);

                    latestTuple.getConVersioned().addVersion(newPart);
                    termFactory.addUncommitted(concept);
                }

            } else {
                System.out.println("******** 2");
                // need to create concept + part
                I_GetConceptData newConcept = termFactory.newConcept(
                        conceptUuid, isDefined, termFactory
                                .getActiveAceFrameConfig());
                I_ConceptAttributeVersioned v = newConcept
                        .getConceptAttributes();

                I_ConceptAttributePart newPart = v.getVersions().get(0);
                // I_ConceptAttributePart newPart = termFactory
                // .newConceptAttributePart();
                newPart
                        .setStatusId(termFactory.getId(statusUuid)
                                .getNativeId());
                newPart.setDefined(isDefined);
                newPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                newPart.setVersion(effectiveDate);

                v.addVersion(newPart);
                termFactory.addUncommitted(newConcept);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(
                    "Exception thrown while importing line: " + inputLine);
        }
    }
}
