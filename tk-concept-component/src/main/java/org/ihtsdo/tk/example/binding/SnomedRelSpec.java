package org.ihtsdo.tk.example.binding;

import org.ihtsdo.tk.spec.RelSpec;

/**
 * @deprecated  see package org.ihtsdo.tk.binding.snomed
 *
 */
public class SnomedRelSpec {
	public static RelSpec FINDING_SITE = 
		new RelSpec(Taxonomies.SNOMED, 
					SnomedRelType.FINDING_SITE, 
					Snomed.BODY_STRUCTURE);

}
