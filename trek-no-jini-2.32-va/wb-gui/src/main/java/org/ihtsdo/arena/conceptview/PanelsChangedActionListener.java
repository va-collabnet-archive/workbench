/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author kec
 */
class PanelsChangedActionListener implements ActionListener, ChangeListener, PropertyChangeListener {
    private ConceptViewSettings settings;

    public PanelsChangedActionListener(ConceptViewSettings settings) {
        this.settings = settings;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        doUpdateLater();
    }

    private void doUpdateLater() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (settings.isNavigatorSetup()) {
                            settings.getNavigator().updateHistoryPanel();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        doUpdateLater();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        doUpdateLater();
    }
    
}
