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
 * Created on Jan 26, 2006
 */
package org.dwfa.bpa.gui;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.bpa.process.I_ContainData;

public class UpdateFieldDocumentListener implements DocumentListener {
    Method setMethod;

    I_ContainData dataContainer;

    JTextArea textField;

    Logger logger;

    /**
     * @param setMethod
     * @param task
     * @param textField
     */
    public UpdateFieldDocumentListener(Method setMethod, I_ContainData taskToInvoke, JTextArea textField, Logger logger) {
        super();
        this.setMethod = setMethod;
        this.dataContainer = taskToInvoke;
        this.textField = textField;
        this.logger = logger;
    }

    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    setMethod.invoke(dataContainer, new Object[] { textField.getText() });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        });

    }

    public void removeUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    setMethod.invoke(dataContainer, new Object[] { textField.getText() });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        });
    }

    public void changedUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    setMethod.invoke(dataContainer, new Object[] { textField.getText() });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        });
    }
}
