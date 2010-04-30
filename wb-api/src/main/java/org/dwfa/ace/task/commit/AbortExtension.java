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

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;

public class AbortExtension implements I_Fixup {
    I_ExtendByRef extension;
    private String textForOption;

    public AbortExtension(I_ExtendByRef extension, String textForOption) {
        super();
        this.extension = extension;
        this.textForOption = textForOption;
    }

    public void fix() throws Exception {
        Terms.get().forget(extension);
    }

    public String toString() {
        return textForOption;
    }

}
