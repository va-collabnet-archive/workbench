package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.member.TkRefsetRevision;

public class ERefsetRevision extends TkRefsetRevision {

    protected static final long serialVersionUID = 1;

    public ERefsetRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public ERefsetRevision(I_ExtendByRefPart part) throws TerminologyException, IOException {
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetRevision() {
        super();
    }
}
