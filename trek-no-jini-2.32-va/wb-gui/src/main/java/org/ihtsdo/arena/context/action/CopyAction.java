package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.dwfa.ace.batch.BatchMonitor;

//element_new.png
public class CopyAction extends AbstractAction {

	private static final long serialVersionUID = 1L;


	public CopyAction() {
		super("copy");
		putValue(LARGE_ICON_KEY, new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/element_new.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

}
