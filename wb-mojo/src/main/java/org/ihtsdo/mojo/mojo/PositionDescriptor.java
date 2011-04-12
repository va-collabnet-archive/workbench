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
package org.ihtsdo.mojo.mojo;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

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

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public PositionBI getPosition() throws Exception {
        I_GetConceptData pathConcept = path.getVerifiedConcept();
        PathBI pathForPosition = Terms.get().getPath(pathConcept.getUids());
        return Terms.get().newPosition(pathForPosition, ThinVersionHelper.convert(timeString));
    }

    @Override
    public String toString() {
        return "Path: " + path + " position: " + timeString;
    }
}
