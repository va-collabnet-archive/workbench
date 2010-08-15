package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;

public class KindOfSpec {
	
	
	/**
	 * The view position for which this cache is valid.
	 */
	public PositionBI viewPosition;
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
	
	public Precedence precedence;
	
	public I_ManageContradiction contradictionMgr;
	
	/**
	 * cached value so that viewPositionSet does not have to be recreated
	 * each time a query is perfomed. 
	 */
	private PositionSetReadOnly viewPositionSet;
	
	public KindOfSpec(PositionBI viewPosition, I_IntSet allowedStatus,
			I_IntSet relTypeNids, int kindNid, Precedence precedence, 
			I_ManageContradiction contradictionMgr) {
		super();
		this.viewPosition = viewPosition;
		this.allowedStatusNids = new IntSet(allowedStatus.getSetValues());
		assert allowedStatus != null: "Cannot use null wildcard for allowed status.";
		assert allowedStatus.size() != 0: "Cannot use an empty set for allowed status.";
		assert allowedStatus.contains(ReferenceConcepts.RETIRED.getNid()) == false: 
			"Cannot include a retired status. May surface cycles: " + allowedStatus;
		this.relTypeNids = new IntSet(relTypeNids.getSetValues());
		this.kindNid = kindNid;
		this.viewPositionSet = new PositionSetReadOnly(viewPosition);
		this.precedence = precedence;
		this.contradictionMgr = contradictionMgr;
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
		return HashFunction.hashCode(new int[] {kindNid, viewPosition.getPath().getConceptNid()});
	}
	
	public PositionSetReadOnly getViewPositionSet() {
		return viewPositionSet;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("KindOfSpec: viewPosition: ");
		buff.append(viewPosition);
		buff.append("\n allowedStatus: ");
		buff.append(allowedStatusNids);
		buff.append("\n relTypes: ");
		buff.append(relTypeNids);
		buff.append("\n kind: ");
		try {
			buff.append(Terms.get().getConcept(kindNid).toString());
		} catch (TerminologyException e) {
			buff.append(e.getLocalizedMessage());
		} catch (IOException e) {
			buff.append(e.getLocalizedMessage());
		}
		buff.append("\n viewPositions: ");
		buff.append(viewPositionSet);
		
		return buff.toString();
	}
	
	

}