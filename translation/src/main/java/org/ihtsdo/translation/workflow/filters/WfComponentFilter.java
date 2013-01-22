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
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.translation.LanguageUtil;

/**
 * The Class WfComponentFilter.
 */
public class WfComponentFilter implements WfFilterBI {

	/** The wf instance text filter. */
	private String wfInstanceTextFilter;
	/** The preferred. */
	private I_GetConceptData preferred;

	/** The synonym. */
	private I_GetConceptData synonym;
	/** The TYPE. */
	private final String TYPE = "WF_COMPONENT_FILTER";

	/**
	 * Instantiates a new wf component filter.
	 * 
	 * @param wfInstanceTextFilter
	 *            the wf instance text filter
	 */
	public WfComponentFilter(String wfInstanceTextFilter) {
		super();
		try {
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonym = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.wfInstanceTextFilter = wfInstanceTextFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo
	 * .project.workflow.model.WfInstance)
	 */
	@Override
	public boolean evaluateInstance(WfProcessInstanceBI instance) {
		try {

			List<I_GetConceptData> langRefset = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			WorkList wl = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(instance.getWorkList().getUuid()), config);
			I_TerminologyProject projectConcept = TerminologyProjectDAO.getProjectForWorklist(wl, config);
			TranslationProject translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
			langRefset = translationProject.getSourceLanguageRefsets();
			descriptions = LanguageUtil.getContextualizedDescriptions(Terms.get().getConcept(instance.getComponentPrimUuid()).getConceptNid(), langRefset
					.get(0).getConceptNid(), true);
			String sourcePreferred = "";
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.get(0).getConceptNid()) {
					if (description.getAcceptabilityId() == preferred.getConceptNid() && description.getTypeId() == synonym.getConceptNid()) {
						sourcePreferred = description.getText();
					}
				}
			}

			return sourcePreferred.toLowerCase().contains(wfInstanceTextFilter.toLowerCase());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sets the wf instance text filter.
	 * 
	 * @param wfInstanceTextFilter
	 *            the new wf instance text filter
	 */
	public void setWfInstanceTextFilter(String wfInstanceTextFilter) {
		this.wfInstanceTextFilter = wfInstanceTextFilter;
	}

	/**
	 * Gets the wf instance text filter.
	 * 
	 * @return the wf instance text filter
	 */
	public String getWfInstanceTextFilter() {
		return wfInstanceTextFilter;
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
