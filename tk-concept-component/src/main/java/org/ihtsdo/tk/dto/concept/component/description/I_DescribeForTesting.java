/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.dto.concept.component.description;

import org.ihtsdo.tk.I_AddCommonTestingProps;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;

// TODO: Auto-generated Javadoc
/**
 * The Interface I_DescribeForTesting.
 */
public interface I_DescribeForTesting extends I_DescribeExternally,
		I_AddCommonTestingProps {
	
	/**
	 * Is spell variant candidate.
	 *
	 * @return the boolean
	 */
	public Boolean isSpellVariantCandidate();
	
	/**
	 * Sets the spell variant candidate.
	 *
	 * @param spellVariantCandidate the new spell variant candidate
	 */
	public void setSpellVariantCandidate(Boolean spellVariantCandidate);
	
	/**
	 * Is ics candidate.
	 *
	 * @return the boolean
	 */
	public Boolean isIcsCandidate();
	
	/**
	 * Sets the ics candidate.
	 *
	 * @param icsCandidate the new ics candidate
	 */
	public void setIcsCandidate(Boolean icsCandidate);
	
	/**
	 * Is spell checking required.
	 *
	 * @return the boolean
	 */
	public Boolean isSpellCheckingRequired();
	
	/**
	 * Sets the spell checking required.
	 *
	 * @param spellCheckingRequired the new spell checking required
	 */
	public void setSpellCheckingRequired(Boolean spellCheckingRequired);

}
