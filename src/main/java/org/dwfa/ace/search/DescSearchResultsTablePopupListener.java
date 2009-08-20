package org.dwfa.ace.search;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DescSearchResultsTablePopupListener implements MouseListener {

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) {
            handlePopup(e);
            e.consume();
        }
	}

	public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
            handlePopup(e);
            e.consume();
        }
	}

	private void handlePopup(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


}
