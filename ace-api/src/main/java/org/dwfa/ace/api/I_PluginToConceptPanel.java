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
package org.dwfa.ace.api;

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.swing.JComponent;

public interface I_PluginToConceptPanel extends Comparable<I_PluginToConceptPanel>, Serializable {

    /**
     * A plugin can have zero or more controls that it wishes to make available
     * to the
     * toggle bar. Each component should also have appropriate tool tips set to
     * explain
     * the control to the end user.
     * 
     * @return
     */
    public List<JComponent> getToggleBarComponents();

    /**
     * Return the component that is shown in the concept panel.
     * 
     * @param host
     * @return
     */
    public JComponent getComponent(I_HostConceptPlugins host);

    /**
     * @param l A listener that wants to be notified when the component should
     *            be
     *            removed from the concept panel.
     */
    public void addShowComponentListener(ActionListener l);

    /**
     * @param l A listener that wants to be notified when the component should
     *            be
     *            removed from the concept panel.
     */
    public void removeShowComponentListener(ActionListener l);

    /**
     * @return true or false depending on if the component is currently shown or
     *         not.
     */
    public boolean showComponent();

    /**
     * Clear all refset listeners from this plugin.
     */
    public void clearRefsetListeners();

    /**
     * 
     * @param listener A component that responds to the current selection for
     *            the purposes
     *            of displaying the proper refset data.
     */
    public void addRefsetListener(I_HoldRefsetData listener);

    /**
     * @param id A unique identifier associated with this plugin.
     */
    public void setId(UUID id);

    /**
     * 
     * @return The unique identifier associated with this plugin.
     */
    public UUID getId();

    /**
     * If two plugins have the same sequence, the sort order of the identifier
     * will
     * be used when comparing to ensure that two plugins with equal sequence
     * numbers
     * do not return a comparable value of 0 (equals).
     * 
     * @param sequence
     */
    public void setSequence(int sequence);

    /**
     * If two plugins have the same sequence, the sort order of the identifier
     * will
     * be used when comparing to ensure that two plugins with equal sequence
     * numbers
     * do not return a comparable value of 0 (equals).
     * 
     * @param sequence
     */
    public int getSequence();

    /**
     * 
     * @return A name for the plugin appropriate to the developer and end-user.
     */
    public String getName();

    /**
     * 
     * @param name A name for the plugin appropriate to the developer and
     *            end-user.
     */
    public void setName(String name);

}
