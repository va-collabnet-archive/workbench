/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.translation.workflow.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfSearchFilterBI;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.ihtsdo.translation.LanguageUtil;

/**
 * The Class WfTargetPreferredFilter.
 */
public class WfTargetPreferredFilter implements WfSearchFilterBI {

	/** The TYPE. */
	private final String TYPE = "WF_TARGET_PREFERRED_FILTER";

	/** The regex. */
	private String regex;

	private static I_GetConceptData synonim;
	private static I_GetConceptData preferred;
	static {
		try {
			synonim = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Instantiates a new wf target preferred filter.
	 * 
	 * @param regex
	 *            the regex
	 */
	public WfTargetPreferredFilter(String regex) {
		super();
		this.regex = regex;
	}

	/**
	 * Sets the state.
	 * 
	 * @param regex
	 *            the new state
	 */
	public void setState(String regex) {
		this.regex = regex;
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public String getState() {
		return regex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo
	 * .project.workflow.model.WfInstance)
	 */
	@Override
	public boolean filter(WfInstance wfInstance) {
		String targetFsn = "";
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			I_GetConceptData langRefset = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			I_TerminologyProject projectConcept = TerminologyProjectDAO.getProjectForWorklist(wfInstance.getWorkList(), config);
			TranslationProject translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
			langRefset = translationProject.getTargetLanguageRefset();
			descriptions = LanguageUtil.getContextualizedDescriptions(Terms.get().uuidToNative(wfInstance.getComponentId()), langRefset.getConceptNid(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
					if (description.getAcceptabilityId() == preferred.getConceptNid() && description.getTypeId() == synonim.getConceptNid()
							&& LanguageUtil.isActive(description.getDescriptionStatusId()) && LanguageUtil.isActive(description.getExtensionStatusId())) {
						targetFsn = description.getText();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return targetFsn.toLowerCase().contains(regex.toLowerCase());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

}
