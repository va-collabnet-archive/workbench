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

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;


public interface I_RelTuple<A extends RelationshipAnalogBI> extends
        I_AmTypedTuple<A>, RelationshipAnalogBI<A> {

    public int getC1Id();

    public int getC2Id();

    public int getRelId();

    public int getCharacteristicId();

    @Override
    public int getGroup();

    public int getRefinabilityId();

    @Override
    public void setStatusId(int statusId) throws PropertyVetoException;

    public void setCharacteristicId(int characteristicId) throws PropertyVetoException;

    public void setRefinabilityId(int refinabilityId) throws PropertyVetoException;

    @Override
    public void setGroup(int group) throws PropertyVetoException;

    /**
     * @deprecated
     */
    public I_RelPart duplicate();

    @Override
    public I_RelPart getMutablePart();

    public I_RelVersioned<A> getRelVersioned();

    @Override
    public I_RelVersioned<A> getFixedPart();

}
