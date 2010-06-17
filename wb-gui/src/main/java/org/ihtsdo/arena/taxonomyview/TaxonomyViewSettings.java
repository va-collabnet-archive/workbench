package org.ihtsdo.arena.taxonomyview;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.TermTreeHelper;
import org.ihtsdo.arena.ArenaComponentSettings;

public class TaxonomyViewSettings extends ArenaComponentSettings {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	// transient
	private JScrollPane view;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	@Override
	public String getTitle() {
		return "Taxonomy Viewer";
	}

	@Override
	public I_HostConceptPlugins getHost() {
		return null;
	}

	public JComponent getLinkComponent() {
		return new JLabel("   ");
	}

	@Override
	public JComponent makeComponent(I_ConfigAceFrame config) {
		if (view == null) {
			TermTreeHelper hierarchicalTreeHelper = new TermTreeHelper(config,
					ace);
			try {
				view = hierarchicalTreeHelper.getHierarchyPanel();
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			JTreeWithDragImage tree =  (JTreeWithDragImage) view.getViewport().getView();
			tree.setFont(tree.getFont().deriveFont(getFontSize()));
		}
		return view;
	}

	@Override
	public List<AbstractButton> getSpecializedButtons() {
		return new ArrayList<AbstractButton>();
	}

	@Override
	protected void setupSubtypes() {
		// TODO Auto-generated method stub
		
	}

}
