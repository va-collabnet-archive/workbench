package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.ihtsdo.arena.WfHxDetailsPanel;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;


public class WfHxDetailsPanelHandler {
	public boolean currentlyDisplayed  = false;
	private ConceptViewRenderer conceptPanelRenderer;
	private ConceptViewSettings conceptSettings;
    private WfHxDetailsPanel detailsPanel;
	private WfHxDetailsConceptChangeListener currentListener;
	private final int rowHeightInPixels = 30;
    
	public class WfHxDetailsConceptChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (currentlyDisplayed) {
	        	Object termComponent = pce.getNewValue();
	            
	            if (termComponent != null && 
	            	I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
	                I_GetConceptData con = (I_GetConceptData) termComponent;
	
	                regenerateWfPanel(con);
	            }
            }
        }
    }


	public WfHxDetailsPanelHandler (ConceptViewRenderer renderer, ConceptViewSettings settings) {
    	conceptPanelRenderer = renderer;
    	conceptSettings = settings;
		currentListener = new WfHxDetailsConceptChangeListener();
		conceptSettings.getHost().addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);

    }
	public void showWfHxDetailsPanel(I_GetConceptData concept) {
        if (detailsPanel == null) {
    		createNewWfPanel();
        }
        
    	if (detailsPanel.isNewHtmlCodeRequired(concept)) {
//    		conceptSettings.getHost().removePropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
    		createNewWfPanel();
    	}
    	
    	currentlyDisplayed = true;
    	detailsPanel.setVisible(true);
    	setWfHxPanelProperties();
    }

	private void setWfHxPanelProperties() {
    	setWfHxDimensions();	
    	setWfHxLocation();			
	}
	
	private void setWfHxDimensions() {
		int width = 325;
		int height = 45;
		boolean rowIdentifed = false;

		if (detailsPanel.getCurrentHtml().contains(WorkflowHistoryRefset.chiefTermReplaceTerm)) {
			width = 390;
		}
	
		String str = detailsPanel.getCurrentHtml();
		String searchTerm = "</tr>";

		// Exclude header from below calculation
		str = str.substring(str.indexOf(searchTerm) + searchTerm.length()); 

		while (str.indexOf(searchTerm) >= 0) {
			rowIdentifed = true;
			height += rowHeightInPixels;
			str = str.substring(str.indexOf(searchTerm) + searchTerm.length());
		}

		if (!rowIdentifed) {
			width = 250;
		}
		
		detailsPanel.setBounds(0, 0, width, height);
	}
	
	public void hideWfHxDetailsPanel() {
        JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane();

        detailsPanel.setVisible(false);
    	detailsPanel.invalidate();
        layers.remove(detailsPanel);
    	currentlyDisplayed = false;
	}



    private void createNewWfPanel() {
        detailsPanel = new WfHxDetailsPanel(conceptSettings);
        detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        detailsPanel.setOpaque(true);
    }


    private void setWfHxLocation() {
    	try {
	        JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane(); 

    		Point loc = SwingUtilities.convertPoint(conceptPanelRenderer, new Point(0, 0), layers);
	        if (layers.getWidth() > loc.x + conceptPanelRenderer.getWidth() + detailsPanel.getWidth()) {
	            loc.x = loc.x + conceptPanelRenderer.getWidth();
	            detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
	        } else {
	            loc.x = loc.x - detailsPanel.getWidth();
	            detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
	        }
	        if (conceptPanelRenderer.getHeight() <  detailsPanel.getHeight()) {
	        	detailsPanel.setBounds(loc.x, loc.y, detailsPanel.getWidth(), conceptPanelRenderer.getHeight());
	        } else{
	        	detailsPanel.setBounds(loc.x, loc.y, detailsPanel.getWidth(), detailsPanel.getHeight());
	        }
	        layers.add(detailsPanel, JLayeredPane.PALETTE_LAYER);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    }	    

    protected void regenerateWfPanel(I_GetConceptData concept) {
    	if (((WfHxDetailsPanel)detailsPanel).isNewHtmlCodeRequired(concept)) {
        	JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane();
            layers.remove(detailsPanel);

            conceptSettings.getHost().removePropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
    		
            createNewWfPanel();
    	}

    	setWfHxPanelProperties();
    }

}
