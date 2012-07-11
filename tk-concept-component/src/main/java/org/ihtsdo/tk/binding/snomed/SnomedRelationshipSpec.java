package org.ihtsdo.tk.binding.snomed;

import org.ihtsdo.tk.spec.RelationshipSpec;

public class SnomedRelationshipSpec {
	public static RelationshipSpec FINDING_SITE = 
		new RelationshipSpec(Taxonomies.SNOMED, 
					SnomedRelationshipType.FINDING_SITE, 
					Snomed.BODY_STRUCTURE);

}
