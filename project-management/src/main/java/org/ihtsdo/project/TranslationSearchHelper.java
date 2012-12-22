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
package org.ihtsdo.project;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class TranslationSearchHelper.
 */
public class TranslationSearchHelper {

    /**
     * Gets the list items for project.
     *
     * @param project the project
     * @return the list items for project
     * @throws Exception the exception
     */
    public static List<ListItemBean> getListItemsForProject(TranslationProject project) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        List<ListItemBean> result = new ArrayList<ListItemBean>();

        I_GetConceptData fsnType = termFactory.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());

        I_GetConceptData preferredType = termFactory.getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());

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
                        if (sourceCd.getTypeId() == fsnType.getNid()) {
                            listItem.setSourceFsn(sourceCd);
                        } else if (sourceCd.getAcceptabilityId() == preferredType.getNid() && sourceCd.getTypeId() == preferredType.getNid()) {
                            listItem.setSourcePrefered(sourceCd);
                        }
                    }
                }
                listItem.setTargetPrefered(targetPref);
                listItem.setStatus(transStatus);
                if (listItem.getSourceFsn() != null || listItem.getSourcePrefered() != null || listItem.getTargetPrefered() != null) {
                    result.add(listItem);
                }
            }

        }
        return result;
    }
}
