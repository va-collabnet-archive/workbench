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
package org.dwfa.ace.list;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.dnd.TerminologyTransferHandler;

public class TerminologyList extends JList {

    private class DeleteAction extends AbstractAction {

        public DeleteAction() {
            super("delete");
        }

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            int selectedIndex = getSelectedIndex();
            if (selectedIndex >= 0) {
                TerminologyListModel tm = (TerminologyListModel) getModel();
                tm.removeElement(selectedIndex);
            }
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TerminologyList(I_ConfigAceFrame config) {
        super(new TerminologyListModel());
        init(true, config);
    }

    public TerminologyList(TerminologyListModel dataModel,
            I_ConfigAceFrame config) {
        super(dataModel);
        init(true, config);
    }

    public TerminologyList(boolean allowDelete, I_ConfigAceFrame config) {
        super(new TerminologyListModel());
        init(allowDelete, config);
    }

    public TerminologyList(TerminologyListModel dataModel, boolean allowDelete,
            I_ConfigAceFrame config) {
        super(dataModel);
        init(allowDelete, config);
    }

    private void init(boolean allowDelete, I_ConfigAceFrame config) {
        setCellRenderer(new AceListRenderer(config));
        setTransferHandler(new TerminologyTransferHandler());
        setDragEnabled(true);
        if (allowDelete) {
            DeleteAction delete = new DeleteAction();
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                delete.getValue(Action.NAME));
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                delete.getValue(Action.NAME));
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                delete.getValue(Action.NAME));
            getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                delete.getValue(Action.NAME));

            ActionMap map = this.getActionMap();
            map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
            map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
            map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
            map.put(delete.getValue(Action.NAME), delete);
        }
    }

}
