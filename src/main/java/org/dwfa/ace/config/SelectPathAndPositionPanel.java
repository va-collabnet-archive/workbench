package org.dwfa.ace.config;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.TimePathId;

public class SelectPathAndPositionPanel extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectPathAndPositionPanel(boolean selectPositionOnly, String purpose, AceFrameConfig aceConfig, PropertySetListenerGlue selectGlue) throws Exception {
		super();
		List<TimePathId> timePathEntries = AceConfig.vodb.getTimePathList();
		for (Path p: AceConfig.vodb.getPaths()) {
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

	public List<Position> getSelectedPositions() {
		List<Position> positions = new ArrayList<Position>();
		for (int i = 0; i < getTabCount(); i++) {
			PositionPanel pp = (PositionPanel) getComponentAt(i);
			if (pp.isPositionSelected()) {
				positions.add(pp.getPosition());
			}
		}
		return positions;
	}

}
