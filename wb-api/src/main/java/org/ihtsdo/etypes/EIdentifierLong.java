package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;

public class EIdentifierLong extends TkIdentifierLong {

    public static final long serialVersionUID = 1;

 
    public EIdentifierLong(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
        denotation = in.readLong();
    }

    public EIdentifierLong(I_IdPart idp) throws TerminologyException, IOException {
        super();
        denotation = (Long) idp.getDenotation();
        authorityUuid = Terms.get().nidToUuid(idp.getAuthorityNid());
        pathUuid = Terms.get().nidToUuid(idp.getPathId());
        statusUuid = Terms.get().nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierLong() {
        super();
    }


    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.LONG;
    }

}
