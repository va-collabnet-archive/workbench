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
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatAnalogBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntAnalogBI;

/**
 *
 * @author kec
 */
public class UpdateFloatDocumentListener implements DocumentListener, ActionListener, VetoableChangeListener {

    private static ThreadGroup updateTextThreadGroup =
            new ThreadGroup("updateTextThreadGroup");
    private static ExecutorService updateDocListenrService = Executors.newFixedThreadPool(1,
            new NamedThreadFactory(updateTextThreadGroup, "updateTextThread"));
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

    private void doAction() throws PropertyVetoException, IOException {
        try {
            refex.setFloat1(Float.parseFloat(text));
        } catch (NumberFormatException e) {
            refex.setFloat1(-1);
        }

        if (!Ts.get().getConcept(refex.getRefexNid()).isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(Ts.get().getConcept(refex.getRefexNid()));
        }
        Ts.get().addUncommitted(Ts.get().getConcept(refex.getReferencedComponentNid()));
    }

    @Override
    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
        try {
            doAction();
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}