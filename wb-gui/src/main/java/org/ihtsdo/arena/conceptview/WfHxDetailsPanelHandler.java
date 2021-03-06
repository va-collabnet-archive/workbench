package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.WfHxDetailsPanel;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;


public class WfHxDetailsPanelHandler {
	private boolean currentlyDisplayed  = false;
	private ConceptViewRenderer conceptPanelRenderer;
	private ConceptViewSettings conceptSettings;
    private WfHxDetailsPanel detailsPanel;
	private WfHxDetailsConceptChangeListener currentListener;
	private ViewCoordinate viewCoord;
    
	public class WfHxDetailsConceptChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (currentlyDisplayed) {
	        	Object termComponent = pce.getNewValue();
	            
	            if (termComponent != null && 
	            	I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
	                I_GetConceptData con = (I_GetConceptData) termComponent;
	
	                regenerateWfPanel(con, false);
	            }
            }
        }
    }


	public WfHxDetailsPanelHandler (ConceptViewRenderer renderer, ConceptViewSettings settings) {
    	conceptPanelRenderer = renderer;
    	conceptSettings = settings;
		currentListener = new WfHxDetailsConceptChangeListener();
		conceptSettings.getHost().addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
		viewCoord = settings.getConfig().getViewCoordinate();

    }
	public void showWfHxDetailsPanel(I_GetConceptData concept) {
        if (detailsPanel == null) {
    		createNewWfPanel();
        }
        
    	if (detailsPanel.isNewHtmlCodeRequired(concept)) {
    		createNewWfPanel();
    	}
    	
    	currentlyDisplayed = true;
    	detailsPanel.setVisible(true);
    	setWfHxLocation();
    }
	
	public void hideWfHxDetailsPanel() {
		if (detailsPanel != null) {
			JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane();
	
	        detailsPanel.setVisible(false);
	    	detailsPanel.invalidate();
	        layers.remove(detailsPanel);
	    	currentlyDisplayed = false;
		}
	}

    private void createNewWfPanel() {
        detailsPanel = new WfHxDetailsPanel(conceptSettings, viewCoord);
        detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        detailsPanel.setOpaque(true);
    }


    public void setWfHxLocation() {
    	try {
        	if (conceptPanelRenderer.getRootPane() != null) {
		        JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane(); 
	
	    		Point loc = SwingUtilities.convertPoint(conceptPanelRenderer, new Point(0, 0), layers);
		        if (layers.getWidth() > loc.x + conceptPanelRenderer.getWidth() + detailsPanel.getWidth()) {
		            loc.x = loc.x + conceptPanelRenderer.getWidth();
		            detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
		        } else {
		            loc.x = loc.x - detailsPanel.getWidth();
		            detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
		        }

		        detailsPanel.setBounds(loc.x, loc.y, detailsPanel.getWidth(), detailsPanel.getHeight());
		        layers.add(detailsPanel, JLayeredPane.PALETTE_LAYER);
        	}
    	} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to display Details Panel with error message: " + e.getMessage());
	    }
    }	    

    protected boolean regenerateWfPanel(I_GetConceptData concept, boolean newHtmlCodeRquired) {
    	boolean wfPanelDetailsUpdated = false;
    	
    	if (detailsPanel != null) {
	    	if (newHtmlCodeRquired || ((WfHxDetailsPanel)detailsPanel).isNewHtmlCodeRequired(concept)) {
	        	JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane();
	            layers.remove(detailsPanel);
	
	            conceptSettings.getHost().removePropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
	    		
	            createNewWfPanel();
	            wfPanelDetailsUpdated = true;
	
	            setWfHxLocation();
	            conceptSettings.getHost().addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
	    	}
    	}

        return wfPanelDetailsUpdated;
    }

    public boolean isWfHxDetailsCurrenltyDisplayed() {
    	return currentlyDisplayed;
    }
    
	public void regenerateWfData(WorkflowHistoryJavaBeanBI bean) {
		if (isWfHxDetailsCurrenltyDisplayed()) {
			JLayeredPane layers = conceptPanelRenderer.getRootPane().getLayeredPane();
	        layers.remove(detailsPanel);
	
	        conceptSettings.getHost().removePropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
	
	        createRegeneratedWfPanel(bean);
	
	    	setWfHxLocation();
	        conceptSettings.getHost().addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, currentListener);
		}
	}
	
	private void createRegeneratedWfPanel(WorkflowHistoryJavaBeanBI bean) {
        detailsPanel = new WfHxDetailsPanel(conceptSettings, viewCoord, bean);
        detailsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        detailsPanel.setOpaque(true);
	}
}
