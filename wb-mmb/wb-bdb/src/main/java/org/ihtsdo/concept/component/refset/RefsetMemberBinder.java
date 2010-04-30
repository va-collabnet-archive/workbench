package org.ihtsdo.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_BindConceptComponents;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


public class RefsetMemberBinder extends TupleBinding<Collection<RefsetMember<?, ?>>> 
	implements I_BindConceptComponents {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	private static int maxReadOnlyStatusAtPositionId = Bdb.getSapDb().getReadOnlyMax();

	RefsetMemberFactory factory = new RefsetMemberFactory();

	private Collection<RefsetMember<?, ?>> refsetMemberList;
	private Concept enclosingConcept; 


	@Override
	public Collection<RefsetMember<?, ?>> entryToObject(TupleInput input) {
		assert enclosingConcept != null;
		int listSize = input.readInt();
		Collection<RefsetMember<?, ?>> newRefsetMemberList;
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
			
			// Can be removed in the future, here strictly for read/write conformance testing.
			try {
                REFSET_TYPES.nidToType(typeNid);
            } catch (IOException e1) {
               AceLog.getAppLog().alertAndLogException(
                   new Exception("For concept: " + enclosingConcept.toString(), e1));
               AceLog.getAppLog().info("List prior to exception: " + 
                   newRefsetMemberList);
               return newRefsetMemberList;
            }
			input.mark(8);
			int nid = input.readInt();
			input.reset();
			RefsetMember<?, ?> refsetMember = (RefsetMember<?, ?>) Concept.componentsCRHM.get(nid);
			if (nidToRefsetMemberMap != null && nidToRefsetMemberMap.containsKey(nid)) {
				if (refsetMember == null) {
					refsetMember = nidToRefsetMemberMap.get(nid);
					RefsetMember<?, ?> oldMember = (RefsetMember<?, ?>) Concept.componentsCRHM.putIfAbsent(nid, refsetMember);
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
						if (refsetMember.getTime() != Long.MIN_VALUE) {
						    Concept.componentsCRHM.putIfAbsent(nid, refsetMember);
	                        RefsetMember<?, ?> oldMember = (RefsetMember<?, ?>) Concept.componentsCRHM.putIfAbsent(nid, refsetMember);
	                        if (oldMember != null) {
	                            refsetMember = oldMember;
	                            if (nidToRefsetMemberMap != null) {
	                                nidToRefsetMemberMap.put(nid, refsetMember);
	                            }
	                        }
						} else {
						    AceLog.getAppLog().warning("\n########## Suppressing refset member:\n     " + refsetMember +
						        "\n##########");
						}
					} else {
					    refsetMember.merge(factory.create(nid, typeNid, enclosingConcept, input));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if (refsetMember.getTime() != Long.MIN_VALUE) {
	                newRefsetMemberList.add(refsetMember);
				}
			}
		}
		return newRefsetMemberList;
	}

	@Override
	public void objectToEntry(Collection<RefsetMember<?, ?>> list,
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
	public void setupBinder(Concept enclosingConcept) {
		this.enclosingConcept = enclosingConcept;
	}


	public void setTermComponentList(
	        Collection<RefsetMember<?, ?>> componentList) {
		this.refsetMemberList = componentList;
		
	}

	public Concept getEnclosingConcept() {
		return enclosingConcept;
	}

	public void setEnclosingConcept(Concept enclosingConcept) {
		this.enclosingConcept = enclosingConcept;
	}

}
