/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena;

import org.ihtsdo.swing.wizard.WizardBI;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import org.ihtsdo.arena.conceptview.ConceptViewRenderer;

/**
 *
 * @author kec
 */
public class WizardPanel extends ScrollablePanel implements WizardBI {

   ConceptViewRenderer renderer;

   public WizardPanel(LayoutManager layout,
           ConceptViewRenderer renderer) {
      super(layout);
      this.renderer = renderer;
   }

   public WizardPanel(LayoutManager layout, ScrollDirection direction,
           ConceptViewRenderer renderer) {
      super(layout, direction);
      this.renderer = renderer;
   }

   public WizardPanel(LayoutManager layout, boolean isDoubleBuffered,
           ConceptViewRenderer renderer) {
      super(layout, isDoubleBuffered);
      this.renderer = renderer;
   }

   public WizardPanel(boolean isDoubleBuffered, ScrollDirection direction,
           ConceptViewRenderer renderer) {
      super(isDoubleBuffered, direction);
      this.renderer = renderer;
   }

   public WizardPanel(boolean isDoubleBuffered,
           ConceptViewRenderer renderer) {
      super(isDoubleBuffered);
      this.renderer = renderer;
   }

   public WizardPanel(ConceptViewRenderer renderer) {
      this.renderer = renderer;
   }

   @Override
   public void setWizardPanelVisible(boolean visible) {
      if (visible) {
         renderer.showWizardPanel();
      } else {
         renderer.showConceptPanel();
      }
   }

   @Override
   public JPanel getWizardPanel() {
      return this;
   }
}
