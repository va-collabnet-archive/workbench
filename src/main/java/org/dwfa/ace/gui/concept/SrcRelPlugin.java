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
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.SrcRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.refset.RefsetUtil;

public class SrcRelPlugin extends RelPlugin {

	public SrcRelPlugin() {
		super(true);
	}
	private JPanel pluginPanel;
	private SrcRelTableModel srcRelTableModel;
	I_HostConceptPlugins host;

   TOGGLES toggleType = TOGGLES.SOURCE_RELS;

	public JPanel getComponent(I_HostConceptPlugins host) {
	  this.host = host;
      if (pluginPanel == null || RefsetUtil.refSetsChanged(host, toggleType, this, visibleExtensions)) {
         createPluginComponent(host);
      }
		return pluginPanel;
	}

   private void createPluginComponent(I_HostConceptPlugins host) {
	  this.host = host;
      if (AceLog.getAppLog().isLoggable(Level.FINE)) {
         AceLog.getAppLog().fine("creating src rel plugin component...");
      }
      srcRelTableModel = new SrcRelTableModel(host,
      		getSrcRelColumns(host.getShowHistory()));
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
	public void update() throws IOException {
		if (host != null) {
			
		if (idPlugin != null) {
			idPlugin.update();
		}

         if (RefsetUtil.refSetsChanged(host, toggleType, this, visibleExtensions)|| host.getToggleState(TOGGLES.ID) != idToggleState) {
				idToggleState = host.getToggleState(TOGGLES.ID);
				createPluginComponent(host);
         }

         PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
			REL_FIELD[] columnEnums = getSrcRelColumns(host.getShowHistory());
			srcRelTableModel.setColumns(getSrcRelColumns(host.getShowHistory()));
			for (int i = 0; i < srcRelTableModel.getColumnCount(); i++) {
				TableColumn column = getRelTable().getColumnModel().getColumn(i);
				REL_FIELD columnDesc = columnEnums[i];
				column.setIdentifier(columnDesc);
				column.setPreferredWidth(columnDesc.getPref());
				column.setMaxWidth(columnDesc.getMax());
				column.setMinWidth(columnDesc.getMin());
			}
			setupEditors(host);
			srcRelTableModel.propertyChange(evt);			
		}
	}
   @Override
   protected String getToolTipText() {
      return "show/hide source relationships for this concept";
   }

	@Override
	protected I_HostConceptPlugins getHost() {
		return host;
	}

}
