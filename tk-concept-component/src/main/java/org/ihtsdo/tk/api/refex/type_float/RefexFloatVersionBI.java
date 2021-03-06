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
package org.ihtsdo.tk.api.refex.type_float;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

/**
 * The Interface RefexFloatVersionBI provides methods for interacting with a
 * particular version of a float type refex member.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface RefexFloatVersionBI<A extends RefexFloatAnalogBI<A>>
        extends RefexVersionBI<A> {

    /**
     * Gets the float value associated with this version of the float refex member.
     *
     * @return the float value associated with this refex version
     */
    float getFloat1();
}
