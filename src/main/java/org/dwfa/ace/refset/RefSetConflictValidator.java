package org.dwfa.ace.refset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.spec.ConceptSpec;

class RefSetConflictValidator implements I_ProcessConcepts {
//  private class MemberSetCalculator implements I_ProcessConcepts {

	  private boolean hasConflicts = false;
	  private HashMap<Integer, Conflicts> conflictedConcepts = new HashMap<Integer, Conflicts>();
	  
	  public static final int NO_TYPE_DEFINED = 0;//????
	  
      private I_TermFactory termFactory;
      private int invalidConcepts;
      private int referenceSetId;
      private int includeLineageId;
      private int excludeLineageId;
      private int includeIndividualId;
      private int excludeIndividualId;
      private int conceptTypeId;

      
      //TODO: Create getters/setters
      private ConceptSpec refSetSpecDescriptor;
      
      public void setConceptSpec(ConceptSpec spec){
    	  this.refSetSpecDescriptor = spec;
      }
      
      /**
       * Calculates a member set given a reference set spec.
       * @param referenceSetId The id of the reference set of which we wish to
       * calculate the member set.
       * @throws Exception
       */
      public RefSetConflictValidator() throws Exception {

          termFactory = LocalVersionedTerminology.get();

          // verify concepts
         
          I_GetConceptData refConcept = termFactory.getConcept( refSetSpecDescriptor.localize().getUids() );
          referenceSetId = refConcept.getConceptId();

          invalidConcepts = 0;

          includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
          excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());
          includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
          excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
          conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
      }

      public RefSetConflictValidator(ConceptSpec specDescriptor) throws Exception{
    	  this();
    	  refSetSpecDescriptor = specDescriptor;
      }
      
      public boolean hasConflicts(){
    	  return this.hasConflicts;
      }
      
      public void validate() throws Exception{
    	  termFactory.iterateConcepts( this );
      }
      
      /**
       * Processes each concept in the database. Validate that any inherited
       * conditions from parents are non-conflicting.
       */
      public void processConcept(I_GetConceptData concept) throws Exception {

          int conceptId = concept.getConceptId();

          if (getLatestRefSetType(concept) == 0) {
              // no ref sets found so need to find the parents of this concept
              // for processing

              List<Integer> parentIds = getParents(concept);

              Conflicts conflicts = processParents(parentIds);
              
              if( !conflicts.getConflictDetails().isEmpty() ){
            	  this.hasConflicts = true;
            	  this.conflictedConcepts.put( conceptId, conflicts );
              }
          }
      }

      /**
       * Returns the FSN associated with the given concept.
       * @param conceptId the ID of the concept who's FSN we want.
       * @return The FSN associated with the specified concept.
       * @throws Exception
       */
      private String getFsnFromConceptId(int conceptId) throws Exception {

          I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(conceptId);

          List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
          int fsnId = LocalVersionedTerminology.get().uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.
                  getUids().iterator().next());
          for (I_DescriptionVersioned description : descriptions) {
              List<I_DescriptionPart> parts = description.getVersions();
              for (I_DescriptionPart part : parts) {
                  if (fsnId == part.getTypeId()) {
                      return part.getText();
                  }
              }
          }

          return "unknown";
      }

      /**
       * Calculates if an agreed inherited ref set type can be calculated given
       * the list of parents.
       * @return CONFLICT if the values conflict, VALID if the values are valid &
       * non-conflicting. ROOT is returned if the concept has no parents to process.
       */
      private Conflicts processParents(List<Integer> parentIds) throws Exception {

          Conflicts conflicts = new Conflicts();        
          
          for (Integer parentId : parentIds) {
              int parentType = getLatestRefSetType(termFactory.getConcept(parentId));

              if (parentType == NO_TYPE_DEFINED || parentType == includeIndividualId ||
                  parentType == excludeIndividualId) {
                  // parent has no inheritable type, so need to process its parents

                  List<Integer> grandParentIds = getParents(
                          termFactory.getConcept(parentId));
                  conflicts.putAll( processParents( grandParentIds ).getConflictDetails() );
              } else {
                  conflicts.put(parentId, parentType);
              }
          }//End for loop

          if(conflicts.getConflictDetails().containsValue(excludeLineageId) && 
        	 conflicts.getConflictDetails().containsValue(includeLineageId)){
        	  //Nothing to do...
          }
          else{
        	  conflicts.clear();
          }

          return conflicts;
      }

      /**
       * Gets the parents of a particular concept.
       * @param concept The concept whose parents we want to find.
       * @return List of parent IDs.
       */
      private List<Integer> getParents(I_GetConceptData concept) throws Exception {
          I_IntSet isARel = termFactory.newIntSet();
          isARel.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                  IS_A_REL.getUids()).getConceptId());
          List<I_RelTuple> relTuples = concept.getSourceRelTuples(null, isARel, null, false);
          List<Integer> parentIds = new ArrayList<Integer>();
          for (I_RelTuple currentRel : relTuples) {
              parentIds.add(currentRel.getC2Id());
          }
          return parentIds;
      }

      /**
       * Gets the latest ref set type for a concept (e.g. include individual).
       * @param concept The concept.
       * @return int representing the internal id of the ref set type.
       */
      private int getLatestRefSetType(I_GetConceptData concept) throws Exception {

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
                  }
                  int typeId = NO_TYPE_DEFINED;

                  for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                      if (currentVersion.getVersion() == latest) {
                          I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) currentVersion;
                          typeId = temp.getConceptId();
                          return typeId;
                      }
                  }
              }
          }
          return 1;//NO_TYPE_DEFINED;
      }

      private class Conflicts{
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
  }//End class RefsetConflictValidator