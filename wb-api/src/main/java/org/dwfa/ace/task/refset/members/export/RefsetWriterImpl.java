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
package org.dwfa.ace.task.refset.members.export;

import java.util.List;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;

// TODO: This class does too much. Refactor.
public final class RefsetWriterImpl implements RefsetWriter {

    private final RefsetUtil refsetUtil;
    private final ProgressLogger progressLogger;
    private final I_TermFactory termFactory;
    private final Logger logger;
    private final RefsetTextWriter refsetTextWriter;
    private final WriterFactory writerFactory;
    private DescriptionSelector descSelector;

    public RefsetWriterImpl(final RefsetWriterParameterObject refsetWriterParameterObject,
            final CommonAPIParameterObject commonAPIParameterObject, DescriptionSelector descSelector) {
        refsetUtil = commonAPIParameterObject.getRefsetUtil();
        termFactory = commonAPIParameterObject.getTermFactory();
        logger = commonAPIParameterObject.getLogger();
        this.descSelector = descSelector;

        progressLogger = refsetWriterParameterObject.getProgressLogger();
        refsetTextWriter = refsetWriterParameterObject.getRefsetTextWriter();
        writerFactory = refsetWriterParameterObject.getWriterFactory();
    }

    public void write(final I_ExtendByRef refset) throws Exception {
        try {
            List<? extends I_DescriptionTuple> refsetDescriptionsList = refsetUtil.getFSNDescriptionsForConceptHavingCurrentStatus(
                termFactory, refset.getRefsetId());

            int componentId = refset.getComponentId();
            I_GetConceptData concept = termFactory.getConcept(componentId);
            I_DescriptionTuple refsetNameDescription = refsetUtil.assertExactlyOne(refsetDescriptionsList);
            List<I_DescriptionTuple> descriptionTuples = (List<I_DescriptionTuple>) refsetUtil.getPTDescriptionsForConceptHavingCurrentStatus(
                termFactory, componentId);

            if (descSelector != null) {
                I_DescriptionTuple preferredDesc = descSelector.getPreferred(descriptionTuples);
                descriptionTuples.clear();
                descriptionTuples.add(preferredDesc);
            }

            String refsetName = refsetNameDescription.getText();
            progressLogger.logProgress(refsetName);

            DescriptionWriter writer = writerFactory.createDescriptionFile(refsetName);
            NoDescriptionWriter noDescWriter = writerFactory.createNoDescriptionFile();

            I_ExtendByRefPart part = refsetUtil.getLatestVersionIfCurrent(refset, termFactory);
            if (part != null) {
                refsetTextWriter.writeRefset(concept, descriptionTuples, writer, part, noDescWriter);
            }
        } catch (Exception e) {
            logger.logWarn(e.getMessage());
        }
    }

    public void closeFiles() throws Exception {
        writerFactory.closeFiles();
    }
}
