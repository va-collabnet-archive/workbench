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
package org.dwfa.ace.tree;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.QueryBean;
import org.dwfa.ace.search.SimilarConceptQuery;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;

public class TreeMouseListener implements MouseListener {

    public class SetSearchToSimilar implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            I_GetConceptData selectedConcept =
                    ace.getAceFrameConfig().getHierarchySelection();
            try {
                QueryBean qb =
                        SimilarConceptQuery.make(selectedConcept, ace
                            .getAceFrameConfig());
                ace.getSearchPanel().setQuery(qb);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (ClassNotFoundException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (InstantiationException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (IllegalAccessException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private ACE ace;

    public TreeMouseListener(ACE ace) {
        super();
        this.ace = ace;
    }

    public void mousePressed(MouseEvent e) {
        JTree tree = (JTree) e.getSource();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        // AceLog.getLog().info("Selected row: " + selRow);
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null) {
            if (selRow != -1) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) selPath.getLastPathComponent();
                I_RenderAndFocusOnBean renderer =
                        (I_RenderAndFocusOnBean) tree.getCellRenderer();
                I_GetConceptDataForTree treeBean =
                        (I_GetConceptDataForTree) node.getUserObject();
                renderer =
                        (TermTreeCellRenderer) renderer
                            .getTreeCellRendererComponent(tree, node, true,
                                tree.isExpanded(selRow), node.isLeaf(), selRow,
                                true);
                Rectangle bounds = tree.getRowBounds(selRow);
                if (e.getClickCount() == 1) {
                    Rectangle iconBounds =
                            renderer.getIconRect(treeBean.getParentDepth());

                    if ((e.getPoint().x > bounds.x + iconBounds.x)
                        && (e.getPoint().x + 1 < bounds.x + iconBounds.x
                            + iconBounds.width)) {
                        openOrCloseParent(tree, model, node, treeBean, bounds);
                    }
                } else if (e.getClickCount() == 2) {
                    openOrCloseParent(tree, model, node, treeBean, bounds);
                }
                // tree.setSelectionPath(new TreePath(selPath.getPath()));
                int newRow = tree.getRowForPath(selPath);
                // AceLog.getLog().info("New row: " + newRow);
                tree.setSelectionInterval(newRow, newRow);

                if (e.isPopupTrigger()) {
                    makeAndShowPopup(e);
                }
            }
        }
    }

    private void makeAndShowPopup(MouseEvent e) {
        JPopupMenu popup;
        try {
            popup = makePopup(e);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private JPopupMenu makePopup(MouseEvent e) throws Exception {
        JPopupMenu popup = new JPopupMenu();
        //        JMenuItem noActionItem = new JMenuItem("");
        //        popup.add(noActionItem);

        if (ace.getRefsetSpecInSpecEditor() != null
            && ace.refsetTabIsSelected()) {
            JTree specTree = ace.getTreeInSpecEditor();
            if (specTree.isVisible() && specTree.getSelectionCount() > 0) {
                TreePath selPath = specTree.getSelectionPath();
                if (selPath != null) {
                    DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) selPath
                                .getLastPathComponent();
                    I_ThinExtByRefVersioned specPart =
                            (I_ThinExtByRefVersioned) node.getUserObject();
                    switch (ThinExtBinder.getExtensionType(specPart)) {
                    case CONCEPT_CONCEPT:
                        addRefsetItems(popup, new File(AceFrame.pluginRoot,
                            "refsetspec/branch-popup"), specPart);
                        popup.addSeparator();
                        break;
                    default:
                    }
                }
            }
        }

        JMenuItem searchForSimilarConcepts =
                new JMenuItem("Search for similar concepts...");
        popup.add(searchForSimilarConcepts);
        searchForSimilarConcepts.addActionListener(new SetSearchToSimilar());
        popup.addSeparator();

        ProcessPopupUtil.addSubMenuItems(popup, new File(AceFrame.pluginRoot,
            "taxonomy"), ace.getAceFrameConfig());

        String userDir =
                ace.getAceFrameConfig().getDbConfig().getProfileFile()
                    .getParentFile().getCanonicalPath();
        JMenu userMenu = new JMenu("User Defined");
        ProcessPopupUtil.addSubMenuItems(userMenu,
            new File(userDir, "process"), ace.getAceFrameConfig());
        if (userMenu.getItemCount() > 0) {
            popup.add(userMenu);
        }

        return popup;
    }

    private void addRefsetItems(JPopupMenu popup, File directory,
            I_ThinExtByRefVersioned specPart) throws TerminologyException {
        ProcessPopupUtil.addSubMenuItems(popup, directory, ace
            .getAceFrameConfig().getWorker(), null);
    }

    private void openOrCloseParent(JTree tree, DefaultTreeModel model,
			DefaultMutableTreeNode node, I_GetConceptDataForTree treeBean,
			Rectangle bounds) {
		boolean addNodes = !treeBean.isParentOpened();

		treeBean.setParentOpened(addNodes);
		tree.paintImmediately(bounds);
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node
				.getParent();
		I_GetConceptDataForTree parentBean = (I_GetConceptDataForTree) parentNode
				.getUserObject();
		if (parentBean == null) {
			return;
		}
		if (addNodes) {
			ace.getAceFrameConfig().getParentExpandedNodes().add(treeBean.getConceptId());
			List<I_RelTuple> tuples;
			try {
				tuples = treeBean.getSourceRelTuples(ace.getAceFrameConfig()
						.getAllowedStatus(), ace.getAceFrameConfig().getDestRelTypes(),
						ace.getAceFrameConfig().getViewPositionSet(), false);
				int[] newNodeIndices = new int[tuples.size()];
				int index = 0;
				int insertIndex = parentNode.getIndex(node);
				for (I_RelTuple t : tuples) {
					newNodeIndices[index++] = insertIndex;

					if (t.getC2Id() == parentBean.getConceptId()
							&& treeBean.getParentDepth() == 0) {
						AceLog.getAppLog().info(
								" parent depth: " + treeBean.getParentDepth());
						continue;
					}

					ConceptBeanForTree extraParentBean = ConceptBeanForTree
							.get(t.getC2Id(), t.getRelId(), treeBean
									.getParentDepth() + 1, true, ace.getAceFrameConfig());
					DefaultMutableTreeNode extraParentNode = new DefaultMutableTreeNode(
							extraParentBean);
					extraParentNode.setAllowsChildren(false);
					parentNode.insert(extraParentNode, insertIndex++);
					treeBean.getExtraParentNodes().add(extraParentNode);
				}
				model.nodesWereInserted(parentNode, newNodeIndices);

			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else { // remove nodes
			removeAllExtraParents(model, treeBean, parentNode);
			ace.getAceFrameConfig().getParentExpandedNodes().remove(treeBean.getConceptId());
		}
		model.nodeStructureChanged(parentNode);
	}

    private void removeAllExtraParents(DefaultTreeModel model,
			I_GetConceptDataForTree treeBean, DefaultMutableTreeNode parentNode) {
		for (DefaultMutableTreeNode extraParentNode : treeBean
				.getExtraParentNodes()) {
			removeAllExtraParents(model,
					(I_GetConceptDataForTree) extraParentNode.getUserObject(),
					parentNode);
			int extraParentIndex = parentNode.getIndex(extraParentNode);
			parentNode.remove(extraParentIndex);
			model.nodesWereRemoved(parentNode, new int[] { extraParentIndex },
					new Object[] { extraParentNode });
		}
		treeBean.getExtraParentNodes().clear();
	}

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            makeAndShowPopup(e);
        }

    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

}
