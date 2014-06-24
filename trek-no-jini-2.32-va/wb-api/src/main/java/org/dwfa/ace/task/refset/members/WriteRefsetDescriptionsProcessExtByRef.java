/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.refset.members.export.RefsetExportValidationException;
import org.dwfa.ace.task.refset.members.export.RefsetExportValidator;
import org.dwfa.ace.task.refset.members.export.RefsetWriter;
import org.dwfa.ace.task.util.Logger;

/**
 * An exporter of reference sets that can be shared between tasks and mojos.
 * Given a reference set extension as a <code>I_ExtendByRef</code>
 * exports it to a file with the reference set name.
 * {@link #processExtensionByReference} should be called for each extension and
 * 
 * @{link #clean} should be called
 *        after all extensions have been supplied to the @{link
 *        #processExtensionByReference} to release allocated resources.
 */
public final class WriteRefsetDescriptionsProcessExtByRef implements CleanableProcessExtByRef {

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

    public void processExtensionByReference(final I_ExtendByRef refset) throws Exception {
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
     * 
     * @throws Exception If an exception occurs.
     */
    public void clean() throws Exception {
        logger.logInfo("Cleaning resources.");
        refsetWriter.closeFiles();
    }
}
