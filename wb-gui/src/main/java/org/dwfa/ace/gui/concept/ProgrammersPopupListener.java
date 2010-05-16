/**
 * 
 */
package org.dwfa.ace.gui.concept;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class ProgrammersPopupListener extends MouseAdapter implements ActionListener, ClipboardOwner {

	private enum MENU_OPTIONS {
		WRITE_LONG_FORM_TO_CLIPBOARD("Write long form to clipboard"),
		SET_FROM_NID("Set from nid"),
		ADD_TO_WATCH_LIST("Add to watch list"),
		REMOVE_FROM_WATCH_LIST("Remove from watch list"),
		GET_CONCEPT_ATTRIBUTES("Get concept attributes"),
        SET_CACHE_SIZE("Set cache size"),
        SET_CACHE_PERCENT("Set cache percent")
		
		;
		
		String menuText;
		
		private MENU_OPTIONS(String menuText) {
			this.menuText = menuText;
		}
		
		public void addToMenu(JPopupMenu popup, ActionListener l) {
			JMenuItem menuItem = new JMenuItem(menuText);
	        menuItem.addActionListener(l);
			popup.add(menuItem);
			
		}
	};
	private static Map<String, MENU_OPTIONS> optionMap = new HashMap<String, MENU_OPTIONS>(MENU_OPTIONS.values().length);

	static {
		for (MENU_OPTIONS option: MENU_OPTIONS.values()) {
			optionMap.put(option.menuText, option);
		}
	}
	/**
	 * 
	 */
	private final ConceptPanel conceptPanel;
	
	JPopupMenu popup = new JPopupMenu();
	
    
	public ProgrammersPopupListener(ConceptPanel conceptPanel) {
		for (MENU_OPTIONS option: MENU_OPTIONS.values()) {
			option.addToMenu(popup, this);
		}
		this.conceptPanel = conceptPanel;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
        	if (e.isAltDown()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
        	}
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (optionMap.get(e.getActionCommand())) {
			case ADD_TO_WATCH_LIST:
				addToWatch();
				break;
			case REMOVE_FROM_WATCH_LIST:
				removeFromWatch();
				break;
			case SET_FROM_NID:
				setFromNid();
				break;
			case WRITE_LONG_FORM_TO_CLIPBOARD:
				writeLongFormToClipboard();
				break;
			case GET_CONCEPT_ATTRIBUTES:
				getConceptAttributes();
				break;
			case SET_CACHE_PERCENT:
			    setCachePercent();
			    break;
			case SET_CACHE_SIZE:
				setCacheSize();
				break;
		}
	}

	private void setCacheSize() {
        String sizeString = askQuestion("Set bdb cache size:", "Enter size[XXXXm|XXg]:", "" + Terms.get().getCacheSize());
        if (sizeString != null) {
            Terms.get().setCacheSize(sizeString);
        }
    }

    private void setCachePercent() {
        String percentString = askQuestion("Set bdb cache percent:", "Enter percent[1..99]:", "" + Terms.get().getCachePercent());
        if (percentString != null) {
            Terms.get().setCachePercent(percentString);
        }
    }

    private void getConceptAttributes() {
		try {
			I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
			I_ConceptAttributeVersioned attr = igcd.getConceptAttributes();
			List<? extends I_ConceptAttributeTuple> tuples = attr.getTuples();
			List<? extends I_ConceptAttributeTuple> tuples2 = attr.getTuples(conceptPanel.getConfig().getAllowedStatus(), 
					conceptPanel.getConfig().getViewPositionSetReadOnly());
			List<? extends I_ConceptAttributeTuple> tuples3 = igcd.getConceptAttributeTuples(null, 
            		conceptPanel.getConfig().getViewPositionSetReadOnly(), 
            		conceptPanel.getConfig().getPrecedence(), conceptPanel.getConfig().getConflictResolutionStrategy());
			List<? extends I_ConceptAttributeTuple> tuples4 = igcd.getConceptAttributeTuples(null, 
            		conceptPanel.getConfig().getViewPositionSetReadOnly(), 
                    conceptPanel.getConfig().getPrecedence(), conceptPanel.getConfig().getConflictResolutionStrategy());
			AceLog.getAppLog().info("attr: " + attr);
			AceLog.getAppLog().info("tuples 1: " + tuples);
			AceLog.getAppLog().info("tuples 2: " + tuples2);
			AceLog.getAppLog().info("tuples 3: " + tuples3);
			AceLog.getAppLog().info("tuples 4: " + tuples4);
		} catch (IOException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (TerminologyException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	private void writeLongFormToClipboard() {
		I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection contents = new StringSelection(igcd.toLongString());
		clip.setContents(contents, this);
	}

	private void setFromNid() {
		String nidString = askQuestion("Set panel to new concept:", "Enter nid:", "-2142075612");
		int nid = Integer.parseInt(nidString);
		try {
			I_GetConceptData concept = Terms.get().getConceptForNid(nid);
			this.conceptPanel.setTermComponent(concept);
		} catch (IOException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	private void removeFromWatch() {
		I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
		Terms.get().removeFromWatchList(igcd);
	}

	private void addToWatch() {
		I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
		Terms.get().addToWatchList(igcd);
	}
	
    public String askQuestion(String realm, String question, String defaultAnswer) {
        return (String) JOptionPane.showInputDialog(this.conceptPanel, question, realm,
            JOptionPane.PLAIN_MESSAGE, null, null, defaultAnswer);
    }

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		//nothing to do...
	}

}