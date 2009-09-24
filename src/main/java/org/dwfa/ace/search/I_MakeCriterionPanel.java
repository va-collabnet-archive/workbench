package org.dwfa.ace.search;

import java.util.List;

public interface I_MakeCriterionPanel {

	public void layoutCriterion();
	
	public List<CriterionPanel> getCriterionPanels();
	
	public CriterionPanel makeCriterionPanel() throws ClassNotFoundException,
													  InstantiationException, 
													  IllegalAccessException;
}
