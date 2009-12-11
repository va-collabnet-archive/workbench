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

public interface I_ConceptAttributePart {

    public abstract int getPathId();

    public abstract void setPathId(int pathId);

    public abstract int getConceptStatus();

    public abstract void setConceptStatus(int conceptStatus);

    public abstract boolean isDefined();

    public abstract void setDefined(boolean defined);

    public abstract int getVersion();

    public abstract void setVersion(int version);

    public abstract I_ConceptAttributePart duplicate();

    public abstract boolean hasNewData(I_ConceptAttributePart another);

}
