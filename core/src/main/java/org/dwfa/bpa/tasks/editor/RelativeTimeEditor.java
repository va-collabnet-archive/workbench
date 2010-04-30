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
/*
 * Created on Apr 22, 2005
 */
package org.dwfa.bpa.tasks.editor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.bpa.gui.TaskPanel;

/**
 * @author kec
 * 
 */
/**
 * @author kec
 * 
 */
/**
 * @author kec
 * 
 */
/**
 * @author kec
 * 
 */
public class RelativeTimeEditor extends PropertyEditorSupport implements ChangeListener {

    private class EditorComponent extends JPanel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         */
        public EditorComponent() {
            super(new FlowLayout());
            this.setOpaque(false);
        }

        /**
         * 
         */
        private void setSpinners() {
            this.removeAll();
            this.add(new JSpinner(RelativeTimeEditor.this.numberSpinner));
            this.add(new JSpinner(RelativeTimeEditor.this.unitSpinner));
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(TaskPanel.class.getName());
    private SpinnerNumberModel numberSpinner;

    private SpinnerListModel unitSpinner;

    private EditorComponent editor = new EditorComponent();

    private static int MIN_IN_YEAR = 60 * 24 * 365;

    private static int MIN_IN_MONTH = 60 * 24 * 30;

    private static int MIN_IN_WEEK = 60 * 24 * 7;

    private static int MIN_IN_DAY = 60 * 24;

    private static int MIN_IN_HOUR = 60;

    private static int MIN_IN_MIN = 1;

    private static String[] unitValues = new String[] { "minute(s)", "hour(s)", "day(s)", "week(s)", "month(s)",
                                                       "year(s)" };

    private static int[] unitWeights = new int[] { MIN_IN_MIN, MIN_IN_HOUR, MIN_IN_DAY, MIN_IN_WEEK, MIN_IN_MONTH,
                                                  MIN_IN_YEAR };

    /**
     *  
     */
    public RelativeTimeEditor() {

    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent arg0) {
        try {
            this.firePropertyChange();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    /**
     * @return relative time in minutes
     * @see java.beans.PropertyEditor#getValue()
     */
    public Integer getValue() {
        Integer value = (Integer) this.numberSpinner.getValue();
        String unit = (String) this.unitSpinner.getValue();
        int unitIndex = -1;
        for (int i = 0; i < unitValues.length; i++) {
            if (unit.equals(unitValues[i])) {
                unitIndex = i;
                break;
            }
        }
        Integer relativeTimeInMins = new Integer(value.intValue() * unitWeights[unitIndex]);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Spinner value: " + value + "\nunit: " + unit + "\nunit index: " + unitIndex
                + "\nunit weight: " + unitWeights[unitIndex] + "\nset deadline (relative time in min): "
                + relativeTimeInMins);
        }
        return relativeTimeInMins;
    }

    /**
     * Must be an <code>Integer</code> representing relative time in minutes.
     * 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        Integer relativeTimeInMins = (Integer) value;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deadline (relative time in min): " + relativeTimeInMins);
        }
        int unitIndex = -1;
        if (relativeTimeInMins.intValue() == 0) {
            unitIndex = 0;
        } else {
            for (int i = (unitWeights.length - 1); i >= 0; i--) {
                if (relativeTimeInMins.intValue() % unitWeights[i] == 0) {
                    unitIndex = i;
                    break;
                }
            }
        }
        this.numberSpinner = new SpinnerNumberModel(relativeTimeInMins.intValue() / unitWeights[unitIndex], // initial
            // value
            0, // min
            99, // max
            1); // step
        this.numberSpinner.addChangeListener(this);
        this.unitSpinner = new SpinnerListModel(unitValues);
        this.unitSpinner.setValue(unitValues[unitIndex]);
        this.unitSpinner.addChangeListener(this);

        this.editor.setSpinners();
    }

    /**
     * @see java.beans.PropertyEditor#isPaintable()
     */
    public boolean isPaintable() {
        return true;
    }

    /**
     * Calls the paint method on this swing component.
     * 
     * @see java.beans.PropertyEditor#paintValue(java.awt.Graphics,
     *      java.awt.Rectangle)
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        this.editor.setBounds(box);
        this.editor.paintAll(gfx);
    }

    /**
     * @see java.beans.PropertyEditor#getJavaInitializationString()
     */
    public String getJavaInitializationString() {
        return "new Integer(" + this.getValue() + ")";
    }

    /**
     * @see java.beans.PropertyEditor#getAsText()
     */
    public String getAsText() {
        return this.getValue().toString();
    }

    /**
     * @see java.beans.PropertyEditor#setAsText(java.lang.String)
     */
    public void setAsText(String text) throws IllegalArgumentException {
        this.setValue(new Integer(text));
    }

    /**
     * Returns null since this editor provides a custom GUI component.
     * 
     * @see java.beans.PropertyEditor#getTags()
     */
    public String[] getTags() {
        return null;
    }

    /**
     * Returns swing component to edit the Relative Time property.
     * 
     * @see java.beans.PropertyEditor#getCustomEditor()
     */
    public Component getCustomEditor() {
        return this.editor;
    }

    /**
     * Returns true since this editor provides a custom GUI component.
     * 
     * @see java.beans.PropertyEditor#supportsCustomEditor()
     */
    public boolean supportsCustomEditor() {
        return true;
    }

}
