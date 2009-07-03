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

	public SelectPathAndPositionPanel(boolean selectPositionOnly, String purpose, 
			I_ConfigAceFrame aceConfig, PropertySetListenerGlue selectGlue) throws Exception {
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
