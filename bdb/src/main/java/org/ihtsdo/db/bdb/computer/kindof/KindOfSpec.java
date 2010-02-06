package org.ihtsdo.db.bdb.computer.kindof;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.types.IntSet;

public class KindOfSpec {
	
	
	/**
	 * The view position for which this cache is valid.
	 */
	public I_Position viewPosition;
	/**
	 * The allowed status cNids for which this cache is valid.
	 */
	public I_IntSet allowedStatusNids;
	/**
	 * The set of destination rels nids for which this cache is valid. 
	 */
	public I_IntSet relTypeNids;
	/**
	 * The cNid of the kind this cache represents.
	 */
	public int kindNid;
	
	/**
	 * cached value so that viewPositionSet does not have to be recreated
	 * each time a query is perfomed. 
	 */
	private PositionSetReadOnly viewPositionSet;
	
	public KindOfSpec(I_Position viewPosition, I_IntSet allowedStatus,
			I_IntSet relTypeNids, int kindNid) {
		super();
		this.viewPosition = viewPosition;
		this.allowedStatusNids = new IntSet(allowedStatus.getSetValues());
		this.relTypeNids = new IntSet(relTypeNids.getSetValues());
		this.kindNid = kindNid;
		this.viewPositionSet = new PositionSetReadOnly(viewPosition);
	}
	@Override
	public boolean equals(Object obj) {
		if (KindOfSpec.class.isAssignableFrom(obj.getClass())) {
			KindOfSpec another = (KindOfSpec) obj;
			if (kindNid != another.kindNid) {
				return false;
			}
			if (!viewPosition.equals(another.viewPosition)) {
				return false;
			}
			if (!allowedStatusNids.equals(another.allowedStatusNids)) {
				return false;
			}
			if (!relTypeNids.equals(another.relTypeNids)) {
				return false;
			}
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {kindNid, viewPosition.getPath().getConceptId()});
	}
	
	public PositionSetReadOnly getViewPositionSet() {
		return viewPositionSet;
	}
	
	

}