/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.ui;

import java.io.IOException;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.gui.concept.AbstractPlugin;
import org.dwfa.ace.log.AceLog;

/**
 * The Class TreeEditorPlugin.
 */
public class TreeEditorPlugin extends AbstractPlugin {

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/** The tree editor panel. */
	private transient JPanel treeEditorPanel;
	
	/** The host. */
	private transient I_HostConceptPlugins host;

	/**
	 * Instantiates a new tree editor plugin.
	 * 
	 * @param selectedByDefault the selected by default
	 * @param sequence the sequence
	 */
	public TreeEditorPlugin(boolean selectedByDefault, int sequence) {
		super(selectedByDefault, sequence);
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 0L;

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getComponentId()
	 */
	@Override
	protected int getComponentId()
	{
		return 0x80000000;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getImageIcon()
	 */
	@Override
	protected ImageIcon getImageIcon() {
		AceLog.getAppLog().info("Getting imageicon for tree editor");

		return new ImageIcon(ACE.class
				.getResource("/24x24/plain/invert_node.png"));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getToolTipText()
	 */
	@Override
	protected String getToolTipText() {
		return "Tree editor view";
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#update()
	 */
	@Override
	public void update() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_PluginToConceptPanel#getComponent(org.dwfa.ace.api.I_HostConceptPlugins)
	 */
	public JComponent getComponent(I_HostConceptPlugins host) {
		AceLog.getAppLog().info("Getting component for tree editor");
		if (treeEditorPanel == null) {
			treeEditorPanel = new JPanel();
			treeEditorPanel.setLayout(new BoxLayout(treeEditorPanel, BoxLayout.PAGE_AXIS));
			treeEditorPanel.add(new JTextArea("Probando text area"));
			treeEditorPanel.add(new JButton("Dos y tres"));
			treeEditorPanel.add(new JButton("Probando"));
			treeEditorPanel.validate();
		}
		return treeEditorPanel;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_PluginToConceptPanel#getId()
	 */
	public UUID getId() {
		return null;// org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES.TREE_EDITOR.getPluginId();
	}

}
