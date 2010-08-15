package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;

public class ERefsetStrRevision extends TkRefsetStrRevision {

    public static final long serialVersionUID = 1;

    public ERefsetStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetStrRevision(I_ExtendByRefPartStr part) throws TerminologyException, IOException {
        stringValue = part.getStringValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetStrRevision() {
        super();
    }
}
