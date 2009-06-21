package org.dwfa.ace.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;

public class DescriptionTableRenderer extends AceTableRenderer {

   boolean renderInactive = false;
   private I_ConfigAceFrame frameConfig;
   
   public DescriptionTableRenderer(I_ConfigAceFrame frameConfig) {
      super();
      setVerticalAlignment(TOP);
      this.frameConfig = frameConfig;
   }

   public DescriptionTableRenderer(I_ConfigAceFrame frameConfig, boolean renderInactive) {
	      this(frameConfig);
	      this.renderInactive = renderInactive;
   }

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
         int row, int column) {
      JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
            column);
      boolean same = false;
      if (isSelected == false) {
    	  if (renderInactive) {
              renderComponent.setBackground(UIManager.getColor("Table.background"));
              renderComponent.setForeground(UIManager.getColor("Table.foreground"));
    	  } else {
              renderComponent.setBackground(colorForRow(row));
              renderComponent.setForeground(UIManager.getColor("Table.foreground"));
    	  }
       } else {	
          renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
          renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
       }

      if (StringWithDescTuple.class.isAssignableFrom(value.getClass())) {
          StringWithDescTuple swt = (StringWithDescTuple) value;
  		if (swt == null || swt.getTuple() == null) {
  			renderComponent.setText("null");
  			return renderComponent;
  		}
        if (swt.getTuple() != null) {
          boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
          if (renderInactive) {
              boolean active = frameConfig.getAllowedStatus().contains(swt.getTuple().getStatusId());
              if (active == false) {
                  renderComponent.setBackground(Color.LIGHT_GRAY);
              }
          }
          if (row > 0) {
             StringWithDescTuple prevSwt = (StringWithDescTuple) table.getValueAt(row - 1, column);
             same = swt.getTuple().getDescId() == prevSwt.getTuple().getDescId();
             if (renderInactive == false) {
                 setBorder(column, this, same, uncommitted);
             }
             if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
                renderComponent.setText("");
             }
          } else {
              if (renderInactive == false) {
                  setBorder(column, this, false, uncommitted);
              }
          }
        }
        TableColumn c = table.getColumnModel().getColumn(column);
        if (swt.wrapLines) {
           if (BasicHTML.isHTMLString(renderComponent.getText())) {
              View v = BasicHTML.createHTMLView(renderComponent, swt.getCellText());
              v.setSize(c.getWidth(), 0);
              float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
              if (prefYSpan > table.getRowHeight(row)) {
                 table.setRowHeight(row, (int) (prefYSpan + 4));
              }
           } else {
              View v = BasicHTML.createHTMLView(renderComponent, "<html>" + swt.getCellText());
              v.setSize(c.getWidth(), 0);
              float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
              if (prefYSpan > table.getRowHeight(row)) {
                 table.setRowHeight(row, (int) (prefYSpan + 4));
                 if (isSelected) {
                    renderComponent.setText("<html>Y" + swt.getCellText());
                 }
              }
              if (table.getRowHeight(row) > 16) {
                 renderComponent.setText("<html>" + swt.getCellText());
              }
           }
        } 
        //renderComponent.setToolTipText(swt.getCellText());
      }

      return renderComponent;
   }

}
