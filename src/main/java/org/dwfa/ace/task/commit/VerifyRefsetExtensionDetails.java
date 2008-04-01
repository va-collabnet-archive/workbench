package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)})
public class VerifyRefsetExtensionDetails extends AbstractExtensionTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    
    private I_TermFactory termFactory = null;
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }
    }

    
    @Override
    public List<AlertToDataConstraintFailure> test(I_ThinExtByRefVersioned extension, 
    		boolean forCommit)
            throws TaskFailedException {
    	List<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
    	
    	termFactory = LocalVersionedTerminology.get();
    	try{
    		/*
    		 * Get concept for Refset Auxilary -> "inclusion type"
    		 */
    		UUID[] ids = new UUID[1];
			ids[0] = UUID.fromString("566c380d-a9ac-318e-9e96-9df2fd405a53");
			I_GetConceptData inclusionTypeConcept = termFactory.getConcept(ids);
    		
			/*
	    	 * Get "is a" source rels for concept
	    	 */
	    	I_IntSet allowedTypes = termFactory.newIntSet();
	    	allowedTypes.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
	    	allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
	    	
			Set<I_GetConceptData> inclusionTypes = inclusionTypeConcept.getDestRelOrigins(null, allowedTypes, null, false);
			
			List<I_GetConceptData> conceptTypesInError = new ArrayList<I_GetConceptData>();
			List<I_GetConceptData> distinctRefsets = new ArrayList<I_GetConceptData>();
			
	    	
	    	for (I_ThinExtByRefVersioned ext: termFactory.getAllExtensionsForComponent(extension.getComponentId(), true)) {
	    		
	    		List<? extends I_ThinExtByRefPart> extensionVersions = ext.getVersions();
	    		int latest = Integer.MIN_VALUE;
                for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                    if (currentVersion.getVersion() > latest) {
                        latest = currentVersion.getVersion();
                    }
                }//End 1st inner for loop
                
                
                
                
                for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                    if (currentVersion.getVersion() == latest) {
                        I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) currentVersion;
                        System.out.println("ext part version type concept -> " + termFactory.getConcept(temp.getConceptId()));
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
	                            		alertString, termFactory.getConcept(extension.getComponentId()));
	                            
	                            alertList.add(alert);                     	
	                        	break;
                        	}//End if
                        }//End if 
                    }//End if 
                }//End 2nd inner for loop
                
                //Check 2 >>> refset does not exist twice against concept
                I_GetConceptData refsetConcept = termFactory.getConcept(ext.getRefsetId());
                if(!distinctRefsets.contains(refsetConcept)){
                	distinctRefsets.add(refsetConcept);
                }
                else{
                	String alertString = "<html>The refset " + refsetConcept.getInitialText()
                    + "<br>has been added twice" 
                    + "<br>Please cancel edits...";
                		
        			
        			AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                    if (forCommit) {
                         alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
                    }
                    AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
                    		alertString, termFactory.getConcept(extension.getComponentId()));
                    
                    alertList.add(alert);                     	
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
    }//End method test
    
}//End class VerifyRefsetExtensionDetails