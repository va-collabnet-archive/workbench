package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;

public class RefSetConflictValidator extends RefsetUtilities/*implements I_ProcessConcepts*/{
	
	  private boolean hasConflicts = false;
	  private HashMap<Integer, Conflicts> conflictedConcepts = new HashMap<Integer, Conflicts>();
	  
	  public static final int NO_TYPE_DEFINED = 0;
	  
      private I_TermFactory termFactory;
      private int referenceSetId = -1;
      private int includeLineageId;
      private int excludeLineageId;
      private int conceptTypeId;
	
      private I_GetConceptData specConcept;
      
      private Set<Integer> refsetMemberComopnentIds = new HashSet<Integer>();
      
      
      public HashMap<Integer, Conflicts> getConflicts(){
    	  return this.conflictedConcepts;
      }
      
      public void setConceptSpec(I_GetConceptData concept){
    	  this.specConcept = concept;
    	  this.referenceSetId = concept.getConceptId();
      }
	
	
	
      /**
       * Calculates a member set given a reference set spec.
       * @param referenceSetId The id of the reference set of which we wish to
       * calculate the member set.
       * @throws Exception
       */
      public RefSetConflictValidator() throws IOException,TerminologyException{
    	  termFactory = LocalVersionedTerminology.get();
    	
    	  includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
    	  excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
    	  conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
      }
	 
      public RefSetConflictValidator(I_GetConceptData SpecificationConcept) throws IOException, TerminologyException{
    	  this();
    	  this.specConcept = SpecificationConcept;
    	  referenceSetId = specConcept.getConceptId();
      }
      
      public boolean hasConflicts(){
    	  return this.hasConflicts;
      }
      
	
      public void validate(/*I_GetConceptData concept*/) throws IOException, TerminologyException{
    	
    	  if(referenceSetId == -1){
    		  //Get all allowed refsets
    		  List<Integer> allowedRefsets = getSpecificationRefsets();
    		  for(Integer refsetId : allowedRefsets){
    			  
    			  referenceSetId = refsetId;
    			  validateWrapper();
    		  }//End for loop
    	  }
    	  else{
    		  validateWrapper();
    	  }
    	      	  
      }//End method validate
      
      private void validateWrapper() throws IOException, TerminologyException{
    	//Get all committed members of the refset
    	  List<I_ThinExtByRefVersioned> committedExtMembers =  termFactory.getRefsetExtensionMembers(referenceSetId);
    	  
    	  for(I_ThinExtByRefVersioned extMember : committedExtMembers){
    		  refsetMemberComopnentIds.add(extMember.getComponentId());
    	  }
    	  
    	      	  
    	  //Get all uncommitted members of the refset
    	  Set<I_Transact> uncommitted =  termFactory.getUncommitted();
    	  
    	  for(I_Transact t : uncommitted){
    		  if(I_GetExtensionData.class.isAssignableFrom(t.getClass())){
    			  I_ThinExtByRefVersioned member = ((I_GetExtensionData)t).getExtension();
    			  if(member != null){
    				  int refsetId = member.getRefsetId();
    				  if(refsetId == referenceSetId){
    					  refsetMemberComopnentIds.add(member.getComponentId());
    				  }
    			  }
    		  }
    	  }//End for loop
    	  
    	  Conflicts conflicts = new Conflicts();
    	  //I think we have all the members now. Lets check for conflicts...
    	  for(Integer componentId : refsetMemberComopnentIds)  {
    		  I_GetConceptData componentConcept = termFactory.getConcept(componentId);
    		  
    		  //Walk through hierarchy until we find a concept that is in the refsetMemberComopnentIds set
    		  conflicts.putAll(findConflicts(componentConcept).getConflictDetails());
    		  
    	  }//End for loop
    	  if(conflicts.getConflictDetails().size() > 0){
    		  hasConflicts = true;
    	  }
      }//End validateWrapper
      
