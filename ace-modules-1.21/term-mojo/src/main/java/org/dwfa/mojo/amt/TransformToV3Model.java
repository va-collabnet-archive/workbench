package org.dwfa.mojo.amt;

import org.dwfa.ace.api.I_GetConceptData;

public class TransformToV3Model {
	public void transformConcept(I_GetConceptData conceptData) {
		for (TransformStages transformStage : TransformStages.values()) {
			transformStage.getModelTransformer().transform(conceptData);
		}
	}
}
