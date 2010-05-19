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
package org.dwfa.ace;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import org.dwfa.ace.api.I_GetConceptData;

public class DropButton extends JButton implements I_DoConceptDrop {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private I_DoConceptDrop dropObject;

    public DropButton(Icon icon, I_DoConceptDrop dropObject) {
        super(icon);
        this.dropObject = dropObject;
    }

    public DropButton(String text, I_DoConceptDrop dropObject) {
        super(text);
        this.dropObject = dropObject;
    }

    public DropButton(Action a, I_DoConceptDrop dropObject) {
        super(a);
        this.dropObject = dropObject;
    }

    public DropButton(String text, Icon icon, I_DoConceptDrop dropObject) {
        super(text, icon);
        this.dropObject = dropObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.I_DoConceptDrop#doDrop(org.dwfa.vodb.types.I_GetConceptData)
     */
    public void doDrop(I_GetConceptData obj) {
        dropObject.doDrop(obj);
    }

}
