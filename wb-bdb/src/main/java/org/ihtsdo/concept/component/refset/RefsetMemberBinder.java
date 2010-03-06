package org.ihtsdo.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_BindConceptComponents;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.util.GCValueComponentMap;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


public class RefsetMemberBinder extends TupleBinding<List<RefsetMember<?, ?>>> 
	implements I_BindConceptComponents {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	private static int maxReadOnlyStatusAtPositionId = Bdb.getSapDb().getReadOnlyMax();

	RefsetMemberFactory factory = new RefsetMemberFactory();

	private ArrayList<RefsetMember<?, ?>> refsetMemberList;
	private Concept enclosingConcept; 
	private GCValueComponentMap componentMap;


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
			RefsetMember<?, ?> refsetMember = (RefsetMember<?, ?>) componentMap.get(nid);
			if (nidToRefsetMemberMap != null && nidToRefsetMemberMap.containsKey(nid)) {
				if (refsetMember == null) {
					refsetMember = nidToRefsetMemberMap.get(nid);
					RefsetMember<?, ?> oldMember = (RefsetMember<?, ?>) componentMap.putIfAbsent(nid, refsetMember);
					if (oldMember != null) {
						refsetMember = oldMember;
						if (nidToRefsetMemberMap != null) {
							nidToRefsetMemberMap.put(nid, refsetMember);
						}
					}
				}
				refsetMember.readComponentFromBdb(input);
			} else {
				try {
					if (refsetMember == null) {
						refsetMember = factory.create(nid, typeNid, enclosingConcept, input);
						componentMap.putIfAbsent(nid, refsetMember);
						RefsetMember<?, ?> oldMember = (RefsetMember<?, ?>) componentMap.putIfAbsent(nid, refsetMember);
						if (oldMember != null) {
							refsetMember = oldMember;
							if (nidToRefsetMemberMap != null) {
								nidToRefsetMemberMap.put(nid, refsetMember);
							}
						}
					} else {
						factory.create(nid, typeNid, enclosingConcept, input);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				newRefsetMemberList.add(refsetMember);
			}
		}
		newRefsetMemberList.trimToSize();
		return newRefsetMemberList;
	}

	@Override
	public void objectToEntry(List<RefsetMember<?, ?>> list,
			TupleOutput output) {
		List<RefsetMember<?, ?>> refsetMembersToWrite = new ArrayList<RefsetMember<?, ?>>(list.size());
		for (RefsetMember<?, ?> refsetMember: list) {
			encountered.incrementAndGet();
			assert refsetMember.primordialSapNid != Integer.MAX_VALUE;
			if (refsetMember.primordialSapNid > maxReadOnlyStatusAtPositionId) {
			    if (refsetMember.getTime() != Long.MIN_VALUE) {
	                refsetMembersToWrite.add(refsetMember);
			    }
			} else {
				if (refsetMember.revisions != null) {
					for (RefsetRevision<?, ?> r: refsetMember.revisions) {
						if (r.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId &&
						        r.getTime() != Long.MIN_VALUE) {
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
	public void setupBinder(Concept enclosingConcept, GCValueComponentMap componentMap) {
		this.enclosingConcept = enclosingConcept;
		this.componentMap = componentMap;
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
