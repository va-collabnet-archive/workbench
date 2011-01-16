package org.ihtsdo.arena.conceptview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.drools.KnowledgeBase;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.ScrollablePanel;
import org.ihtsdo.arena.context.action.BpActionFactory;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.util.swing.GuiUtil;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * @author Administrator
 * 
 */
public class ConceptViewRenderer extends JLayeredPane
{

	private class RendererComponentAdaptor extends ComponentAdapter implements AncestorListener {

		@Override
		public void componentMoved(ComponentEvent e) {
			settings.hideNavigator();
		}

		@Override
		public void componentResized(ComponentEvent e) {
			settings.hideNavigator();
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			settings.hideNavigator();
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			settings.hideNavigator();
		}

		@Override
		public void ancestorAdded(AncestorEvent event) {
			settings.hideNavigator();
		}
		
	}
    /**
     * 
     */
    private static final long serialVersionUID = 2106746763664760745L;

 
    /**
     * 
     */
    protected static ConceptViewRenderer dragSource = null;

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

	private ConceptViewSettings settings;


	private ConceptViewTitle title;

	private ScrollablePanel workflowPanel = new ScrollablePanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
	private JScrollPane workflowScrollPane = new JScrollPane(workflowPanel, 
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	private KnowledgeBase contextualConceptActionsKBase;


	private JScrollPane scrollPane;


	private JToggleButton workflowToggleButton;

		
    /**
     * 
     */
    public ConceptViewRenderer(Object cellObj,
            final mxGraphComponent graphContainer, ACE ace)
    {
    	
		try {
			contextualConceptActionsKBase = EditPanelKb.setupKb(new File("drools-rules/ContextualConceptActionsPanel.drl"));
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		workflowPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        this.cell = (mxCell) cellObj;
        this.graphContainer = graphContainer;
        this.graph = graphContainer.getGraph();
        this.settings = (ConceptViewSettings) this.cell.getValue();
        this.settings.setup(ace, cell, graphContainer, graph, this);
        this.settings.addHostListener(new HostListener());
        setLayout(new BorderLayout());

        title = new ConceptViewTitle(graph, cell, settings);

        add(title, BorderLayout.NORTH);

        scrollPane = null;

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
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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

                
        gbc.gridx++;
        workflowToggleButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/16x16/plain/media_step_forward.png")));
        workflowToggleButton.setToolTipText("show context sensitive actions available for this concept...");
        workflowToggleButton.setSelected(false);
        
        workflowToggleButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton) e.getSource();
				if (button.isSelected()) {
		            remove(scrollPane);
		            Collection<Action> actions = getKbActions();
		            workflowPanel.removeAll();
		            for (Action a: actions) {
		            	JButton actionButton = new JButton(a);
		            	actionButton.setHorizontalTextPosition(SwingConstants.CENTER);
		            	actionButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		            	actionButton.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								workflowToggleButton.doClick();
							}
						});
		            	workflowPanel.add(actionButton);
		            }
		            add(workflowScrollPane, BorderLayout.CENTER);
					// Populate here...

		            workflowPanel.setVisible(true);
					scrollPane.setVisible(false);
					GuiUtil.tickle(ConceptViewRenderer.this);

				} else {
					workflowPanel.setVisible(false);
		            remove(workflowScrollPane);
		            add(scrollPane, BorderLayout.CENTER);
					scrollPane.setVisible(true);
				}
			}
		});
        
        workflowToggleButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        footerPanel.add(workflowToggleButton, gbc);
        
        gbc.weightx = 1;
        JPanel fillerPanel = new JPanel();
        fillerPanel.setBackground(footerPanel.getBackground());
        
        footerPanel.add(fillerPanel, gbc);
        
        gbc.weightx = 0;
        gbc.gridx++;
        JButton cancelButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/delete.png")));
        cancelButton.setToolTipText("cancel changes to concept");
        cancelButton.addActionListener(new CancelActionListener());
        cancelButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        footerPanel.add(cancelButton, gbc);
        
        gbc.gridx++;
        JButton commitButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/check.png")));
        commitButton.setToolTipText("commit changes to concept");
        commitButton.addActionListener(new CommitActionListener());
        commitButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        footerPanel.add(commitButton, gbc);
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        footerPanel.add(settings.getResizeLabel(), gbc);
        footerPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.gray));
        add(footerPanel, BorderLayout.SOUTH);

        //add(workflowPanel);
        //workflowPanel.setVisible(false);
        //workflowPanel.setBounds(0, 0, 100, 200);
        setMinimumSize(new Dimension(40, 20));
        RendererComponentAdaptor rca = new RendererComponentAdaptor();
        addAncestorListener(rca);
        addComponentListener(rca);
    }

    private void updateLabel() {
    	title.updateTitle();
    }

	private Collection<Action> getKbActions() {
		Collection<Action> actions = new ArrayList<Action>();
		
		try {
			StatefulKnowledgeSession ksession = contextualConceptActionsKBase.newStatefulKnowledgeSession();
			boolean uselogger = false;
			
			KnowledgeRuntimeLogger logger = null;
			if (uselogger) {
				logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
			}
			try {
				
				ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();
				ksession.setGlobal("vc", coordinate);
				ksession.setGlobal("actions", actions);
				ksession.setGlobal("actionFactory", new BpActionFactory(settings.getConfig(), 
						settings.getHost()));
				if (settings.getConcept() != null) {
					ksession.insert(Ts.get().getConceptVersion(coordinate, settings.getConcept().getNid()));
				} else {
					ksession.insert("null concept");
				}
				ksession.fireAllRules();
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} finally {
				if (logger != null) {
					logger.close();
				}
			}
		} catch (Throwable e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return actions;
	}

	

    /**
     * 
     */
    public static ConceptViewRenderer getVertex(Component component)
    {
        while (component != null)
        {
            if (component instanceof ConceptViewRenderer)
            {
                return (ConceptViewRenderer) component;
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
