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

import javax.security.sasl.AuthenticationException;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;

public class ChangeFramePassword implements ActionListener {

    ACE acePanel;

    public ChangeFramePassword(ACE acePanel) {
        super();
        this.acePanel = acePanel;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            I_ConfigAceFrame frameConfig = acePanel.getAceFrameConfig();
            SvnPrompter prompter = new SvnPrompter();
            prompter.setParentContainer(acePanel);
            if (prompter.prompt("Current username/password", frameConfig.getUsername())) {
                if (prompter.getUsername() != null) {
                    if (prompter.getUsername().equals(frameConfig.getUsername()) == false) {
                        throw new AuthenticationException("username does not match");
                    }
                }
                if (prompter.getPassword() != null) {
                    if (prompter.getPassword().equals(frameConfig.getPassword()) == false) {
                        throw new AuthenticationException("password does not match");
                    }
                }
                if (prompter.prompt("New username/password", AceConfig.config.getUsername())) {
                    frameConfig.setUsername(prompter.getUsername());
                    frameConfig.setPassword(prompter.getPassword());
                }
            }

        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }

}
