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
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.gui.concept.AbstractPlugin;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.translation.TranslationTermsPanel;

/**
 * The Class SimilarityPlugintst.
 */
public class SecondLangViewerPlugin extends AbstractPlugin {

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/** The similarity panel. */
	private transient TranslationTermsPanel translationTermsPanel;
	
	/** The plugin mark. */
	private transient PluginContainerFrame pluginMark;
	
	/** The host. */
	private transient I_HostConceptPlugins host;

	/** The source lang code. */
	private List<String> langCode;
	
	/** The config. */
	private I_ConfigAceFrame config;

	private String languageLabelType;

	private TOGGLES orderLangViewerPanel;

	private String sequence;

	/**
	 * Instantiates a new similarity plugintst.
	 * 
	 * @param selectedByDefault the selected by default
	 * @param sequence the sequence
	 * @param orderlangviewerPanel 
	 */
	public SecondLangViewerPlugin(boolean selectedByDefault, int sequence, TOGGLES orderLangViewerPanel) {
		super(selectedByDefault, sequence);
		this.sequence=String.valueOf(sequence);
		this.orderLangViewerPanel=orderLangViewerPanel;
	}
	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 0L;

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getComponentId()
	 */
	@Override
	protected int getComponentId()
	{
		return 0x80000000 + Integer.parseInt(sequence);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getImageIcon()
	 */
	@Override
	protected ImageIcon getImageIcon() {
		AceLog.getAppLog().info("Getting imageicon for language viewer panel");

		return new ImageIcon("icons/" + sequence + ".png");
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getToolTipText()
	 */
	@Override
	protected String getToolTipText() {
		return "Second language viewer";
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#update()
	 */
	@Override
	public void update() throws IOException {
		System.out.println("");
		I_GetConceptData hostConcept = null;
		if (host!=null){
		if (host.getTermComponent() != null) {
			try {
				hostConcept = Terms.get().getConcept(host.getTermComponent().getNid());
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("updating --------------------" );
		translationTermsPanel.update(hostConcept);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_PluginToConceptPanel#getComponent(org.dwfa.ace.api.I_HostConceptPlugins)
	 */
	public JComponent getComponent(I_HostConceptPlugins host) {
		AceLog.getAppLog().info("Getting component for tree editor");
		
		if (translationTermsPanel == null) {
			I_GetConceptData hostConcept = null;
			if (host.getTermComponent() != null) {
				try {
					hostConcept = Terms.get().getConcept(host.getTermComponent().getNid());
				} catch (TerminologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			treeEditorPanel = new ConceptTreeEditor(hostConcept);
			this.host=host;
			translationTermsPanel = new TranslationTermsPanel(null, hostConcept);
			pluginMark=new PluginContainerFrame(translationTermsPanel,"Language Viewer");
			this.host.addPropertyChangeListener("termComponent", this);
			System.out.println("getcomponent");
		}
		return pluginMark;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_PluginToConceptPanel#getId()
	 */
	public UUID getId() {
		return orderLangViewerPanel.getPluginId(); 
		// TODO: fix reference to concept planel plugin // org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES.SIMILARITY_PANEL.getPluginId() ;
	}
	

}
