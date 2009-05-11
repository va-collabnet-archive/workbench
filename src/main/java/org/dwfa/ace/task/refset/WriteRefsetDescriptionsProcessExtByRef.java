package org.dwfa.ace.task.refset;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.task.util.LogMill;
import org.dwfa.ace.task.util.Logger;
import org.dwfa.ace.task.util.SimpleLogMill;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WriteRefsetDescriptionsProcessExtByRef implements I_ProcessExtByRef {

    private static final Collection<UUID> CURRENT_STATUS_UUIDS = ArchitectonicAuxiliary.Concept.CURRENT.getUids();
    private static final Collection<UUID> PREFERED_TERM_UUIDS = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids();
    private static final Collection<UUID> FULLY_SPECIFIED_UUIDS = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids();

    private final Map<String, Writer> fileMap;
    private final Map<String, Integer> progressMap;
    private Writer noDescriptionWriter;
    private final Logger logger;
    private final String outputPath;
    private LogMill logMill;

    public WriteRefsetDescriptionsProcessExtByRef(final Logger logger, final String outputPath) {
        this.logger = logger;
        this.outputPath = outputPath;
        fileMap = new HashMap<String, Writer>();
        progressMap = new HashMap<String, Integer>();
        logMill = new SimpleLogMill();
    }

    public void processExtensionByReference(final I_ThinExtByRefVersioned refset) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData refsetConcept = termFactory.getConcept(refset.getRefsetId());

        if (refset.getTypeId() != RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) {
            logMill.logInfo(logger, "Skipping non-concept type refset " + refsetConcept.getId().getUIDs().iterator().next());
            return;
        }


        I_ConceptAttributePart latestAttributePart = getLastestAttributePart(refsetConcept);
        if (latestAttributePart == null || latestAttributePart.getConceptStatus() !=
                ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()) {
            logMill.logInfo(logger, "Skipping non-current refset " + refsetConcept.getId().getUIDs().iterator().next());
            return;
        }

        writeRefset(refset, termFactory);
    }

    private void writeRefset(final I_ThinExtByRefVersioned refset, final I_TermFactory termFactory) throws Exception {
        noDescriptionWriter = createFileForConceptsWithoutDescriptions(outputPath);
        I_IntSet status = createIntSet(termFactory, CURRENT_STATUS_UUIDS);
        I_IntSet fsn = createIntSet(termFactory, FULLY_SPECIFIED_UUIDS);
        I_IntSet preferredTerm = createIntSet(termFactory, PREFERED_TERM_UUIDS);

        I_DescriptionTuple refsetName = assertExactlyOne(
                termFactory.getConcept((refset.getRefsetId())).getDescriptionTuples(status, fsn, null));
        logProgress(refsetName.getText());

        Writer writer = getWriter(refsetName.getText());
        I_ThinExtByRefPart part = getLatestVersionIfCurrent(refset, termFactory);

        try {
            if (part != null) {
                writeRefset(refset, termFactory, status, preferredTerm, writer, part);
            }
        } catch (Exception e) {
            logMill.logWarn(logger, e.getMessage());
        } finally {
            closeFiles();
        }
    }

    private I_ConceptAttributePart getLastestAttributePart(final I_GetConceptData refsetConcept) throws IOException {
        List<I_ConceptAttributePart> refsetAttibuteParts = refsetConcept.getConceptAttributes().getVersions();
        I_ConceptAttributePart latestAttributePart = null;
        for (I_ConceptAttributePart attributePart : refsetAttibuteParts) {
            if (latestAttributePart == null || attributePart.getVersion() >= latestAttributePart.getVersion()) {
                latestAttributePart = attributePart;
            }
        }
        return latestAttributePart;
    }

    private void writeRefset(final I_ThinExtByRefVersioned refset, final I_TermFactory termFactory,
                             final I_IntSet status, final I_IntSet preferredTerm, final Writer writer,
                             final I_ThinExtByRefPart part) throws Exception {
        I_GetConceptData concept = termFactory.getConcept((refset.getComponentId()));
        String conceptUuids = concept.getUids().iterator().next().toString();
        I_GetConceptData value = termFactory.getConcept(((I_ThinExtByRefPartConcept) part).getConceptId());

        List<I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(status, preferredTerm, null);
        if (descriptionTuples.size() == 0) {
            logMill.logWarn(logger, "Concept " + conceptUuids + " has no active preferred term");
            noDescriptionWriter.append("Concept " + conceptUuids + " has no active preferred term");
            noDescriptionWriter.append("\r\n");
        } else {
            String conceptName;
            int descriptionId;
            I_DescriptionTuple descriptionTuple = descriptionTuples.iterator().next();
            conceptName = descriptionTuple.getText();
            descriptionId = descriptionTuple.getDescId();
            if (value.getConceptId() != ConceptConstants.PARENT_MARKER.localize().getNid()) {
                writer.append(getSnomedId(descriptionId, termFactory));
                writer.append("\t");
                writer.append(getSnomedId(concept.getConceptId(), termFactory));
                writer.append("\t");
                writer.append(conceptName);
                writer.append("\t");
                writer.append(value.toString());
                writer.append("\r\n");
            }
        }
    }

    private I_IntSet createIntSet(final I_TermFactory termFactory, final Collection<UUID> uuid) throws Exception {
        I_IntSet status = termFactory.newIntSet();
        status.add(termFactory.getConcept(uuid).getConceptId());
        status.add(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(uuid));
        return status;
    }

    private void closeFiles() throws IOException {
        for (final Writer fileWriter : fileMap.values()) {
            flushAndClose(fileWriter);
        }

        flushAndClose(noDescriptionWriter);
    }

    private void flushAndClose(final Writer fileWriter) throws IOException {
        fileWriter.flush();
        fileWriter.close();
    }

    public I_ThinExtByRefPart getLatestVersionIfCurrent(final I_ThinExtByRefVersioned ext, final I_TermFactory termFactory)
            throws TerminologyException, IOException {
        I_ThinExtByRefPart latest = null;
        List<? extends I_ThinExtByRefPart> versions = ext.getVersions();
        for (final I_ThinExtByRefPart version : versions) {

            if (latest == null) {
                latest = version;
            } else {
                if (latest.getVersion()<version.getVersion()) {
                    latest = version;
                }
            }
        }

        if (latest != null && !(latest.getStatus() == termFactory.getConcept(CURRENT_STATUS_UUIDS).getConceptId())) {
            latest = null;
        }

        return latest;
    }

    private String getSnomedId(final int nid, final I_TermFactory termFactory) throws IOException, TerminologyException {

        if (nid == 0) {
            return "no identifier";
        }

        I_IdVersioned idVersioned = termFactory.getId(nid);
        for (final I_IdPart idPart : idVersioned.getVersions()) {
            if (idPart.getSource() == termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids())) {
                return idPart.getSourceId().toString();
            }
        }

        return "no SCTID found";
    }

    private Writer createFileForConceptsWithoutDescriptions(String outputDirectoryPath) throws Exception {
        File outputDirectory = new File(outputDirectoryPath);
        Writer noDescriptionWriter = new BufferedWriter(new FileWriter(new File(outputDirectory, "Concepts with no descriptions.txt")));

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        return  noDescriptionWriter;
    }    

    private Writer getWriter(final String text) throws IOException {
        Writer writer = fileMap.get(text);
        if (writer == null) {
            File outputFile = new File(new File(outputPath), text + ".refset.text");
            System.out.println("making directory - " + outputFile.getParentFile());
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            writer = new BufferedWriter(new FileWriter(outputFile));
            fileMap.put(text, writer);
        }
        return writer;
    }

    private <T> T assertExactlyOne(
            final Collection<T> collection) {
        assert collection.size() == 1 : "Collection " + collection + " was expected to only have one element";
        return collection.iterator().next();
    }

    private void logProgress(final String refsetName) {
        Integer progress = progressMap.get(refsetName);
        if (progress == null) {
            progress = 0;
        }

        progressMap.put(refsetName, progress++);

        if (progress % 1000 == 0) {
            logMill.logInfo(logger, "Exported " + progress + " of refset " + refsetName);
        }
    }
}
