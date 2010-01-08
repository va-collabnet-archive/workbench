package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public class EIdentifierVersionString extends EIdentifierVersion {

    public static final long serialVersionUID = 1;

    protected String denotation;

    public EIdentifierVersionString(DataInput in) throws IOException, ClassNotFoundException {
        super(in);
        denotation = in.readUTF();
    }

    public EIdentifierVersionString(I_IdPart idp) throws TerminologyException, IOException {
        denotation = (String) idp.getDenotation();
        authorityUuid = nidToUuid(idp.getAuthorityNid());
        pathUuid = nidToUuid(idp.getPathId());
        statusUuid = nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeUTF(denotation);
    }

    @Override
    public String getDenotation() {
        return denotation;
    }

    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.STRING;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(", denotation:");
        buff.append(this.denotation);
        buff.append("; ");

        return buff.toString();
    }
}
