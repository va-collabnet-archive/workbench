package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;

public class ERefsetLongRevision extends TkRefsetLongRevision {

    public static final long serialVersionUID = 1;

    public ERefsetLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetLongRevision(I_ExtendByRefPartLong part) throws TerminologyException, IOException {
        longValue = part.getLongValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetLongRevision() {
        super();
    }

}
