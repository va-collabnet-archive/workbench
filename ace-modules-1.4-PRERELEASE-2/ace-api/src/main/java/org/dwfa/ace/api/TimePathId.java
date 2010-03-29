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

import java.io.IOException;
import java.util.Date;

import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class TimePathId {
    int time;
    int pathId;

    public int getPathId() {
        return pathId;
    }

    public int getTime() {
        return time;
    }

    public TimePathId(int time, int pathId) {
        super();
        this.time = time;
        this.pathId = pathId;
    }

    @Override
    public boolean equals(Object obj) {
        TimePathId another = (TimePathId) obj;
        return time == another.time && pathId == another.pathId;
    }

    @Override
    public int hashCode() {
        return time;
    }

    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        pathId = jarToDbNativeMap.get(pathId);
    }

    public String toString() {
        long thickTime = LocalVersionedTerminology.get().convertToThickVersion(time);
        try {
            I_GetConceptData path = LocalVersionedTerminology.get().getConcept(pathId);
            return new Date(thickTime) + " on path " + path;
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return new Date(thickTime) + " on path " + pathId;
    }
}
