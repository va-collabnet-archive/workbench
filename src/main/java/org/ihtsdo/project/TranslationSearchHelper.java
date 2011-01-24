package org.ihtsdo.project;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.refset.LanguageMembershipRefset;

public class TranslationSearchHelper {

	public static List<ListItemBean> getListItemsForProject(TranslationProject project) throws Exception {
		I_TermFactory termFactory = Terms.get();
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		
		List<ListItemBean> result = new ArrayList<ListItemBean>();

		I_GetConceptData fsnType = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

		I_GetConceptData preferredType = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

		JList conceptList = config.getBatchConceptList();
		I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();

		I_GetConceptData targetLanguage = TerminologyProjectDAO.getTargetLanguageRefsetForProject(project, config);
		LanguageMembershipRefset targetLangRefset = new LanguageMembershipRefset(targetLanguage, config);

		List<I_GetConceptData> sourceLanguages = TerminologyProjectDAO.getSourceLanguageRefsetsForProject(project, config);

		
		
		for (int index = 0; index < model.getSize(); index++) {
			I_GetConceptData concept = model.getElementAt(index);
			List<ContextualizedDescription> targetConstDescriptions = ContextualizedDescription.getContextualizedDescriptions(concept.getNid(), targetLangRefset.getRefsetId(), true);
			

			I_GetConceptData transStatus = targetLangRefset.getPromotionRefset(config).getPromotionStatus(concept.getConceptNid(), config);
			
			ContextualizedDescription targetPref = null;
			for (ContextualizedDescription targetCd : targetConstDescriptions) {
				if (targetCd.getLanguageExtension() != null) {
					if (targetCd.getAcceptabilityId() == preferredType.getNid() && targetCd.getTypeId() == preferredType.getNid()) {
						targetPref = targetCd;
					}
				}
			}
			
			for (I_GetConceptData sourceLang : sourceLanguages) {
				LanguageMembershipRefset sourceLangRefset = new LanguageMembershipRefset(sourceLang, config);

				List<ContextualizedDescription> sourceContDescriptions = ContextualizedDescription.getContextualizedDescriptions(concept.getNid(), sourceLangRefset.getRefsetId(), true);
				ListItemBean listItem = new ListItemBean();
				for (ContextualizedDescription sourceCd : sourceContDescriptions) {
					if (sourceCd.getLanguageExtension() != null) {
						if (sourceCd.getTypeId() == fsnType.getNid() && sourceCd.getAcceptabilityId() == preferredType.getNid()) {
							listItem.setSourceFsn(sourceCd);
						} else if (sourceCd.getAcceptabilityId() == preferredType.getNid() && sourceCd.getTypeId() == preferredType.getNid()) {
							listItem.setSourcePrefered(sourceCd);
						}
					}
				}
				listItem.setTargetPrefered(targetPref);
				listItem.setStatus(transStatus);
				if(listItem.getSourceFsn() != null || listItem.getSourcePrefered() != null || listItem.getTargetPrefered() != null){
					result.add(listItem);
				}
			}

		}
		return result;
	}


}

