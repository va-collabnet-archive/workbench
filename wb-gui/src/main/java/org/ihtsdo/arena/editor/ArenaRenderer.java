package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.ace.ACE;
import org.ihtsdo.arena.ArenaComponentSettings;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
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

	private ArenaComponentSettings settings;

	private JLabel label;
		
    /**
     * 
     */
    public ArenaRenderer(Object cellObj,
            final mxGraphComponent graphContainer, ACE ace)
    {
        this.cell = (mxCell) cellObj;
        this.graphContainer = graphContainer;
        this.graph = graphContainer.getGraph();
        this.settings = (ArenaComponentSettings) this.cell.getValue();
        this.settings.setup(ace, cell, graphContainer, graph, this);
        this.settings.addHostListener(new HostListener());
        setLayout(new BorderLayout());

        JPanel title = new JPanel();
        title.setBackground(new Color(255, 213, 162));
        title.setOpaque(true);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY));
        title.setLayout(new BorderLayout());
        title.add(settings.getLinkComponent(), BorderLayout.WEST);

        label = new JLabel(String.valueOf(graph.getLabel(cell)));
        label.setFont(title.getFont().deriveFont(Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        title.add(label, BorderLayout.CENTER);

        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);
        
        for (AbstractButton ab: settings.getButtons()) {
            toolBar2.add(ab);
        }

        title.add(toolBar2, BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = null;

         if (graph.getModel().getChildCount(cell) == 0)  {
             renderedComponent = settings.getComponent(ace.getAceFrameConfig());
             if (JScrollPane.class.isAssignableFrom(renderedComponent.getClass())) {
                 scrollPane = (JScrollPane) renderedComponent;
             } else {
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


        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(settings.getResizeLabel(), BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);


        setMinimumSize(new Dimension(40, 20));
        updateLabel();
    }


    private void updateLabel() {
    	label.setText(settings.getTitle());
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
			updateLabel();
		}
    	
    }
 }
