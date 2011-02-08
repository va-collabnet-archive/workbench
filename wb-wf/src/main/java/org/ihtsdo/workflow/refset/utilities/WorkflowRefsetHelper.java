package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowRefsetHelper {
	private static HashMap<String, I_GetConceptData> modelers = null;
	   
	public static HashMap<String, I_GetConceptData> getModelers() throws TerminologyException, IOException {
		if (modelers == null)
			initializeModelers();
		
		return modelers;
	}
	
	public static I_GetConceptData getCurrentModeler() throws TerminologyException, IOException {
		return modelers.get(Terms.get().getActiveAceFrameConfig().getUsername());
	}

	public static String identifyFSN(I_GetConceptData con) throws TerminologyException, IOException {
    	Collection<I_DescriptionVersioned> c = (Collection<I_DescriptionVersioned>) con.getDescriptions();
    	
    	Iterator<I_DescriptionVersioned> itr = c.iterator();
    	I_TermFactory tf = Terms.get();
    	
   		while (itr.hasNext())
   		{
   	   		I_DescriptionVersioned<?> v = (I_DescriptionVersioned)itr.next();

	   		for (I_DescriptionTuple tuple : v.getTuples())
	   		{
	   			I_DescriptionTuple t = tuple;
	   			
	   			if (tuple.getTypeId() == tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid())
	   				return tuple.getText();
	   		}
   		}
   		
   		return "";
    }

	public static void initializeModelers() throws TerminologyException, IOException 
	{
		I_TermFactory tf = Terms.get();
		
    	modelers = new HashMap<String, I_GetConceptData>();

    	I_GetConceptData con = tf.getConcept(ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM.localize().getUids());
    	modelers.put("mvanber", con);
    	
    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.KIRSTEN_HAAKE.localize().getUids());
    	modelers.put("khaake", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.JALEH_MIZRA.localize().getUids());
    	modelers.put("jmirza", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.PENNY_LIVESAY.localize().getUids());
    	modelers.put("llivesa", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.MARY_GERARD.localize().getUids());
    	modelers.put("mgerard", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.MIKE_SMITH.localize().getUids());
    	modelers.put("msmith", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.localize().getUids());
    	modelers.put("phought", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.localize().getUids());
    	modelers.put("pbrottm", con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.KENT_SPACKMAN.localize().getUids());
    	modelers.put("spackman", con);
    }

	public static I_GetConceptData lookupModeler(String name) throws TerminologyException, IOException 
	{
		if (modelers == null)
			initializeModelers();

		if (!modelers.containsKey(name))
			return Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.getUids());
		else
			return modelers.get(name);			
    }

	public static I_GetConceptData getAction(String action) throws TerminologyException, IOException {
		I_TermFactory tf = Terms.get();
		
		if (action.equalsIgnoreCase("empty"))
			return tf.getConcept(WorkflowAuxiliary.Concept.EMPTY.getUids());
		else if (action.equalsIgnoreCase("accept"))
			return tf.getConcept(WorkflowAuxiliary.Concept.ACCEPT.getUids());
		else if (action.equalsIgnoreCase("retire"))
			return tf.getConcept(WorkflowAuxiliary.Concept.RETIRE.getUids());
		else if (action.equalsIgnoreCase("review"))
			return tf.getConcept(WorkflowAuxiliary.Concept.REVIEW.getUids());
		else if (action.equalsIgnoreCase("consensus"))
			return tf.getConcept(WorkflowAuxiliary.Concept.CONSENSUS.getUids());
		else if (action.equalsIgnoreCase("escalate"))
			return tf.getConcept(WorkflowAuxiliary.Concept.ESCALATE.getUids());
		else if (action.equalsIgnoreCase("work_up"))
			return tf.getConcept(WorkflowAuxiliary.Concept.WORK_UP.getUids());
		else if (action.equalsIgnoreCase("discuss"))
			return tf.getConcept(WorkflowAuxiliary.Concept.DISCUSS.getUids());
		else if (action.equalsIgnoreCase("empty") || action.equalsIgnoreCase("<empty>"))
			return tf.getConcept(WorkflowAuxiliary.Concept.EMPTY.getUids());
		else
			return null;
				
	}

	public static I_GetConceptData getState(String state) throws TerminologyException, IOException {
		I_TermFactory tf = Terms.get();
		
		if (state.equalsIgnoreCase("approved"))
			return tf.getConcept(WorkflowAuxiliary.Concept.APPROVED.getUids());
		else if (state.equalsIgnoreCase("changed"))
			return tf.getConcept(WorkflowAuxiliary.Concept.CHANGED.getUids());
		else if (state.equalsIgnoreCase("changed_in_batch"))
			return tf.getConcept(WorkflowAuxiliary.Concept.CHANGED_IN_BATCH.getUids());
		else if (state.equalsIgnoreCase("consensus"))
			return tf.getConcept(WorkflowAuxiliary.Concept.CONSENSUS.getUids());
		else if (state.equalsIgnoreCase("discuss"))
			return tf.getConcept(WorkflowAuxiliary.Concept.DISCUSSED.getUids());
		else if (state.equalsIgnoreCase("done"))
			return tf.getConcept(WorkflowAuxiliary.Concept.DONE.getUids());
		// Empty State nec?
		//else if (state.equalsIgnoreCase("empty") || state.equalsIgnoreCase("<empty>"))
		//	return tf.getConcept(WorkflowAuxiliary.Concept.EMPTY.getUids());
		else if (state.equalsIgnoreCase("escalated"))
			return tf.getConcept(WorkflowAuxiliary.Concept.ESCALATED.getUids());
		else if ((state.equalsIgnoreCase("first_review")) || (state.equalsIgnoreCase("first review"))) 
			return tf.getConcept(WorkflowAuxiliary.Concept.FIRST_REVIEW.getUids());
		else if (state.equalsIgnoreCase("review_chief_term") || state.equalsIgnoreCase("review chief term"))
			return tf.getConcept(WorkflowAuxiliary.Concept.REVIEW_CHIEF_TERM.getUids());
		else if (state.equalsIgnoreCase("revised"))
			return tf.getConcept(WorkflowAuxiliary.Concept.REVISED.getUids());
		else if ((state.equalsIgnoreCase("second_review")) || (state.equalsIgnoreCase("second review"))) 
			return tf.getConcept(WorkflowAuxiliary.Concept.SECOND_REVIEW.getUids());
		else if (state.equalsIgnoreCase("to_retire") || state.equalsIgnoreCase("toRetire"))
			return tf.getConcept(WorkflowAuxiliary.Concept.TO_RETIRE.getUids());
		else if (state.equalsIgnoreCase("new"))
			return tf.getConcept(WorkflowAuxiliary.Concept.NEW.getUids());
		else if (state.equalsIgnoreCase("work_up"))
			return tf.getConcept(WorkflowAuxiliary.Concept.WORKED_UP.getUids());
		else
			return null;
	}


    public static I_GetConceptData getModelerCategory(String category) throws TerminologyException, IOException {
    	I_TermFactory tf = Terms.get();
    	
    	if (category.equalsIgnoreCase("a"))
   			return tf.getConcept(WorkflowAuxiliary.Concept.ROLE_A.getUids());
    	else if (category.length() == 2 && category.charAt(0) == 'b' && category.charAt(1) == '+')
   			return tf.getConcept(WorkflowAuxiliary.Concept.ROLE_BPLUS.getUids());
		else if (category.equalsIgnoreCase("b"))
			return tf.getConcept(WorkflowAuxiliary.Concept.ROLE_B.getUids());
    	else if (category.equalsIgnoreCase("c"))
   			return tf.getConcept(WorkflowAuxiliary.Concept.ROLE_C.getUids());
    	else if (category.equalsIgnoreCase("d"))
   			return tf.getConcept(WorkflowAuxiliary.Concept.ROLE_D.getUids());
    	else if (category.equalsIgnoreCase("Any"))
   			return tf.getConcept(WorkflowAuxiliary.Concept.ROLE_ANY.getUids());
		else
			return null;
    }
    
    public static WorkflowHistoryJavaBean fillOutWorkflowHistoryJavaBean(I_ExtendByRefPartStr props) throws NumberFormatException, TerminologyException, IOException
    {
    	WorkflowHistoryJavaBean bean = new WorkflowHistoryJavaBean();
    	WorkflowHistoryRefset refset = new WorkflowHistoryRefset();

    	String xmlVals = props.getStringValue();
    	
    	bean.setAction(refset.getAction(xmlVals));
    	bean.setConceptId(refset.getConceptId(xmlVals));
    	bean.setFSN(refset.getFSN(xmlVals));
    	bean.setModeler(refset.getModeler(xmlVals));
    	bean.setPath(refset.getPath(xmlVals));
    	bean.setState(refset.getState(xmlVals));
    	bean.setTimeStamp(refset.getTimeStamp(xmlVals));
    	bean.setUseCase(refset.getUseCase(xmlVals));
    	bean.setWorkflowId(refset.getWorkflowId(xmlVals));
    	
    	return bean;
    }
}