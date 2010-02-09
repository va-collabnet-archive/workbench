package org.ihtsdo.db.bdb.concept.component.relationship;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Revision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntRevision;
import org.ihtsdo.etypes.ERelationshipVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelationshipRevision 
	extends Revision<RelationshipRevision, Relationship> 
	implements I_RelPart {
	
	private transient Relationship relationship;
	
	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" relationship:" + this.relationship);
        buf.append(" characteristicNid:" + this.characteristicNid);
        buf.append(" group:" + this.group);
        buf.append(" refinabilityNid:" + this.refinabilityNid);
        buf.append(" typeNid:" + this.typeNid);
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
    public String validate(RelationshipRevision another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.characteristicNid != another.characteristicNid) {
            buf.append("\tRelationshipRevision.characteristicNid not equal: \n" + 
                "\t\tthis.characteristicNid = " + this.characteristicNid + "\n" + 
                "\t\tanother.characteristicNid = " + another.characteristicNid + "\n");
        }
        if (this.group != another.group) {
            buf.append("\tRelationshipRevision.group not equal: \n" + 
                "\t\tthis.group = " + this.group + "\n" + 
                "\t\tanother.group = " + another.group + "\n");
        }
        if (this.refinabilityNid != another.refinabilityNid) {
            buf.append("\tRelationshipRevision.refinabilityNid not equal: \n" + 
                "\t\tthis.refinabilityNid = " + this.refinabilityNid + "\n" + 
                "\t\tanother.refinabilityNid = " + another.refinabilityNid + "\n");
        }
        if (this.typeNid != another.typeNid) {
            buf.append("\tRelationshipRevision.typeNid not equal: \n" + 
                "\t\tthis.typeNid = " + this.typeNid + "\n" + 
                "\t\tanother.typeNid = " + another.typeNid + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (RelationshipRevision.class.isAssignableFrom(obj.getClass())) {
            RelationshipRevision another = (RelationshipRevision) obj;
            return this.sapNid == another.sapNid;
        }
        return false;
    }

    
    public RelationshipRevision(int statusAtPositionNid, 
			Relationship primordialRel) {
		super(statusAtPositionNid, primordialRel);
	}

	public RelationshipRevision(RelationshipRevision another, 
			Relationship primordialRel) {
		super(another.sapNid, primordialRel);
		this.characteristicNid = another.characteristicNid;
		this.group = another.group;
		this.refinabilityNid = another.refinabilityNid;
		this.typeNid = another.typeNid;
	}

	public RelationshipRevision(I_RelPart another, int statusNid,
			int pathNid, long time, 
			Relationship primordialRel) {
		super(statusNid, pathNid, time, primordialRel);
		this.characteristicNid = another.getCharacteristicId();
		this.group = another.getGroup();
		this.refinabilityNid = another.getRefinabilityId();
		this.typeNid = another.getTypeId();
	}

	public RelationshipRevision(TupleInput input, 
			Relationship primordialRel) {
		super(input.readInt(), primordialRel);
		this.characteristicNid = input.readInt();
		this.group = input.readInt();
		this.refinabilityNid = input.readInt();
		this.typeNid = input.readInt();
	}

	public RelationshipRevision(ERelationshipVersion erv, 
			Relationship primordialRel) {
		super(Bdb.uuidToNid(erv.getStatusUuid()), 
				Bdb.uuidToNid(erv.getPathUuid()), erv.getTime(), 
				primordialRel);
		this.characteristicNid = Bdb.uuidToNid(erv.getCharacteristicUuid());
		this.group = erv.getGroup();
		this.refinabilityNid = Bdb.uuidToNid(erv.getRefinabilityUuid());
		this.typeNid = Bdb.uuidToNid(erv.getTypeUuid());
		this.sapNid = Bdb.getSapNid(erv);
	}

    public RelationshipRevision() {
        super();
    }

	@Override
	public void writeFieldsToBdb(TupleOutput output) {
		output.writeInt(characteristicNid);
		output.writeInt(group);
		output.writeInt(refinabilityNid);
		output.writeInt(typeNid);
	}	

	public int getCharacteristicId() {
		return characteristicNid;
	}

	public void setCharacteristicId(int characteristicNid) {
		this.characteristicNid = characteristicNid;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getRefinabilityId() {
		return refinabilityNid;
	}

	public void setRefinabilityId(int refinabilityNid) {
		this.refinabilityNid = refinabilityNid;
	}

	public int getTypeId() {
		return typeNid;
	}

	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RelationshipRevision duplicate() {
		return new RelationshipRevision(this, primordialComponent);
	}
	
	@Override
	public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new RelationshipRevision(this, statusNid, pathNid, time,
				primordialComponent);
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nids = new ArrayIntList(5);
		nids.add(characteristicNid);
		nids.add(refinabilityNid);
		nids.add(typeNid);
		return nids;
	}	
}
