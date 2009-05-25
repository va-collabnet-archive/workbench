package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_DescriptionTuple;

import java.util.List;

public interface DescriptionWriter extends ExportWriter {

    void write(I_GetConceptData concept, List<I_DescriptionTuple> descriptionTuples) throws Exception;
}
