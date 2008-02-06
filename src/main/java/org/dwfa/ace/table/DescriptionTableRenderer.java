package org.dwfa.ace.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.TableColumn;
import javax.swing.text.View;

import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;

public class DescriptionTableRenderer extends AceTableRenderer {

   public DescriptionTableRenderer() {
      super();
      setVerticalAlignment(TOP);
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
          renderComponent.setBackground(colorForRow(row));
          renderComponent.setForeground(UIManager.getColor("Table.foreground"));
       } else {
          renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
          renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
       }

      StringWithDescTuple swt = (StringWithDescTuple) value;
      boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;
      if (row > 0) {
         StringWithDescTuple prevSwt = (StringWithDescTuple) table.getValueAt(row - 1, column);
         same = swt.getTuple().getDescId() == prevSwt.getTuple().getDescId();
         setBorder(column, this, same, uncommitted);
         if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
            renderComponent.setText("");
         }
      } else {
         setBorder(column, this, false, uncommitted);
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
      renderComponent.setToolTipText(swt.getCellText());
      return renderComponent;
   }

}
