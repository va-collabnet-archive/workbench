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
package org.ihtsdo.tk.api.ext;

/**
 * The Interface I_ConceptualizeExternally is implemented by the TK Concept
 * Attributes classes provides methods to access the information
 * associated with concept attributes.
 */
public interface I_ConceptualizeExternally extends I_VersionExternally {

    /**
     * Checks if the associated concept is defined.
     *
     * @return <code>true</code>, if the concept is defined
     */
    public boolean isDefined();
}