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
package org.ihtsdo.tk.api.refex.type_boolean;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.refex.RefexAnalogBI;

/**
 * The Interface RefexBooleanAnalogBI provides methods for editing a boolean
 * type refex analog. The preferred method of editing terminology is through a
 * blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprint
 */
public interface RefexBooleanAnalogBI<A extends RefexBooleanAnalogBI<A>>
        extends RefexAnalogBI<A>, RefexBooleanVersionBI<A> {

    /**
     * Sets the boolean value,
     * <code>boolean1</code>, associated with this boolean refex member.
     *
     * @param boolean1 the boolean value associated with the refex member
     * @throws PropertyVetoException if the new value is not valid
     */
    void setBoolean1(boolean boolean1) throws PropertyVetoException;
}
