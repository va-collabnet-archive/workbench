package org.dwfa.bpa.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class SortClickListener extends MouseAdapter {
    private int headerClickCount = 0;

    @Override
    public void mouseClicked(MouseEvent e) {
        
        JTableHeader header = (JTableHeader) e.getSource();
        headerClickCount++;
        if (headerClickCount > 2 || e.getClickCount() > 1) {
            header.getTable().getRowSorter().setSortKeys(null);
            headerClickCount = 0;
        } 
    }
    
    public static void setupSorter(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().addMouseListener(new SortClickListener());
    }
}
