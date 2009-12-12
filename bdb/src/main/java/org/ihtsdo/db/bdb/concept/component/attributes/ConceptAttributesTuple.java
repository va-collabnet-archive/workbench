package org.ihtsdo.db.bdb.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public class ConceptAttributesTuple 
	extends Tuple <ConceptAttributesPart, ConceptAttributes>
	implements I_ConceptAttributeTuple<ConceptAttributesPart, ConceptAttributesTuple, ConceptAttributes> {
	
	ConceptAttributes core;
	ConceptAttributesPart part;

	public ArrayIntList getPartComponentNids() {
		return part.getPartComponentNids();
	}

	transient Integer hash;

	public ConceptAttributesTuple(ConceptAttributes core,
			ConceptAttributesPart part) {
		super();
		this.core = core;
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConId()
	 */
	public int getConId() {
		return core.getConId();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.ace.api.I_AmPart#setStatusId(int)
	 */
	@Deprecated
	public void setStatusId(int statusId) {
		part.setStatusId(statusId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getPathId()
	 */
	public int getPathId() {
		return part.getPathId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getVersion()
	 */
	public int getVersion() {
		return part.getVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.vodb.types.I_ConceptAttributeTuple#hasNewData(org.dwfa.vodb.
	 * types.ThinConPart)
	 */
	public boolean hasNewData(ConceptAttributesPart another) {
		return part.hasNewData(another);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#isDefined()
	 */
	public boolean isDefined() {
		return part.isDefined();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.vodb.types.I_ConceptAttributeTuple#setStatusId(java.lang.Integer
	 * )
	 */
	@Deprecated
	public void setStatusId(Integer statusId) {
		part.setStatusId(statusId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#setDefined(boolean)
	 */
	public void setDefined(boolean defined) {
		part.setDefined(defined);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConVersioned()
	 */
	public ConceptAttributes getConVersioned() {
		return core;
	}

	@Override
	public boolean equals(Object obj) {
		ConceptAttributesTuple another = (ConceptAttributesTuple) obj;
		return core.equals(another.core) && part.equals(another.part);
	}

	@Override
	public int hashCode() {
		if (hash == null) {
			hash = HashFunction.hashCode(new int[] { core.hashCode(),
					part.hashCode() });
		}
		return hash;
	}

	public String toString() {
		return "ThinConTuple id: " + getConId() + " status: "
				+ getStatusId() + " defined: " + isDefined() + " path: "
				+ getPathId() + " version: " + getVersion();
	}

	public ConceptAttributesPart getPart() {
		return part;
	}

	public int getStatusId() {
		return part.getStatusId();
	}

	public void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}

	public void setVersion(int version) {
		throw new UnsupportedOperationException();
	}

	public ConceptAttributes getFixedPart() {
		return core;
	}

	public ConceptAttributesPart duplicate() {
		return part.duplicate();
	}

	public int getFixedPartId() {
		return core.getNid();
	}

	public int getStatusAtPositionNid() {
		return part.getStatusAtPositionNid();
	}

	public long getTime() {
		return part.getTime();
	}

	public ConceptAttributesPart makeAnalog(int statusNid, int pathNid, long time) {
		return this.part.makeAnalog(statusNid, pathNid, time);
	}

}
