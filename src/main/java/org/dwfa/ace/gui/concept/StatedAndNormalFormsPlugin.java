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
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class StatedAndNormalFormsPlugin extends AbstractPlugin {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient LogicalFormsPanel formsPanel;

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

    public StatedAndNormalFormsPlugin(boolean selectedByDefault, int sequence) {
        super(selectedByDefault, sequence);
    }

    public UUID getId() {
        return TOGGLES.STATED_INFERRED.getPluginId();
    }

    public LogicalFormsPanel getComponent(I_HostConceptPlugins host) {
        if (formsPanel == null) {
            setHost(host);
            formsPanel = new LogicalFormsPanel();
            host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
            host.addPropertyChangeListener("commit", this);
            try {
                formsPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        return formsPanel;
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/chrystal_ball.png"));
    }

    @Override
    public void update() throws IOException {
        if (showComponent() && formsPanel != null) {
            formsPanel.setConcept((ConceptBean) getHost().getTermComponent(), getHost().getConfig());
        }

    }

    @Override
    protected String getToolTipText() {
        return "<html>show/hide the stated and normal forms panel...";
    }

    @Override
    protected int getComponentId() {
        return Integer.MIN_VALUE;
    }

}
