package org.ihtsdo.cs.econcept.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;

// Ensure multiple members are not added with identical saps
public class WfDuplicateSapMemberFilter extends AbstractWfChangeSetFilter {
	private HashMap<UUID, ArrayList<TkRefexAbstractMember<?>>> membersAnnotationMap = new HashMap<UUID, ArrayList<TkRefexAbstractMember<?>>>();

	WfDuplicateSapMemberFilter(String filePath) {
		super(filePath);
	}

	@Override
	public boolean scrubMembers(
			HashSet<TkRefexAbstractMember<?>> wfMembersToCommit) {
		for (TkRefexAbstractMember<?> member : wfMembersToCommit) {
			boolean badDupFound = false;

			if (membersAnnotationMap.containsKey(member.getComponentUuid())) {
				// Search already visited Concept's Members to ensure don't have duplicate SAP
				for (TkRefexAbstractMember<?> testMember : membersAnnotationMap.get(member.getComponentUuid())) {
					if (testMember.getStatusUuid().equals(member.getStatusUuid()) &&
						testMember.getAuthorUuid().equals(member.getAuthorUuid()) && 
						testMember.getPathUuid().equals(member.getPathUuid()) && 
						testMember.getTime() == member.getTime()) {
						
						if (((TkRefsetStrMember)member).getString1().equals(((TkRefsetStrMember)testMember).getString1())) {
							AceLog.getAppLog().severe("Same SAP but with different Refset Str Value with member: " + ((TkRefsetStrMember)member).getString1() +
													  "testMember: " + ((TkRefsetStrMember)testMember).getString1());
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

					ArrayList<TkRefexAbstractMember<?>> wfMembers = membersAnnotationMap.get(member.getComponentUuid());
					wfMembers.add(member);
					membersAnnotationMap.put(member.getComponentUuid(), wfMembers);			
				}
			} else {
				wfMembersToProcess.add(member);

				ArrayList<TkRefexAbstractMember<?>> wfMembers = new ArrayList<TkRefexAbstractMember<?>>();
				wfMembers.add(member);
				membersAnnotationMap.put(member.getComponentUuid(), wfMembers);			
			}				
		}
		
		return true;
	}

	@Override
	public HashSet<TkRefexAbstractMember<?>> getApprovedMembers() {
		return wfMembersToProcess;
	}
}
