package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JComponent;

import org.ihtsdo.tk.api.ComponentVersionBI;

public abstract class ComponentVersionDragPanel<T extends ComponentVersionBI> extends DragPanel<T> implements I_ToggleSubPanels {

    private void setPanelVisibility(List<JComponent> componentList, EnumSet<SubPanels> panels, SubPanels panel) {
        for (JComponent refexPanel: componentList) {
            if (panels.contains(panel)) {
                refexPanel.setVisible(true);
            } else {
                refexPanel.setVisible(false);
            }
        }
    }

    public enum SubPanels { REFEX, ALERT, TEMPLATE };
    
    private List<JComponent> refexSubPanels = new ArrayList<JComponent>();
   private List<JComponent> alertSubPanels = new ArrayList<JComponent>(); 
    private List<JComponent> templateSubPanels = new ArrayList<JComponent>(); 

     public List<JComponent> getAlertSubPanels() {
        return alertSubPanels;
    }

    public List<JComponent> getRefexSubPanels() {
        return refexSubPanels;
    }

    public List<JComponent> getTemplateSubPanels() {
        return templateSubPanels;
    }
     /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ComponentVersionDragPanel(ConceptViewSettings settings) {
        super(settings);
    }

    public ComponentVersionDragPanel(LayoutManager layout,
            ConceptViewSettings settings) {
        super(layout, settings);
    }

    @Override
    public String getUserString(T obj) {
        return obj.toUserString();
    }
    
    @Override
    public void showSubPanels(EnumSet<SubPanels> panels) {
        setPanelVisibility(refexSubPanels, panels, SubPanels.REFEX);
        setPanelVisibility(alertSubPanels, panels, SubPanels.ALERT);
        setPanelVisibility(templateSubPanels, panels, SubPanels.TEMPLATE);
        
    }
}
