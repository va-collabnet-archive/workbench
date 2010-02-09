package org.ihtsdo.db.bdb.concept.component.identifier;

import java.io.IOException;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.db.bdb.concept.component.image.ImageRevision;
import org.ihtsdo.etypes.EIdentifierVersionLong;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionLong extends IdentifierVersion {
	private long longDenotation;

	public IdentifierVersionLong(TupleInput input) {
		super(input);
		longDenotation = input.readLong();
	}

	public IdentifierVersionLong(EIdentifierVersionLong idv) {
		super(idv);
		longDenotation = idv.getDenotation();
	}

    public IdentifierVersionLong() {
        super();
    }

	@Override
	public IDENTIFIER_PART_TYPES getType() {
		return IDENTIFIER_PART_TYPES.LONG;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeLong(longDenotation);
	}

	@Override
	public Object getDenotation() {
		return longDenotation;
	}

	@Override
	public void setDenotation(Object sourceDenotation) {
		longDenotation = (Long) sourceDenotation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();
	    
	    buf.append(this.getClass().getSimpleName() + ":{");
	    buf.append(" longDenotation:" + this.longDenotation);
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
    public String validate(IdentifierVersionLong another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.longDenotation != another.longDenotation) {
            buf.append("\tIdentifierVersionLong.longDenotation not equal: \n" + 
                "\t\tthis.longDenotation = " + this.longDenotation + "\n" + 
                "\t\tanother.longDenotation = " + another.longDenotation + "\n");
        }       
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
    
	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
		if (IdentifierVersionLong.class.isAssignableFrom(obj.getClass())) {
			IdentifierVersionLong another = (IdentifierVersionLong) obj;
            return this.getSapNid() == another.getSapNid(); 
		}
		return false;
	}

}
