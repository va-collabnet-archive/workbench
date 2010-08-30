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

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JComponent;

/**
 * @author kec
 * 
 */
public interface I_DoDragAndDrop {
    public void highlightForDrop(boolean highlight);

    public void highlightForDrag(boolean highlight);

    /**
     * @return Returns the acceptableActions.
     */
    public int getAcceptableActions();

    public boolean isFlavorSupportedForImport(DataFlavor flavor);

    public boolean isFlavorSupportedForExport(DataFlavor flavor);

    public Collection<DataFlavor> getLocalFlavors();

    public Collection<DataFlavor> getSerialFlavors();

    public DataFlavor[] getImportDataFlavors();

    public Image createImage(int width, int height);

    public boolean isDragging();

    public JComponent getJComponent();

    public Transferable getTransferable() throws Exception;

    public void setDroppedObject(Object obj, DropTargetDropEvent ev);

    public void setDroppedObject(Object obj);

    public Logger getLogger();

}
