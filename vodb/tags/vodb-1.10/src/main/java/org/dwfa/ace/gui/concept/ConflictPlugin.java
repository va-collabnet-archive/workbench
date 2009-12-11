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

import javax.swing.ImageIcon;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class ConflictPlugin extends AbstractPlugin {
	public ConflictPlugin() {
		super(false);
	}

	private ConflictPanel conflictPanel;
	private I_HostConceptPlugins host;

	public ConflictPanel getComponent(I_HostConceptPlugins host) {
		if (conflictPanel == null) {
			this.host = host;
			conflictPanel = new ConflictPanel();
			host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
			host.addPropertyChangeListener("commit", this);
			try {
				conflictPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		return conflictPanel;
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class
				.getResource("/24x24/plain/transform.png"));
	}

	@Override
	public void update() throws IOException {
		if (showComponent() && conflictPanel != null) {
			conflictPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
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
