package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;

import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class KindOfSpec {


	/**
	 * The view position for which this cache is valid.
	 */
	public PositionBI viewPosition;
	public PositionBI getViewPosition() {
		return viewPosition;
	}
	public NidSetBI getAllowedStatusNids() {
		return allowedStatusNids;
	}
	public NidSetBI getRelTypeNids() {
		return relTypeNids;
	}
	public int getKindNid() {
		return kindNid;
	}
	public Precedence getPrecedence() {
		return precedence;
	}
	public ContradictionManagerBI getContradictionMgr() {
		return contradictionMgr;
	}

	/**
	 * The allowed status cNids for which this cache is valid.
	 */
	public NidSetBI allowedStatusNids;
	/**
	 * The set of destination rels nids for which this cache is valid.
	 */
	public NidSetBI relTypeNids;
	/**
	 * The cNid of the kind this cache represents.
	 */
	public int kindNid;

	public Precedence precedence;

	public ContradictionManagerBI contradictionMgr;

	/**
	 * cached value so that viewPositionSet does not have to be recreated
	 * each time a query is perfomed.
	 */
	private PositionSetReadOnly viewPositionSet;

        private int classifierNid;

        private RelAssertionType relAssertionType;

	public ViewCoordinate getCoordinate() {
		return new ViewCoordinate(precedence, viewPositionSet,
                        allowedStatusNids, relTypeNids,
                        contradictionMgr, Integer.MIN_VALUE,
                        classifierNid, relAssertionType, null);
	}
	public KindOfSpec(PositionBI viewPosition, NidSetBI allowedStatus,
			NidSetBI relTypeNids, int kindNid, Precedence precedence,
			ContradictionManagerBI contradictionMgr, int classifierNid,
                        RelAssertionType relAssertionType) {
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
                this.classifierNid = classifierNid;
                this.relAssertionType = relAssertionType;
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