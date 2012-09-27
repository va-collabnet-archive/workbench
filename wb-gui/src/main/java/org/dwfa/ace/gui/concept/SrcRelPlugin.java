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

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.SrcRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.refset.RefsetUtil;
import org.dwfa.tapi.TerminologyException;

public class SrcRelPlugin extends RelPlugin {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient JPanel pluginPanel;
    private transient SrcRelTableModel srcRelTableModel;
    private static TOGGLES toggleType = TOGGLES.SOURCE_RELS;

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

    public SrcRelPlugin(boolean selectedByDefault, int sequence) {
        super(selectedByDefault, sequence);
    }

    @Override
    public UUID getId() {
        return TOGGLES.SOURCE_RELS.getPluginId();
    }

    @Override
    public JPanel getComponent(I_HostConceptPlugins host) throws TerminologyException, IOException {
        setHost(host);
        if (pluginPanel == null || RefsetUtil.refSetsChanged(host, toggleType, this, visibleExtensions)) {
            createPluginComponent(host);
        }
        return pluginPanel;
    }

    private void createPluginComponent(I_HostConceptPlugins host) throws TerminologyException, IOException {
        setHost(host);
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("creating src rel plugin component...");
        }
        srcRelTableModel = new SrcRelTableModel(host, getSrcRelColumns(host.getShowHistory()), host.getConfig());
        pluginPanel = getRelPanel(host, srcRelTableModel, "Source relationships:", true, toggleType);
        host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
        host.addPropertyChangeListener("commit", this);
        PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
        srcRelTableModel.propertyChange(evt);
    }

    private REL_FIELD[] getSrcRelColumns(boolean showHistory) {
        List<REL_FIELD> fields = new ArrayList<REL_FIELD>();
        fields.add(REL_FIELD.REL_TYPE);
        fields.add(REL_FIELD.DEST_ID);
        fields.add(REL_FIELD.CHARACTERISTIC);
        fields.add(REL_FIELD.REFINABILITY);
        fields.add(REL_FIELD.GROUP);
        fields.add(REL_FIELD.STATUS);
        if (showHistory) {
            fields.add(REL_FIELD.AUTHOR);
            fields.add(REL_FIELD.VERSION);
            fields.add(REL_FIELD.PATH);
        }
        return fields.toArray(new REL_FIELD[fields.size()]);
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/node.png"));
    }

    @Override
    public void update() throws IOException, TerminologyException {
        if (getHost() != null) {

            if (idPlugin != null) {
                idPlugin.update();
            }

            if (RefsetUtil.refSetsChanged(getHost(), toggleType, this, visibleExtensions)
                || getHost().getToggleState(TOGGLES.ID) != idToggleState) {
                idToggleState = getHost().getToggleState(TOGGLES.ID);
                createPluginComponent(getHost());
            }

            PropertyChangeEvent evt = new PropertyChangeEvent(getHost(), "termComponent", null,
                getHost().getTermComponent());
            REL_FIELD[] columnEnums = getSrcRelColumns(getHost().getShowHistory());
            srcRelTableModel.setColumns(getSrcRelColumns(getHost().getShowHistory()));
            for (int i = 0; i < srcRelTableModel.getColumnCount(); i++) {
                TableColumn column = getRelTable().getColumnModel().getColumn(i);
                REL_FIELD columnDesc = columnEnums[i];
                column.setIdentifier(columnDesc);
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());
            }
            setupEditors(getHost());
            srcRelTableModel.propertyChange(evt);
        }
    }

    @Override
    protected String getToolTipText() {
        return "show/hide source relationships for this concept";
    }
}
