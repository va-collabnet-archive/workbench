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
package org.ihtsdo.tk.api.refex.type_nid;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

/**
 * The Interface RefexNidVersionBI provides methods for interacting with a
 * particular version of a nid type refex member. This is not the referenced
 * component of the refex member, rather an addition component to be associated.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface RefexNidVersionBI<A extends RefexNidAnalogBI<A>>
        extends RefexVersionBI<A> {

    /**
     * Gets the nid associated with this version of the nid refex member.
     *
     * @return the nid associated with this refex version
     */
    int getNid1();
}
