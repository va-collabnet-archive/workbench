package org.ihtsdo.db.bdb.concept.component.refset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_BindConceptComponents;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


public class RefsetMemberBinder extends TupleBinding<ArrayList<RefsetMember<?, ?>>> 
	implements I_BindConceptComponents {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	private static int maxReadOnlyStatusAtPositionId = Bdb.getSapDb().getReadOnlyMax();

	RefsetMemberFactory factory = new RefsetMemberFactory();

	private ArrayList<RefsetMember<?, ?>> refsetMemberList;
	private Concept enclosingConcept; 

	@Override
	public ArrayList<RefsetMember<?, ?>> entryToObject(TupleInput input) {
		assert enclosingConcept != null;
		int listSize = input.readInt();
		if (refsetMemberList != null) {
			refsetMemberList.ensureCapacity(listSize + refsetMemberList.size());
		}
		ArrayList<RefsetMember<?, ?>> newRefsetMemberList;
		HashMap<Integer, RefsetMember<?, ?>> nidToRefsetMemberMap = null;
		if (refsetMemberList != null) {
			newRefsetMemberList = refsetMemberList;
			nidToRefsetMemberMap = new HashMap<Integer, RefsetMember<?, ?>>(listSize);
			for (RefsetMember<?, ?> component: refsetMemberList) {
				nidToRefsetMemberMap.put(component.nid, component);
			}
		} else {
			newRefsetMemberList = new ArrayList<RefsetMember<?, ?>>(listSize);
		}
		
		for (int index = 0; index < listSize; index++) {
			int typeNid = input.readInt();
			input.mark(8);
			int nid = input.readInt();
			input.reset();
			RefsetMember<?, ?> refsetMember;
			if (nidToRefsetMemberMap != null && nidToRefsetMemberMap.containsKey(nid)) {
				refsetMember = nidToRefsetMemberMap.get(nid);
				refsetMember.readComponentFromBdb(input);
			} else {
				refsetMember = factory.create(nid, typeNid, enclosingConcept, input);
				newRefsetMemberList.add(refsetMember);
			}
		}
		newRefsetMemberList.trimToSize();
		return newRefsetMemberList;
	}

	@Override
	public void objectToEntry(ArrayList<RefsetMember<?, ?>> refsetMemberList,
			TupleOutput output) {
		List<RefsetMember<?, ?>> refsetMembersToWrite = new ArrayList<RefsetMember<?, ?>>(refsetMemberList.size());
		for (RefsetMember<?, ?> refsetMember: refsetMemberList) {
			encountered.incrementAndGet();
			assert refsetMember.primordialSapNid != Integer.MAX_VALUE;
			if (refsetMember.primordialSapNid > maxReadOnlyStatusAtPositionId) {
				refsetMembersToWrite.add(refsetMember);
			} else {
				if (refsetMember.revisions != null) {
					for (RefsetRevision<?, ?> extraVersions: refsetMember.revisions) {
						if (extraVersions.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId) {
							refsetMembersToWrite.add(refsetMember);
							break;
						}
					}
				}
			}
		}
		output.writeInt(refsetMembersToWrite.size()); // List size
		for (RefsetMember<?, ?> refsetMember: refsetMembersToWrite) {
			written.incrementAndGet();
			output.writeInt(refsetMember.getTypeId());
			refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
		}
	}


	@Override
	public void setupBinder(Concept enclosingConcept) {
		this.enclosingConcept = enclosingConcept;
	}


	public void setTermComponentList(
			ArrayList<RefsetMember<?, ?>> componentList) {
		this.refsetMemberList = componentList;
		
	}

	public Concept getEnclosingConcept() {
		return enclosingConcept;
	}

	public void setEnclosingConcept(Concept enclosingConcept) {
		this.enclosingConcept = enclosingConcept;
	}

}
