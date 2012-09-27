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
package org.dwfa.ace.path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

public class SelectPathAndPositionPanel extends JTabbedPane {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public SelectPathAndPositionPanel(boolean selectPositionOnly, String purpose, I_ConfigAceFrame aceConfig,
            PropertySetListenerGlue selectGlue) throws Exception {
        super();
        List<TimePathId> timePathEntries = Terms.get().getTimePathList();
        for (PathBI p : Terms.get().getPaths()) {
            I_GetConceptData cb = Terms.get().getConcept(p.getConceptNid());
            int modTimeCount = 0;
            for (TimePathId tp : timePathEntries) {
                if (tp.getPathId() == p.getConceptNid()) {
                    modTimeCount++;
                }
            }
            PositionPanel pp = new PositionPanel(p, selectPositionOnly, purpose, cb.getInitialText(), aceConfig,
                selectGlue);
            addTab(cb.getInitialText(), pp);
        }
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public List<PositionBI> getSelectedPositions() throws TerminologyException, IOException {
        List<PositionBI> positions = new ArrayList<PositionBI>();
        for (int i = 0; i < getTabCount(); i++) {
            PositionPanel pp = (PositionPanel) getComponentAt(i);
            if (pp.isPositionSelected()) {
                positions.add(pp.getPosition());
            }
        }
        return positions;
    }

}
