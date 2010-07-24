package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.media.TkMediaRevision;

public class EImageRevision extends TkMediaRevision {

    public static final long serialVersionUID = 1;

    public EImageRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EImageRevision(I_ImagePart part) throws TerminologyException, IOException {
        textDescription = part.getTextDescription();
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EImageRevision() {
        super();
    }

}