      /*
       * Method to find all refset conflicts against concept.
       */
      private Conflicts findConflicts(I_GetConceptData concept) throws IOException,TerminologyException{
    	  
    	  Conflicts conflicts = new Conflicts();
    	  List<Integer> parentIds = getParents(concept);
    	  int conceptRefsetTypeId = getLatestRefSetType(concept);
    	  
    	  for (Integer parentId : parentIds) {
    		  System.out.println("parentIdA...."+parentId);
    		  if(refsetMemberComopnentIds.contains(parentId)){
    			  System.out.println("parentId...."+parentId);
    			  I_GetConceptData parentConcept = termFactory.getConcept(parentId);
    			  for(Integer id : refsetMemberComopnentIds){
    				  if(id.intValue() == parentId.intValue()){
    					  int memberRefsetTypeId = getLatestRefSetType(parentConcept);  					  
    					  if(memberRefsetTypeId == 0){
    						  conflicts.putAll(findConflicts(parentConcept).getConflictDetails());
    					  }
    					  else{
    						  if(memberRefsetTypeId != conceptRefsetTypeId &&
    						     (memberRefsetTypeId == includeLineageId || memberRefsetTypeId == excludeLineageId) &&
    						     (conceptRefsetTypeId == includeLineageId || conceptRefsetTypeId == excludeLineageId)){
    							  conflicts.put(parentId, memberRefsetTypeId);
    						  }
    					  }
    				  }
    			  }//End inner for loop
    		  }
    	  }//End for loop
    	  
    	  return conflicts;
      }//End method findConflicts
      

      
      /**
       * Gets the parents of a particular concept.
       * @param concept The concept whose parents we want to find.
       * @return List of parent IDs.
       */
      private List<Integer> getParents(I_GetConceptData concept) throws IOException,TerminologyException {
          I_IntSet isARel = termFactory.newIntSet();
          isARel.add(termFactory.uuidToNative( ArchitectonicAuxiliary.Concept.IS_A_REL.getUids() ));
          isARel.add(termFactory.uuidToNative( SNOMED.Concept.IS_A.getUids() ));
        
          
          Set<I_GetConceptData> parentConcepts = concept.getSourceRelTargets(null, null, null, false);
         
          List<Integer> parentIds = new ArrayList<Integer>();
          for(I_GetConceptData parentConcept : parentConcepts){
        	I_RelVersioned dest=  parentConcept.getSourceRels().iterator().next();
        	parentIds.add(parentConcept.getConceptId());
          }
          

          return parentIds;
      }//End method getParents
      
      /*
       * Gets the latest ref set type for a concept (e.g. include individual).
       * @param concept The concept.
       * @return int representing the internal id of the ref set type.
       */
      private int getLatestRefSetType(I_GetConceptData concept) throws IOException {

          int conceptId = concept.getConceptId();

          List<I_GetExtensionData> extensions =
              termFactory.getExtensionsForComponent(conceptId);

          for (I_GetExtensionData extensionData: extensions) {
              I_ThinExtByRefVersioned part = extensionData.getExtension();
              List<? extends I_ThinExtByRefPart> extensionVersions = part.getVersions();

              if (part.getTypeId() == conceptTypeId &&
                          part.getRefsetId() == referenceSetId) {

                  int latest = Integer.MIN_VALUE;
                  for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                      if (currentVersion.getVersion() > latest) {
                          latest = currentVersion.getVersion();
                      }
                  }//End 1st inner for loop
                  int typeId = NO_TYPE_DEFINED;

                  for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                      if (currentVersion.getVersion() == latest) {
                          I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) currentVersion;
                          typeId = temp.getConceptId();
                          return typeId;
                      }
                  }//End 2nd inner for loop
              }
          }//End outer for loop
          return NO_TYPE_DEFINED;
      }//End method getLatestRefSetType
      
      
      /**
       * 
       * Nested class to store conflict details for use from calling class. 
       *
       */
      public class Conflicts{
    	  private HashMap<Integer, Integer> conflictDetails = new HashMap<Integer, Integer>();
    	  
    	  public void put(Integer conflictingConceptId, Integer refsetTypeId){
    		  conflictDetails.put(conflictingConceptId, refsetTypeId);
    	  }
    	  
    	  public void putAll(HashMap<Integer, Integer> conflicts){
    		  conflictDetails.putAll( conflicts );
    	  }
    	  
    	  public HashMap<Integer, Integer> getConflictDetails(){
    		  return conflictDetails;
    	  }
    	  
    	  public void clear(){
    		  conflictDetails.clear();
    	  }
    	  
      }//End Inner class Conflicts
}//End class RefSetConflictValidator