package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ExtensionValidator{
		
	private I_TermFactory termFactory = null;
	private HashMap<String, Integer> extensions = new HashMap<String, Integer>();
	private int[] extNids = {ConceptConstants.BOOLEAN_EXT.localize().getNid()
							,ConceptConstants.CONCEPT_EXT.localize().getNid()
							,ConceptConstants.CON_INT_EXT.localize().getNid()
							,
							}; 
		
	public ExtensionValidator(){
		termFactory = LocalVersionedTerminology.get();
		
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.BOOLEAN.name(), ConceptConstants.BOOLEAN_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.CONCEPT.name(), ConceptConstants.CONCEPT_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.CON_INT.name(), ConceptConstants.CON_INT_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.CROSS_MAP.name(), ConceptConstants.CROSS_MAP_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.CROSS_MAP_FOR_REL.name(), ConceptConstants.CROSS_MAP_REL_EXT.localize().getNid());
		
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.INTEGER.name(), ConceptConstants.INT_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.LANGUAGE.name(), ConceptConstants.LANGUAGE_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.MEASUREMENT.name(), ConceptConstants.MEASUREMENT_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.SCOPED_LANGUAGE.name(), ConceptConstants.SCOPED_LANG_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.STRING.name(), ConceptConstants.STRING_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.TEMPLATE.name(), ConceptConstants.TEMPLATE_EXT.localize().getNid());
		extensions.put(I_HostConceptPlugins.REFSET_TYPES.TEMPLATE_FOR_REL.name(), ConceptConstants.TEMPLATE_FOR_REL_EXT.localize().getNid());
		
		
	}//End constructor
				
	public List<AlertToDataConstraintFailure> validate(int componentId
			, int refsetType
			, boolean forCommit) throws TaskFailedException{
		
		List<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
		List<I_GetConceptData> conceptTypesInError = new ArrayList<I_GetConceptData>();
		List<I_GetConceptData> distinctRefsets = new ArrayList<I_GetConceptData>();
		
		I_GetConceptData inclusionTypeConcept = null;
		
		try{
			
			I_IntSet allowedTypes = termFactory.newIntSet();
	    	allowedTypes.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
	    	allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			
			
			// refesetType == null --> do all refset types (wildcarrd)
			
			if(refsetType == -1){
				for( I_HostConceptPlugins.REFSET_TYPES rsTypes : I_HostConceptPlugins.REFSET_TYPES.values() ){
	    			
					int typeId = extensions.get(rsTypes.name()).intValue();
					
					validate(componentId, typeId, forCommit);
	    		}// End for loop
			}//End if  
			
			
	    	// Could not use switch statement as it requires constant. Did not want to hard code Nid
	    	if(refsetType ==  extensions.get(REFSET_TYPES.BOOLEAN.name()).intValue()){
	    		
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.CONCEPT.name()).intValue()){
	    		/*
				 * Get concept for Refset Auxilary -> "inclusion type"
				 */
				inclusionTypeConcept = termFactory.getConcept(ConceptConstants.REFSET.localize().getNid());
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.CON_INT.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.CROSS_MAP.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.CROSS_MAP_FOR_REL.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.INTEGER.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.LANGUAGE.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.MEASUREMENT.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.SCOPED_LANGUAGE.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.STRING.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.TEMPLATE.name()).intValue()){
	    	}
	    	else if(refsetType == extensions.get(REFSET_TYPES.TEMPLATE_FOR_REL.name()).intValue()){
	    	}
	    	
	    	
	    	
			if(inclusionTypeConcept == null)
				return alertList;
			
			/*
	    	 * Get "is a" source rels for concept
	    	 */
	    	Set<I_GetConceptData> inclusionTypes = inclusionTypeConcept.getDestRelOrigins(null, allowedTypes, null, false);
			
			
			for (I_ThinExtByRefVersioned ext: termFactory.getAllExtensionsForComponent(componentId, false)) {
	    		
	    		List<? extends I_ThinExtByRefPart> extensionVersions = ext.getVersions();
	    		int latest = Integer.MIN_VALUE;
	            for (I_ThinExtByRefPart currentVersion : extensionVersions) {
	                if (currentVersion.getVersion() > latest) {
	                    latest = currentVersion.getVersion();
	                }
	            }//End 1st inner for loop
	            
	            boolean alertAdded = false;
	            	            
	            for (I_ThinExtByRefPart currentVersion : extensionVersions) {
	                if (currentVersion.getVersion() == latest) {
	                    I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) currentVersion;
	//                    System.out.println("ext part version type concept -> " + termFactory.getConcept(temp.getConceptId()));
	                    I_GetConceptData extConceptType = termFactory.getConcept(temp.getConceptId()); 
	                   
	                    //Check 1 >>> concept value is child of Refset Auxilary -> inclusion Type
	                    if(!inclusionTypes.contains(extConceptType)){
	                    	
	                    	if(!conceptTypesInError.contains(extConceptType)){
	                    		conceptTypesInError.add(extConceptType);
	                        	String alertString = "<html>The concept type " + extConceptType.getInitialText()
	                            + "<br>is not a child of  " + inclusionTypeConcept.getInitialText()
	                            + "<br>Please cancel edits...";
	                        		
	                			
	                			AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
	                            if (forCommit) {
	                                 alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
	                            }
	                            AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
	                            		alertString, termFactory.getConcept(componentId));
	                            
	                            alertList.add(alert);                     	
	                            alertAdded = true;
	                    	}//End if
	                    }//End if 
	                    
	                  //Check 2 >>> refset does not exist twice against concept
	                    I_GetConceptData refsetConcept = termFactory.getConcept(ext.getRefsetId());
	                    if(!distinctRefsets.contains(refsetConcept)){
	                    	distinctRefsets.add(refsetConcept);
	                    }
	                    else{
	                    	String alertString = "<html>The refset " + refsetConcept.getInitialText()
	                        + "<br>has been added more than once" 
	                        + "<br>Please cancel edits...";
	                    		
	            			
	            			AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
	                        if (forCommit) {
	                             alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
	                        }
	//                        AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
	//                        		alertString, termFactory.getConcept(extension.getComponentId()));
	                        AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
	                        		alertString, termFactory.getConcept(refsetConcept.getConceptId()));
	                        
	                        
	                        if(!alertList.contains(alert)){
	                        	alertList.add(alert);
	                        }
	                        alertAdded = true;
	                    }//End if
	                    
	                    if(alertAdded) break;
	                    
	    	        }//End 2nd inner for loop
	                    
	            }//End if 
	        }//End for loop
		
		}
		catch (IOException e) {
            throw new TaskFailedException(e);
    	}
         catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
		
		
		return alertList; 
		
	}//End method validate
	
}//End class ExtensionValidator