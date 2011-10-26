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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.gui.concept.AbstractPlugin;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.SimilarityResultsToCptPanel;

/**
 * The Class SimilarityPlugintst.
 */
public class SimilarityPlugintst extends AbstractPlugin {

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/** The similarity panel. */
	private transient SimilarityResultsToCptPanel similarityPanel;
	
	/** The plugin mark. */
	private transient PluginContainerFrame pluginMark;
	
	/** The host. */
	private transient I_HostConceptPlugins host;
	
	/** The query. */
	private String query;
	
	/** The source lang code. */
	private String sourceLangCode;
	
	/** The target lang code. */
	private String targetLangCode;
	
	/** The matches. */
	private List<SimilarityMatchedItem> matches;
	
	/** The config. */
	private I_ConfigAceFrame config;

	/**
	 * Instantiates a new similarity plugintst.
	 * 
	 * @param selectedByDefault the selected by default
	 * @param sequence the sequence
	 */
	public SimilarityPlugintst(boolean selectedByDefault, int sequence) {
		super(selectedByDefault, sequence);

		try {
			this.query="";
	
			this.sourceLangCode = "en";
			this.targetLangCode = "es";
			matches=new ArrayList<SimilarityMatchedItem>();
			config=LocalVersionedTerminology.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Sets the config data.
	 * 
	 * @param query the query
	 * @param sourceLangCode the source lang code
	 * @param targetLangCode the target lang code
	 * @param results the results
	 * @param config the config
	 */
	public void setConfigData(String query, String sourceLangCode, String targetLangCode, List<SimilarityMatchedItem> results,
			I_ConfigAceFrame config){
		this.query=query;
		this.sourceLangCode=sourceLangCode;
		this.targetLangCode=targetLangCode;
		this.matches=results;
		this.config=config;
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
		AceLog.getAppLog().info("Getting imageicon for concept similarity");

		return new ImageIcon(ACE.class
				.getResource("/24x24/plain/invert_node.png"));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.gui.concept.AbstractPlugin#getToolTipText()
	 */
	@Override
	protected String getToolTipText() {
		return "Concept Similarity";
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
				hostConcept = LocalVersionedTerminology.get().getConcept(host.getTermComponent().getNid());
				query=hostConcept.getInitialText();
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("updating --------------------" );
		similarityPanel.update(query);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_PluginToConceptPanel#getComponent(org.dwfa.ace.api.I_HostConceptPlugins)
	 */
	public JComponent getComponent(I_HostConceptPlugins host) {
		AceLog.getAppLog().info("Getting component for tree editor");
		
		if (similarityPanel == null) {
			I_GetConceptData hostConcept = null;
			if (host.getTermComponent() != null) {
				try {
					hostConcept = LocalVersionedTerminology.get().getConcept(host.getTermComponent().getNid());
					query=hostConcept.getInitialText();
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
			similarityPanel = new SimilarityResultsToCptPanel(query, sourceLangCode, targetLangCode, matches, config);
			pluginMark=new PluginContainerFrame(similarityPanel,"Similarity");
			this.host.addPropertyChangeListener("termComponent", this);
			System.out.println("getcomponent");
		}
		return pluginMark;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_PluginToConceptPanel#getId()
	 */
	public UUID getId() {
		return null; // TODO: fix reference to concept planel plugin // org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES.SIMILARITY_PANEL.getPluginId() ;
	}
	

}
