package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.relationship.TkRelationshipRevision;

public class ERelationshipRevision extends TkRelationshipRevision {

    public static final long serialVersionUID = 1;

    public ERelationshipRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERelationshipRevision(I_RelPart part) throws TerminologyException, IOException {
        characteristicUuid = nidToUuid(part.getCharacteristicId());
        refinabilityUuid = nidToUuid(part.getRefinabilityId());
        group = part.getGroup();
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERelationshipRevision() {
        super();
    }

}
