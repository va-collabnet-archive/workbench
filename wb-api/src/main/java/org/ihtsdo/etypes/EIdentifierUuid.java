package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.TkComponent;
import org.ihtsdo.tk.concept.component.identifier.TkIdentifierUuid;

public class EIdentifierUuid extends TkIdentifierUuid {

    public static final long serialVersionUID = 1;

    public EIdentifierUuid(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EIdentifierUuid(I_IdPart idp) throws TerminologyException, IOException {
        denotation = (UUID) idp.getDenotation();
        authorityUuid = nidToUuid(idp.getAuthorityNid());
        pathUuid = nidToUuid(idp.getPathId());
        statusUuid = nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierUuid() {
        super();
    }

    public EIdentifierUuid(TkComponent<?> eComponent) {
        denotation = eComponent.primordialUuid;
        authorityUuid = eComponent.getAuthorUuid();
        pathUuid = eComponent.getPathUuid();
        statusUuid = eComponent.getStatusUuid();
        time = eComponent.getTime();
    }
}
