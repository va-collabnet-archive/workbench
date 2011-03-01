package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;



/* 
* @author Jesse Efron
* 
*/
public class EditorCategoryUtilities {
	private static HashMap<String, I_GetConceptData> modelers = null;
	   
	private static void initializeModelers() throws TerminologyException, IOException 
	{
		I_TermFactory tf = Terms.get();
		
    	I_GetConceptData con = tf.getConcept(ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM.localize().getUids());
    	modelers.put(getSynonym(con), con);
    
    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.KIRSTEN_HAAKE.localize().getUids());
    	modelers.put(getSynonym(con), con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.JALEH_MIZRA.localize().getUids());
    	modelers.put(getSynonym(con), con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.PENNY_LIVESAY.localize().getUids());
    	modelers.put(getSynonym(con), con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.MARY_GERARD.localize().getUids());
    	modelers.put(getSynonym(con), con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.MIKE_SMITH.localize().getUids());
    	modelers.put(getSynonym(con), con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.localize().getUids());
    	modelers.put(getSynonym(con), con);

    	con = tf.getConcept(ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.localize().getUids());
    	modelers.put(getSynonym(con), con);
    }

	public static HashMap<String, I_GetConceptData> getModelers(I_TermFactory tf) throws TerminologyException, IOException {
		if (modelers == null)
			initializeModelers();
		
		return modelers;
	}

    private static String getSynonym(I_GetConceptData con) throws TerminologyException, IOException {
    	Collection<I_DescriptionVersioned> c = (Collection<I_DescriptionVersioned>) con.getDescriptions();
    	
    	I_TermFactory tf = Terms.get();
   		
    	Iterator<I_DescriptionVersioned> itr = c.iterator();
    	
    	while (itr.hasNext())
   		{
   	   		I_DescriptionVersioned<?> v = (I_DescriptionVersioned)itr.next();

	   		for (I_DescriptionTuple tuple : v.getTuples())
	   		{
	   			I_DescriptionTuple t = tuple;
	   			int a = t.getTypeId();
	   			int b = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid();
	   			int d = tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNid();
	   			int e = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNid();
	   			if (tuple.getTypeId() == tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNid())
	   				return tuple.getText();
	   		}
   		}
   		
   		return "";
    }
}
