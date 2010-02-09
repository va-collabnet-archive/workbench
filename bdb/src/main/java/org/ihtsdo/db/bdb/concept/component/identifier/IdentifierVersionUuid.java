package org.ihtsdo.db.bdb.concept.component.identifier;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierVersionUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionUuid extends IdentifierVersion {
	private int uNid;
	
	public IdentifierVersionUuid(TupleInput input) {
		super(input);
		uNid = input.readInt();
	}

	public IdentifierVersionUuid(EIdentifierVersionUuid idv) {
		super(idv);
		uNid = Bdb.getUuidsToNidMap().getUNid(idv.getDenotation());
	}

    public IdentifierVersionUuid() {
        super();
    }

	@Override
	public IDENTIFIER_PART_TYPES getType() {
		return IDENTIFIER_PART_TYPES.UUID;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeInt(uNid);
	}

	public UUID getUuid() {
		return Bdb.getUuidDb().getUuid(uNid);
	}

	@Override
	public Object getDenotation() {
		return Bdb.getUuidDb().getUuid(uNid);
	}

	@Override
	public void setDenotation(Object sourceDenotation) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();
	    
	    buf.append(this.getClass().getSimpleName() + ":{");
	    buf.append(" uNid:" + getUuid());
	    buf.append(" }=> ");
	    buf.append(super.toString());
	    return buf.toString();
	}

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(IdentifierVersionUuid another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.uNid != another.uNid) {
            buf.append("\tIdentifierVersionUuid.uNid not equal: \n" + 
                "\t\tthis.uNid = " + this.uNid + "\n" + 
                "\t\tanother.uNid = " + another.uNid + "\n");
        }       
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
    
	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
		if (IdentifierVersionUuid.class.isAssignableFrom(obj.getClass())) {
			IdentifierVersionUuid another = (IdentifierVersionUuid) obj;
			return this.uNid == another.uNid && super.equals(another);
		}
		return false;
	}

}
