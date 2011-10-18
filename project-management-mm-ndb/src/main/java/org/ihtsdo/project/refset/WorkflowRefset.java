package org.ihtsdo.project.refset;


import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.RefsetAuxiliary;

public abstract class WorkflowRefset extends Refset {

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
		try {
			I_GetConceptData promotionRel = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
			I_GetConceptData refsetConcept = getRefsetConcept();
			if (refsetConcept == null) {
				return null;
			}
	
			return new PromotionRefset(getLatestSourceRelationshipTarget(refsetConcept, promotionRel, config));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public WorkflowRefset() {
		super();
	}

}