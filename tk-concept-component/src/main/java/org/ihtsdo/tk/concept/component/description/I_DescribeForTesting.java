package org.ihtsdo.tk.concept.component.description;

import org.ihtsdo.tk.I_AddCommonTestingProps;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;

public interface I_DescribeForTesting extends I_DescribeExternally,
		I_AddCommonTestingProps {
	
	public Boolean isSpellVariantCandidate();
	public void setSpellVariantCandidate(Boolean spellVariantCandidate);
	
	public Boolean isIcsCandidate();
	public void setIcsCandidate(Boolean icsCandidate);
	
	public Boolean isSpellCheckingRequired();
	public void setSpellCheckingRequired(Boolean spellCheckingRequired);

}
