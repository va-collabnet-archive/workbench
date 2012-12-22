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
package org.ihtsdo.project.refset;

import java.util.HashMap;
import java.util.Map;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * The Class WorkflowRefset.
 */
public abstract class WorkflowRefset extends Refset {

    /**
     * The prom refset cache.
     */
    private static Map<Integer, PromotionRefset> promRefsetCache;

    /**
     * Gets the comments refset.
     *
     * @param config the config
     * @return the comments refset
     */
    public CommentsRefset getCommentsRefset(I_ConfigAceFrame config) {
        try {
            I_GetConceptData commentsRel = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            I_GetConceptData refsetConcept = getRefsetConcept();
            if (refsetConcept == null) {
                return null;
            }

            return new CommentsRefset(getLatestSourceRelationshipTarget(refsetConcept, commentsRel, config));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Gets the promotion refset.
     *
     * @param config the config
     * @return the promotion refset
     */
    public PromotionRefset getPromotionRefset(I_ConfigAceFrame config) {
        if (WorkflowRefset.promRefsetCache == null) {
            WorkflowRefset.promRefsetCache = new HashMap<Integer, PromotionRefset>();
        }
        I_GetConceptData refsetConcept = getRefsetConcept();
        if (refsetConcept != null && WorkflowRefset.promRefsetCache.get(refsetConcept.getNid()) != null) {
            return WorkflowRefset.promRefsetCache.get(getRefsetConcept().getNid());
        }

        try {
            I_GetConceptData promotionRel = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            if (refsetConcept == null) {
                return null;
            }

            I_GetConceptData target = getLatestSourceRelationshipTarget(refsetConcept, promotionRel, config);
            if (target == null) {
                throw new Exception("Can't find promotion refset");
            }
            PromotionRefset promRef = new PromotionRefset(target);

            WorkflowRefset.promRefsetCache.put(refsetConcept.getNid(), promRef);

            return promRef;
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Instantiates a new workflow refset.
     */
    public WorkflowRefset() {
        super();
    }
}