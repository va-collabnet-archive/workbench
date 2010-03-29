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
package org.dwfa.ace.task.commit;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;

public class AbortDescriptionPart implements I_Fixup {
    I_GetConceptData concept;
    I_DescriptionVersioned desc;
    I_DescriptionPart part;

    public AbortDescriptionPart(I_GetConceptData concept, I_DescriptionVersioned desc, I_DescriptionPart part) {
        super();
        this.concept = concept;
        this.desc = desc;
        this.part = part;
    }

    public void fix() throws Exception {
        I_TermFactory tf = LocalVersionedTerminology.get();
        desc.getVersions().remove(part);
        tf.addUncommitted(concept);
        AceLog.getAppLog().info("Aborted add desc part: " + part);
    }

    public String toString() {
        return "remove " + part.getText();
    }

}
