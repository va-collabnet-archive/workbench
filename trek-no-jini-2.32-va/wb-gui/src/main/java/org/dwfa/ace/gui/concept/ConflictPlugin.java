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
package org.dwfa.ace.gui.concept;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import javax.swing.ImageIcon;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class ConflictPlugin extends AbstractPlugin {
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient ConflictPanel conflictPanel;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public ConflictPlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
    }

    public UUID getId() {
        return I_HostConceptPlugins.TOGGLES.CONFLICT.getPluginId();
    }

    public ConflictPanel getComponent(I_HostConceptPlugins host) throws TerminologyException {
        if (conflictPanel == null) {
            setHost(host);
            conflictPanel = new ConflictPanel();
            host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
            host.addPropertyChangeListener("commit", this);
            try {
                conflictPanel.setConcept((I_GetConceptData) host.getTermComponent(), host.getConfig());
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        return conflictPanel;
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/transform.png"));
    }

    @Override
    public void update() throws IOException, TerminologyException {
        if (showComponent() && conflictPanel != null) {
            conflictPanel.setConcept((I_GetConceptData) getHost().getTermComponent(), getHost().getConfig());
        }
    }

    @Override
    protected String getToolTipText() {
        return "show/hide the conflict view for this concept";
    }

    @Override
    protected int getComponentId() {
        return Integer.MIN_VALUE;
    }

}
