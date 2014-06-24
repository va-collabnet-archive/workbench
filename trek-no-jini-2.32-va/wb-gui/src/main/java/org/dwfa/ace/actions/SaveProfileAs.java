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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.fd.FileDialogUtil;

public class SaveProfileAs implements ActionListener {

    Frame parentFrame;

    public void actionPerformed(ActionEvent e) {
        try {
            File outFile = FileDialogUtil.getNewFile("Save profile as...", new File("profiles/default.ace"),
                parentFrame);
            AceConfig.config.setProfileFile(outFile);
            AceConfig.config.save();
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }

    public SaveProfileAs(Frame parentFrame) {
        super();
        this.parentFrame = parentFrame;
    }

}
