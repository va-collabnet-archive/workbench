package org.ihtsdo.arena.taxonomyview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.dwfa.ace.ACE;
import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.arena.conceptview.ConceptViewTitle;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class TaxonomyViewRenderer  extends JComponent
{

    /**
     * 
     */
    private static final long serialVersionUID = 2106746763664760745L;

 
    /**
     * 
     */
    protected static TaxonomyViewRenderer dragSource = null;

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

	private TaxonomyViewSettings settings;


	private TaxonomyViewTitle title;

		
    /**
     * 
     */
    public TaxonomyViewRenderer(Object cellObj,
            final mxGraphComponent graphContainer, ACE ace)
    {
        this.cell = (mxCell) cellObj;
        this.graphContainer = graphContainer;
        this.graph = graphContainer.getGraph();
        this.settings = (TaxonomyViewSettings) this.cell.getValue();
        this.settings.setup(ace, cell, graphContainer, graph, this);
        this.settings.addHostListener(new HostListener());
        setLayout(new BorderLayout());

        title = new TaxonomyViewTitle(graph, cell, settings);

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
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            setOpaque(true);
			scrollPane.getVerticalScrollBar().addAdjustmentListener(
					new AdjustmentListener() {

						public void adjustmentValueChanged(AdjustmentEvent e) {
							graphContainer.refresh();
						}

					});
			scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
		}


        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 8, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

       
        gbc.weightx = 1;
        JPanel fillerPanel = new JPanel();
        fillerPanel.setBackground(footerPanel.getBackground());
        
        footerPanel.add(fillerPanel, gbc);
        
        gbc.weightx = 0;

        gbc.gridx++;
        footerPanel.add(settings.getResizeLabel(), gbc);
        footerPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.gray));
        add(footerPanel, BorderLayout.SOUTH);


        setMinimumSize(new Dimension(40, 20));
    }


    private void updateLabel() {
    	title.updateTitle();
    }



    /**
     * 
     */
    public static TaxonomyViewRenderer getVertex(Component component)
    {
        while (component != null)
        {
            if (component instanceof TaxonomyViewRenderer)
            {
                return (TaxonomyViewRenderer) component;
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