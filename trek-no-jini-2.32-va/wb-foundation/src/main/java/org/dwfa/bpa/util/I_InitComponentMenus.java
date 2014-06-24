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
/*
 * Created on May 21, 2005
 */
package org.dwfa.bpa.util;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * @author kec
 * 
 */
public interface I_InitComponentMenus {
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception;

    public JMenu getQuitMenu();

    public void addInternalFrames(JMenu menu);

    public JMenuItem[] getNewWindowMenu();
}
