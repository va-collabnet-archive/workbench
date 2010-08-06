package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntRevision;

public class ERefsetIntRevision extends TkRefsetIntRevision {

    public static final long serialVersionUID = 1;

    public ERefsetIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetIntRevision(I_ExtendByRefPartInt part) throws TerminologyException, IOException {
        intValue = part.getIntValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetIntRevision() {
        super();
    }

}
