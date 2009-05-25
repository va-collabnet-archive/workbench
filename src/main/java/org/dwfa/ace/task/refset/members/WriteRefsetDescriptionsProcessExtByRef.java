package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.refset.members.export.RefsetExportValidationException;
import org.dwfa.ace.task.refset.members.export.RefsetExportValidator;
import org.dwfa.ace.task.refset.members.export.RefsetWriter;
import org.dwfa.ace.task.refset.members.export.StatusUUIDs;
import org.dwfa.ace.task.util.Logger;

/**
 * An exporter of reference sets that can be shared between tasks and mojos. Given a reference set extension as a
 * <code>I_ThinExtByRefVersioned</code> exports it to a file with the reference set name.
 * @{link #processExtensionByReference} should be called for each extension and @{link #clean} should be called
 * after all extensions have been supplied to the @{link #processExtensionByReference} to release allocated resources.
 */
public final class WriteRefsetDescriptionsProcessExtByRef implements CleanableProcessExtByRef, StatusUUIDs {

    private final Logger logger;
    private final RefsetUtil refsetUtil;
    private final RefsetExportValidator refsetExportValidator;
    private final RefsetWriter refsetWriter;
    private final I_TermFactory termFactory;

    public WriteRefsetDescriptionsProcessExtByRef(final RefsetExportValidator refsetExportValidator,
          final RefsetWriter refsetWriter, final RefsetUtil refsetUtil, final I_TermFactory termFactory,
          final Logger logger) {
        this.termFactory = termFactory;
        this.logger = logger;
        this.refsetExportValidator = refsetExportValidator;
        this.refsetWriter = refsetWriter;
        this.refsetUtil = refsetUtil;
    }

    public void processExtensionByReference(final I_ThinExtByRefVersioned refset) throws Exception {
        I_GetConceptData refsetConcept = termFactory.getConcept(refset.getRefsetId());
        try {
            refsetExportValidator.validateIsConceptExtension(refset.getTypeId(), refsetUtil);
            refsetExportValidator.validateIsCurrent(refsetConcept, refsetUtil);
            refsetWriter.write(refset);
        } catch (RefsetExportValidationException e) {
            logger.logWarn(e.getMessage());
        }
    }

    /**
     * Call this method to close all open files.
     * @throws Exception If an exception occurs.
     */
    public void clean() throws Exception {
        logger.logInfo("Cleaning resources.");
        refsetWriter.closeFiles();
    }
}
