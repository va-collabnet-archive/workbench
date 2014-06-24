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

package org.ihtsdo.tk.api.refex.type_float;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.refex.RefexAnalogBI;

/**
 * The Interface RefexFloatAnalogBI contains methods for editing a float type refex analog. The
 * preferred method of editing terminology is through a blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprint
 */
public interface RefexFloatAnalogBI<A extends RefexFloatAnalogBI<A>> extends RefexAnalogBI<A> {
    
     /**
      * Sets the float value, <code>float1</code>, associated with this float refex member.
      *
      * @param float1 the float value associated with this refex member
      * @throws PropertyVetoException if the new value is not valid
      */
     void setFloat1(float float1) throws PropertyVetoException;

}
