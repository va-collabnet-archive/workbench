package org.dwfa.ace;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import org.dwfa.vodb.types.I_GetConceptData;

public class DropButton extends JButton implements I_DoConceptDrop {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private I_DoConceptDrop dropObject;
	public DropButton(Icon icon, I_DoConceptDrop dropObject) {
		super(icon);
		this.dropObject = dropObject;
	}

	public DropButton(String text, I_DoConceptDrop dropObject) {
		super(text);
		this.dropObject = dropObject;
	}

	public DropButton(Action a, I_DoConceptDrop dropObject) {
		super(a);
		this.dropObject = dropObject;
	}

	public DropButton(String text, Icon icon, I_DoConceptDrop dropObject) {
		super(text, icon);
		this.dropObject = dropObject;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.I_DoConceptDrop#doDrop(org.dwfa.vodb.types.I_GetConceptData)
	 */
	public void doDrop(I_GetConceptData obj) {
		dropObject.doDrop(obj);
	}

}
