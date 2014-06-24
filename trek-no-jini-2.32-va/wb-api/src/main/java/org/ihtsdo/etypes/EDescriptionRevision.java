package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

public class EDescriptionRevision extends TkDescriptionRevision {

    public static final long serialVersionUID = 1;

    public EDescriptionRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public EDescriptionRevision() {
        super();
    }
}
