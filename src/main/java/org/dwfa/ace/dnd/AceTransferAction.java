/**
 * 
 */
package org.dwfa.ace.dnd;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

import org.dwfa.ace.AceLog;

public class AceTransferAction extends AbstractAction implements UIResource {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AceTransferAction(String name) {
        super(name);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof JComponent) {
            JComponent c = (JComponent) src;
            TransferHandler th = c.getTransferHandler();
            Clipboard clipboard = getClipboard(c);
            String name = (String) getValue(Action.NAME);
            if (AceLog.getLog().isLoggable(Level.FINE)) {
            	AceLog.getLog().fine("Doing transfer action: " + name
                        + " with transfer handler: " + th);
            }

            Transferable trans = null;

            // any of these calls may throw IllegalStateException
            try {
                if ((clipboard != null) && (th != null) && (name != null)) {
                    if ("cut".equals(name)) {
                        th.exportToClipboard(c, clipboard,
                                DnDConstants.ACTION_MOVE);
                    } else if ("copy".equals(name)) {
                        th.exportToClipboard(c, clipboard,
                                DnDConstants.ACTION_COPY);
                    } else if ("paste".equals(name)) {
                        trans = clipboard.getContents(null);
                    }
                } else {
                	AceLog.getLog().log(Level.WARNING, "clipboard, th, or name is null: " + clipboard + " " + th + " " + name);
                }
            } catch (IllegalStateException ise) {
            	AceLog.getLog().log(Level.SEVERE, ise.getMessage(), ise);
                UIManager.getLookAndFeel().provideErrorFeedback(c);
                return;
            }

            // this is a paste action, import data into the component
            if (trans != null) {
                th.importData(c, trans);
            }
        }
    }

    /**
     * Returns the clipboard to use for cut/copy/paste.
     */
    private Clipboard getClipboard(JComponent c) {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }


}