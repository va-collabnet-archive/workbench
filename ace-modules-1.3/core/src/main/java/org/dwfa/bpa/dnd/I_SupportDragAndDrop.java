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
package org.dwfa.bpa.dnd;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;

public interface I_SupportDragAndDrop {

    /**
     * @return Returns the acceptableActions.
     */
    public int getAcceptableActions();

    public boolean isDragging();

    public void highlightForDrag(boolean highlight);

    public void highlightForDrop(boolean highlight);

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt);

    /**
     * 
     */
    public void resetRecognizer();

    public Transferable getTransferable() throws Exception;

    /**
     * @see java.awt.dnd.DropTarget#isActive()
     */
    public boolean isDropActive();

}
