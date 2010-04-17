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
package org.dwfa.mojo;

import java.text.ParseException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class PositionDescriptor {
    private ConceptDescriptor path;
    private String timeString;

    public ConceptDescriptor getPath() {
        return path;
    }

    public void setPath(ConceptDescriptor path) {
        this.path = path;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) throws ParseException {
        this.timeString = timeString;
    }

    public I_Position getPosition() throws Exception {
        I_GetConceptData pathConcept = path.getVerifiedConcept();
        I_Path pathForPosition = LocalVersionedTerminology.get().getPath(pathConcept.getUids());
        int version = ThinVersionHelper.convert(timeString);
        return LocalVersionedTerminology.get().newPosition(pathForPosition, version);
    }

    public String toString() {
        return "Path: " + path + " position: " + timeString;
    }
}
