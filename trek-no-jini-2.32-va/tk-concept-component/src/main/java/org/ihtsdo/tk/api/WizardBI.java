/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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

package org.ihtsdo.tk.api;

import javax.swing.JPanel;

/**
 * The Interface WizardBI represents the wizard panel associated with an arena panel.
 *
 * @deprecated need to move this class out of the API
 */
public interface WizardBI {

   /**
    * Gets the wizard panel of an arena panel.
    *
    * @return the wizard panel
    * @deprecated need to move this class out of the API
    */
   JPanel getWizardPanel();

   /**
    * Sets the wizard panel visible.
    *
    * @param visible set to <code>true</code> to make the wizard panel visible
    * @deprecated need to move this class out of the API
    */
   void setWizardPanelVisible(boolean visible);

}
