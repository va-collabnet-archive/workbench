package org.ihtsdo.translation.ui.translation;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.translation.FSNGenerationException;
import org.ihtsdo.translation.LanguageUtil;

public class TranslationHelper implements TranslationHelperBI {

	@Override
	public boolean addFsn(TranslationProject project, I_GetConceptData concept)
			throws FSNGenerationException, Exception {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		
		I_GetConceptData targetLangRefset = project.getTargetLanguageRefset();
		
		LanguageMembershipRefset targetRefset=new LanguageMembershipRefset(targetLangRefset, config);
		
		List<I_GetConceptData> sourceLangRefsets = project.getSourceLanguageRefsets();
		
		LanguageMembershipRefset sourceRefset=new LanguageMembershipRefset(sourceLangRefsets.iterator().next(), config);
		
		I_ContextualizeDescription fsn = LanguageUtil.generateFSN(concept, sourceRefset, targetRefset, project, config);
		
		return (fsn!=null);
	}

	@Override
	public boolean contextualizeThisDescription(
			LanguageMembershipRefset refset,
			ContextualizedDescription description, TranslationProject project,
			I_GetConceptData acceptability) throws FSNGenerationException,
			Exception {

		I_ContextualizeDescription contextualizedDescription = description.contextualizeThisDescription(refset.getRefsetId(), acceptability.getConceptNid());
		
		if (contextualizedDescription!=null){
			addFsn(project,description.getConcept());
			return true;
		}
		
		return false;
	}

	@Override
	public List<ContextualizedDescription> getSourceTerms(
			I_GetConceptData concept, TranslationProject project) throws Exception {
		
		List<ContextualizedDescription> contextDescriptions=new ArrayList<ContextualizedDescription>();
		
		for (I_GetConceptData langRefset : project.getSourceLanguageRefsets()) {
			List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);

			for (ContextualizedDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
					contextDescriptions.add(description);
				}
			}
		}
		return contextDescriptions ;			
	}

	@Override
	public List<ContextualizedDescription> getTargetTerms(
			I_GetConceptData concept, TranslationProject project) throws Exception {

		List<ContextualizedDescription> contextDescriptions=new ArrayList<ContextualizedDescription>();
		
		I_GetConceptData langRefset = project.getTargetLanguageRefset();
		
		List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);

		for (ContextualizedDescription description : descriptions) {
			if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
				contextDescriptions.add(description);
			}
		}
		return contextDescriptions;
	}

	@Override
	public boolean save(TranslationProject project,
			ContextualizedDescription description)
			throws FSNGenerationException, Exception {

		boolean result = description.persistChanges();

		if (result){
			addFsn(project,description.getConcept());
		}
		return result;
	}

}
