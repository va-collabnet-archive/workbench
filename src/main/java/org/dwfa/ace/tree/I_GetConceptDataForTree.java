package org.dwfa.ace.tree;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_GetConceptData;
import org.dwfa.vodb.types.ThinDescTuple;

import com.sleepycat.je.DatabaseException;

public interface I_GetConceptDataForTree extends I_GetConceptData {
	public boolean isParentOpened();
	public void setParentOpened(boolean opened);
	public int getParentDepth();
	public boolean isSecondaryParentNode();
	public ConceptBean getCoreBean();
	public List<DefaultMutableTreeNode> getExtraParentNodes();
	public ThinDescTuple getDescTuple(AceFrameConfig aceConfig) throws DatabaseException;
}
