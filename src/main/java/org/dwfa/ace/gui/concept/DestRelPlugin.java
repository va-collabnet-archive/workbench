package org.dwfa.ace.gui.concept;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DestRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.refset.RefsetUtil;

public class DestRelPlugin extends RelPlugin {

   public DestRelPlugin() {
      super(false);
   }

   private JPanel pluginPanel;

   private DestRelTableModel destRelTableModel;

   I_HostConceptPlugins host;
   
   TOGGLES toggleType = TOGGLES.DEST_RELS;

   public JPanel getComponent(I_HostConceptPlugins host) {
      if (pluginPanel == null || RefsetUtil.refSetsChanged(host, toggleType, this, visibleExtensions)) {
         createPluginComponent(host);
      }
      return pluginPanel;
   }

   private void createPluginComponent(I_HostConceptPlugins host) {
      if (AceLog.getAppLog().isLoggable(Level.FINE)) {
         AceLog.getAppLog().fine("creating dest rel plugin component...");
      }
      destRelTableModel = new DestRelTableModel(host, getDestRelColumns(host.getShowHistory()));
      pluginPanel = getRelPanel(host, destRelTableModel, "Destination relationships:", false, toggleType);
      host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
      host.addPropertyChangeListener("commit", this);
      this.host = host;
      PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
      destRelTableModel.propertyChange(evt);
   }

   private REL_FIELD[] getDestRelColumns(boolean showHistory) {
      List<REL_FIELD> fields = new ArrayList<REL_FIELD>();
      fields.add(REL_FIELD.SOURCE_ID);
      fields.add(REL_FIELD.REL_TYPE);
      fields.add(REL_FIELD.CHARACTERISTIC);
      fields.add(REL_FIELD.REFINABILITY);
      fields.add(REL_FIELD.STATUS);
      if (showHistory) {
         fields.add(REL_FIELD.VERSION);
         fields.add(REL_FIELD.BRANCH);
      }
      return fields.toArray(new REL_FIELD[fields.size()]);
   }

   @Override
   protected ImageIcon getImageIcon() {
      return new ImageIcon(ACE.class.getResource("/24x24/plain/invert_node.png"));
   }

   @Override
   public void update() throws IOException {
      if (host != null) {

         if (RefsetUtil.refSetsChanged(host, toggleType, this, visibleExtensions)) {
            createPluginComponent(host);
         }
         PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
         REL_FIELD[] columnEnums = getDestRelColumns(host.getShowHistory());
         destRelTableModel.setColumns(getDestRelColumns(host.getShowHistory()));
         for (int i = 0; i < destRelTableModel.getColumnCount(); i++) {
            TableColumn column = getRelTable().getColumnModel().getColumn(i);
            REL_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
         }
         setupEditors(host);
         destRelTableModel.propertyChange(evt);
      }
   }

   @Override
   protected String getToolTipText() {
      return "show/hide destination relationshipts for this concept";
   }

}
