package org.ihtsdo.arena;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.dwfa.ace.table.AceTableRenderer;

public class WfHxDetailsPanelRenderer extends AceTableRenderer {
	private static final long serialVersionUID = 1L;

	private Color currColor;
	private List<Boolean> changingColorRowIndices;  
      
    public WfHxDetailsPanelRenderer(List<Boolean> newUids) {    
        setOpaque(true);    
        changingColorRowIndices = newUids;
    }    
  
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {       

    	if (row >= 0) {
    		// First line White
	    	if(changingColorRowIndices.get(row)) {                              
	            currColor = STRIPE;                 
	        } else {  
	            currColor = Color.WHITE;  
	        } 
    	} else {
    		// Header
    		currColor = Color.CYAN;
    	}
    	
        super.setBackground(currColor);  
    	super.setForeground(Color.black);     

    	setFont(table.getFont());
        setHorizontalAlignment(SwingConstants.CENTER);
        setValue(value);  

        return this;    
    }
}