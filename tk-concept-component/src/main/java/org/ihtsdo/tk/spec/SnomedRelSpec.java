package org.ihtsdo.tk.spec;

public class SnomedRelSpec {
	public static RelSpec FINDING_SITE = 
		new RelSpec(Taxonomies.SNOMED, 
					SnomedRelType.FINDING_SITE, 
					Snomed.BODY_STRUCTURE);

}
