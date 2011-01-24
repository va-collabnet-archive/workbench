package org.ihtsdo.ace.task.contradiction;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JList;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
 
public class ContradictionReporter {
	private Writer outputFile = null;
	private ViewCoordinate ViewCoordinate = null;
	
	public ContradictionReporter(String outputFileLocation) {
		try {
			outputFile = new OutputStreamWriter(new FileOutputStream(outputFileLocation));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ContradictionReporter() {
	}
	
	public void setCoordinate(ViewCoordinate c) {
		ViewCoordinate = c;
	}
	
	public void identifyInConceptListPanel(Set<Integer> nids) {
		try {
			JList conceptList = Terms.get().getActiveAceFrameConfig().getBatchConceptList();
	        I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
	        model.clear();

	        TreeSet<I_GetConceptData> sortedConcepts = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());
	        for (Integer nid : nids)
	        {
	        	I_GetConceptData concept = Terms.get().getConcept(nid);
	        	sortedConcepts.add(concept);
	        	
	        }
	        
	        for (I_GetConceptData con : sortedConcepts)
	        	model.addElement(con);

        	Terms.get().getActiveAceFrameConfig().showListView();
        	Terms.get().getActiveAceFrameConfig().setShowProcessBuilder(false);
        	
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void identifyAsText(Set<Integer> nids) {
		try {
			for (Integer nid : nids)
			{
				I_GetConceptData con = Terms.get().getConcept(nid);
				
				identifyConflictingConceptAttributes(con);
				identifyConflictingDescriptions(con);
				identifyConflictingSourceRelationships(con);
				identifyConflictingDestinationRelationships(con);
				
				outputFile.flush();
				outputFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void identifyConflictingConceptAttributes(I_GetConceptData con) throws IOException {
		// not with ViewCoordinate
		Collection<? extends ConAttrVersionBI> conAttrVersions = con.getConAttrs().getVersions();
		outputFile.write("Handling Concept Attributes");

		int counter = 1;
		for (ConAttrVersionBI version : conAttrVersions)
		{
			String s = "\nCount: " + counter++;
			outputFile.write(s);
			
			s = "\tWith Values: " + version.toUserString();
			outputFile.write(s);
		}

		outputFile.flush();
	}

	private void identifyConflictingDescriptions(I_GetConceptData con) throws IOException {
		Collection<? extends DescriptionChronicleBI> descriptions = con.getDescs();
		outputFile.write("\n\nHandling Descriptions");

		int counter = 1;
		Iterator<? extends DescriptionChronicleBI> itr = descriptions.iterator();

		while (itr.hasNext())
		{
			DescriptionChronicleBI desc = (DescriptionChronicleBI) itr.next();
			for (DescriptionVersionBI d : desc.getVersions(ViewCoordinate))
			{
				String s = "\nCount: " + counter++;
				outputFile.write(s);
				
				s = "\tWith Values: " + d.toUserString();
				outputFile.write(s);
			}
		}
		
		outputFile.flush();
	}

	private void identifyConflictingSourceRelationships(I_GetConceptData con) throws IOException {
		Collection<? extends RelationshipChronicleBI> relationships = con.getSourceRels();
		outputFile.write("\n\nHandling Source Relationships");


		int counter = 1;
		
		Iterator<? extends RelationshipChronicleBI> itr = relationships.iterator();

		while (itr.hasNext())
		{
			RelationshipChronicleBI desc = (RelationshipChronicleBI) itr.next();
			for (RelationshipVersionBI r : desc.getVersions(ViewCoordinate))
			{
				String s = "\nCount: " + counter++;
				outputFile.write(s);
				
				s = "\tWith Values: " + r.toUserString();
				outputFile.write(s);
			}
		}

		outputFile.flush();
	}

	private void identifyConflictingDestinationRelationships(I_GetConceptData con) throws IOException {
		Collection<? extends RelationshipChronicleBI> relationships = con.getSourceRels();
		outputFile.write("\n\nHandling Destination Relationships");


		int counter = 1;
		
		Iterator<? extends RelationshipChronicleBI> itr = relationships.iterator();

		while (itr.hasNext())
		{
			RelationshipChronicleBI desc = (RelationshipChronicleBI) itr.next();
			for (RelationshipVersionBI r : desc.getVersions(ViewCoordinate))
			{
				String s = "\nCount: " + counter++;
				outputFile.write(s);
				
				s = "\tWith Values: " + r.toUserString();
				outputFile.write(s);
			}
		}

		outputFile.flush();
	}
	
}
