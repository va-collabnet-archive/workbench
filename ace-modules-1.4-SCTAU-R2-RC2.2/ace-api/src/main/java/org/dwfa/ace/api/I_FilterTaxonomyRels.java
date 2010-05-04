/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;

import org.dwfa.tapi.TerminologyException;

public interface I_FilterTaxonomyRels {

    /**
     * The filter can suppress children in the taxonomy view by removing any
     * relationship tuples
     * from the srcRels or destRels lists. Only the relationship tuples
     * remaining after the filter will
     * be displayed in the taxonomy view.
     * 
     * @param node The taxonomy node that the srcRels and destRels are relative
     *            to.
     * @param srcRels The source relationships tuples that meet the criterion in
     *            the preferences panel to be displayed in the taxonomy view.
     * @param destRels The destination relationship tuples that meet the
     *            criterion in the preference panel to be displayed in the
     *            taxonomy view.
     * @throws IOException
     * @throws TerminologyException
     */
    public void filter(I_GetConceptData node, List<I_RelTuple> srcRels, List<I_RelTuple> destRels,
            I_ConfigAceFrame frameConfig) throws TerminologyException, IOException;

}
