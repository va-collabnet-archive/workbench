package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.arena.conceptview.ConceptViewSettings.SIDE;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.Comment;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.project.workflow.model.WfComment;
import org.ihtsdo.tk.workflow.api.WfActivityInstanceBI;
import org.ihtsdo.tk.workflow.api.WfCommentBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;

public class InstanceActivitiesPanel extends JPanel {

	private final JScrollPane historyScroller;
	private final ConceptView view;
	private SIDE side;
	private JPanel panel;
	private JTree tree;
	private TreeModel model;
	public I_GetConceptData concept;
	private JScrollPane scrollPane;
	private JPopupMenu popupMenu;
	private WorkListBI worklist;
	private JMenuItem mntmAddComment;

	public InstanceActivitiesPanel(I_ConfigAceFrame config, ConceptView view) {
		initComponents();
		this.historyScroller = new JScrollPane(new JLabel("History panel"));
		this.historyScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.historyScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		this.historyScroller.setVisible(false);
		this.view = view;

		ConceptChangeListener ccl = new ConceptChangeListener();
		this.view.addHostListener(ccl);
	}

	private void initComponents() {
		this.setSize(400, 400);
		this.setPreferredSize(new Dimension(400, 400));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 82, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		panel.setBackground(ConceptViewTitle.TITLE_COLOR);
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);

		JLabel title = new JLabel("Workflow history");
		panel.add(title);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		tree = new JTree();

		scrollPane.setViewportView(tree);
		tree.setCellRenderer(new MyRenderer());

		popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);

		mntmAddComment = new JMenuItem("Add comment");
		mntmAddComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (worklist != null) {
					WorkflowStore wfs = new WorkflowStore();
					try {
						String comment = JOptionPane.showInputDialog(InstanceActivitiesPanel.this, "", "Comment", JOptionPane.PLAIN_MESSAGE);
						if (comment != null && !comment.equals("")) {
							wfs.getProcessInstance(worklist, concept.getPrimUuid()).addComment(new WfComment(comment));
							updateActivityInstances(concept);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		popupMenu.add(mntmAddComment);
	}

	public void setDropSide(SIDE side) {
		if (this.side != side) {
			this.side = side;
		}
	}

	// ~--- inner classes
	// -------------------------------------------------------
	public class ConceptChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent pce) {
			if (concept != null && pce.getNewValue() != null) {
				if (!pce.getNewValue().equals(concept)) {
					concept = (I_GetConceptData) pce.getNewValue();
					updateActivityInstances(concept);
				}
			}
		}
	}

	public void updateActivityInstances(I_GetConceptData concept) {
		this.concept = concept;
		try {
			WorkflowStore ws = new WorkflowStore();
			Collection<WfProcessInstanceBI> instances = ws.getProcessInstances(concept);
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(concept.toUserString());
			model = new DefaultTreeModel(root);
			tree.setModel(model);
			if (instances != null && !instances.isEmpty()) {
				HashMap<String, DefaultMutableTreeNode> wlNodes = new HashMap<String, DefaultMutableTreeNode>();
				for (WfProcessInstanceBI wfProcessInstanceBI : instances) {
					WorkListBI wl = wfProcessInstanceBI.getWorkList();
					Collection<WfCommentBI> comments = wfProcessInstanceBI.getComments();
					if (!wlNodes.containsKey(wl.getName())) {
						DefaultMutableTreeNode wlNode = new DefaultMutableTreeNode(wl);
						root.add(wlNode);
						wlNodes.put(wl.getName(), wlNode);
					}
					LinkedList<WfActivityInstanceBI> activities = wfProcessInstanceBI.getActivityInstances();
					long lastAdded = Long.MIN_VALUE;
					List<WfCommentBI> comentsAcumul =  new ArrayList<WfCommentBI>();
					for (WfActivityInstanceBI wfActivityInstanceBI : activities) {
						comentsAcumul =  new ArrayList<WfCommentBI>();
						for (WfCommentBI comment : comments) {
							if (comment.getDate() > lastAdded) {
								if (wfActivityInstanceBI.getTime() > comment.getDate()) {
									wlNodes.get(wl.getName()).add(new DefaultMutableTreeNode(comment));
									lastAdded = comment.getDate();
								} else {
									comentsAcumul.add(comment);
								}
							}
						}
						wlNodes.get(wl.getName()).add(new DefaultMutableTreeNode(wfActivityInstanceBI));
					}
					for (WfCommentBI wfCommentBI : comentsAcumul) {
						wlNodes.get(wl.getName()).add(new DefaultMutableTreeNode(wfCommentBI));
					}
				}
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
			} else {
				model = new DefaultTreeModel(new DefaultMutableTreeNode("No process instances"));
				tree.setModel(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (isWorklist(value)) {
				setIcon(new ImageIcon("icons/table.png"));
				setToolTipText(value.toString());
			} else if (isComment(value)) {
				setIcon(new ImageIcon("icons/message.png"));
				setToolTipText(value.toString());

			} else {
				setIcon(new ImageIcon("icons/element_next.png"));
				setToolTipText(value.toString());
			}

			return this;
		}

		private boolean isComment(Object value) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object nodeInfo = (Object) (node.getUserObject());
			if (nodeInfo instanceof WfCommentBI) {
				return true;
			}
			return false;
		}

		protected boolean isWorklist(Object value) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object nodeInfo = (Object) (node.getUserObject());
			if (nodeInfo instanceof WorkList) {
				return true;
			}
			return false;
		}
	}

	private void addPopup(JTree component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				try {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						tree.setSelectionPath(path);
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
						if (selectedNode.getUserObject() instanceof WorkListBI) {
							worklist = (WorkListBI) selectedNode.getUserObject();
							popup.show(e.getComponent(), e.getX(), e.getY());
						}

					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}
}
