package org.dwfa.ace.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public class TermTreeCellRenderer extends DefaultTreeCellRenderer {

	private static ImageIcon multiParentClosed = new ImageIcon(ACE.class
			.getResource("/16x16/plain/nav_up_green.png"));

	private static ImageIcon multiParentOpen = new ImageIcon(ACE.class
			.getResource("/16x16/plain/nav_up_right_green.png"));

	private static ImageIcon focusMultiParentOpen = new ImageIcon(ACE.class
			.getResource("/16x16/plain/nav_up_right_blue.png"));

	private static ImageIcon multiParentRoot = new ImageIcon(ACE.class
			.getResource("/16x16/plain/pin_green.png"));

	private AceFrameConfig aceConfig;
	
	private boolean drawsFocusBorderAroundIcon = false;

	private boolean drawDashedFocusIndicator;
    // If drawDashedFocusIndicator is true, the following are used.
    /**
     * Background color of the tree.
     */
    private Color treeBGColor;
    /**
     * Color to draw the focus indicator in, determined from the background.
     * color.
     */
    private Color focusBGColor;
    
    private ConceptBean focusBean;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TermTreeCellRenderer(AceFrameConfig aceConfig) {
		super();
		this.aceConfig = aceConfig;
		setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
		Object value = UIManager.get("Tree.drawDashedFocusIndicator");
		drawDashedFocusIndicator = (value != null && ((Boolean)value).
					    booleanValue());
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		try {
			if (node.getUserObject() != null) {
				if (I_GetConceptDataForTree.class.isAssignableFrom(node
						.getUserObject().getClass())) {
					I_GetConceptDataForTree cb = (I_GetConceptDataForTree) node
							.getUserObject();
					Rectangle iconRect = getIconRect(cb.getParentDepth());
					I_DescriptionTuple tdt = cb.getDescTuple(aceConfig);
					if (tdt != null) {
						setText(cb.getDescTuple(aceConfig).getText());
					} else {
						setText("null desc: " + cb.getInitialText());
					}
					;
					int sourceRelTupleSize = cb.getSourceRelTuples(
							aceConfig.getAllowedStatus(),
							aceConfig.getDestRelTypes(),
							aceConfig.getViewPositionSet(), false).size();
					if (sourceRelTupleSize > 1) {
						if (cb.isParentOpened()) {
							this.setIcon(multiParentOpen);
						} else {
							this.setIcon(multiParentClosed);
						}
						this.setBorder(BorderFactory.createMatteBorder(0,
								iconRect.x, 0, 0,
								getBackgroundNonSelectionColor()));
						// this.setBorder(BorderFactory.createMatteBorder(0,
						// indent, 0, 0, multiParentClosed));

					} else {
						if (cb.isSecondaryParentNode()) {
							this.setBorder(BorderFactory.createMatteBorder(0,
									iconRect.x, 0, 0,
									getBackgroundNonSelectionColor()));
							if (sourceRelTupleSize == 0) {
								this.setIcon(multiParentRoot);
							} else {
								if (cb.isParentOpened()) {
									if (focusBean != null && cb.getConceptId() == focusBean.getConceptId()) {
										this.setIcon(focusMultiParentOpen);
									} else {
										this.setIcon(multiParentOpen);
									}
								} else {
									if (focusBean != null && cb.getConceptId() == focusBean.getConceptId()) {
										this.setIcon(focusMultiParentOpen);
									} else {
										this.setIcon(multiParentClosed);
									}
								}
							}
						} else {
							this.setIcon(null);
						}
					}
				} else {
					this.setIcon(null);
					this.setText(node.getUserObject().toString());
				}
			} else {
				this.setText("ROOT? (User object is null)");
			}

		} catch (DatabaseException e) {
			this.setText(e.toString());
			e.printStackTrace();
		}
		return this;
	}

	public Rectangle getIconRect(int parentDepth) {
		int indent = (multiParentOpen.getIconWidth() * parentDepth);
		if (indent > 0) {
			indent = indent + 4;
		}
		return new Rectangle(indent, 0, multiParentOpen.getIconWidth(),
				multiParentOpen.getIconHeight());
	}

	/**
	 * Paints the value. The background is filled based on selected.
	 */
	public void paint(Graphics g) {
		Color bColor;

		if (selected) {
			bColor = getBackgroundSelectionColor();
		} else {
			bColor = getBackgroundNonSelectionColor();
			if (bColor == null)
				bColor = getBackground();
		}
		int imageOffset = -1;
		if (bColor != null) {

			imageOffset = getLabelStart();
			g.setColor(bColor);
			if (getComponentOrientation().isLeftToRight()) {
				g.fillRect(imageOffset, 0, getWidth() - imageOffset,
						getHeight());
			} else {
				g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
			}
		}

		if (hasFocus) {
			if (drawsFocusBorderAroundIcon) {
				imageOffset = 0;
			} else if (imageOffset == -1) {
				imageOffset = getLabelStart();
			}
			if (getComponentOrientation().isLeftToRight()) {
				paintFocus(g, imageOffset, 0, getWidth() - imageOffset,
						getHeight());
			} else {
				paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight());
			}
		}
		super.paint(g);
	}

	private void paintFocus(Graphics g, int x, int y, int w, int h) {
		Color bsColor = getBorderSelectionColor();

		if (bsColor != null && (selected || !drawDashedFocusIndicator)) {
			g.setColor(bsColor);
			g.drawRect(x, y, w - 1, h - 1);
		}
		if (drawDashedFocusIndicator) {
			Color color;
			if (selected) {
				color = getBackgroundSelectionColor();
			} else {
				color = getBackgroundNonSelectionColor();
				if (color == null) {
					color = getBackground();
				}
			}

			if (treeBGColor != color) {
				treeBGColor = color;
				focusBGColor = new Color(~color.getRGB());
			}
			g.setColor(focusBGColor);
			BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
		}
	}

	private int getLabelStart() {
		Icon currentI = getIcon();
		if (currentI != null && getText() != null) {
			if (getBorder() != null) {
				return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1) + getBorder().getBorderInsets(this).left;
			}
			return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
		}
		return 0;
	}

	public ConceptBean getFocusBean() {
		return focusBean;
	}

	public void setFocusBean(ConceptBean focusBean) {
		this.focusBean = focusBean;
	}

}
