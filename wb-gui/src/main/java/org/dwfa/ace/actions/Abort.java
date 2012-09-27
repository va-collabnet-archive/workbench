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
package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class Abort implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        try {
            Terms.get().cancel();
            Terms.get().getActiveAceFrameConfig().refreshRefsetTab();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (TerminologyException e2) {
            AceLog.getAppLog().alertAndLogException(e2);
        }
    }
}
