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

package org.dwfa.bpa.process;

import java.awt.Frame;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;

import org.dwfa.bpa.gui.GridBagPanel;

/**
 * A interface for a workspace that allows tasks to control windows and
 * interact with the user.
 * 
 * @author kec
 * @todo refactor to a web workspace and a swing workspace.
 * 
 */
public interface I_Workspace {
    /**
     * Adds a panel to the workspace.
     * 
     * @param panel The panel to add to the workspace.
     */
    public void addGridBagPanel(GridBagPanel panel);

    /**
     * @return Returns the panelList.
     */
    public List<GridBagPanel> getPanelList();

    /**
     * Bounds for the workspace.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setWorkspaceBounds(int x, int y, int width, int height);

    public void setWorkspaceBounds(Rectangle bounds);

    /**
     * Shows or hides the workspace
     * 
     * @param visible
     */
    public void setWorkspaceVisible(boolean visible);

    /**
     * Retrieves a panel by its name.
     * 
     * @param panelName Name of the panel to be retrieved.
     * @return The panel corresponding to the provided name.
     * @throws NoMatchingEntryException
     */
    public GridBagPanel getPanel(String panelName) throws NoMatchingEntryException;

    /**
     * @return the name of the workspace.
     */
    public String getName();

    /**
     * @param name the name of the workspace.
     */
    public void setName(String name);

    public Frame getFrame();

    /**
     * @return the identifier for this workspace.
     */
    public UUID getId();

    /**
     * @param id the identifier for this workspace.
     */
    public void setId(UUID id);

    /**
     * Make this workspace the front-most window.
     */
    public void bringToFront();

    public void setStatusMessage(String message);

    public String getStatusMessage();

    public void setAltMessage(String message);

    public String getAltMessage();

    public Object getAttachment(String key);

    public void setAttachment(String key, Object attachment);

    public void addAttachmentListener(PropertyChangeListener l);

    public void addAttachmentListener(String property, PropertyChangeListener l);

    public void removeAttachmentListener(PropertyChangeListener l);

    public void removeAttachmentListener(String property, PropertyChangeListener l);

    public boolean isShownInInternalFrame();

    public void setShownInInternalFrame(boolean b);

    public void setOneTouchExpandable(boolean b);

    public void acquireOwnership();

    public void releaseOwnership();

}
