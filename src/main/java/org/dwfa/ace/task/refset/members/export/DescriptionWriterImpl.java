package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;

import java.io.Writer;
import java.util.List;

public final class DescriptionWriterImpl implements DescriptionWriter {

    private final Writer writer;
    private final String lineSeparator;
    private final RefsetUtil refsetUtil;
    private final I_TermFactory termFactory;

    //TODO: move lineSeparator into a utility class.
    public DescriptionWriterImpl(final Writer writer, final I_TermFactory termFactory, final RefsetUtil refsetUtil,
                                 final String lineSeparator) {
        this.refsetUtil = refsetUtil;
        this.lineSeparator = lineSeparator;
        this.writer = writer;
        this.termFactory = termFactory;
    }

    public void write(final I_GetConceptData concept, final List<I_DescriptionTuple> descriptionTuples)
            throws Exception {
        I_DescriptionTuple descriptionTuple = descriptionTuples.iterator().next();
        writer.append(lineSeparator);
        writer.append(refsetUtil.getSnomedId(concept.getConceptId(), termFactory));
        writer.append("\t");
        writer.append(descriptionTuple.getText());
    }

    public void close() throws Exception {
        writer.flush();
        writer.close();
    }

    public DescriptionWriter append(final String text) throws Exception {
        writer.append(text);
        return this;
    }
}
