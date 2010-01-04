package org.ihtsdo.db.bdb.concept.component.refset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.I_BindConceptComponents;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


public class RefsetMemberBinder extends TupleBinding<ArrayList<RefsetMember<?, ?>>> 
	implements I_BindConceptComponents {

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
	private int conceptNid; 
	private boolean editable;

	@Override
	public ArrayList<RefsetMember<?, ?>> entryToObject(TupleInput input) {
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
			int partCount = input.readShort();
			RefsetMember<?, ?> refsetMember;
			if (nidToRefsetMemberMap != null && nidToRefsetMemberMap.containsKey(nid)) {
				refsetMember = nidToRefsetMemberMap.get(nid);
				int totalSize = refsetMember.additionalVersions.size() + partCount;
				refsetMember.additionalVersions.ensureCapacity(totalSize);
			} else {
				refsetMember = factory.create(nid, partCount, editable, input, conceptNid);
				newRefsetMemberList.add(refsetMember);
			}
			refsetMember.readComponentFromBdb(input, conceptNid, partCount);
		}
		newRefsetMemberList.trimToSize();
		return newRefsetMemberList;
	}

	@Override
	public void objectToEntry(ArrayList<RefsetMember<?, ?>> refsetMemberList,
			TupleOutput output) {
		List<RefsetMember<?, ?>> refsetMembersToWrite = new ArrayList<RefsetMember<?, ?>>(refsetMemberList.size());
		for (RefsetMember<?, ?> conceptComponent: refsetMemberList) {
			for (RefsetVersion<?, ?> version: conceptComponent.additionalVersions) {
				if (version.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId) {
					refsetMembersToWrite.add(conceptComponent);
					break;
				}
			}
		}
		output.writeInt(refsetMembersToWrite.size()); // List size
		for (RefsetMember<?, ?> refsetMember: refsetMembersToWrite) {
			refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
		}
	}


	@Override
	public int getConceptNid() {
		return conceptNid;
	}


	@Override
	public boolean isEditable() {
		return editable;
	}


	@Override
	public void setupBinder(int conceptNid, boolean editable) {
		this.conceptNid = conceptNid;
		this.editable = editable;
	}


	public void setTermComponentList(
			ArrayList<RefsetMember<?, ?>> componentList) {
		this.refsetMemberList = componentList;
		
	}

}
