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

public interface I_RelPart extends I_AmTypedPart {

    public boolean hasNewData(I_RelPart another);

    public void setPathId(int pathId);

    public int getCharacteristicId();

    public void setCharacteristicId(int characteristicId);

    public int getGroup();

    public void setGroup(int group);

    public int getRefinabilityId();

    public void setRefinabilityId(int refinabilityId);

    public int getRelTypeId();

    public void setRelTypeId(int relTypeId);

    public void setVersion(int version);

    public void setStatusId(int statusId);

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public I_RelPart duplicate();

}
