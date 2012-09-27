package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;

public class EConceptAttributesRevision extends TkConceptAttributesRevision {
    public static final long serialVersionUID = 1;

    public EConceptAttributesRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EConceptAttributesRevision() {
        super();
    }
}
