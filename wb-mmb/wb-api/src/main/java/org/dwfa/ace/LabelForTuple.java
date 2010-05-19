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
package org.dwfa.ace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;

public abstract class LabelForTuple extends JLabel implements I_ImplementActiveLabel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    Set<ActionListener> listeners = new HashSet<ActionListener>();

    public void doubleClick() {
        ActionEvent e = new ActionEvent(this, DOUBLE_CLICK, "doubleClick");
        for (ActionListener l : listeners) {
            l.actionPerformed(e);
        }
    }

    public void showPopup() {
        ActionEvent e = new ActionEvent(this, POPUP, "popup");
        for (ActionListener l : listeners) {
            l.actionPerformed(e);
        }
    }

    private boolean longForm;
    private boolean showStatus;

    public LabelForTuple(boolean longForm, boolean showStatus) {
        super();
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    doubleClick();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup();
                    e.consume();
                }
            }
        });
        this.longForm = longForm;
        this.showStatus = showStatus;
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }

    public JLabel getLabel() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return tupleEquals(obj);
    }

    protected abstract boolean tupleEquals(Object obj);

    protected abstract int tupleHash();

    protected abstract String getTupleString();

    @Override
    public int hashCode() {
        return tupleHash();
    }

    public String toString() {
        return this.getClass().getSimpleName() + ": " + getTupleString();
    }

    public boolean isLongForm() {
        return longForm;
    }

    public boolean getShowStatus() {
        return showStatus;
    }

}
