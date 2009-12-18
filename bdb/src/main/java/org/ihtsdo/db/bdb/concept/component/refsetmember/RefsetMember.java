package org.ihtsdo.db.bdb.concept.component.refsetmember;

import java.io.IOException;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RefsetMember extends ConceptComponent<RefsetMemberVariablePart> 
	implements I_ThinExtByRefVersioned<RefsetMemberVariablePart, RefsetMemberTuple> {

	private int refsetNid;
	private int componentNid;
	private int memberTypeNid; 


	public RefsetMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
	}

	
	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid) {
		refsetNid = conceptNid;
		componentNid = input.readInt();
		memberTypeNid = input.readInt();
	}

	@Override
	public void readPartFromBdb(TupleInput input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeComponentToBdb(TupleOutput output) {
		output.writeInt(componentNid);
		
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}

}
