package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;

import java.util.List;

//TODO: This class does too much. Refactor.
public final class RefsetWriterImpl implements RefsetWriter {


    private final RefsetUtil refsetUtil;
    private final ProgressLogger progressLogger;
    private final I_TermFactory termFactory;
    private final Logger logger;
    private final RefsetTextWriter refsetTextWriter;
    private final WriterFactory writerFactory;


    public RefsetWriterImpl(final RefsetWriterParameterObject refsetWriterParameterObject,
                            final CommonAPIParameterObject commonAPIParameterObject) {
        refsetUtil = commonAPIParameterObject.getRefsetUtil();
        termFactory = commonAPIParameterObject.getTermFactory();
        logger = commonAPIParameterObject.getLogger();

        progressLogger = refsetWriterParameterObject.getProgressLogger();
        refsetTextWriter = refsetWriterParameterObject.getRefsetTextWriter();
        writerFactory = refsetWriterParameterObject.getWriterFactory();
    }

    public void write(final I_ThinExtByRefVersioned refset) throws Exception {
        try {
            List<I_DescriptionTuple> refsetDescriptionsList =
                    refsetUtil.getFSNDescriptionsForConceptHavingCurrentStatus(termFactory, refset.getRefsetId());

            int componentId = refset.getComponentId();
            I_GetConceptData concept = termFactory.getConcept(componentId);
            I_DescriptionTuple refsetNameDescription = refsetUtil.assertExactlyOne(refsetDescriptionsList);
            List<I_DescriptionTuple> descriptionTuples =
                    refsetUtil.getPTDescriptionsForConceptHavingCurrentStatus(termFactory, componentId);

            String refsetName = refsetNameDescription.getText();
            progressLogger.logProgress(refsetName);

            DescriptionWriter writer = writerFactory.createDescriptionFile(refsetName);
            NoDescriptionWriter noDescWriter = writerFactory.createNoDescriptionFile();

            I_ThinExtByRefPart part = refsetUtil.getLatestVersionIfCurrent(refset, termFactory);
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
