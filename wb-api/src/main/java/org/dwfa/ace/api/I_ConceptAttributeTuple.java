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

import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;

public interface I_ConceptAttributeTuple<A extends ConAttrAnalogBI>
        extends I_AmTuple<A>, ConAttrVersionBI<A> {


    @Override
    public boolean isDefined();

    public void setDefined(boolean defined);

    public I_ConceptAttributePart duplicate();

    @Override
    public I_ConceptAttributePart getMutablePart();
    
    /**
     * 
     * @return
     * @deprecated ;
     */
    public int getConceptStatus();

    /**
     * 
     * @return
     * @deprecated ;
     */
    public int getConId();

    /**
     * 
     * @return
     * @deprecated ;
     */
    public I_ConceptAttributeVersioned getConVersioned();



}
