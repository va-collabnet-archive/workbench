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

import org.apache.commons.collections.primitives.ArrayIntList;

public interface I_AmPart {

    public long getTime();

    /**
     * 1. Analog, an object, concept or situation which in some way resembles a different situation
     * 2. Analogy, in language, a comparison between concepts
     * @param statusNid
     * @param pathNid
     * @param time
     * @return
     */
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time);

    public int getPathId();

    public int getVersion();

    public int getStatusId();

    public void setPathId(int pathId);

    public void setVersion(int version);

    public void setStatusId(int statusId);

    public I_AmPart duplicate();

    public ArrayIntList getPartComponentNids();

}
