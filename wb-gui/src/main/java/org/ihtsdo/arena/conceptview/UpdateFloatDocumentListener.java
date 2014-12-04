/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatAnalogBI;

/**
 *
 * @author kec
 */
public class UpdateFloatDocumentListener implements DocumentListener, ActionListener, VetoableChangeListener {

    private static ThreadGroup updateFloatThreadGroup =
            new ThreadGroup("updateFloatThreadGroup");
    private static ExecutorService updateDocListenrService = Executors.newFixedThreadPool(1,
            new NamedThreadFactory(updateFloatThreadGroup, "updateFloatThread"));
    FixedWidthJEditorPane editorPane;
    RefexFloatAnalogBI refex;
    Timer t;
    I_GetConceptData c;
    boolean update = false;
    I_ConfigAceFrame config;
    TerminologyBuilderBI tc;
    String text;

    public UpdateFloatDocumentListener(FixedWidthJEditorPane editorPane,
            RefexAnalogBI refex) throws TerminologyException, IOException {
        super();
        this.editorPane = editorPane;
        this.refex = (RefexFloatAnalogBI) refex;
        t = new Timer(5000, this);
        Ts.get().addVetoablePropertyChangeListener(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, this);
    }
    long lastChange = Long.MIN_VALUE;

    @Override
    public void insertUpdate(DocumentEvent e) {
        lastChange++;
        text = editorPane.extractText();
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        lastChange++;
        text = editorPane.extractText();
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        lastChange++;
        text = editorPane.extractText();
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }
    long lastAction = Long.MIN_VALUE;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (lastAction < lastChange) {
            lastAction = lastChange;

            updateDocListenrService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (update) {
                            doAction();
                        }
                        update = false;
                    } catch (IOException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    } catch (PropertyVetoException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                }
            });
        }
    }

    private boolean doAction() throws PropertyVetoException, IOException {
        if (update) {
            try {
                refex.setFloat1(Float.parseFloat(text));
                if (!Ts.get().getConcept(refex.getRefexNid()).isAnnotationStyleRefex()) {
                    Ts.get().addUncommitted(Ts.get().getConcept(refex.getRefexNid()));
                }
                ComponentChronicleBI<?> referencedComponent
                        = Ts.get().getComponent(refex.getReferencedComponentNid());
                Ts.get().addUncommitted(Ts.get().getConcept(referencedComponent.getConceptNid()));
            } catch (NumberFormatException e) {
                update = false;
                refex.setFloat1(-1);
                if (!Ts.get().getConcept(refex.getRefexNid()).isAnnotationStyleRefex()) {
                    Ts.get().addUncommitted(Ts.get().getConcept(refex.getRefexNid()));
                }
                ComponentChronicleBI<?> referencedComponent
                        = Ts.get().getComponent(refex.getReferencedComponentNid());
                Ts.get().addUncommitted(Ts.get().getConcept(referencedComponent.getConceptNid()));
                return false;
            }
        }
        update = false;
        return true;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
        try {
            if (text != null) {
                boolean valid = doAction();
                if(!valid){
                    throw new PropertyVetoException("Invalid refset value", pce);
                }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}