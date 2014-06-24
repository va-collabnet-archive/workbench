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

import java.util.UUID;

/**
 * The Interface I_DescribeExternally is implemented by the TK Description
 * classes provides methods to access the information associated with
 * descriptions.
 */
public interface I_DescribeExternally extends I_VersionExternally {

    /**
     * Checks the text associated with a description is initial case significant.
     *
     * @return <code>true</code>, if the description text is initial case significant
     */
    public boolean isInitialCaseSignificant();

    /**
     * Gets a two character abbreviation of the language of a description.
     *
     * @return two character String abbreviation of the language of a description
     */
    public String getLang();

    /**
     * Gets the text associated with a description.
     *
     * @return the String representing the description text
     */
    public String getText();

    /**
     * Gets the uuid of the type associated with a description.
     *
     * @return the uuid of the type associated with a description
     */
    public UUID getTypeUuid();
}