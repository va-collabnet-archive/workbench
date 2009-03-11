/*
 * Created on Jan 26, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
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
    public UpdateFieldDocumentListener(Method setMethod,
            I_ContainData taskToInvoke, JTextArea textField, Logger logger) {
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
                    setMethod.invoke(dataContainer,
                            new Object[] { textField.getText() });
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
                    setMethod.invoke(dataContainer,
                            new Object[] { textField.getText() });
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
                    setMethod.invoke(dataContainer,
                            new Object[] { textField.getText() });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        });
    }
}