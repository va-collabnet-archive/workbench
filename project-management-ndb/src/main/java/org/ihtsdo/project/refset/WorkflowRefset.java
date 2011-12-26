package org.ihtsdo.project.refset;



import java.util.HashMap;
import java.util.Map;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.RefsetAuxiliary;

public abstract class WorkflowRefset extends Refset {
	
	private static Map<Integer, PromotionRefset> promRefsetCache;
	
	

	public CommentsRefset getCommentsRefset(I_ConfigAceFrame config) {
		try {
			I_GetConceptData commentsRel = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData refsetConcept = getRefsetConcept();
			if (refsetConcept == null) {
				return null;
			}
	
			return new CommentsRefset(getLatestSourceRelationshipTarget(refsetConcept, commentsRel, config));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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
	
			PromotionRefset promRef = new PromotionRefset(getLatestSourceRelationshipTarget(refsetConcept, promotionRel, config));
			WorkflowRefset.promRefsetCache.put(refsetConcept.getNid(), promRef);
			
			return promRef;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public WorkflowRefset() {
		super();
	}

}