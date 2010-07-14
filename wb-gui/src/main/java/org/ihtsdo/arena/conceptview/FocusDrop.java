package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.tapi.TerminologyException;


public class FocusDrop extends JLabel implements I_AcceptConcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTreeWithDragImage navigatorTree;
	private I_ConfigAceFrame config;

	public FocusDrop(ImageIcon imageIcon, JTreeWithDragImage navigatorTree, I_ConfigAceFrame config) {
		super(imageIcon);
		this.navigatorTree = navigatorTree;
		this.config = config;
		setOpaque(true);
		setBackground(new Color(230, 230, 230));
		setMinimumSize(new Dimension(20, 20));
		setBorder(BorderFactory.createLoweredBevelBorder());
		setTransferHandler(new TerminologyTransferHandler(this));
	}

	public void setConcept(I_GetConceptData c) {
        try {
            new ExpandPathToNodeStateListener(navigatorTree, config ,c);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
		}
	}

}
