/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.dwfa.ace.log.AceLog;

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
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Doing transfer action: " + name + " with transfer handler: " + th);
            }

            Transferable trans = null;

            // any of these calls may throw IllegalStateException
            try {
                if ((clipboard != null) && (th != null) && (name != null)) {
                    if ("cut".equals(name)) {
                        th.exportToClipboard(c, clipboard, DnDConstants.ACTION_MOVE);
                    } else if ("copy".equals(name)) {
                        th.exportToClipboard(c, clipboard, DnDConstants.ACTION_COPY);
                    } else if ("paste".equals(name)) {
                        trans = clipboard.getContents(null);
                    }
                } else {
                    AceLog.getAppLog().log(Level.WARNING,
                        "clipboard, th, or name is null: " + clipboard + " " + th + " " + name);
                }
            } catch (IllegalStateException ise) {
                AceLog.getAppLog().log(Level.SEVERE, ise.getMessage(), ise);
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
