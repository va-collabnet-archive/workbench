package org.dwfa.ace.task.refset.refresh;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.I_ConceptEnumeration;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The RefreshRefsetSpecCompareTask uses the information 
 * collected in the RefreshRefsetSpecWizardTask task to create a list of differences 
 * between the selected Refset and the selected version of SNOMED. 
 * 
 * @author Perry Reid
 * @version 1, November 2009
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class RefreshRefsetSpecCompareTask extends AbstractTask {


    /* -----------------------
     * Properties
     * -----------------------
     */
	// Serialization Properties 
	private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Concept Constants:  (taken from: SNOMED CT Concept -> Linkage concept -> 
    // 				Attribute -> Concept history attribute) 
    public static final ConceptSpec SAME_AS = 
    	new ConceptSpec("SAME AS", "87594159-50f0-3b5f-aa4f-f6061c0ce497");
    public static final ConceptSpec MAY_BE_A = 
    	new ConceptSpec("MAY BE A", "721dadc2-53a0-3ffa-8abd-80ff6aa87db2");
    public static final ConceptSpec REPLACED_BY = 
    	new ConceptSpec("REPLACED BY", "0b010f24-523b-3ae4-b3a2-ec1f425c8a85");
    public static final ConceptSpec MOVED_TO = 
    	new ConceptSpec("MOVED TO", "c3394436-568c-327a-9d20-4a258d65a936");
    // Concept Constants: (taken from: Terminology Auxiliary Concept -> status -> inactive) 
    public static final ConceptSpec MOVED_ELSEWHERE = 
    	new ConceptSpec("moved elsewhere", "76367831-522f-3250-83a4-8609ab298436");

	// Task Attribute Properties     
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
 	private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
	private String refsetPositionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
	private String snomedPositionSetPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
	private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
	private String changeMapPropName = ProcessAttachmentKeys.CON_CON_MAP.getAttachmentKey();

	// Other Properties 
    private Condition condition;
    private I_TermFactory termFactory;
    private I_ConfigAceFrame config;

 
    /* -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(refsetPositionSetPropName);
        out.writeObject(snomedPositionSetPropName);
        out.writeObject(uuidListListPropName);
        out.writeObject(changeMapPropName);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
            	profilePropName = (String) in.readObject();
            	refsetUuidPropName = (String) in.readObject();
            	refsetPositionSetPropName = (String) in.readObject();
                snomedPositionSetPropName = (String) in.readObject();
                uuidListListPropName = (String) in.readObject();
                changeMapPropName = (String) in.readObject();
            } 
            // Initialize transient properties...
            
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }


    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a process to another user's input queue).
     * 
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }



    /**
     * Performs the primary action of the task, which in this case is Identify all the 
     * differences between the selected Refset Spec and the selected version of SNOMED
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

    	try {
    		/* --------------------------------------------
    		 *  Get Values from process Keys 
    		 *  -------------------------------------------
    		 */
    		termFactory = LocalVersionedTerminology.get();
    		config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
 	        UUID refsetSpecUuid = (UUID) process.getProperty(refsetUuidPropName);
	        I_GetConceptData refsetSpecConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(refsetSpecUuid); 
	        Set<I_Position> refsetPositionSet = (Set<I_Position>) process.getProperty(refsetPositionSetPropName);
	        Set<I_Position> snomedPositionSet = (Set<I_Position>) process.getProperty(snomedPositionSetPropName);

	        // DEBUG:  Echo out the retrieved values 
	        System.out.println("PARAMETERS PASSED IN THROUGH KEYS");
	        System.out.println("=================================");
	        System.out.println("   REFSET SPEC NAME = " + refsetSpecConcept.getInitialText());
	        System.out.println("   REFSET POSITION SET = " + refsetPositionSet.toString());
	        System.out.println("   SNOMED POSITION SET = " + snomedPositionSet.toString());

	        
	        /* ---------------------------------------------------
	         * Define some local variables to support the queries
	         * ---------------------------------------------------
	         */
//    		List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
//    	    Map<List<UUID>, I_GetConceptData> changesMap = new HashMap<List<UUID>, I_GetConceptData>();

    	    List<I_GetConceptData> changesList = new ArrayList<I_GetConceptData>();
    	    Map<I_GetConceptData, I_GetConceptData> changesMap = new HashMap<I_GetConceptData, I_GetConceptData>();
	        I_IntSet allowedTypes; 
	        I_IntSet allowedStatus; 
	        // Define the status: "Not Current" 
	        // NOTE: The status "Not Current" means a member of the set of not current status 
	        //       values. Since there is no single value, you need to test for membership  
	        //       in the set of all the children of inactive: 
	        I_IntSet notCurrentStatus = termFactory.newIntSet();
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.CONFLICTING.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.NOT_YET_CREATED.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.RETIRED_MISSPELLED.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.IMPLIED_RELATIONSHIP.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid());
	        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.EXTINCT.localize().getNid());
       
	        
	        
	        /* ---------------------------------------------------------------------------
	         * (QUERY A) 
	         * Find concepts in the subset that have a relationship SAME_AS, MAY_BE_A, 
	         * or REPLACED_BY, and are not current (i.e. status in 1,2,3,4,5,10). 
	         * ---------------------------------------------------------------------------
	         */
	        
	        // Set Allowed Status: "Not Current" 
	        allowedStatus = notCurrentStatus;	        
	        
	        // Set Allowed Types 
			allowedTypes = termFactory.newIntSet();
			allowedTypes.add(SAME_AS.localize().getNid());
			allowedTypes.add(MAY_BE_A.localize().getNid());
			allowedTypes.add(REPLACED_BY.localize().getNid());
			
			// Execute Query "A"
			Set<I_GetConceptData> refsetSpec_A_SourceRelTargets = refsetSpecConcept.getSourceRelTargets(
					allowedStatus, allowedTypes, refsetPositionSet, false, true); 

			// If no records are found 
			if (refsetSpec_A_SourceRelTargets == null || refsetSpec_A_SourceRelTargets.size() == 0) {
	    		// Display a Debug message 
	    		JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
	    				"Query 'A' found no records to be refreshed for refset: \n" + 
	    				refsetSpecConcept.getInitialText(),
	    				"DEBUG Message",
	    				JOptionPane.INFORMATION_MESSAGE);
	    		// Cancel the task 
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_CANCELED);
	        } else {
		        //TODO Remove Debug Messages... 
		        System.out.println("QUERY A - getSourceRelTargets()");
		        System.out.println("==============================="); 
		        System.out.println("   NUMBER FOUND = " + refsetSpec_A_SourceRelTargets.size());
		        System.out.println("   INCLUDE:");
		        if (refsetSpec_A_SourceRelTargets.size() < 1000) {
			        for (I_GetConceptData concept : refsetSpec_A_SourceRelTargets) {
				        System.out.println("      " + concept.toString());
			        }	        	
		        }

		        // Add the results of the query to the List and the Map
		        for (I_GetConceptData oldConcept : refsetSpec_A_SourceRelTargets) {
		    	    
		        	// Remember the oldConcept is a list for future reference 
		        	changesList.add(oldConcept);
		    	    
		    	    // Search SNOMED for changes to this concept 
		    	    //TODO 
		        	I_GetConceptData newConcept = null;  
		        	
		    	    // Put the oldConcept and the newConcept into the changesMap 
		    	    changesMap.put(oldConcept, newConcept);  

//		        	List<UUID> uuidList = new ArrayList<UUID>();
//	    			uuidList = (List<UUID>) concept.getUids();
//	        		uuidListOfLists.add(uuidList);
		        }	        	

		        // Set task completion status 
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_COMPLETE);
	        }
	        


	        /* ---------------------------------------------------------------------------
	         * (QUERY B) 
	         * Find concepts in the subset that have a relationship MOVED_TO or 
	         * MOVED_ELSEWHERE and concept status is 10.  
	         * ---------------------------------------------------------------------------
	         */
	        // Set allowed status: Not Current 
	        allowedStatus = notCurrentStatus;
	        
	        // Set Allowed Types 
			allowedTypes = termFactory.newIntSet();
			allowedTypes.add(MOVED_TO.localize().getNid());
			allowedTypes.add(MOVED_ELSEWHERE.localize().getNid());
			
			// Execute Query B
			Set<I_GetConceptData> refsetSpec_B_SourceRelTargets = refsetSpecConcept.getSourceRelTargets(
					allowedStatus, allowedTypes, refsetPositionSet, false, true); 

			// If no records are found for Query B... 
			if (refsetSpec_B_SourceRelTargets == null || refsetSpec_B_SourceRelTargets.size() == 0) {
	    		// Display a Debug message 
	    		JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
	    				"Query 'B' found no records to be refreshed for refset: \n" + 
	    				refsetSpecConcept.getInitialText(),
	    				"DEBUG Message",
	    				JOptionPane.INFORMATION_MESSAGE);
	    		// Cancel the task 
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_CANCELED);
	        } else {
		        //TODO Remove Debug Messages... 
		        System.out.println("QUERY B - getSourceRelTargets()");
		        System.out.println("==============================="); 
		        System.out.println("   NUMBER FOUND = " + refsetSpec_B_SourceRelTargets.size());
		        System.out.println("   INCLUDE:");
		        if (refsetSpec_B_SourceRelTargets.size() < 1000) {
			        for (I_GetConceptData concept : refsetSpec_B_SourceRelTargets) {
				        System.out.println("      " + concept.toString());
			        }	        	
		        }

		        // Add the results of the query to the List and the Map
		        for (I_GetConceptData oldConcept : refsetSpec_B_SourceRelTargets) {
		    	    
		        	// Remember the oldConcept is a list for future reference 
		        	changesList.add(oldConcept);
		    	    
		    	    // Search SNOMED for changes to this concept 
		    	    //TODO 
		        	I_GetConceptData newConcept = null;  
		        	
		    	    // Put the oldConcept and the newConcept into the changesMap 
		    	    changesMap.put(oldConcept, newConcept);  

		        }	        	

		        
//		        // Add the results of the query to the List
//		        for (I_GetConceptData concept : refsetSpec_B_SourceRelTargets) {
//	    			List<UUID> uuidList = new ArrayList<UUID>();
//	    			uuidList = (List<UUID>) concept.getUids();
//	        		uuidListOfLists.add(uuidList);
//		        }	        	

		        // Set task completion status 
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_COMPLETE);
	        }

			
			
	        /* ---------------------------------------------------------------------------
	         * (QUERY C) 
	         * Find concepts in the subset that have a concept status of 1 - "retired".  
	         * ---------------------------------------------------------------------------
	         */
	        // Allowed Statuses 
	        allowedStatus = termFactory.newIntSet();
	        allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
	        
	        // Allowed Types 
			allowedTypes = termFactory.newIntSet();
			
			// Execute Query C
			Set<I_GetConceptData> refsetSpec_C_SourceRelTargets = refsetSpecConcept.getSourceRelTargets(
					allowedStatus, allowedTypes, refsetPositionSet, false, true); 

			// If no records are found for Query C... 
			if (refsetSpec_C_SourceRelTargets == null || refsetSpec_C_SourceRelTargets.size() == 0) {
	    		
				// Display a Debug message 
	    		JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
	    				"Query 'C' found no records to be refreshed for refset: \n" + 
	    				refsetSpecConcept.getInitialText(),
	    				"DEBUG Message",
	    				JOptionPane.INFORMATION_MESSAGE);
	    		
	    		// Cancel the task 
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_CANCELED);
	        } else {
		        //TODO Remove Debug Messages... 
		        System.out.println("QUERY C - getSourceRelTargets()");
		        System.out.println("==============================="); 
		        System.out.println("   NUMBER FOUND = " + refsetSpec_C_SourceRelTargets.size());
		        System.out.println("   INCLUDE:");
		        if (refsetSpec_C_SourceRelTargets.size() < 1000) {
			        for (I_GetConceptData concept : refsetSpec_C_SourceRelTargets) {
				        System.out.println("      " + concept.toString());
			        }	        	
		        }

		        // Add the results of the query to the List and the Map
		        for (I_GetConceptData oldConcept : refsetSpec_C_SourceRelTargets) {
		    	    
		        	// Remember the oldConcept is a list for future reference 
		        	changesList.add(oldConcept);
		    	    
		    	    // Search SNOMED for changes to this concept 
		    	    //TODO 
		        	I_GetConceptData newConcept = null;  
		        	
		    	    // Put the oldConcept and the newConcept into the changesMap 
		    	    changesMap.put(oldConcept, newConcept);  

		        }	        	

//		        // Add the results of the query to the List
//		        for (I_GetConceptData concept : refsetSpec_C_SourceRelTargets) {
//	    			List<UUID> uuidList = new ArrayList<UUID>();
//	    			uuidList = (List<UUID>) concept.getUids();
//	        		uuidListOfLists.add(uuidList);
//		        }	        	

		        // Set task completion status 
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_COMPLETE);
	        }
	    	
	        
    		/* --------------------------------------------
    		 *  CREATE TEST MODE LIST 
    		 *  -------------------------------------------
    		 */ 	

			// If in test mode send a hard coded list of concepts 
			boolean inTestMode = true; 
    		
    		if (inTestMode) {
    			
        	    changesList = new ArrayList<I_GetConceptData>();
        	    changesMap = new HashMap<I_GetConceptData, I_GetConceptData>();

	     		// for testing purposes (until the queries above are working)... 
    			// simply populate the property with a list of valid concepts         	
	    		String uuidTempList[] = { 
	    				"ba1c7007-0c89-31d5-87e0-31d35d557e62", 
	    				"373d2be5-6c33-3607-baec-80f1b18e28e8", 
	    				"b6f7ab2f-7b18-385f-a1f7-905a1a5bb60f" };
	    		for (String uuidStr: uuidTempList){ 	
	    			UUID uuid = UUID.fromString(uuidStr);
	    			I_GetConceptData oldConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(uuid);

	    			// Remember the oldConcept is a list for future reference
	    			changesList.add(oldConcept);
	    			
	    			// Put the oldConcept and the newConcept into the changesMap
	    			I_GetConceptData newConcept = null; 
	    			changesMap.put(oldConcept, newConcept); 
	    			
	    		}
    			
    			
//        		// Reinitialize the list 
//    			List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
// 			
//	     		// for testing purposes (until the queries above are working)... 
//    			// simply populate the property with a list of valid UUIDs         	
//	    		String uuidTempList[] = { 
//	    				"ba1c7007-0c89-31d5-87e0-31d35d557e62", 
//	    				"373d2be5-6c33-3607-baec-80f1b18e28e8", 
//	    				"b6f7ab2f-7b18-385f-a1f7-905a1a5bb60f" };
//	    		for (String uuidStr: uuidTempList){ 	
//	    			List<UUID> uuidList = new ArrayList<UUID>();
//	    			UUID uuid = UUID.fromString(uuidStr);
//	    			uuidList.add(uuid);
//	        		uuidListOfLists.add(uuidList);
//	    		}
	    		// Display a Debug message 
	    		JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
	    				"RefreshRefsetSpecCompareTask.evaluate():" + 
	    				"Placed a list of test UUIDs in the key: " + getUuidListListPropName(),
	    				"DEBUG Message",
	    				JOptionPane.INFORMATION_MESSAGE);
	    		
	    
	    		RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_COMPLETE);
       		}

    		
        	/* -------------------------------------------------------------
        	 *  Store the list of differences in the uuidListListPropName
        	 *  ------------------------------------------------------------
        	 */
    		process.setProperty(this.uuidListListPropName, changesList);
    		process.setProperty(this.changeMapPropName, changesMap);


    		return getCondition();
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw new TaskFailedException(ex);
    	}

    }
   
    public void setCondition(Condition c) {
        condition = c;
    }

    public Condition getCondition() {
        return condition;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }
	public String getProfilePropName() {
		return profilePropName;
	}
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}
	public String getRefsetPositionSetPropName() {
		return refsetPositionSetPropName;
	}
	public void setRefsetPositionSetPropName(String refsetPositionSetPropName) {
		this.refsetPositionSetPropName = refsetPositionSetPropName;
	}
	public String getRefsetUuidPropName() {
		return refsetUuidPropName;
	}
	public void setRefsetUuidPropName(String refsetUuidPropName) {
		this.refsetUuidPropName = refsetUuidPropName;
	}
	public String getSnomedPositionSetPropName() {
		return snomedPositionSetPropName;
	}
	public void setSnomedPositionSetPropName(String snomedPositionSetPropName) {
		this.snomedPositionSetPropName = snomedPositionSetPropName;
	}
	public String getUuidListListPropName() {
		return uuidListListPropName;
	}
	public void setUuidListListPropName(String uuidListListPropName) {
		this.uuidListListPropName = uuidListListPropName;
	}
	public String getChangeMapPropName() {
		return changeMapPropName;
	}
	public void setChangeMapPropName(String changeMapPropName) {
		this.changeMapPropName = changeMapPropName;
	}

}
