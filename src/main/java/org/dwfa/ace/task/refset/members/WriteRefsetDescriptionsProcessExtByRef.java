package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
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

/**
 * An exporter of reference sets that can be shared between tasks and mojos. Given a reference set extension as a
 * <code>I_ThinExtByRefVersioned</code> exports it to a file with the reference set name.
 * @{link #processExtensionByReference} should be called for each extension and @{link #clean} should be called
 * after all extensions have been supplied to the @{link #processExtensionByReference} to release allocated resources.
 */
public final class WriteRefsetDescriptionsProcessExtByRef implements CleanableProcessExtByRef {

    private static final Collection<UUID> CURRENT_STATUS_UUIDS =
            ArchitectonicAuxiliary.Concept.CURRENT.getUids();
    private static final Collection<UUID> PREFERED_TERM_UUIDS =
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids();
    private static final Collection<UUID> FULLY_SPECIFIED_UUIDS =
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids();
    private static final String CONCEPTS_WITH_NO_DESCS_KEY = "_CONCEPTS_WITH_NO_DESCS_KEY_";
    private static final String CONCEPT_ID_HEADER = "Concept ID";
    private static final String PREFERRED_TERM_HEADER = "PREFERRED_TERM";
    
    private final Logger logger;
    private final File outputDirectory;
    private final LogMill logMill;
    private final Map<String, Writer> fileMap;
    private final Map<String, Integer> progressMap;
    private final RefsetUtil refsetUtil;
    private final String lineSeparator;


    public WriteRefsetDescriptionsProcessExtByRef(final Logger logger, final File outputDirectory) {
        this.logger = logger;
        this.outputDirectory = outputDirectory;
        fileMap = new HashMap<String, Writer>();
        progressMap = new HashMap<String, Integer>();        
        logMill = new SimpleLogMill();
        refsetUtil = new RefsetUtilImpl();
        lineSeparator = System.getProperty("line.separator");
    }

    public void processExtensionByReference(final I_ThinExtByRefVersioned refset) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData refsetConcept = termFactory.getConcept(refset.getRefsetId());

        if (refset.getTypeId() != RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) {
            logMill.logInfo(logger, "Skipping non-concept type refset " +
                    refsetConcept.getId().getUIDs().iterator().next());
            return;
        }


        I_ConceptAttributePart latestAttributePart = refsetUtil.getLastestAttributePart(refsetConcept);
        if (latestAttributePart == null || latestAttributePart.getConceptStatus() !=
                ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()) {
            logMill.logInfo(logger, "Skipping non-current refset " + refsetConcept.getId().getUIDs().iterator().next());
            return;
        }

        writeRefset(refset, termFactory);
    }

    /**
     * Call this method to close all open files.
     * @throws Exception If an exception occurs.
     */
    @Override
    public void clean() throws Exception {
        logMill.logInfo(logger, "closing files.");
        closeFiles();
    }    

    private void writeRefset(final I_ThinExtByRefVersioned refset, final I_TermFactory termFactory) throws Exception {
        I_IntSet status = refsetUtil.createIntSet(termFactory, CURRENT_STATUS_UUIDS);
        I_IntSet fsn = refsetUtil.createIntSet(termFactory, FULLY_SPECIFIED_UUIDS);
        I_IntSet preferredTerm = refsetUtil.createIntSet(termFactory, PREFERED_TERM_UUIDS);

        I_DescriptionTuple refsetName = refsetUtil.assertExactlyOne(
                termFactory.getConcept((refset.getRefsetId())).getDescriptionTuples(status, fsn, null));
        logProgress(refsetName.getText());

        Writer writer = getWriter(refsetName.getText());        

        try {
            I_ThinExtByRefPart part = getLatestVersionIfCurrent(refset, termFactory);
            if (part != null) {
                writeRefset(refset, termFactory, status, preferredTerm, writer, part);
            }
        } catch (Exception e) {
            logMill.logWarn(logger, e.getMessage());
        }
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
            Writer noDescriptionWriter = getWriterForConceptsWithoutDescriptions();
            noDescriptionWriter.append("Concept " + conceptUuids + " has no active preferred term");
            noDescriptionWriter.append(lineSeparator);
        } else {
            if (value.getConceptId() != ConceptConstants.PARENT_MARKER.localize().getNid()) {
                I_DescriptionTuple descriptionTuple = descriptionTuples.iterator().next();
                writer.append(lineSeparator);                
                writer.append(refsetUtil.getSnomedId(concept.getConceptId(), termFactory));
                writer.append("\t");
                writer.append(descriptionTuple.getText());
            }
        }
    }

    private I_ThinExtByRefPart getLatestVersionIfCurrent(final I_ThinExtByRefVersioned ext,
        final I_TermFactory termFactory) throws TerminologyException, IOException {
        I_ThinExtByRefPart latest = refsetUtil.getLatestVersionIfCurrent(ext, termFactory);

        if (latest != null && !(latest.getStatus() == termFactory.getConcept(CURRENT_STATUS_UUIDS).getConceptId())) {
            latest = null;
        }

        return latest;
    }

    private Writer getWriterForConceptsWithoutDescriptions() throws Exception {
        return getWriter(CONCEPTS_WITH_NO_DESCS_KEY, "Concepts with no descriptions.txt", false);
    }

    private Writer getWriter(final String text) throws IOException {
        return getWriter(text, text + ".refset.text", true);
    }

    private Writer getWriter(String key, String fileName, final boolean addHeader) throws IOException {
        if (!fileMap.containsKey(key)) {
            File outputFile = new File(outputDirectory, fileName);
            if (outputFile.getParentFile().mkdirs()) {
                logMill.logInfo(logger, "making directory - " + outputFile.getParentFile());
            }

            outputFile.createNewFile();
            fileMap.put(key, new BufferedWriter(new FileWriter(outputFile)));

            if (addHeader) {
                addHeader(fileMap.get(key));
            }
        }

        return fileMap.get(key);
    }

    private void addHeader(final Writer writer) throws IOException {
        writer.append(CONCEPT_ID_HEADER);
        writer.append("\t");
        writer.append(PREFERRED_TERM_HEADER);
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

    private void closeFiles() throws IOException {
        for (Writer fileWriter : fileMap.values()) {
            flushAndClose(fileWriter);
        }
    }

    private void flushAndClose(final Writer fileWriter) throws IOException {
        fileWriter.flush();
        fileWriter.close();
    }
}
