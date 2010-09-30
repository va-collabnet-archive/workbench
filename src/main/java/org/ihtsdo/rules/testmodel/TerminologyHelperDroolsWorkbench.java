package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;

public class TerminologyHelperDroolsWorkbench extends TerminologyHelperDrools {
	
	public boolean isMemberOf(String conceptUUID, String refsetUUID) {
		boolean result = false;
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			result = RulesLibrary.isIncludedInRefsetSpec(tf.getConcept(UUID.fromString(refsetUUID)), 
					tf.getConcept(UUID.fromString(conceptUUID)), config);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isParentOf(UUID parent, UUID subtype) throws TerminologyException, IOException {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_GetConceptData wbParent = Terms.get().getConcept(parent);
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		return wbParent.isParentOf(wbSubtype);
	}

	public boolean isParentOfOrEqualTo(UUID parent, UUID subtype) throws TerminologyException, IOException {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_GetConceptData wbParent = Terms.get().getConcept(parent);
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		return wbParent.isParentOfOrEqualTo(wbSubtype);
	}

	public boolean isParentOf(String parents, UUID subtype) throws Exception {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		boolean result = false;
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		String[] parentsStrings = parents.trim().split(",");
		for (String parentString : parentsStrings) {
			I_GetConceptData wbParent = Terms.get().getConcept(UUID.fromString(parentString.trim()));
			if (wbParent.isParentOf(wbSubtype)) result = true;
		}
		return result;
	}

	public boolean isParentOfOrEqualTo(String parents, UUID subtype)
	throws Exception {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		boolean result = false;
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		String[] parentsStrings = parents.trim().split(",");
		for (String parentString : parentsStrings) {
			I_GetConceptData wbParent = Terms.get().getConcept(UUID.fromString(parentString.trim()));
			if (wbParent.isParentOfOrEqualTo(wbSubtype)) result = true;
		}
		return result;
	}

	public boolean isFsnTextNotUnique(String fsn, String conceptUuid) throws Exception{
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			I_GetConceptData fsnType = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			SearchResult results = tf.doLuceneSearch(fsn);
			TopDocs topDocs = results.topDocs;
			ScoreDoc[] docs = topDocs.scoreDocs;
			for (int i = 0 ; i < docs.length  ; i++) {
				try{
					Document doc = results.searcher.doc(i);
					int cnid = Integer.parseInt(doc.get("cnid"));
					int dnid = Integer.parseInt(doc.get("dnid"));
					I_DescriptionVersioned potential_fsn = Terms.get().getDescription(dnid, cnid);
					if (potential_fsn != null) {
						
						DescriptionVersionBI description = (DescriptionVersionBI) 
						Ts.get().getComponentVersion(Terms.get().getActiveAceFrameConfig().getCoordinate(), dnid);
						
						if (description.getTypeNid() == fsnType.getConceptNid()
								&& description.getText().equals(fsn)
								&& description.getLang().equals(potential_fsn.getLang())) {
							result = true;
						}
					}
				}catch(Exception e){
					//Do Nothing
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return result;
	}

}
