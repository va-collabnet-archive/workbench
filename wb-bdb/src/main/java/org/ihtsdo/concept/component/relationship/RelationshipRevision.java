package org.ihtsdo.concept.component.relationship;

import java.beans.PropertyVetoException;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelationshipRevision 
	extends Revision<RelationshipRevision, Relationship> 
	implements I_RelPart, RelationshipAnalogBI {
	
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
		buf.append("src:");
		ConceptComponent.addNidToBuffer(buf, this.primordialComponent.enclosingConceptNid);
		buf.append(" t:");
		ConceptComponent.addNidToBuffer(buf, typeNid);
		buf.append(" dest:");
		ConceptComponent.addNidToBuffer(buf, this.primordialComponent.getC2Id());
        buf.append(" characteristic:");
		ConceptComponent.addNidToBuffer(buf, this.characteristicNid);
        buf.append(" group:" + this.group);
        buf.append(" refinability:");
		ConceptComponent.addNidToBuffer(buf, this.refinabilityNid);
        buf.append(super.toString());
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

	   public RelationshipRevision(Relationship primordialRel) {
	        super(primordialRel.primordialSapNid, primordialRel);
	        this.characteristicNid = primordialRel.getCharacteristicNid();
	        this.group = primordialRel.getGroup();
	        this.refinabilityNid = primordialRel.getRefinabilityNid();
	        this.typeNid = primordialRel.getTypeNid();
	    }

	public RelationshipRevision(I_RelPart another, int statusNid, int authorNid,
			int pathNid, long time, 
			Relationship primordialRel) {
		super(statusNid, authorNid, pathNid, time, primordialRel);
		this.characteristicNid = another.getCharacteristicId();
		this.group = another.getGroup();
		this.refinabilityNid = another.getRefinabilityId();
		this.typeNid = another.getTypeNid();
	}

	public RelationshipRevision(TupleInput input, 
			Relationship primordialRel) {
		super(input.readInt(), primordialRel);
		this.characteristicNid = input.readInt();
		this.group = input.readInt();
		this.refinabilityNid = input.readInt();
		this.typeNid = input.readInt();
	}

	public RelationshipRevision(TkRelationshipRevision erv, 
			Relationship primordialRel) {
		super(Bdb.uuidToNid(erv.getStatusUuid()), 
				Bdb.uuidToNid(erv.getAuthorUuid()), 
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
        modified();
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
        modified();
	}

	public int getRefinabilityId() {
		return refinabilityNid;
	}

	public void setRefinabilityId(int refinabilityNid) {
		this.refinabilityNid = refinabilityNid;
        modified();
	}
	@Deprecated
	public int getTypeId() {
		return typeNid;
	}
	@Deprecated
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
        modified();
	}

	public int getTypeNid() {
		return typeNid;
	}

	public void setTypeNid(int typeNid) {
		this.typeNid = typeNid;
        modified();
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
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
		return new RelationshipRevision(this, statusNid, Terms.get().getAuthorNid(), pathNid, time,
				primordialComponent);
	}

	@Override
	public RelationshipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);
            return this;
        }
		return new RelationshipRevision(this, statusNid, authorNid, pathNid, time,
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


	public int getRefinabilityNid() {
		return refinabilityNid;
	}


	public int getCharacteristicNid() {
		return characteristicNid;
	}


	public void setCharacteristicNid(int characteristicNid) {
		this.characteristicNid = characteristicNid;
	}


	public void setRefinabilityNid(int refinabilityNid) {
		this.refinabilityNid = refinabilityNid;
	}


	@Override
	public void setDestinationNid(int nid) throws PropertyVetoException {
		throw new UnsupportedOperationException();
	}


	@Override
	public int getDestinationNid() {
		return primordialComponent.getDestinationNid();
	}


	@Override
	public int getOriginNid() {
		return primordialComponent.getOriginNid();
	}	
	
	
	@Override
	public Relationship.Version getVersion(Coordinate c)
			throws ContraditionException {
		return primordialComponent.getVersion(c);
	}

	@Override
	public Collection<Relationship.Version> getVersions(
			Coordinate c) {
		return primordialComponent.getVersions(c);
	}		

    @Override
	public String toUserString() {
        StringBuffer buf = new StringBuffer();
        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        ConceptComponent.addTextToBuffer(buf, primordialComponent.getDestinationNid());
        return buf.toString();
	}
}
