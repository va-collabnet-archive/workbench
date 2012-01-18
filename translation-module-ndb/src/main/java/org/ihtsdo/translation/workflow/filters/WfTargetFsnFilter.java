package org.ihtsdo.translation.workflow.filters;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.LanguageUtil;

public class WfTargetFsnFilter implements WfSearchFilterBI{
	private final String TYPE = "WF_TARGET_PREFERRED_FILTER";
	private String regex;

	public WfTargetFsnFilter(String regex) {
		super();
		this.regex = regex;
	}

	public void setState(String regex) {
		this.regex = regex;
	}

	public String getState() {
		return regex;
	}

	@Override
	public boolean filter(WfInstance wfInstance) {
		String targetFSN = "";
		try {
			I_GetConceptData langRefset = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			I_TerminologyProject projectConcept = TerminologyProjectDAO.getProjectForWorklist(wfInstance.getWorkList(), config );
			TranslationProject translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
			langRefset = translationProject.getTargetLanguageRefset();
			descriptions = LanguageUtil.getContextualizedDescriptions(Terms.get().uuidToNative(wfInstance.getComponentId()), langRefset.getConceptNid(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
					if (description.getTypeId() == Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid()).getConceptNid()) {
						targetFSN  = description.getText();
					} 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return targetFSN.contains(regex);
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
}
