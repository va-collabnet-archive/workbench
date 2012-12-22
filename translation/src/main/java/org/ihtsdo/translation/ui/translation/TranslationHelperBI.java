package org.ihtsdo.translation.ui.translation;

import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.translation.FSNGenerationException;

public interface TranslationHelperBI {

	List<ContextualizedDescription> getSourceTerms(I_GetConceptData concept, TranslationProject project) throws Exception;

	List<ContextualizedDescription> getTargetTerms(I_GetConceptData concept, TranslationProject project) throws Exception;

	boolean addFsn(TranslationProject project, I_GetConceptData concept) throws FSNGenerationException, Exception;

	boolean save(TranslationProject project, ContextualizedDescription description) throws FSNGenerationException, Exception;

	boolean contextualizeThisDescription(LanguageMembershipRefset refset, ContextualizedDescription description, TranslationProject project, I_GetConceptData acceptability) throws FSNGenerationException, Exception;

}
