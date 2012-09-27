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

// TODO: Auto-generated Javadoc
/**
 * The Interface RefexNidNidAnalogBI.
 *
 * @param <A> the generic type
 * @author kec
 */
public interface RefexNidNidAnalogBI<A extends RefexNidNidAnalogBI<A>>  
    extends RefexNidAnalogBI<A>, RefexNidNidVersionBI<A> {
     
     /**
      * Sets the nid2.
      *
      * @param nid2 the new nid2
      * @throws PropertyVetoException the property veto exception
      */
     void setNid2(int nid2) throws PropertyVetoException;

}
