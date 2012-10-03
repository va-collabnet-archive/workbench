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

package org.ihtsdo.tk.api.refex.type_nid_nid;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.refex.type_nid.RefexNidAnalogBI;

/**
 * The Interface RefexNidNidAnalogBI provides methods for editing a nid-nid
 * type refex analog. The preferred method of editing terminology is through a
 * blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprint
 */
public interface RefexNidNidAnalogBI<A extends RefexNidNidAnalogBI<A>>  
    extends RefexNidAnalogBI<A>, RefexNidNidVersionBI<A> {
     
     /**
      * Sets the second nid value,
      * <code>nid2</code>, associated with this nid-nid refex member.
      *
      * @param nid2 the second nid value associated with the refex member
      * @throws PropertyVetoException the property veto exception
      */
     void setNid2(int nid2) throws PropertyVetoException;

}
