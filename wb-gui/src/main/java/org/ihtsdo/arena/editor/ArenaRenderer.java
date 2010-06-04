package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.EnumMap;
import org.ihtsdo.arena.editor.ArenaEditor.CELL_ATTRIBUTES;
import org.ihtsdo.arena.editor.ArenaEditor.COMPONENT_KIND;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

/**
 * @author Administrator
 * 
 */
public class ArenaRenderer extends JComponent
{

    /**
     * 
     */
    private static final long serialVersionUID = 2106746763664760745L;

    /**
     * 
     */
    public static final String IMAGE_PATH = "/com/mxgraph/examples/swing/images/";

    /**
     * 
     */
    protected static ArenaRenderer dragSource = null;

    /**
     * 
     */
    protected static int sourceRow = 0;

    /**
     * 
     */
    protected mxCell cell;

    /**
     * 
     */
    protected mxGraphComponent graphContainer;

    /**
     * 
     */
    protected mxGraph graph;

    /**
     * 
     */
    public JComponent renderedComponent;

	private JLabel resizeLabel;
	
	private EnumMap dataMap;

	private I_HostConceptPlugins host;
	private I_GetConceptData concept;

	private JLabel label;
	
	private ACE ace;

	private JButton goToLinkButton;
	
    /**
     * 
     */
    @SuppressWarnings("serial")
    public ArenaRenderer(Object cellObj,
            final mxGraphComponent graphContainer, ACE ace)
    {
    	this.ace = ace;
        this.cell = (mxCell) cellObj;
        this.graphContainer = graphContainer;
        this.graph = graphContainer.getGraph();
        this.dataMap = (EnumMap) this.cell.getValue();
        if (this.dataMap != null && dataMap.get(CELL_ATTRIBUTES.LINKED_TAB) != null) {
        	Integer linkedTab = (Integer) dataMap.get(CELL_ATTRIBUTES.LINKED_TAB);
            try {
            	host = Terms.get().getActiveAceFrameConfig().getConceptViewer(linkedTab);
            	concept = (I_GetConceptData) host.getTermComponent();
            	host.addPropertyChangeListener("termComponent", new HostListener());
			} catch (TerminologyException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
        }
        setLayout(new BorderLayout());

        JPanel title = new JPanel();
        title.setBackground(new Color(149, 173, 239));
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
        title.setLayout(new BorderLayout());

        goToLinkButton = new JButton(new AbstractAction("", new ImageIcon(ArenaRenderer.class
                .getResource("/16x16/plain/pin_green.png"))) {

					@Override
					public void actionPerformed(ActionEvent e) {
						Integer linkedTab = -1;
						if (dataMap != null ) {
							linkedTab = (Integer) dataMap.get(CELL_ATTRIBUTES.LINKED_TAB);
						}
						if (linkedTab != null && linkedTab != -1) {
							try {
								Terms.get().getActiveAceFrameConfig().selectConceptViewer(linkedTab);
							} catch (Exception e1) {
								AceLog.getAppLog().alertAndLogException(e1);
							} 
						} else {
							goToLinkButton.setVisible(false);
						}
					}
        	
        });
        Integer linkedTab = -1;
    	if (dataMap != null ) {
			linkedTab = (Integer) dataMap.get(CELL_ATTRIBUTES.LINKED_TAB);
			if (linkedTab != null && linkedTab != -1) {
				goToLinkButton.setVisible(true);
			} else {
				goToLinkButton.setVisible(false);
			}
		}
    	
    	
        goToLinkButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 1));
        title.add(goToLinkButton, BorderLayout.WEST);

        label = new JLabel(String.valueOf(graph.getLabel(cell)));
        label.setForeground(Color.WHITE);
        label.setFont(title.getFont().deriveFont(Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        title.add(label, BorderLayout.CENTER);

        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);
        JButton button = new JButton(new AbstractAction("", new ImageIcon(
                ArenaRenderer.class.getResource(IMAGE_PATH + "minimize.gif")))
        {

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
                                ArenaRenderer.class
                                        .getResource(IMAGE_PATH
                                                + ((graph.isCellCollapsed(cell)) ? "maximize.gif"
                                                        : "minimize.gif"))));
            }
        });
        button.setPreferredSize(new Dimension(16, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Collapse/Expand");
        button.setOpaque(false);
        toolBar2.add(button);

        title.add(toolBar2, BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = null;
        if (graph.getModel().getChildCount(cell) == 0)  {
        	
        	if (dataMap.get(CELL_ATTRIBUTES.COMPONENT_KIND) == COMPONENT_KIND.TAXONOMY) {
        		TermTreeHelper hierarchicalTreeHelper =
                       new TermTreeHelper(ace.getAceFrameConfig(), ace);
        		try {
					renderedComponent = hierarchicalTreeHelper.getHierarchyPanel();
					add(renderedComponent, BorderLayout.CENTER);
					label.setText("   taxonomy");
				} catch (TerminologyException e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				} catch (IOException e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				}
        	} else {
                renderedComponent = new ArenaConceptView(ace.getAceFrameConfig());
                scrollPane = new JScrollPane(renderedComponent);
        	}
        }

		if (scrollPane != null) {
            add(scrollPane, BorderLayout.CENTER);
            scrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            scrollPane.getViewport().setBackground(Color.WHITE);
            setOpaque(true);
			scrollPane.getVerticalScrollBar().addAdjustmentListener(
					new AdjustmentListener() {

						public void adjustmentValueChanged(AdjustmentEvent e) {
							graphContainer.refresh();
						}

					});
		}

        resizeLabel = new JLabel(new ImageIcon(ArenaRenderer.class
                .getResource(IMAGE_PATH + "resize.gif")));
        resizeLabel.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(resizeLabel, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        ResizeHandler resizeHandler = new ResizeHandler();
        resizeLabel.addMouseListener(resizeHandler);
        resizeLabel.addMouseMotionListener(resizeHandler);

        setMinimumSize(new Dimension(40, 20));
        updateLabel();
    }
    
    private void updateLabel() {
    	if (concept != null) {
    		label.setText(concept.toString());
    		if (ArenaConceptView.class.isAssignableFrom(renderedComponent.getClass())) {
    			ArenaConceptView acv = (ArenaConceptView) renderedComponent;
    			acv.layoutConcept(concept);
    		}
    	}
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


    /**
     * 
     */
    public static ArenaRenderer getVertex(Component component)
    {
        while (component != null)
        {
            if (component instanceof ArenaRenderer)
            {
                return (ArenaRenderer) component;
            }
            component = component.getParent();
        }

        return null;
    }

    private class HostListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			concept = (I_GetConceptData) host.getTermComponent();
			updateLabel();
		}
    	
    }
 }
