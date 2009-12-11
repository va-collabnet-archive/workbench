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
package org.dwfa.ace.config;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.vodb.types.ConceptBean;

public class SelectPathAndPositionPanel extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectPathAndPositionPanel(boolean selectPositionOnly, String purpose, I_ConfigAceFrame aceConfig, PropertySetListenerGlue selectGlue) throws Exception {
		super();
		List<TimePathId> timePathEntries = AceConfig.getVodb().getTimePathList();
		for (I_Path p: AceConfig.getVodb().getPaths()) {
			ConceptBean cb = ConceptBean.get(p.getConceptId());
			int modTimeCount = 0;
			for (TimePathId tp: timePathEntries) {
				if (tp.getPathId() == p.getConceptId()) {
					modTimeCount++;
				}
			}
			PositionPanel pp = new PositionPanel(p,
					selectPositionOnly, purpose,
		            cb.getInitialText(), aceConfig, selectGlue) ;
			addTab(cb.getInitialText(), pp);
		}
	}

	public List<I_Position> getSelectedPositions() {
		List<I_Position> positions = new ArrayList<I_Position>();
		for (int i = 0; i < getTabCount(); i++) {
			PositionPanel pp = (PositionPanel) getComponentAt(i);
			if (pp.isPositionSelected()) {
				positions.add(pp.getPosition());
			}
		}
		return positions;
	}

}
