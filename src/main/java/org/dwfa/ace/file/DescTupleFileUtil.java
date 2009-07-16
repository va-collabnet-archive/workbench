package org.dwfa.ace.file;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class DescTupleFileUtil {

    public static String exportTuple(I_DescriptionTuple descTuple)
            throws TerminologyException, IOException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        UUID tupleUuid = ArchitectonicAuxiliary.Concept.DESC_TUPLE.getUids()
                .iterator().next();
        UUID conceptUuid = termFactory.getUids(descTuple.getConceptId())
                .iterator().next();
        UUID descUuid = termFactory.getUids(descTuple.getDescId()).iterator()
                .next();
        String text = descTuple.getText();
        String lang = descTuple.getLang();
        UUID typeUuid = termFactory.getUids(descTuple.getTypeId()).iterator()
                .next();
        UUID pathUuid = termFactory.getUids(descTuple.getPathId()).iterator()
                .next();
        UUID statusUuid = termFactory.getUids(descTuple.getStatusId())
                .iterator().next();
        boolean initialCapSignificant = descTuple.getInitialCaseSignificant();

        return tupleUuid + "\t" + conceptUuid + "\t" + descUuid + "\t" + text
                + "\t" + lang + "\t" + initialCapSignificant + "\t" + typeUuid
                + "\t" + pathUuid + "\t" + statusUuid + "\n";
    }

    public static void importTuple(String inputLine)
            throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID conceptUuid = UUID.fromString(lineParts[1]);
            UUID descUuid = UUID.fromString(lineParts[2]);
            String text = lineParts[3];
            String lang = lineParts[4];
            boolean initialCapSignificant = Boolean.parseBoolean(lineParts[5]);
            UUID typeUuid = UUID.fromString(lineParts[6]);
            UUID pathUuid = UUID.fromString(lineParts[7]);
            UUID statusUuid = UUID.fromString(lineParts[8]);

            I_TermFactory termFactory = LocalVersionedTerminology.get();
            I_DescriptionPart newPart = termFactory.newDescriptionPart();

            newPart.setLang(lang);
            newPart.setText(text);
            newPart.setInitialCaseSignificant(initialCapSignificant);
            newPart.setTypeId(termFactory.getId(typeUuid).getNativeId());
            newPart.setStatusId(termFactory.getId(statusUuid).getNativeId());
            newPart.setPathId(termFactory.getId(pathUuid).getNativeId());

            if (termFactory.hasId(conceptUuid)) {

                int conceptId = termFactory.getId(conceptUuid).getNativeId();
                I_IntSet allowedStatus = termFactory.newIntSet();
                allowedStatus.add(newPart.getStatusId());
                I_IntSet allowedTypes = termFactory.newIntSet();
                allowedTypes.add(newPart.getTypeId());
                I_GetConceptData concept = termFactory.getConcept(conceptId);
                Set<I_Position> positions = termFactory
                        .getActiveAceFrameConfig().getViewPositionSet();
                boolean returnConflictResolvedLatestState = true;

                // check if the part exists
                List<I_DescriptionTuple> parts = concept.getDescriptionTuples(
                        allowedStatus, allowedTypes, positions,
                        returnConflictResolvedLatestState);
                I_DescriptionTuple latestTuple = null;
                for (I_DescriptionTuple part : parts) {
                    if (latestTuple == null
                            || part.getVersion() >= latestTuple.getVersion()) {
                        latestTuple = part;
                    }
                }

                if (latestTuple == null) {
                    Collection<I_Path> paths = termFactory.getPaths();
                    paths.clear();
                    paths.add(termFactory.getPath(new UUID[] { pathUuid }));
                    termFactory.uuidToNativeWithGeneration(descUuid,
                            ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                                    .localize().getNid(), paths,
                            Integer.MAX_VALUE);

                    I_DescriptionVersioned v = termFactory.newDescription(
                            descUuid, concept, lang, text, termFactory
                                    .getConcept(new UUID[] { typeUuid }),
                            termFactory.getActiveAceFrameConfig());
                    v.addVersion(newPart);
                    termFactory.addUncommitted(concept);
                } else if (!latestTuple.getPart().equals(newPart)) {
                    latestTuple.getPart().hasNewData(newPart);
                    termFactory.addUncommitted(concept);
                }
            } else {
                // concept doesn't exist
                throw new TerminologyException(
                        "Concept with ID : "
                                + conceptUuid
                                + " does not exist in database. Cannot complete import of "
                                + inputLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(
                    "Exception thrown while importing line: " + inputLine);
        }
    }
}
