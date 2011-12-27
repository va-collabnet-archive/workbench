package org.ihtsdo.cs.econcept.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;

// Ensure multiple members are not added with identical saps
public class WfDuplicateSapMemberFilter extends AbstractWfChangeSetFilter {
	private HashMap<UUID, ArrayList<TkRefsetAbstractMember<?>>> membersAnnotationMap = new HashMap<UUID, ArrayList<TkRefsetAbstractMember<?>>>();

	WfDuplicateSapMemberFilter(String filePath) {
		super(filePath);
	}

	@Override
	public boolean scrubMembers(
			HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit) {
		for (TkRefsetAbstractMember<?> member : wfMembersToCommit) {
			boolean badDupFound = false;

			if (membersAnnotationMap.containsKey(member.getComponentUuid())) {
				// Search already visited Concept's Members to ensure don't have duplicate SAP
				for (TkRefsetAbstractMember<?> testMember : membersAnnotationMap.get(member.getComponentUuid())) {
					if (testMember.getStatusUuid().equals(member.getStatusUuid()) &&
						testMember.getAuthorUuid().equals(member.getAuthorUuid()) && 
						testMember.getPathUuid().equals(member.getPathUuid()) && 
						testMember.getTime() == member.getTime()) {
						
						if (((TkRefsetStrMember)member).getStrValue().equals(((TkRefsetStrMember)testMember).getStrValue())) {
							AceLog.getAppLog().severe("Same SAP but with different Refset Str Value with member: " + ((TkRefsetStrMember)member).getStrValue() +
													  "testMember: " + ((TkRefsetStrMember)testMember).getStrValue());
							return false;
						} else {
							wfMembersToIgnore.add(member);
							printError(member);
							printError("For testMember: S: " + testMember.getStatusUuid() + " A: " + testMember.getAuthorUuid() + 
									   " P: " + testMember.getPathUuid() + " T: " + testMember.getTime());
							printError("For     member: S: " + member.getStatusUuid() + " A: " + member.getAuthorUuid() + 
									   " P: " + member.getPathUuid() + " T: " + member.getTime() + "\n");
							badDupFound = true;
							break;
						}
					}
				}
				
				if (!badDupFound) {
					wfMembersToProcess.add(member);

					ArrayList<TkRefsetAbstractMember<?>> wfMembers = membersAnnotationMap.get(member.getComponentUuid());
					wfMembers.add(member);
					membersAnnotationMap.put(member.getComponentUuid(), wfMembers);			
				}
			} else {
				wfMembersToProcess.add(member);

				ArrayList<TkRefsetAbstractMember<?>> wfMembers = new ArrayList<TkRefsetAbstractMember<?>>();
				wfMembers.add(member);
				membersAnnotationMap.put(member.getComponentUuid(), wfMembers);			
			}				
		}
		
		return true;
	}

	@Override
	public HashSet<TkRefsetAbstractMember<?>> getApprovedMembers() {
		return wfMembersToProcess;
	}
}
