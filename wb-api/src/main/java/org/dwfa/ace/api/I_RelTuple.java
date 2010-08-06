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

import org.ihtsdo.tk.api.RelationshipChronicleBI;

public interface I_RelTuple extends I_AmTypedTuple {

    public int getC1Id();

    public int getC2Id();

    public int getRelId();

    public int getCharacteristicId();

    public int getGroup();

    public int getRefinabilityId();

    public void setStatusId(int statusId);

    public void setCharacteristicId(int characteristicId);

    public void setRefinabilityId(int refinabilityId);

    public void setGroup(int group);

    /**
     * @deprecated
     */
    public I_RelPart duplicate();

    public I_RelPart getMutablePart();

    public I_RelVersioned getRelVersioned();

    public I_RelVersioned getFixedPart();

}
