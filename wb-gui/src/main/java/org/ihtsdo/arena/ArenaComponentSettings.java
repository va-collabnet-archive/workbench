package org.ihtsdo.arena;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.ihtsdo.arena.conceptview.ConceptViewRenderer;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

public abstract class ArenaComponentSettings implements Serializable,
		ComponentListener, HierarchyBoundsListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
	   /**
     * 
     */
    public static final String IMAGE_PATH = "/com/mxgraph/examples/swing/images/";


	// dataVersion = 1;
	private boolean visible = true;
	private mxRectangle bounds = new mxRectangle();
	private mxRectangle alternateBounds = new mxRectangle();
	private float fontSize = 9;

	// transient
	protected ACE ace;
    protected mxCell cell;
    protected mxGraph graph;
    protected JLabel resizeLabel;
    protected mxGraphComponent graphContainer;
    protected JComponent preferences;
    protected JComponent renderer;
	private DefaultMutableTreeNode prefRoot;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeBoolean(visible);
		out.writeObject(bounds);
		out.writeObject(alternateBounds);
		out.writeFloat(fontSize);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			visible = in.readBoolean();
			bounds = (mxRectangle) in.readObject();
			alternateBounds = (mxRectangle) in.readObject();
			fontSize = in.readFloat();
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	
	public DefaultMutableTreeNode getPrefRoot() {
		return prefRoot;
	}

	public I_ConfigAceFrame getConfig() {
		if (ace != null) {
			return ace.getAceFrameConfig();
		}
		return null;
	}

	public void setup(ACE ace, mxCell cell, mxGraphComponent graphContainer, 
			mxGraph graph, JComponent renderer) {
		this.ace = ace;
		this.cell = cell;
		this.graphContainer = graphContainer;
		this.graph = graph;
		this.renderer = renderer;
		this.prefRoot = new DefaultMutableTreeNode("PrefRoot");
		SpinnerModel fontSizeModel =
	        new SpinnerNumberModel(fontSize, //initial value
	                               9, //min
	                               20, //max
	                               1);                //step
		JSpinner fontSizeSpinner = new JSpinner(fontSizeModel);
		JPanel fontSizePanel  = new JPanel(new GridLayout(1, 1));
		fontSizePanel.add(fontSizeSpinner);
		
		this.prefRoot.add(new PreferencesNode("Font Size", fontSizePanel));
		setupSubtypes();
	}

	protected abstract void setupSubtypes();

	@Override
	public void componentHidden(ComponentEvent e) {
		visible = false;
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		handleMoved();
	}

	private void handleMoved() {
		bounds = new mxRectangle(renderer.getBounds());
		if (preferences != null) {
			setPrefLocation();
		}
	}

	
	@Override
	public void ancestorMoved(HierarchyEvent e) {
		handleMoved();
	}

	@Override
	public void ancestorResized(HierarchyEvent e) {
		handleMoved();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		handleMoved();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		visible = true;
	}

	protected abstract JComponent makeComponent(I_ConfigAceFrame config);

	public abstract String getTitle();
	
	public abstract I_GetConceptData getConcept();


	public JComponent getComponent(I_ConfigAceFrame config) {
		JComponent c = makeComponent(config);
		c.addComponentListener(this);
		c.addHierarchyBoundsListener(this);
		return c;
	}

	public void setBounds(mxRectangle bounds) {
		this.bounds = bounds;
	}

	public void setAlternateBounds(mxRectangle alternateBounds) {
		this.alternateBounds = alternateBounds;
	}

	public mxRectangle getAlternateBounds() {
		return alternateBounds;
	}

	public mxRectangle getBounds() {
		return bounds;
	}

	public abstract I_HostConceptPlugins getHost();

	public void addHostListener(PropertyChangeListener l) {
		if (getHost() != null) {
			getHost().addPropertyChangeListener("termComponent", l);
		}
	}
	
	public abstract JComponent getLinkComponent();

	@Override
	public String toString() {
		return getTitle();
	}
	
	public abstract List<AbstractButton> getSpecializedButtons();


	public List<AbstractButton> getButtons() {
		List<AbstractButton> buttons = getSpecializedButtons();
		buttons.add(getButtonPreferencesButton());
		buttons.add(getCollapseExpandButton());
		return buttons;
	}
	
	protected JComponent getPreferences() {
		if (preferences == null) {
			preferences = new ArenaPreferencesPanel(this);
			preferences.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
			preferences.setOpaque(true);
			preferences.setBounds(0, 0, 450, 20);
		}
		return preferences;
	}
	
	protected AbstractButton getButtonPreferencesButton() {
		AbstractButton button = new JToggleButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/preferences.png")))
        {

        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean showPreferences = false;
        	
            public void actionPerformed(ActionEvent e) {
            	showPreferences = !showPreferences;
           		JLayeredPane layers = renderer.getRootPane().getLayeredPane();
             	if (showPreferences) {
            		setPrefLocation();
            		getPreferences().setVisible(true);
            	} else {
            		getPreferences().setVisible(false);
            		getPreferences().invalidate();
             		layers.remove(preferences);
            	}
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("show concept viewer prefs");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		return button;
	}

	
	
	protected AbstractButton getButton(String iconLocStr, String toolTipStr) {
		AbstractButton button = new JToggleButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource(iconLocStr)))
        {

        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean collapsed = false;
        	
            public void actionPerformed(ActionEvent e)
            {

            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText(toolTipStr);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		return button;
	}
	
	public JLabel getResizeLabel() {
		if (resizeLabel == null) {
	        resizeLabel = new JLabel(new ImageIcon(ACE.class.getResource("/16x16/plain/resize.gif")));
	        resizeLabel.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
	        ResizeHandler resizeHandler = new ResizeHandler();
	        resizeLabel.addMouseListener(resizeHandler);
	        resizeLabel.addMouseMotionListener(resizeHandler);
		}
		return resizeLabel;
		
	}

	private void setPrefLocation() {
   		JLayeredPane layers = renderer.getRootPane().getLayeredPane();
		Point loc = SwingUtilities.convertPoint(renderer, new Point(0,0), layers);
		if (layers.getWidth() > loc.x + renderer.getWidth() + getPreferences().getWidth()) {
			loc.x = loc.x + renderer.getWidth();
			getPreferences().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
		} else {
			loc.x = loc.x - getPreferences().getWidth();
			getPreferences().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
		}
		getPreferences().setBounds(loc.x, loc.y, getPreferences().getWidth(), renderer.getHeight());
		layers.add(getPreferences(), JLayeredPane.PALETTE_LAYER);
	}

	private JButton getCollapseExpandButton() {
		JButton button = new JButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource(IMAGE_PATH + "minimize.gif")))
        {

        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean collapsed = false;
        	
            public void actionPerformed(ActionEvent e)
            {
            	collapsed = !collapsed;
            	resizeLabel.setVisible(!collapsed);
            	if (collapsed) {
                	Rectangle bounds = cell.getGeometry().getRectangle();
                	cell.getGeometry().setAlternateBounds(new mxRectangle(0, 0, bounds.getWidth(), 20));
            	}
                graph.foldCells(collapsed, false, new Object[] { cell });
                ((JButton) e.getSource())
                        .setIcon(new ImageIcon(
                                ConceptViewRenderer.class
                                        .getResource(IMAGE_PATH
                                                + ((graph.isCellCollapsed(cell)) ? "maximize.gif"
                                                        : "minimize.gif"))));
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Collapse/Expand");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
		return button;
	}
	
	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}


    
    /**
     * Implements an event redirector for the specified handle index, where 0
     * is the top right, and 1-7 are the top center, rop right, middle left,
     * middle right, bottom left, bottom center and bottom right, respectively.
     * Default index is 7 (bottom right).
     */
    public class ResizeHandler implements MouseListener, MouseMotionListener
    {

        protected int index;

        public ResizeHandler()
        {
            this(7);
        }

        public ResizeHandler(int index)
        {
            this.index = index;
        }

        public void mouseClicked(MouseEvent e)
        {
            // ignore
        }

        public void mouseEntered(MouseEvent e)
        {
            // ignore
        }

        public void mouseExited(MouseEvent e)
        {
            // ignore
        }

        public void mousePressed(MouseEvent e)
        {
            // Selects to create a handler for resizing
            if (!graph.isCellSelected(cell))
            {
                graphContainer.selectCellForEvent(cell, e);
            }

            // Initiates a resize event in the handler
            mxCellHandler handler = graphContainer.getSubHandler().getHandler(cell);

            if (handler != null)
            {
                // Starts the resize at index 7 (bottom right)
                handler.start(SwingUtilities.convertMouseEvent((Component) e
                        .getSource(), e, graphContainer.getGraphControl()),
                        index);
                e.consume();
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            graphContainer.getGraphControl().dispatchEvent(
                    SwingUtilities.convertMouseEvent((Component) e.getSource(),
                            e, graphContainer.getGraphControl()));
        }

        public void mouseDragged(MouseEvent e)
        {
            graphContainer.getGraphControl().dispatchEvent(
                    SwingUtilities.convertMouseEvent((Component) e.getSource(),
                            e, graphContainer.getGraphControl()));
        }

        public void mouseMoved(MouseEvent e)
        {
            // ignore
        }
    }

}
