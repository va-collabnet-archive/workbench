package org.dwfa.ace.list;

import java.awt.Component;
import java.io.IOException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;

public class AceListRenderer extends DefaultListCellRenderer {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private I_ConfigAceFrame config;

   public AceListRenderer(I_ConfigAceFrame config) {
      super();
      this.config = config;
   }

   @Override
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
         boolean cellHasFocus) {
      JLabel renderComponent = (JLabel) super
            .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value != null) {
         I_GetConceptData concept = (I_GetConceptData) value;
         if (I_GetConceptData.class.isAssignableFrom(value.getClass())) {
            try {
               I_DescriptionTuple desc = concept.getDescTuple(config.getShortLabelDescPreferenceList(), config);
               if (desc != null) {
                  renderComponent.setText(desc.getText());
               } else {
                  AceLog.getAppLog().info(" descTuple is null: " + concept.toString());
                  renderComponent.setText(concept.getInitialText());
               }
            } catch (IOException e) {
               this.setText(e.getMessage());
               AceLog.getAppLog().alertAndLogException(e);
            }
         } else {
            renderComponent.setText(concept.toString());
         }
      } else {
         renderComponent.setText("<html><font color=red>Empty");
      }

      return renderComponent;
   }

}
