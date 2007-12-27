package org.dwfa.ace.tree;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.vodb.types.ConceptBean;

public interface I_GetConceptDataForTree extends I_GetConceptData {
	public boolean isParentOpened();
	public void setParentOpened(boolean opened);
	public int getParentDepth();
	public boolean isSecondaryParentNode();
	public ConceptBean getCoreBean();
	public List<DefaultMutableTreeNode> getExtraParentNodes();
	public I_DescriptionTuple getDescTuple(I_ConfigAceFrame aceConfig) throws IOException;
	public int getRelId();
}
