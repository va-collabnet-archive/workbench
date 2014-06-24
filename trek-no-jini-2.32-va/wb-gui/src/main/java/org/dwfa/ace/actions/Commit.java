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

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.ihtsdo.rules.RulesLibrary;

public class Commit implements ActionListener {
   @Override
   public void actionPerformed(ActionEvent e) {
      try {
          int response = 100; //some value that is not "yes" or "no" value
          if (RulesLibrary.rulesDisabled) {
              response = JOptionPane.showConfirmDialog(null, "QA is disabled. Are you sure you want to commit?",
                      "QA Disabled!", JOptionPane.YES_NO_OPTION);
          }
          if(!RulesLibrary.rulesDisabled || response == JOptionPane.YES_OPTION){
              Terms.get().commit();
          }
      } catch (Exception e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      }
   }
}
