package org.ihtsdo.tk.api.coordinate;

import java.io.IOException;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.example.binding.TermAux;
import org.ihtsdo.tk.hash.Hashcode;

public class KindOfSpec extends IsaCoordinate {

	/**
	 * The cNid of the kind this cache represents.
	 */
	public int kindNid;
	
	public int getKindNid() {
		return kindNid;
	}

	public KindOfSpec(PositionBI viewPosition, NidSetBI allowedStatus,
			NidSetBI relTypeNids, int kindNid, Precedence precedence,
			ContradictionManagerBI contradictionMgr, int classifierNid,
                        RelAssertionType relAssertionType) {
		super(viewPosition,allowedStatus, relTypeNids, precedence,
				contradictionMgr, classifierNid, relAssertionType);
		this.kindNid = kindNid;
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
		return Hashcode.compute(new int[] {kindNid, viewPosition.getPath().getConceptNid()});
	}

	@Override
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
			buff.append(Ts.get().getConcept(kindNid).toString());
		} catch (IOException e) {
			buff.append(e.getLocalizedMessage());
		}
		buff.append("\n viewPositions: ");
		buff.append(viewPositionSet);

		return buff.toString();
	}
	
	public IsaCoordinate getIsaCoordinate() {
		return new IsaCoordinate(viewPosition, allowedStatusNids, relTypeNids, precedence,
				contradictionMgr, classifierNid, relAssertionType);
	}



}