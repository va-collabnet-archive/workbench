package org.dwfa.ace.file;

import java.io.BufferedWriter;
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

    public static I_GetConceptData lastConcept = null;

    public static String exportTuple(I_GetConceptData concept) throws TerminologyException, IOException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        String idTuple = IDTupleFileUtil.exportTuple(termFactory.getId(concept.getUids().iterator().next()));

        RefsetUtilImpl refsetUtil = new RefsetUtilImpl();
        I_ConceptAttributePart part = refsetUtil.getLastestAttributePart(concept);

        UUID conceptTupleUuid = ArchitectonicAuxiliary.Concept.CON_TUPLE.getUids().iterator().next();
        UUID conceptUuid = termFactory.getUids(concept.getConceptId()).iterator().next();
        UUID statusUuid = termFactory.getUids(part.getStatusId()).iterator().next();
        boolean isDefined = part.isDefined();
        UUID pathUuid = termFactory.getUids(part.getPathId()).iterator().next();
        int effectiveDate = part.getVersion();

        return idTuple + conceptTupleUuid + "\t" + conceptUuid + "\t" + isDefined + "\t" + pathUuid + "\t" + statusUuid
            + "\t" + effectiveDate + "\n";
    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            UUID pathToOverrideUuid) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID conceptUuid;
            boolean isDefined;
            UUID pathUuid;
            UUID statusUuid;
            int effectiveDate;

            try {
                conceptUuid = UUID.fromString(lineParts[1]);
                if (pathToOverrideUuid == null) {
                    pathUuid = UUID.fromString(lineParts[3]);
                } else {
                    pathUuid = pathToOverrideUuid;
                }
                statusUuid = UUID.fromString(lineParts[4]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                isDefined = Boolean.parseBoolean(lineParts[2]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse boolean from string -> boolean " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Integer.parseInt(lineParts[5]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse integer from string -> integer " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(pathUuid, pathUuid);
            }
            if (!termFactory.hasId(conceptUuid)) {
                String errorMessage = "conceptUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(conceptUuid, pathUuid);
            }
            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(statusUuid, pathUuid);
            }

            if (termFactory.hasConcept(termFactory.getId(conceptUuid).getNativeId())) {

                int conceptId = termFactory.getId(conceptUuid).getNativeId();
                I_IntSet allowedStatus = termFactory.newIntSet();
                allowedStatus.add(termFactory.getId(statusUuid).getNativeId());
                I_GetConceptData concept = termFactory.getConcept(conceptId);
                lastConcept = concept;
                Set<I_Position> positions = termFactory.getActiveAceFrameConfig().getViewPositionSet();
                boolean addUncommitted = true;
                boolean returnConflictResolvedLatestState = true;

                // check if the part exists
                List<I_ConceptAttributeTuple> parts =
                        concept.getConceptAttributeTuples(allowedStatus, positions, addUncommitted,
                            returnConflictResolvedLatestState);
                I_ConceptAttributeTuple latestTuple = null;
                for (I_ConceptAttributeTuple part : parts) {
                    if (latestTuple == null || part.getVersion() >= latestTuple.getVersion()) {
                        latestTuple = part;
                    }
                }

                if (latestTuple == null) {
                    throw new Exception("Concept UUID exists but has no tuples.");
                } else {
                    I_ConceptAttributePart newPart = latestTuple.getPart().duplicate();
                    newPart.setStatusId(termFactory.getId(statusUuid).getNativeId());
                    newPart.setDefined(isDefined);
                    newPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                    newPart.setVersion(effectiveDate);

                    latestTuple.getConVersioned().addVersion(newPart);
                    termFactory.addUncommitted(concept);
                    // termFactory.commit();
                }
            } else {
                // need to create concept + part
                I_GetConceptData newConcept =
                        termFactory.newConcept(conceptUuid, isDefined, termFactory.getActiveAceFrameConfig());
                I_ConceptAttributeVersioned v = newConcept.getConceptAttributes();

                I_ConceptAttributePart newPart = v.getVersions().get(0).duplicate();
                newPart.setStatusId(termFactory.getId(statusUuid).getNativeId());
                newPart.setDefined(isDefined);
                newPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                newPart.setVersion(effectiveDate);
                v.addVersion(newPart);
                termFactory.addUncommitted(newConcept);
                lastConcept = newConcept;
                // termFactory.commit();
            }
        } catch (Exception e) {
            String errorMessage = "Exception of unknown cause thrown while importing concept tuple";
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

    public static I_GetConceptData getLastConcept() {
        return lastConcept;
    }
}
