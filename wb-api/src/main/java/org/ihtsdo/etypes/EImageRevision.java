package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;

public class EImageRevision extends TkMediaRevision {

    public static final long serialVersionUID = 1;

    public EImageRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EImageRevision(I_ImagePart part) throws TerminologyException, IOException {
        textDescription = part.getTextDescription();
        typeUuid = Terms.get().nidToUuid(part.getTypeId());
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EImageRevision() {
        super();
    }

}
