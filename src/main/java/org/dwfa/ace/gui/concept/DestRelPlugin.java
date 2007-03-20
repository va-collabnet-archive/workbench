package org.dwfa.ace.gui.concept;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.table.DestRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;

import com.sleepycat.je.DatabaseException;

public class DestRelPlugin extends RelPlugin {

	private JPanel pluginPanel;
	private DestRelTableModel destRelTableModel;
	I_HostConceptPlugins host;

	public JPanel getComponent(I_HostConceptPlugins host) {
		if (pluginPanel == null) {
			destRelTableModel = new DestRelTableModel(host,
					getDestRelColumns(host.getShowHistory()));
			pluginPanel = getRelPanel(host, destRelTableModel, "Destination relationships:", true);
			host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
			this.host = host;
		}
		return pluginPanel;
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
	protected boolean isSelectedByDefault() {
		return true;
	}

	@Override
	public void update() throws DatabaseException {
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
		destRelTableModel.propertyChange(evt);
	}
}
