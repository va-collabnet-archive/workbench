package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cidcidflt.TkRefsetCidFloatRevision;

public class ERefsetCidFloatRevision extends TkRefsetCidFloatRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidFloatRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidFloatRevision(I_ExtendByRefPartCidFloat part) throws TerminologyException, IOException {
        super();
        c1Uuid = nidToUuid(part.getUnitsOfMeasureId());
        floatValue = (float) part.getMeasurementValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidFloatRevision() {
        super();
    }
}
