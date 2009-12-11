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

public interface I_RelPart {

    public abstract boolean hasNewData(I_RelPart another);

    public abstract int getPathId();

    public abstract void setPathId(int pathId);

    public abstract int getCharacteristicId();

    public abstract void setCharacteristicId(int characteristicId);

    public abstract int getGroup();

    public abstract void setGroup(int group);

    public abstract int getRefinabilityId();

    public abstract void setRefinabilityId(int refinabilityId);

    public abstract int getRelTypeId();

    public abstract void setRelTypeId(int relTypeId);

    public abstract int getVersion();

    public abstract void setVersion(int version);

    public abstract int getStatusId();

    public abstract void setStatusId(int statusId);

    public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public abstract I_RelPart duplicate();

}
