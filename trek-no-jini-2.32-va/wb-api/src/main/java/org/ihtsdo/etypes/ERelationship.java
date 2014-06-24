package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

public class ERelationship extends TkRelationship {

    public static final long serialVersionUID = 1;

    public ERelationship(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERelationship(I_RelVersioned<?> rel) throws TerminologyException, IOException {
        super(rel);
    }

    public ERelationship() {
        super();
    }

}
