package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;

public class ERefsetCidLongRevision extends TkRefsetCidLongRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidLongRevision(I_ExtendByRefPartCidLong part) throws TerminologyException, IOException {
        c1Uuid = Terms.get().nidToUuid(part.getC1id());
        longValue = part.getLongValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidLongRevision() {
        super();
    }

}
