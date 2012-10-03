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
package org.ihtsdo.tk.api.refex;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.AnalogBI;

/**
 * The Interface RefexAnalogBI contains methods for editing a refex analog. The
 * preferred method of editing terminology is through a blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprint
 */
public interface RefexAnalogBI<A extends RefexAnalogBI<A>>
        extends RefexVersionBI<A>, AnalogBI {

    /**
     * Sets concept representing the refex collection for this refex member
     * based on the specified
     * <code>collectionNid</code>.
     *
     * @param collectionNid the nid associated with the refex collection concept
     * @throws PropertyVetoException if the new value is not valid
     */
    void setCollectionNid(int collectionNid) throws PropertyVetoException;

    /**
     * Sets the referenced component for this refex member based on the
     * specified
     * <code>componentNid</code>.
     *
     * @param componentNid the nid associated with the referenced component
     * @throws PropertyVetoException if the new value is not valid
     */
    void setReferencedComponentNid(int componentNid) throws PropertyVetoException;
}
