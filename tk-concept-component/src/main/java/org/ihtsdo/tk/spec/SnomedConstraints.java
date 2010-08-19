package org.ihtsdo.tk.spec;

import org.ihtsdo.tk.api.constraint.RelConstraintOutgoing;

public class SnomedConstraints {
	public static RelConstraintOutgoing FINDING_SITE_OUT = 
		SnomedRelSpec.FINDING_SITE.getOriginatingRelConstraint();
	
	public static RelConstraintOutgoing FINDING_SITE_IN = 
		SnomedRelSpec.FINDING_SITE.getOriginatingRelConstraint();

}
