/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.dto.concept.component.description;

import org.ihtsdo.tk.I_AddCommonTestingProps;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;

/**
 * The Interface I_DescribeForTesting contains methods for interacting with
 * description test properties.
 */
public interface I_DescribeForTesting extends I_DescribeExternally,
        I_AddCommonTestingProps {

    /**
     * Tests if the description is set as having spelling variants.
     *
     * @return <code>true</code> if the description has spelling variants
     */
    public Boolean isSpellVariantCandidate();

    /**
     * Sets the description as having spelling variants.
     *
     * @param spellVariantCandidate set to <code>true</code> to indicate the
     * description has spelling variants
     */
    public void setSpellVariantCandidate(Boolean spellVariantCandidate);

    /**
     * Tests if the description is set as being initial case significant.
     *
     * @return <code>true</code> if the description is initial case significant
     */
    public Boolean isIcsCandidate();

    /**
     * Sets description as being initial case significant.
     *
     * @param icsCandidate set to <code>true</code> to indicate the
     * description is initial case significant
     */
    public void setIcsCandidate(Boolean icsCandidate);

    /**
     * Tests if the description is set as requiring spell checking.
     *
     * @return <code>true</code> if the description requires spell checking
     */
    public Boolean isSpellCheckingRequired();

    /**
     * Sets description as requiring spell checking.
     *
     * @param spellCheckingRequired set to <code>true</code> to indicate the
     * description requires spell checking
     */
    public void setSpellCheckingRequired(Boolean spellCheckingRequired);
}
