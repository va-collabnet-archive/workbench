package org.ihtsdo.db.bdb.concept.component.refset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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

	private static int maxReadOnlyStatusAtPositionId = Bdb.getStatusAtPositionDb().getReadOnlyMax();
	
	private static ThreadLocal<RefsetMemberBinder>  binders = 
		new ThreadLocal<RefsetMemberBinder>() {

		@Override
		protected RefsetMemberBinder initialValue() {
			return new RefsetMemberBinder();
		}
	};
	public static RefsetMemberBinder getBinder() {
		return binders.get();
	}

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
			int nid = input.readInt();
			UUID primoridalUuid = new UUID(input.readLong(), input.readLong());
			int partCount = input.readShort();
			RefsetMember<?, ?> refsetMember;
			if (nidToRefsetMemberMap != null && nidToRefsetMemberMap.containsKey(nid)) {
				refsetMember = nidToRefsetMemberMap.get(nid);
				int totalSize = refsetMember.additionalVersions.size() + partCount;
				refsetMember.additionalVersions.ensureCapacity(totalSize);
			} else {
				refsetMember = factory.create(nid, partCount, enclosingConcept, 
						input, primoridalUuid);
				newRefsetMemberList.add(refsetMember);
			}
			refsetMember.readComponentFromBdb(input, partCount);
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
			assert refsetMember.primordialStatusAtPositionNid != Integer.MAX_VALUE;
			if (refsetMember.primordialStatusAtPositionNid > maxReadOnlyStatusAtPositionId) {
				refsetMembersToWrite.add(refsetMember);
			} else {
				if (refsetMember.additionalVersions != null) {
					for (RefsetVersion<?, ?> extraVersions: refsetMember.additionalVersions) {
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
