package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.Boolean.TkRefsetBooleanRevision;

public class ERefsetBooleanRevision extends TkRefsetBooleanRevision {

    public static final long serialVersionUID = 1;

    public ERefsetBooleanRevision() {
        super();
    }

    public ERefsetBooleanRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetBooleanRevision(I_ExtendByRefPartBoolean part) throws TerminologyException, IOException {
        booleanValue = part.getBooleanValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }
}
