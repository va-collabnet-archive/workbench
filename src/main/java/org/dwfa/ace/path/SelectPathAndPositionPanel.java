package org.dwfa.ace.path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.tapi.TerminologyException;

public class SelectPathAndPositionPanel extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectPathAndPositionPanel(boolean selectPositionOnly, String purpose, 
			I_ConfigAceFrame aceConfig, PropertySetListenerGlue selectGlue) throws Exception {
		super();
		List<TimePathId> timePathEntries = LocalVersionedTerminology.get().getTimePathList();
		for (I_Path p: LocalVersionedTerminology.get().getPaths()) {
			I_GetConceptData cb = LocalVersionedTerminology.get().getConcept(p.getConceptId());
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
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	public List<I_Position> getSelectedPositions() throws TerminologyException, IOException {
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