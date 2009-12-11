/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class RefsetOverlapValidator extends RefsetUtilities{
	
	private I_TermFactory termFactory;
	
	private int firstReferenceSetId = -1;
	private int secondReferenceSetId = -1;
	
	private Set<Integer> firstRefsetMemberComopnentIds = new HashSet<Integer>();
	private Set<Integer> secondRefsetMemberComopnentIds = new HashSet<Integer>();
	private Set<Integer> overlapedMemberComopnentIds = new HashSet<Integer>();
	
	/*
	 * Define Getter and setter methods 
	 */
	public void setFirstRefsetId(int refsetId){
		this.firstReferenceSetId = refsetId;
	}
	
	public void setSecondRefsetId(int refsetId){
		this.secondReferenceSetId = refsetId;
	}
	
	public Set<Integer> getOverlapedMemberComopnentIds(){
		return this.overlapedMemberComopnentIds;
	}
	
	/*
	 * Define constructors
	 */
	public RefsetOverlapValidator(){
		termFactory = LocalVersionedTerminology.get();
	}
	
	public RefsetOverlapValidator(int firstRefsetId, int secondRefsetId){
		super();
		this.firstReferenceSetId = firstRefsetId;
		this.secondReferenceSetId = secondRefsetId;
	}
	
	
	/*
	 * Define methods
	 */
	public void validate(){		
		try{
			if(firstReferenceSetId == -1 || secondReferenceSetId == -1){
				/*
				 * No specific refsets have been set. 
				 * Need to get all refsets and compare against each other
				 */
				List<Integer> allowedRefsets = new ArrayList<Integer>();
				allowedRefsets = getSpecificationRefsets();
				List<Integer> compareRefsets = new ArrayList<Integer>();
				compareRefsets.addAll(allowedRefsets);
				
				for(Integer i : allowedRefsets){
					System.out.println("Allowed refset........................ "+termFactory.getConcept(i.intValue()).toString());
					Set<Integer> refsetMembers = getRefsetMembers(i.intValue());
					for(Integer ii: compareRefsets){
						if(ii.intValue() != i.intValue()){
							Set<Integer> comparedMembers = getRefsetMembers(ii.intValue());
							for(Integer member : comparedMembers){
								System.out.println("member........................ "+termFactory.getConcept(member.intValue()).toString());
								if(refsetMembers.contains(member)){
									if(!overlapedMemberComopnentIds.contains(member)){
										overlapedMemberComopnentIds.add(member);
									}
								}
							}//End inner-inner for loop
						}//End if
					}//End inner for loop
				}//End for loop
			}//End if
			else{
				/*
				 * Get all member sets for each refset
				 */
				firstRefsetMemberComopnentIds = getRefsetMembers(firstReferenceSetId);
				secondRefsetMemberComopnentIds = getRefsetMembers(secondReferenceSetId);
				
				/*
				 * Compare members in each set
				 */
				compareMembers();
			}//End else
		}
		catch(IOException e){AceLog.getAppLog().alertAndLogException(e);}
		catch(TerminologyException e){AceLog.getAppLog().alertAndLogException(e);}
	}//End method validate
	
	public boolean hasOverlaps(){
		return overlapedMemberComopnentIds.size() > 0 ? true: false;
	}
	
		
	private void compareMembers(){
		for(Integer member  : firstRefsetMemberComopnentIds){
			if(secondRefsetMemberComopnentIds.contains(member)){
				if(!overlapedMemberComopnentIds.contains(member)){
					overlapedMemberComopnentIds.add(member);
				}
			}
		}//End for loop
		
	}
	
	private Set<Integer> getRefsetMembers(int referenceSetId){
		Set<Integer> memberComopnentIds = new HashSet<Integer>();
		
		try{
			//Get all committed members of the refset
  	  		List<I_ThinExtByRefVersioned> committedExtMembers =  termFactory.getRefsetExtensionMembers(referenceSetId);
  	  
	  	  	for(I_ThinExtByRefVersioned extMember : committedExtMembers){
	  	  		memberComopnentIds.add(extMember.getComponentId());
	  	  	}
	  	  
	  	      	  
	  	  	//Get all uncommitted members of the refset
	  	  	Set<I_Transact> uncommitted =  termFactory.getUncommitted();
	  	  
	  	  	for(I_Transact t : uncommitted){
	  	  		if(I_GetExtensionData.class.isAssignableFrom(t.getClass())){
	  	  			I_ThinExtByRefVersioned member = ((I_GetExtensionData)t).getExtension();
	  	  			if(member != null){
	  	  				int refsetId = member.getRefsetId();
	  	  				if(refsetId == referenceSetId){
	  	  					memberComopnentIds.add(member.getComponentId());
	  	  				}
	  	  			}
	  	  		}
	  	  	}//End for loop
		}
		catch(IOException e){AceLog.getAppLog().alertAndLogException(e);}
		
		return memberComopnentIds;
		
	}//end mthod getRefsetMebers
	
}//End class RefsetOverlapValidator
