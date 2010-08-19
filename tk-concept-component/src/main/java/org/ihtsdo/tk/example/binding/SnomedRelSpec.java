package org.ihtsdo.tk.example.binding;

import org.ihtsdo.tk.spec.RelSpec;

public class SnomedRelSpec {
	public static RelSpec FINDING_SITE = 
		new RelSpec(Taxonomies.SNOMED, 
					SnomedRelType.FINDING_SITE, 
					Snomed.BODY_STRUCTURE);

}
